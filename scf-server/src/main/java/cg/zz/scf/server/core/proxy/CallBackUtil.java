package cg.zz.scf.server.core.proxy;

import java.util.concurrent.TimeUnit;

import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.util.SystemUtils;
import cg.zz.utility.jsr166.LinkedTransferQueue;
import cg.zz.utility.jsr166.TransferQueue;

public final class CallBackUtil {
	
	/**
	 * 获得线程数量
	 */
	private static final int COUNT = SystemUtils.getHalfCpuProcessorCount();
	
	private final TransferQueue<WData> checkQueue = new LinkedTransferQueue<WData>();
	
	int taskTimeOut = 1000;//任务超时时间
	
	private Thread[] workers;
	
	public CallBackUtil() {
		String sTaskTimeOut = Global.getInstance().getServiceConfig().getString("back.task.timeout");
		if (sTaskTimeOut != null && !"".equals(sTaskTimeOut)) {
			this.taskTimeOut = Integer.parseInt(sTaskTimeOut);
			this.taskTimeOut = (this.taskTimeOut * 3 / 2 + 1);
		}
		
		this.workers = new Thread[COUNT];
		for (int i = 0; i < COUNT; i++) {
			this.workers[i] = new Thread(new CallBackHandle());
			this.workers[i].setName("CallBackHandle thread[" + i+ "]");
			this.workers[i].setDaemon(true);
			this.workers[i].start();
		}
	}
	
	/**
	 * 将WData添加到队列的尾部
	 * @param wd - WData
	 */
	public void offer(WData wd) {
		this.checkQueue.offer(wd);
	}
	
	class CallBackHandle implements Runnable {

		@Override
		public void run() {
			while (true) {
				try {
					WData wd = CallBackUtil.this.checkQueue.poll(1500L, TimeUnit.MILLISECONDS);
					if (wd != null) {
						//如果context被删除了。则直接移除
						if (AsynBack.contextMap.get(Integer.valueOf(wd.getSessionID())).isDel()) {
							AsynBack.contextMap.remove(Integer.valueOf(wd.getSessionID()));
							continue;
						}
						
						if (System.currentTimeMillis() - wd.getTime() > CallBackUtil.this.taskTimeOut) {
							AsynBack.send(wd.getSessionID(), new Exception("wait other server receive timeout.wait time is " + CallBackUtil.this.taskTimeOut));
							//这里再放回去有啥用？？
							//send中会将SCFContext的isDel设置为true，肯定会走上面的contextMap.remove逻辑，感觉没啥意义。。
							CallBackUtil.this.offer(wd);
							continue;
						}
						
						CallBackUtil.this.offer(wd);
						Thread.sleep(1L);
					}
				} catch (InterruptedException e) {
					try {
						Thread.sleep(10L);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
					e.printStackTrace();
				} catch (Exception ex) {
					try {
						Thread.sleep(10L);
					} catch (InterruptedException ie) {
						ie.printStackTrace();
					}
					ex.printStackTrace();
				}
			}
		}
		
	}

}
