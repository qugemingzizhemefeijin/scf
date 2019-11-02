package cg.zz.scf.client.communication.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeoutException;

import cg.zz.scf.client.configuration.ServiceConfig;
import cg.zz.scf.client.configuration.commmunication.SocketPoolProfile;
import cg.zz.scf.client.loadbalance.Server;
import cg.zz.scf.client.proxy.ServiceProxy;
import cg.zz.scf.client.secure.SecureKey;
import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;
import cg.zz.scf.protocol.exception.RebootException;
import cg.zz.scf.protocol.exception.ThrowErrorHelper;
import cg.zz.scf.protocol.sdp.ExceptionProtocol;
import cg.zz.scf.protocol.sdp.HandclaspProtocol;
import cg.zz.scf.protocol.sfp.enumeration.SDPType;
import cg.zz.scf.protocol.sfp.v1.Protocol;

/**
 * Server下的Socket连接池，每个&lt;add name="enterprise" host="127.0.0.1" port="19000" maxCurrentUser="100" /&gt;标签都会创建此连接池对象
 *
 */
public class ScoketPool {
	
	private static final ILog logger = LogFactory.getLogger(ScoketPool.class);
	
	/**
	 * 服务连接地址
	 */
	private InetSocketAddress endPoint;
	
	/**
	 * socket连接池配置信息
	 */
	private SocketPoolProfile socketPoolConfig;
	
	/**
	 * 连接池队列
	 */
	private CQueueL queue = null;
	
	/**
	 * 连接池所属服务器对象
	 */
	private Server server;
	
	/**
	 * 服务器配置
	 */
	private ServiceConfig serviceConfig;
	
	/**
	 * 构造连接池对象
	 * @param server - 服务器对象
	 * @param config - 连接池配置信息
	 */
	public ScoketPool(Server server, SocketPoolProfile config) {
		this.server = server;
		this.socketPoolConfig = config;
		this.endPoint = new InetSocketAddress(server.getAddress(), server.getPort());
		this.queue = new CQueueL(config.getShrinkInterval(), config.getMinPoolSize());
	}

	/**
	 * 构造连接池对象
	 * @param server - 服务器对象
	 * @param serviceconfig - 服务配置
	 */
	public ScoketPool(Server server, ServiceConfig serviceconfig) {
		this.server = server;
		this.socketPoolConfig = serviceconfig.getSocketPool();
		this.serviceConfig = serviceconfig;
		this.queue = new CQueueL(serviceconfig.getSocketPool().getShrinkInterval(), serviceconfig.getSocketPool().getMinPoolSize());
	}
	
	/**
	 * 获得队列的元素数量
	 * @return int
	 */
	public int count() {
		return this.queue.size();
	}
	
	/**
	 * 从连接池中获得一个连接
	 * @return CSocket
	 * @throws TimeoutException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws Throwable
	 * @throws Exception
	 */
	public synchronized CSocket getSocket() throws TimeoutException, IOException, InterruptedException, Throwable, Exception {
		CSocket rSocket = null;
		//队列中有数据直接获取
		if (this.queue.size() > 0) {
			rSocket = this.queue.dequeue();
		} else if (this.queue.getTotal() < this.socketPoolConfig.getMaxPoolSize()) {//如果队列中的socket并且当前已经创建出来的连接少于最大连接数，则直接创建出一个新的
			//如果启动权限认证则注册之前进行授权文件认证
			CSocket socket = new CSocket(this.endPoint, this, this.socketPoolConfig);
			//如果启动加密机制则进行DES密钥叫唤和字符密钥交换。完成一系列交换后，将连接放入连接池
			if (checkRights(socket) && checkApprove(socket)) {
				this.queue.register(socket);
				rSocket = socket;
			} else {
				logger.error("授权文件没有通过校验!");
				throw new Exception("授权文件没有通过校验!");
			}
		} else {
			//如果队列没， 并且队列也没法再创建了，则来到此处。。再多给一次机会拿连接
			if (this.queue.size() > 0) {
				rSocket = this.queue.dequeue();
			} else {
				//等待指定时间还拿不到连接则抛出TimeoutException异常
				rSocket = this.queue.dequeue(this.socketPoolConfig.getWaitTimeout());
				if (rSocket == null) {
					logger.error("socket connection pool is full!");
					throw new TimeoutException("socket connection pool is full!");
				}
			}
		}
		
		if (rSocket == null) {
			throw new Exception("GetSocket socket is null!");
		}
		
		//设置当前socket不在连接池中
		rSocket.setInPool(false);
		return rSocket;
	}
	
	/**
	 * 归还连接到连接池
	 * @param socket - CSocket
	 */
	public void release(CSocket socket) {
		if (socket == null) {
			logger.warn("socket is null when release(CSocket socket)");
		} else if (!socket.connecting()) {//连接断开了，则销毁并关闭socket通道
			logger.warn("socket is closed when release(CSocket socket)--" + socket.toString());
			destroy(socket);
		} else if (this.socketPoolConfig.AutoShrink() && this.queue.shrink()) {//如果连接池是可伸缩的，并且当前是可被缩小
			socket.waitDestroy();//将socket设置为待销毁。真正的是在Worker类中执行CSocket的frameHandle方法进行销毁处理
			logger.info("this socket is waitDestroy!");
		} else if (!socket.inPool()) {//归还连接到连接池
			this.queue.enqueue(socket);
			socket.setInPool(true);
		}
	}
	
	/**
	 * 销毁一个连接
	 * @param socket - CSocket
	 */
	public void destroy(CSocket socket) {
		try {
			logger.warn("socket destroyed!--" + socket.toString());
			socket.disconnect();
		} catch (Throwable err) {
			logger.error("socket destroy error!--" + socket.toString(), err);
		} finally {
			this.queue.remove(socket);
		}
	}
	
	/**
	 * 销毁整个连接池里面的所有的连接
	 * @throws Exception
	 */
	public void destroy() throws Exception {
		synchronized (this) {
			List<CSocket> csList = this.queue.getAllSocket();
			for (int i = 0; i < csList.size(); i++) {
				//这里这么写的原因是防止有新归还的socket造成报错
				if (i < csList.size()) {
					csList.get(i).dispose(true);
				}
			}
		}
	}
	
	/**
	 * DES密钥交换
	 * 
	 * @param scoket - CSocket
	 * @return boolean 是否校验成功
	 * @author HaoXB
	 * @throws Throwable
	 * @date 2010-09-01 处理过程: 1、建立连接
	 *       2、客户端生成RSA公(CPublicKey)/私(CPrivateKey)钥,并将公钥(CPublicKey)传送给服务器端
	 *       3、服务器端接收客户端提供的公钥(CPublicKey),并生成新的RSA公(SPublicKey)/私(SPrivateKey)钥,将公钥(SPublicKey)传送给客户端
	 *       4、客户端用服务器端提供的公钥(SPublicKey)加密授权文件，并传送给服务器端
	 *       5、服务器端通过服务器端私钥(SPrivateKey)解密、并校验授权文件是否正确，如果正确则返回通过客户端公钥(CPublicKey)加密的RSA密钥，否则返回null/false
	 *       6、客户端通过客户端私钥(CPrivateKey)解密服务器端返回数据获得RSA密钥
	 *       7、客户端、服务器端通过RSA加密数据进行交互
	 */
	private boolean checkRights(CSocket scoket) throws Throwable {
		long startTime = System.currentTimeMillis();
		//如果没有启用权限认证，则直接返回true,直接注册socket
		if (this.serviceConfig.getSecureKey().getInfo() == null) {
			return true;
		}
		
		//----发送客户端公钥去服务器端、并获取服务器端公钥--start---------------------
		SecureKey sk = new SecureKey();
		ServiceProxy proxy = ServiceProxy.getProxy(this.serviceConfig.getServicename());
		HandclaspProtocol handclaspProtocol = new HandclaspProtocol("1", sk.getStringPublicKey());
		Protocol publicKeyProtocol = proxy.createProtocol(handclaspProtocol);

		try {
			scoket.registerRec(publicKeyProtocol.getSessionID());
			scoket.send(publicKeyProtocol.toBytes());
			logger.info("send client publicKey sucess!");
		} finally {
			//scoket.dispose();
		}
		
		/**
		 * 过程3,接收服务器端生成公钥
		 */
		byte[] receivePublicBuffer = scoket.receive(publicKeyProtocol.getSessionID(), this.server.getCurrUserCount());
		if (receivePublicBuffer == null) {
			logger.warn("获取服务器公钥失败!");
			return false;
		}

		//将接受饿byte[]封装为Protocol对象
		Protocol serverPublicProtocol = Protocol.fromBytes(receivePublicBuffer);
		HandclaspProtocol _handclaspProtocol = null;

		//判断服务器返回的消息类型，交换密钥类型。否则抛出对应的异常信息
		if (serverPublicProtocol.getSdpType() == SDPType.Handclasp) {
			_handclaspProtocol = (HandclaspProtocol) serverPublicProtocol.getSdpEntity();
			logger.debug("get server publicKey time:" + (System.currentTimeMillis() - startTime) + "ms");
		} else {
			if (serverPublicProtocol.getSdpType() == SDPType.Exception) {
				ExceptionProtocol ep = (ExceptionProtocol) serverPublicProtocol.getSdpEntity();
				throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
			} else if (serverPublicProtocol.getSdpType() == SDPType.Reset) {
				throw new RebootException("this server is reboot!");
			}
			throw new Exception("userdatatype error!");
		}

		logger.info("receive server publicKey sucess!");

		String keyInfo = this.serviceConfig.getSecureKey().getInfo();
		if (keyInfo == null || "".equals(keyInfo)) {
			logger.warn("获取授权文件失败!");
			return false;
		}

		//将服务器返回的公钥用来加密DES KEY并发送给服务端
		String ciphertext = sk.encryptByPublicKeyString(keyInfo, _handclaspProtocol.getData());
		//----发送客户端公钥去服务器端、并获取服务器端公钥--end---------------------
        //---发送授权文件到服务器端认证--------------------start------------------------
		HandclaspProtocol handclaspProtocol_ = new HandclaspProtocol("2", ciphertext);
		Protocol protocol_mw = proxy.createProtocol(handclaspProtocol_);

		try {
			scoket.registerRec(protocol_mw.getSessionID());
			scoket.send(protocol_mw.toBytes());
			logger.info("send keyInfo sucess!");
		} finally {
			//scoket.dispose();
		}

		/**
         * 过程5 从服务端获取DES密钥（注意服务端发来的是用客户端的公钥加密后的密文，需要在下面解开）
         */
		byte[] receiveDESKey = scoket.receive(protocol_mw.getSessionID(), this.server.getCurrUserCount());
		if (receiveDESKey == null) {
			logger.warn("获取DES密钥失败!");
			return false;
		}
		logger.info("receive DESKey sucess!");

		HandclaspProtocol handclaspProtocol_mw = null;
		Protocol serverDesKeyProtocol = Protocol.fromBytes(receiveDESKey);
		if (serverDesKeyProtocol.getSdpType() == SDPType.Handclasp) {
			handclaspProtocol_mw = (HandclaspProtocol) serverDesKeyProtocol.getSdpEntity();
		} else {
			if (serverDesKeyProtocol.getSdpType() == SDPType.Exception) {
				ExceptionProtocol ep = (ExceptionProtocol) serverDesKeyProtocol.getSdpEntity();
				throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
			}
			if (serverDesKeyProtocol.getSdpType() == SDPType.Reset) {
				throw new RebootException("this server is reboot!");
			}
			throw new Exception("userdatatype error!");
		}

		/**
		 * 解密获取服务端的DES密钥（使用客户端的私钥来解密）
		 */
		byte[] DESKeyStr = sk.decryptByPrivateKeyByte(handclaspProtocol_mw.getData(), sk.getStringPrivateKey());
		if (DESKeyStr == null) {
			logger.warn("解密DES密钥失败!");
			return false;
		}
		//---发送授权文件到服务器端认证--------------------end-------------------------------

		//保存服务端的DES密钥，用于数据加密，并设置socket的rights为true，代表此socket是的数据需要进行加解密处理
		scoket.setDESKey(DESKeyStr);
		scoket.setRights(true);
		logger.info("securekey use Time is " + String.valueOf(System.currentTimeMillis() - startTime) + " millisecond");
		return (scoket.getDESKey() != null) && (scoket.getDESKey().length > 0);
	}
	
	/**
	 * 这个方法其实就是将客户端维护的密码发送给服务端，服务端校验后返回是否成功（此处密钥是明文传递的，有点像用户登录传递密码一样）<br/>
	 * 此方法好像服务端没有做任何逻辑来响应
	 * @param socket - CSocket
	 * @return boolean
	 * @throws Throwable
	 */
	private boolean checkApprove(CSocket socket) throws Throwable {
		long startTime = System.currentTimeMillis();
		
		if (this.serviceConfig.getApproveKey() == null || this.serviceConfig.getApproveKey().getInfo() == null) {
			return true;
		}
		
		ServiceProxy proxy = ServiceProxy.getProxy(this.serviceConfig.getServicename());
		String approveProtocol = this.serviceConfig.getApproveKey().getInfo();
		Protocol publicKeyProtocol = proxy.createApproveProtocol(approveProtocol);
		
		//将客户端的密钥发送给服务端
		socket.registerRec(publicKeyProtocol.getSessionID());
		byte[] b = publicKeyProtocol.toBytes();
		socket.send(b);
		logger.info("send client publicKey sucess!");
		
		//这里获取服务器的密钥
		byte[] receivePublicBuffer = socket.receive(publicKeyProtocol.getSessionID(), this.server.getCurrUserCount());
		if (receivePublicBuffer == null) {
			logger.warn("获取服务器公钥失败!");
			return false;
		}
		
		//下面代码就是获取服务端返回的信息，判断客户端维护的服务名称跟服务端返回的是否一致
		Protocol serverPublicProtocol = Protocol.fromBytes(receivePublicBuffer);
		String _approveProtocol = null;

		if (serverPublicProtocol.getSdpType() == SDPType.StringKey) {
			_approveProtocol = (String) serverPublicProtocol.getSdpEntity();
			logger.debug("approve server time:" + (System.currentTimeMillis() - startTime) + "ms");
		} else {
			if (serverPublicProtocol.getSdpType() == SDPType.Exception) {
				ExceptionProtocol ep = (ExceptionProtocol) serverPublicProtocol.getSdpEntity();
				throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
			}
			if (serverPublicProtocol.getSdpType() == SDPType.Reset) {
				throw new RebootException("this server is reboot!");
			}
			throw new Exception("userdatatype error!  " + serverPublicProtocol.getSdpType());
		}

		if (_approveProtocol.equalsIgnoreCase(this.serviceConfig.getServicename())) {
			return true;
		}

		throw new Exception("approve error!");
	}
	
	/**
	 * 获得服务名称
	 * @return String
	 */
	public String getServicename() {
		return this.serviceConfig.getServicename();
	}

}
