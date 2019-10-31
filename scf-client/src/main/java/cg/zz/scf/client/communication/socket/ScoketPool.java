package cg.zz.scf.client.communication.socket;

import java.io.IOException;
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
 * 连接池
 *
 */
public class ScoketPool {
	
	private static final ILog logger = LogFactory.getLogger(ScoketPool.class);
	
	//private InetSocketAddress endPoint;
	private SocketPoolProfile socketPoolConfig;
	private CQueueL queue = null;
	private Server server;
	private ServiceConfig serviceConfig;
	
	public ScoketPool(Server server, SocketPoolProfile config) {
		this.server = server;
		//this.endPoint = new InetSocketAddress(server.getAddress(), server.getPort());
		this.socketPoolConfig = config;
		this.queue = new CQueueL(config.getShrinkInterval(), config.getMinPoolSize());
	}

	public ScoketPool(Server server, ServiceConfig serviceconfig) {
		this.server = server;

		this.socketPoolConfig = serviceconfig.getSocketPool();
		this.queue = new CQueueL(serviceconfig.getSocketPool().getShrinkInterval(), serviceconfig.getSocketPool().getMinPoolSize());
		this.serviceConfig = serviceconfig;
	}
	
	/**
	 * 获得队列的元素数量
	 * @return int
	 */
	public int count() {
		return this.queue.size();
	}
	
	/**
	 * 获得一个连接
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
		} else if (this.queue.getTotal() < this.socketPoolConfig.getMaxPoolSize()) {//如果当前已经创建出来的数据少于最大数据，并且队列中的数据没有了，则直接创建出一个新的数据出来
			//如果启动权限认证则注册之前进行授权文件认证
			CSocket socket = new CSocket(this.server.getAddress(), this.server.getPort(), this, this.socketPoolConfig);
			if (checkRights(socket) && checkApprove(socket)) {
				this.queue.register(socket);
				rSocket = socket;
			} else {
				logger.error("授权文件没有通过校验!");
				throw new Exception("授权文件没有通过校验!");
			}
		} else {//如果队列没， 并且队列也没法再创建了，则来到地处
			if (this.queue.size() > 0) {
				rSocket = this.queue.dequeue();
			} else {
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
		rSocket.setInPool(false);
		return rSocket;
	}
	
	/**
	 * 归还一个连接到连接池
	 * @param socket - CSocket
	 */
	public void release(CSocket socket) {
		if (socket == null) {
			logger.warn("socket is null when release(CSocket socket)");
		} else if (!socket.connecting()) {
			logger.warn("socket is closed when release(CSocket socket)--" + socket.toString());
			destroy(socket);
		} else if ((this.socketPoolConfig.AutoShrink()) && (this.queue.shrink())) {
			socket.waitDestroy();
			logger.info("this socket is waitDestroy!");
		} else if (!socket.inPool()) {
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
			for (int i = 0; i < csList.size(); i++)
				if (i < csList.size()) csList.get(i).dispose(true);
		}
	}
	
	/**
	 * 权限认证
	 * 
	 * @param scoket
	 * @return 是否校验成功
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
		
		handclaspProtocol = null;

		/**
		 * 过程3,接收服务器端生成公钥
		 */
		byte[] receivePublicBuffer = scoket.receive(publicKeyProtocol.getSessionID(), this.server.getCurrUserCount());
		if (receivePublicBuffer == null) {
			logger.warn("获取服务器公钥失败!");
			return false;
		}

		Protocol serverPublicProtocol = Protocol.fromBytes(receivePublicBuffer);
		HandclaspProtocol _handclaspProtocol = null;

		if (serverPublicProtocol.getSdpType() == SDPType.Handclasp) {
			_handclaspProtocol = (HandclaspProtocol) serverPublicProtocol.getSdpEntity();
			logger.debug("get server publicKey time:" + (System.currentTimeMillis() - startTime) + "ms");
		} else {
			if (serverPublicProtocol.getSdpType() == SDPType.Exception) {
				ExceptionProtocol ep = (ExceptionProtocol) serverPublicProtocol.getSdpEntity();
				throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
			}
			if (serverPublicProtocol.getSdpType() == SDPType.Reset) {
				throw new RebootException("this server is reboot!");
			}
			throw new Exception("userdatatype error!");
		}

		logger.info("receive server publicKey sucess!");
		publicKeyProtocol = null;

		String keyInfo = this.serviceConfig.getSecureKey().getInfo();
		if (keyInfo == null || "".equals(keyInfo)) {
			logger.warn("获取授权文件失败!");
			return false;
		}

		String ciphertext = sk.encryptByPublicKeyString(keyInfo, _handclaspProtocol.getData());
		_handclaspProtocol = null;
		serverPublicProtocol = null;
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

		handclaspProtocol_ = null;
		/**
	         * 过程5
	         * 获取由客户端公钥加密后的DES密钥
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
		 * 解密获取DES密钥
		 */
		byte[] DESKeyStr = sk.decryptByPrivateKeyByte(handclaspProtocol_mw.getData(), sk.getStringPrivateKey());
		if (DESKeyStr == null) {
			logger.warn("解密DES密钥失败!");
			return false;
		}
		handclaspProtocol_mw = null;
		protocol_mw = null;
		//---发送授权文件到服务器端认证--------------------end-------------------------------

		scoket.setDESKey(DESKeyStr);
		scoket.setRights(true);
		logger.info("securekey use Time is " + String.valueOf(System.currentTimeMillis() - startTime) + " millisecond");
		return (scoket.getDESKey() != null) && (scoket.getDESKey().length > 0);
	}
	
	/**
	 * 检查票据
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
		
		socket.registerRec(publicKeyProtocol.getSessionID());
		byte[] b = publicKeyProtocol.toBytes();
		socket.send(b);
		logger.info("send client publicKey sucess!");
		
		approveProtocol = null;
		byte[] receivePublicBuffer = socket.receive(publicKeyProtocol.getSessionID(), this.server.getCurrUserCount());
		if (receivePublicBuffer == null) {
			logger.warn("获取服务器公钥失败!");
			return false;
		}
		
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
