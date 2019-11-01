package cg.zz.scf.client.loadbalance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import cg.zz.scf.client.communication.socket.CSocket;
import cg.zz.scf.client.communication.socket.ScoketPool;
import cg.zz.scf.client.communication.socket.ThreadRenameFactory;
import cg.zz.scf.client.communication.socket.WindowData;
import cg.zz.scf.client.configuration.loadbalance.ServerProfile;
import cg.zz.scf.client.loadbalance.component.ServerState;
import cg.zz.scf.client.proxy.builder.ReceiveHandler;
import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;
import cg.zz.scf.protocol.sfp.v1.Protocol;

/**
 * 服务
 *
 */
public class Server {
	
	private static ILog logger = LogFactory.getLogger(Server.class);
	
	/**
	 * 端口
	 */
	private int port;
	
	/**
	 * 权重
	 */
	private int weight;
	
	/**
	 * 名称
	 */
	private String name;
	
	/**
	 * 僵死时间
	 */
	private long deadTime;
	
	/**
	 * 地址
	 */
	private String address;
	
	/**
	 * 僵死超时时间
	 */
	private int deadTimeout;
	
	/**
	 * 权重比例
	 */
	private float weightRage;
	
	/**
	 * 当前正在请求的数量
	 */
	private int currUserCount;
	
	/**
	 * 当前服务器状态
	 */
	private ServerState state;
	
	/**
	 * 服务器的连接池对象
	 */
	private ScoketPool scoketpool;
	
	/**
	 * 是否正在测试中
	 */
	private boolean testing = false;
	
	/**
	 * 线程池
	 */
	private final ScheduledExecutorService scheduler;
	
	/**
	 * 创建Server对象，在Dispatcher对象初始化的时候会被调用
	 * @param config - 服务配置
	 */
	protected Server(ServerProfile config) {
		this.name = config.getName();
		this.address = config.getHost();
		this.port = config.getPort();
		this.weightRage = config.getWeithtRate();
		this.deadTimeout = config.getDeadTimeout();
		if (this.weightRage >= 0.0F)
			this.state = ServerState.Normal;
		else {
			this.state = ServerState.Disable;
		}
		this.scheduler = Executors.newScheduledThreadPool(2, new ThreadRenameFactory("Async " + getName() + "-Server Thread"));
	}
	
	public long getDeadTime() {
		return this.deadTime;
	}

	public void setDeadTime(long deadTime) {
		this.deadTime = deadTime;
	}

	public String getName() {
		return this.name;
	}

	public String getAddress() {
		return this.address;
	}

	public int getCurrUserCount() {
		return this.currUserCount;
	}

	public int getPort() {
		return this.port;
	}

	public ScoketPool getScoketpool() {
		return this.scoketpool;
	}

	protected void setScoketpool(ScoketPool scoketpool) {
		this.scoketpool = scoketpool;
	}

	public ServerState getState() {
		return this.state;
	}

	public synchronized void setState(ServerState state) {
		this.state = state;
	}

	public int getWeight() {
		return this.weight;
	}

	public float getWeightRage() {
		return this.weightRage;
	}

	public int getDeadTimeout() {
		return this.deadTimeout;
	}

	protected void setDeadTimeout(int deadTimeout) {
		this.deadTimeout = deadTimeout;
	}

	public boolean isTesting() {
		return this.testing;
	}

	public void setTesting(boolean testing) {
		this.testing = testing;
	}
	
	public Protocol request(Protocol p) throws Exception, Throwable {
		if (this.state == ServerState.Dead) {
			logger.warn("This proxy server is unavailable.state:" + this.state + "+host:" + this.address);
			throw new Exception("This proxy server is unavailable.state:" + this.state + "+host:" + this.address);
		}
		
		increaseCU();
		CSocket socket = null;
		try {
			try {
				logger.info("发送数据中.....");
				socket = this.scoketpool.getSocket();
				byte[] data = p.toBytes(socket.isRights(), socket.getDESKey());
				socket.registerRec(p.getSessionID());
				socket.send(data);
			} catch (Throwable ex) {
				logger.error("Server get socket Exception", ex);
				throw ex;
			} finally {
				if (socket != null) {
					socket.dispose();
				}
			}
			logger.info("接收数据中.....");
			byte[] buffer = socket.receive(p.getSessionID(), currUserCount);
			Protocol result = Protocol.fromBytes(buffer, socket.isRights(), socket.getDESKey());
			if (this.state == ServerState.Testing) {
				relive();
			}
			return result;
		} catch (IOException ex) {
			logger.error("io exception", ex);
			if (socket == null || !socket.connecting()) {
				if (!test()) {
					markAsDead();
				}
			}
			throw ex;
		} catch (Throwable ex) {
			logger.error("request other Exception", ex);
			throw ex;
		} finally {
			if (state == ServerState.Testing) {
				markAsDead();
			}
			if (socket != null) {
				socket.unregisterRec(p.getSessionID());
			}
			decreaseCU();
		}
	}
	
	public void requestAsync(Protocol p, ReceiveHandler receiveHandler) throws Exception, Throwable {
		if (this.state == ServerState.Dead) {
			logger.warn("This proxy server is unavailable.state:" + this.state + "+host:" + this.address);
			throw new Exception("This proxy server is unavailable.state:" + this.state + "+host:" + this.address);
		}
		
		increaseCU();
		CSocket socket = null;
		try {
			try {
				socket = this.scoketpool.getSocket();
				byte[] data = p.toBytes(socket.isRights(), socket.getDESKey());
				WindowData wd = new WindowData(receiveHandler, socket, data, p.getSessionID());
				socket.registerRec(p.getSessionID(), wd);
				socket.offerAsyncWrite(wd);
			} catch (Throwable ex) {
				logger.error("Server get socket Exception", ex);
				throw ex;
			} finally {
				if (socket != null) socket.dispose();
			}
		} catch (IOException ex) {
			logger.error("io exception", ex);
			if (((socket == null) || (!socket.connecting())) && (!test())) {
				markAsDead();
			}

			throw ex;
		} catch (Throwable ex) {
			logger.error("request other Exception", ex);
			throw ex;
		} finally {
			decreaseCU();
		}
	}
	
	public String toString() {
		return "Name:" + this.name + ",Address:" + this.address + ",Port:" + this.port + ",Weight:" + this.weight + ",State:" + this.state.toString() + ",CurrUserCount:" + this.currUserCount + ",ScoketPool:" + this.scoketpool.count();
	}

	private synchronized void increaseCU() {
		this.currUserCount += 1;
	}

	private synchronized void decreaseCU() {
		this.currUserCount -= 1;
		if (this.currUserCount <= 0) this.currUserCount = 0;
	}
	
	/**
	 * 标记服务已经僵死
	 * @throws Exception
	 */
	private void markAsDead() throws Exception {
		logger.info("markAsDead server:" + this.state + "--server hashcode:" + hashCode() + "--conn count:" + this.scoketpool.count());
		ServerChecker.check(this);
		if (this.state == ServerState.Dead) {
			logger.info("before markAsDead the server is dead!!!");
			return;
		}
		synchronized (this) {
			if (this.state == ServerState.Dead) {
				logger.info("before markAsDead the server is dead!!!");
				return;
			}
			logger.warn("this server is dead!host:" + this.address);
			setState(ServerState.Dead);
			this.deadTime = System.currentTimeMillis();
			this.scoketpool.destroy();
		}
	}
	
	/**
	 * 设置当前服务状态为重启
	 * @throws Exception
	 * @throws Throwable
	 */
	public void createReboot() throws Exception, Throwable {
		synchronized (this) {
			if (this.state == ServerState.Reboot) {
				logger.info("before markAsReboot the server is Reboot!");
				return;
			}

			logger.warn("this server is reboot! host:" + this.address);
			setState(ServerState.Reboot);
			setDeadTime(System.currentTimeMillis());

			/**
			 * 如果当前连接处于重启状态则注销当前服务所有socket 任务调度 3秒后执行
			 */
			this.scheduler.schedule(new TimerJob(this), 3L, TimeUnit.SECONDS);
		}
	}
	
	/**
	 * 设置当前服务为僵死状态
	 */
	public void createDead() throws Exception {
		markAsDead();
	}
	
	/**
	 * 设置当前服务为正常状态
	 */
	public void markAsNormal() {
		relive();
	}
	
	/**
	 * relive Server if this Server is died
	 */
	public void relive() {
		logger.info("this server is relive!host:" + this.address);
		if (this.state == ServerState.Normal) {
			return;
		}
		synchronized (this) {
			if (this.state == ServerState.Normal) {
				return;
			}
			logger.info("this server is relive!host:" + this.address);
			this.state = ServerState.Normal;
		}
	}
	
	/**
	 * 测试是否可以连接到服务器
	 * @return boolean
	 */
	private boolean test() {
		if (this.testing) {//正在测试中，则直接返回true
			return true;
		}
		this.testing = true;
		boolean result = false;
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(this.address, this.port), 100);
			socket.close();
			result = true;
		} catch (Exception localException) {
		} finally {
			logger.info("test server :" + this.address + ":" + this.port + "--alive:" + result);
			this.testing = false;
		}
		return result;
	}
	
	/**
	 * 测试是否可以连接
	 * @return boolean
	 */
	public boolean testing() {
		return test();
	}

}
