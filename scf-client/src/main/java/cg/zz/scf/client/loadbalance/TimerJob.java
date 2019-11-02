package cg.zz.scf.client.loadbalance;

import cg.zz.scf.client.communication.socket.ScoketPool;

/**
 * 用于销毁所有的Socket连接，在Server.createReboot()设置服务器重启的时候启动。
 * @author chengang
 *
 */
public class TimerJob implements Runnable {
	
	/**
	 * 服务器对象
	 */
	private Server server = null;
	
	/**
	 * 构造定时任务，传入服务器对象
	 * @param server - Server
	 */
	public TimerJob(Server server) {
		this.server = server;
	}

	@Override
	public void run() {
		try {
			//获取连接池并全部销毁
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
