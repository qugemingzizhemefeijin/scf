package cg.zz.scf.client.loadbalance;

import cg.zz.scf.client.communication.socket.ScoketPool;

/**
 * 用于销毁所有的Socket连接
 * @author chengang
 *
 */
public class TimerJob implements Runnable {
	
	private Server server = null;
	
	public TimerJob(Server server) {
		this.server = server;
	}

	@Override
	public void run() {
		try {
			ScoketPool sp = this.server.getScoketpool();
			try {
				sp.destroy();
			} catch (Throwable e) {
				System.out.println("destroy socket fail!");
				e.printStackTrace();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

}
