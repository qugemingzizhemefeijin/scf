package cg.zz.scf.client.communication.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;

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
 * 客户端连接
 *
 */
public class CSocket {
	
	private static final ILog logger = LogFactory.getLogger(CSocket.class);
	
	private byte[] DESKey;// DES密钥
	private boolean rights;// 是否启用认证
	private Socket socket;
	private ScoketPool pool;
	private SocketChannel channel;
	private ByteBuffer receiveBuffer, sendBuffer;
	private SocketPoolProfile socketConfig;
	private boolean _inPool = false;
	private boolean _connecting = false;
	private DataReceiver dataReceiver = null;
	private boolean waitDestroy = false;
	private final Object sendLockHelper = new Object();
	private final Object receiveLockHelper = new Object();
	private CByteArrayOutputStream receiveData = new CByteArrayOutputStream();
	private ConcurrentHashMap<Integer, WindowData> WaitWindows = new ConcurrentHashMap<Integer, WindowData>();
	private static NIOHandler handler = null;
	
	private volatile int index = 0;
	private volatile boolean handling = false;//是否正被锁着
	
	protected CSocket(InetSocketAddress endpoint, ScoketPool _pool, SocketPoolProfile config) throws Exception {
		this.socketConfig = config;
		this.pool = _pool;
		this.channel = SocketChannel.open();
		this.channel.configureBlocking(false);
		this.channel.socket().setSendBufferSize(config.getSendBufferSize());
		this.channel.socket().setReceiveBufferSize(config.getRecvBufferSize());
		this.receiveBuffer = ByteBuffer.allocate(config.getBufferSize());
		this.sendBuffer = ByteBuffer.allocate(config.getMaxPakageSize());
		this.channel.connect(endpoint);
		
		long begin = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - begin > SCFConst.SOCKET_CONNECT_TIMEOUT) {
				this.channel.close();
			        throw new ConnectTimeOutException("connect to " + endpoint + " timeout："+SCFConst.SOCKET_CONNECT_TIMEOUT+"ms");
			}
			this.channel.finishConnect();
			if (this.channel.isConnected()) break;
			
			try {
				Thread.sleep(50L);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
		this.socket = this.channel.socket();
		this._connecting = true;
		this.dataReceiver = DataReceiver.instance();
		this.dataReceiver.RegSocketChannel(this);
		handler = NIOHandler.getInstance();
		
		logger.info("MaxPakageSize:" + config.getMaxPakageSize());
		logger.info("SendBufferSize:" + config.getSendBufferSize());
		logger.info("RecvBufferSize:" + config.getRecvBufferSize());
		logger.info("create a new connection :" + toString());
	}
	
	protected CSocket(String addr, int port, ScoketPool _pool, SocketPoolProfile config) throws Exception {
		this.socketConfig = config;
		this.pool = _pool;
		this.channel = SocketChannel.open();
		this.channel.configureBlocking(false);
		this.channel.socket().setSendBufferSize(config.getSendBufferSize());
		this.channel.socket().setReceiveBufferSize(config.getRecvBufferSize());
		this.receiveBuffer = ByteBuffer.allocate(config.getBufferSize());
		this.sendBuffer = ByteBuffer.allocate(config.getMaxPakageSize());
		this.channel.connect(new InetSocketAddress(addr, port));
		
		long begin = System.currentTimeMillis();
		while (true) {
			if (System.currentTimeMillis() - begin > SCFConst.SOCKET_CONNECT_TIMEOUT) {
				this.channel.close();
				throw new ConnectTimeOutException("connect to " + addr + ":" + port + " timeout："+SCFConst.SOCKET_CONNECT_TIMEOUT+"ms");
			}
			this.channel.finishConnect();
			if (this.channel.isConnected()) break;
			try {
				Thread.sleep(50L);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}
		
		this.socket = this.channel.socket();
		this._connecting = true;
		this.dataReceiver = DataReceiver.instance();
		this.dataReceiver.RegSocketChannel(this);
		handler = NIOHandler.getInstance();

		logger.info("MaxPakageSize:" + config.getMaxPakageSize());
		logger.info("SendBufferSize:" + config.getSendBufferSize());
		logger.info("RecvBufferSize:" + config.getRecvBufferSize());
		logger.info("create a new connection :" + toString());
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
	 * 接收数据
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
		timeout = 1000000;
		if (!event.waitOne(timeout)) {
			throw new TimeoutException("ServiceName:[" + getServiceName() + "],ServiceIP:[" + getServiceIP() + "],Receive data timeout or error!timeout:" + timeout + "ms,queue length:" + queueLen);
		}
		
		byte[] data = wd.getData();
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
	 * 注册一个sessionId
	 * @param sessionId
	 */
	public void registerRec(int sessionId) {
		AutoResetEvent event = new AutoResetEvent();
		WindowData wd = new WindowData(event);
		this.WaitWindows.put(Integer.valueOf(sessionId), wd);
	}
	
	/**
	 * 注册一个sessionId
	 * @param sessionId - int
	 * @param wd - WindowData
	 */
	public void registerRec(int sessionId, WindowData wd) {
		this.WaitWindows.put(Integer.valueOf(sessionId), wd);
	}
	
	/**
	 * 注销一个sessionId
	 * @param sessionId
	 */
	public void unregisterRec(int sessionId) {
		this.WaitWindows.remove(Integer.valueOf(sessionId));
	}
	
	/**
	 * 判断是否已经有了sessionId了
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

	public void offerAsyncWrite(WindowData wd) {
		handler.offerWriteData(wd);
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
	 * 该链接是否是空闲状态
	 * @return boolean
	 */
	protected boolean isIdle() {
		return this.WaitWindows.size() <= 0;
	}

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
