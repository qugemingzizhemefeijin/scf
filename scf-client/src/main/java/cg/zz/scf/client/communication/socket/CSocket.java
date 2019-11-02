package cg.zz.scf.client.communication.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import cg.zz.scf.client.SCFConst;
import cg.zz.scf.client.configuration.commmunication.SocketPoolProfile;
import cg.zz.scf.client.utility.AutoResetEvent;
import cg.zz.scf.client.utility.exception.ConnectTimeOutException;
import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;
import cg.zz.scf.protocol.exception.DataOverFlowException;
import cg.zz.scf.protocol.exception.ProtocolException;
import cg.zz.scf.protocol.exception.TimeoutException;
import cg.zz.scf.protocol.sfp.v1.SFPStruct;
import cg.zz.scf.protocol.utility.ByteConverter;
import cg.zz.scf.protocol.utility.ProtocolConst;

/**
 * RPC通信连接（这里用的原生的Java Socket，估计是为了防止客户端过度依赖比如netty框架）
 *
 */
public class CSocket {
	
	private static final ILog logger = LogFactory.getLogger(CSocket.class);
	
	/**
	 * DES密钥
	 */
	private byte[] DESKey;
	
	/**
	 * 是否需要DES加解密
	 */
	private boolean rights;
	
	/**
	 * 真正的socket对象
	 */
	private Socket socket;
	
	/**
	 * 此对象所属的连接池对象
	 */
	private ScoketPool pool;
	
	/**
	 * SocketChannel
	 */
	private SocketChannel channel;
	
	/**
	 * 数据的读写缓冲
	 */
	private ByteBuffer receiveBuffer, sendBuffer;
	
	/**
	 * 连接池配置属性
	 */
	private SocketPoolProfile socketConfig;
	
	/**
	 * 是否当前对象在连接池中
	 */
	private boolean _inPool = false;
	
	/**
	 * 是否正在连接中
	 */
	private boolean _connecting = false;
	
	/**
	 * Socket IO线程数据接收器
	 */
	private DataReceiver dataReceiver = null;
	
	/**
	 * 是否等待销毁，如果连接池缩小，此值将未true，并随后在Worker调用CSocket的frameHandle方法中进行释放
	 */
	private boolean waitDestroy = false;
	
	/**
	 * 发送消息锁
	 */
	private final Object sendLockHelper = new Object();
	
	/**
	 * 接收消息锁
	 */
	private final Object receiveLockHelper = new Object();
	
	/**
	 * 接收消息字节缓冲流
	 */
	private CByteArrayOutputStream receiveData = new CByteArrayOutputStream();
	
	/**
	 * 每个socket都维护了sessionID和WindowData的映射关系。在接收到消息的时候，从这里可以解锁休眠线程或者执行回调函数
	 */
	private Map<Integer, WindowData> WaitWindows = new ConcurrentHashMap<>();
	
	/**
	 * 异步消息发送处理器
	 */
	private static final NIOHandler ASYNC_MESSAGE_HANDLER = NIOHandler.getInstance();
	
	/**
	 * 用于匹配消息结束标志的计数值，如果index=ProtocolConst.P_END_TAG.length，则代表本次消息已经处理接收完毕。。用于解决半包，粘包问题
	 */
	private volatile int index = 0;
	
	/**
	 * 是否正被处理接收到的消息
	 */
	private volatile boolean handling = false;
	
	/**
	 * 创建Socket连接
	 * @param endpoint - 连接的服务器地址
	 * @param _pool - 所属的连接池对象
	 * @param config - 连接池配置
	 * @throws Exception
	 */
	protected CSocket(InetSocketAddress endpoint, ScoketPool _pool, SocketPoolProfile config) throws Exception {
		this.socketConfig = config;
		this.pool = _pool;
		//新建SocketChannel对象
		this.channel = SocketChannel.open();
		//设置为非阻塞模式
		this.channel.configureBlocking(false);
		//设置socket的发送缓存区大小
		this.channel.socket().setSendBufferSize(config.getSendBufferSize());
		//设置socket的接收缓存区大小
		this.channel.socket().setReceiveBufferSize(config.getRecvBufferSize());
		//数据读缓冲
		this.receiveBuffer = ByteBuffer.allocate(config.getBufferSize());
		//数据写缓冲
		this.sendBuffer = ByteBuffer.allocate(config.getMaxPakageSize());
		//连接到服务器
		this.channel.connect(endpoint);
		
		//判断socket是否连接超时
		long begin = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - begin > SCFConst.SOCKET_CONNECT_TIMEOUT) {
				this.channel.close();
			    throw new ConnectTimeOutException("connect to " + endpoint + " timeout："+SCFConst.SOCKET_CONNECT_TIMEOUT+"ms");
			}
			//isConnected方法只有在状态为ST_CONNECTED = 2的时候，才会返回true，否则返回false
			//finishConnect如果状态=2则返回true，否则状态不是ST_PENDING = 1的时候返回false，则会抛出异常
			this.channel.finishConnect();
			if (this.channel.isConnected()) {
				break;
			}
			try {
				TimeUnit.MILLISECONDS.sleep(50);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
		//获取Socket对象
		this.socket = this.channel.socket();
		//设置当前状态为已连接
		this._connecting = true;
		//Socket IO线程数据接收器
		this.dataReceiver = DataReceiver.instance();
		//将其注册到IO线程接收器中
		this.dataReceiver.RegSocketChannel(this);
		
		logger.info("MaxPakageSize:" + config.getMaxPakageSize());
		logger.info("SendBufferSize:" + config.getSendBufferSize());
		logger.info("RecvBufferSize:" + config.getRecvBufferSize());
		logger.info("create a new connection :" + toString());
	}
	
	/**
	 * 
	 * 创建Socket连接
	 * @param addr - 服务器IP地址
	 * @param port - 服务器端口
	 * @param _pool - 所属的连接池对象
	 * @param config - 连接池配置
	 * @throws Exception
	 */
	protected CSocket(String addr, int port, ScoketPool _pool, SocketPoolProfile config) throws Exception {
		this(new InetSocketAddress(addr, port) , _pool , config);
	}
	
	/**
	 * 发送数据
	 * @param data - byte[]
	 * @return int 发送的字节数
	 * @throws IOException
	 * @throws Throwable
	 */
	public int send(byte[] data) throws IOException, Throwable {
		try {
			synchronized (this.sendLockHelper) {
				int pakageSize = data.length + ProtocolConst.P_START_TAG.length + ProtocolConst.P_END_TAG.length;
				if (this.sendBuffer.capacity() < pakageSize) {
					throw new DataOverFlowException("数据包(size:" + pakageSize + ")超过最大限制,请修改或增加配置文件中的<SocketPool maxPakageSize=\"" + this.socketConfig.getMaxPakageSize() + "\"/>节点属性！");
				}
				
				int count = 0;
		        this.sendBuffer.clear();
		        this.sendBuffer.put(ProtocolConst.P_START_TAG);
		        this.sendBuffer.put(data);
		        this.sendBuffer.put(ProtocolConst.P_END_TAG);
		        this.sendBuffer.flip();
			        
				int retryCount = 0;
				while (this.sendBuffer.hasRemaining()) {
					count += this.channel.write(this.sendBuffer);

					if (retryCount++ > 30) {
						throw new Exception("retry write count(" + retryCount + ") above 30");
					}
				}
				return count;
			}
		} catch (IOException ex) {
			_connecting = false;
			throw ex;
		} catch (NotYetConnectedException ex) {
			_connecting = false;
			throw ex;
		}
	}
	
	/**
	 * 接收同步数据
	 * @param sessionId - int
	 * @param queueLen - 
	 * @return byte[]
	 * @throws IOException
	 * @throws TimeoutException
	 * @throws Exception
	 */
	public byte[] receive(int sessionId, int queueLen) throws IOException, TimeoutException, Exception {
		WindowData wd = (WindowData)this.WaitWindows.get(Integer.valueOf(sessionId));
		if (wd == null) {
			throw new Exception("Need invoke 'registerRec' method before invoke 'receive' method!");
		}
		AutoResetEvent event = wd.getEvent();
		int timeout = getReadTimeout(this.socketConfig.getReceiveTimeout(), queueLen);
		if (!event.waitOne(timeout)) {
			throw new TimeoutException("ServiceName:[" + getServiceName() + "],ServiceIP:[" + getServiceIP() + "],Receive data timeout or error!timeout:" + timeout + "ms,queue length:" + queueLen);
		}
		
		byte[] data = wd.getData();
		//从data第二个位置读取4字节的消息长度，如果读取到的数值跟data.length一致，则认为消息可靠
		int offset = SFPStruct.Version;
		int len = ByteConverter.bytesToIntLittleEndian(data, offset);
		if (len != data.length) {
			throw new ProtocolException("The data length inconsistent!datalen:" + data.length + ",check len:" + len);
		}
		return data;
	}
	
	protected void frameHandle() throws Exception {
		if (this.handling) {
			return;
		}
		
		synchronized (this.receiveLockHelper) {
			this.handling = true;
			
			try {
				if (this.waitDestroy && isIdle()) {
					logger.info("Shrinking the connection:" + toString());
					dispose(true);

					this.handling = false;
					return;
				}
				this.receiveBuffer.clear();
				try {
					int re = this.channel.read(this.receiveBuffer);
					if (re < 0) {
						closeAndDisponse();
						logger.error("server is close.this socket will close.");
						
						this.handling = false;
						return;
					}
				} catch (IOException ex) {
					this._connecting = false;
					throw ex;
				} catch (NotYetConnectedException e) {
					this._connecting = false;
					throw e;
				}
				
				this.receiveBuffer.flip();
				if (this.receiveBuffer.remaining() == 0) {
					this.handling = false;
					return;
				}
				
				logger.debug("客户端接收到数据了...........");
				do {
					byte b = this.receiveBuffer.get();
					this.receiveData.write(b);
					if (b == ProtocolConst.P_END_TAG[this.index]) {
						this.index += 1;
						if (this.index == ProtocolConst.P_END_TAG.length) {
							byte[] pak = this.receiveData.toByteArray(ProtocolConst.P_START_TAG.length, this.receiveData.size() - ProtocolConst.P_END_TAG.length - ProtocolConst.P_START_TAG.length);
							int pSessionId = ByteConverter.bytesToIntLittleEndian(pak, 5);
							WindowData wd = this.WaitWindows.get(Integer.valueOf(pSessionId));
							if (wd != null) {
								if (wd.getFlag() == 0) {
									wd.setData(pak);
									wd.getEvent().set();
								} else if (wd.getFlag() == 1) {
									wd.getReceiveHandler().notify(pak);
									unregisterRec(pSessionId);
								}
							}
							this.index = 0;
							this.receiveData.reset();
						}
					} else if (this.index != 0) {
						if (b == ProtocolConst.P_END_TAG[0])
							this.index = 1;
						else
							this.index = 0;
					}
				} while (this.receiveBuffer.remaining() > 0);
			} catch (Exception ex) {
				this.index = 0;
			        throw ex;
			} finally {
				this.handling = false;
			}
		}
	}
	
	/**
	 * 向WaitWindows中注册一个sessionId
	 * @param sessionId - int
	 */
	public void registerRec(int sessionId) {
		AutoResetEvent event = new AutoResetEvent();
		WindowData wd = new WindowData(event);
		this.WaitWindows.put(Integer.valueOf(sessionId), wd);
	}
	
	/**
	 * 向WaitWindows中注册一个sessionId
	 * @param sessionId - int
	 * @param wd - WindowData
	 */
	public void registerRec(int sessionId, WindowData wd) {
		this.WaitWindows.put(Integer.valueOf(sessionId), wd);
	}
	
	/**
	 * 从WaitWindows中注销一个sessionId
	 * @param sessionId
	 */
	public void unregisterRec(int sessionId) {
		this.WaitWindows.remove(Integer.valueOf(sessionId));
	}
	
	/**
	 * 判断WaitWindows中是否已经有指定的sessionId
	 * @param sessionId - int
	 * @return boolean
	 */
	public boolean hasSessionId(int sessionId) {
		return this.WaitWindows.containsKey(Integer.valueOf(sessionId));
	}
	
	/**
	 * 关闭连接并且真销毁连接
	 */
	public void closeAndDisponse() {
		close();
		dispose(true);
	}
	
	/**
	 * 将连接返回给连接池
	 */
	public void close() {
		this.pool.release(this);
	}
	
	/**
	 * 销毁连接，此方法为假销毁，实际调用close方法
	 * @throws Exception
	 */
	public void dispose() throws Exception {
		dispose(false);
	}
	
	/**
	 * 销毁连接
	 * @param flag - 是否真销毁
	 */
	public void dispose(boolean flag) {
		if (flag) {
			logger.warning("destory a connection");
			try {
				this.dataReceiver.UnRegSocketChannel(this);
			} finally {
				this.pool.destroy(this);
			}
		} else {
			close();
		}
	}
	
	protected void disconnect() throws IOException {
		if (this.channel != null) {
			this.channel.close();
		}
		if (this.socket != null) {
			this.socket.close();
		}
		this._connecting = false;
	}

	/**
	 * 将本次应该发送的消息发送到异步队列中发送
	 * @param wd - WindowData
	 */
	public void offerAsyncWrite(WindowData wd) {
		ASYNC_MESSAGE_HANDLER.offerWriteData(wd);
	}
	
	public int getTimeOut(int queueLen) {
		return getReadTimeout(this.socketConfig.getReceiveTimeout(), queueLen);
	}
	
	/**
	 * 获得读取数据的超时时间
	 * @param baseReadTimeout - 基础的超时时间
	 * @param queueLen
	 * @return
	 */
	private int getReadTimeout(int baseReadTimeout, int queueLen) {
		if (!this.socketConfig.isProtected()) {
			return baseReadTimeout;
		}
		if (queueLen <= 0) {
			queueLen = 1;
		}
		int result = baseReadTimeout;
		int flag = (queueLen - 100) / 10;
		if (flag >= 0) {
			if (flag == 0) {
				flag = 1;
			}
			result = baseReadTimeout / (2 * flag);
		} else if (flag < -7) {
			result = baseReadTimeout - flag * (baseReadTimeout / 10);
		}

		if (result > 2 * baseReadTimeout)
			result = baseReadTimeout;
		else if (result < 5) {
			result = 5;//min timeout is 5ms
		}
		if (queueLen > 50) {
			logger.warn("--ServiceName:[" + getServiceName() + "],ServiceIP:[" + getServiceIP() + "],IsProtected:true,queueLen:" + queueLen + ",timeout:" + result + ",baseReadTimeout:" + baseReadTimeout);
		}
		return result;
	}
	
	@Override
	protected void finalize() throws Throwable {
		try {
			if (this._connecting || (this.channel != null && this.channel.isOpen()))
				dispose(true);
		} catch (Throwable t) {
			logger.error("Pool Release Error!:", t);
		} finally {
			super.finalize();
		}
	}
	
	/**
	 * 是否正在连接中
	 * @return boolean
	 */
	public boolean connecting() {
		return this._connecting;
	}
	
	protected boolean inPool() {
		return this._inPool;
	}

	protected void setInPool(boolean inPool) {
		this._inPool = inPool;
	}
	
	protected SocketChannel getChannle() {
		return this.channel;
	}

	/**
	 * 该连接是否是空闲状态
	 * @return boolean
	 */
	protected boolean isIdle() {
		return this.WaitWindows.size() <= 0;
	}

	/**
	 * 设置连接等待消息，最终在frameHandle方法中消息，调用在Worker对象中
	 */
	protected void waitDestroy() {
		this.waitDestroy = true;
	}

	public boolean isRights() {
		return this.rights;
	}

	public void setRights(boolean rights) {
		this.rights = rights;
	}
	
	public byte[] getDESKey() {
		return this.DESKey;
	}

	public void setDESKey(byte[] dESKey) {
		this.DESKey = dESKey;
	}

	/**
	 * 获得服务的IP
	 * @return String
	 */
	public String getServiceIP() {
		if (this.socket != null && !this.socket.isClosed()) {
			try {
				return this.socket.getInetAddress().getHostAddress();
			} catch (Exception ex) {
				return null;
			}
		}
		return null;
	}

	/**
	 * 获得服务的名称
	 * @return String
	 */
	public String getServiceName() {
		if (this.pool != null) {
			return this.pool.getServicename();
		}
		return null;
	}

	/**
	 * 获得配置的接收数据超时时间
	 * @return int
	 */
	public int getConfigTime() {
		return getReadTimeout(this.socketConfig.getReceiveTimeout(), 1);
	}

	public String toString() {
		try {
			return this.socket == null ? "" : this.socket.toString();
		} catch (Throwable ex) {
			return "Socket[error:" + ex.getMessage() + "]";
		}
	}

	public static void closeRecv() {
		try {
			DataReceiver.closeRecv();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
