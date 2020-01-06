package cg.zz.scf.client.loadbalance;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cg.zz.scf.client.loadbalance.component.ServerState;
import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;

/**
 * 服务检查
 * @author chengang
 *
 */
public class ServerChecker {
	
	private static final ILog logger = LogFactory.getLogger(ServerChecker.class);
	private static final long CHECKER_ALIVE_TIME = 180000L;//180秒
	private static final List<Server> checkList = Collections.synchronizedList(new ArrayList<Server>());
	
	/**
	 * 锁
	 */
	private static final Object locker = new Object();
	private static Thread checker = null;
	private static long checkerStartTime;
	
	/**
	 * 将服务加入到待检测列表中，并且启动线程来检查服务的连通
	 * @param serv - Server
	 */
	public static void check(Server serv) {
		serv.setState(ServerState.Testing);
		
		if (!checkList.contains(serv)) {
			synchronized (locker) {
				if (!checkList.contains(serv)) {
					checkList.add(serv);
				}
			}
		}
		
		if (checker == null || checker.getState() == Thread.State.TERMINATED) {
			synchronized (locker) {
				if (checker == null || checker.getState() == Thread.State.TERMINATED) {
					//这里实际上用一个单线程的线程池会更好
					checker = createChecker();
					checkerStartTime = System.currentTimeMillis();
					checker.start();
				}
			}
		}
	}
	
	/**
	 * 测试连接是否正常
	 * @param serv - Server
	 * @return boolean
	 */
	private static boolean test(Server serv) {
		String strAddr = serv.getAddress() + ":" + serv.getPort();
		logger.warn("test server:" + strAddr);
		
		Socket sock = null;
		try {
			InetSocketAddress addr = new InetSocketAddress(serv.getAddress(), serv.getPort());
			sock = new Socket();
			sock.connect(addr, 2000);
			return sock.isConnected();
		} catch (IOException e) {
			logger.error("SCF server(" + strAddr + ") is dead");
		} finally {
			if (sock != null) {
				try {
					sock.close();
				} catch (IOException e) {
					logger.error("close test sock error", e);
				}
			}
		}
		
		return false;
	}
	
	/**
	 * 创建线程对象
	 * @return Thread
	 */
	private static Thread createChecker() {
		Thread t = new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (ServerChecker.checkList.size() == 0 && System.currentTimeMillis() - ServerChecker.checkerStartTime > CHECKER_ALIVE_TIME) {
						ServerChecker.logger.info("all server is ok the checker thread exit");
						return;
					}
					try {
						if (ServerChecker.checkList.size() > 0) {
							for (int i = 0; i < ServerChecker.checkList.size(); i++) {
								Server serv = ServerChecker.checkList.get(i);
								if (serv != null) {
									//这里根据检查的结果，设置服务是死了还是正常
									if (ServerChecker.test(serv)) {
										ServerChecker.checkList.remove(i);
										serv.setState(ServerState.Normal);
									} else {
										serv.setState(ServerState.Dead);
									}
								}
							}
						}
					} catch (Exception ex) {
						ServerChecker.logger.error("checker thread error", ex);
						try {
							Thread.sleep(10000L);
						} catch (InterruptedException e) {
							ServerChecker.logger.error("checker thread sleep error", e);
						}
					} finally {
						try {
							Thread.sleep(10000L);
						} catch (InterruptedException e) {
							ServerChecker.logger.error("checker thread sleep error", e);
						}
					}
				}
			}
		});
		
		t.setName("SCF server state checker");
		t.setDaemon(true);
		return t;
	}

}
