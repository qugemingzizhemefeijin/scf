package cg.zz.scf.client.communication.socket;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;

public class NIOHandler {
	
	private static final ILog logger = LogFactory.getLogger(NIOHandler.class);

	private static final NIOHandler handler = new NIOHandler();
	final int q_size = 30000;
	
	private static final ThreadPoolExecutor writeExe = new ThreadPoolExecutor(1, 1, 1500L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), new ThreadRenameFactory("NIOHandler-Send-Thread"));

	private static final ThreadPoolExecutor timeOutExe = new ThreadPoolExecutor(1, 1, 1500L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), new ThreadRenameFactory("NIOHandler-TimeOut-Thread"));

	public static NIOHandler getInstance() {
		return handler;
	}
	
	/**
	 * 将消息放入异步队列中发送，队列最多支持3W条消息
	 * @param wd - WindowData
	 */
	public void offerWriteData(WindowData wd) {
		if (getWriteQueueSize() > q_size || getTimeOutQueueSize() > q_size) {
			logger.warn("writeQueue size > " + q_size);
			wd.getReceiveHandler().callBack(new Exception("writeQueue size > " + q_size));
			return;
		}
		try {
			sendInvoke(wd);
			timeOutInvoke(wd);
		} catch (Exception e) {
			logger.warn("input queue error");
			wd.getReceiveHandler().callBack(new Exception("input queue error"));
			return;
		}
	}
	
	private void sendInvoke(final WindowData wd) {
		writeExe.execute(new Runnable() {
			public void run() {
				try {
					if (wd != null) {
						switch (wd.getFlag()) {
						case 1:
							wd.getCsocket().send(wd.getSendData());
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private void timeOutInvoke(final WindowData wd) {
		timeOutExe.execute(new Runnable() {
			public void run() {
				try {
					if (wd != null) {
						if (System.currentTimeMillis() - wd.getTimestamp() > wd.getCsocket().getTimeOut(NIOHandler.getWriteQueueSize())) {
							String exceptionMsg = "ServiceName:[" + wd.getCsocket().getServiceName() + "],ServiceIP:[" + wd.getCsocket().getServiceIP() + "],Receive data timeout or error!timeout:" + (System.currentTimeMillis() - wd.getTimestamp());
							wd.getReceiveHandler().callBack(new Exception(exceptionMsg));
							wd.getCsocket().unregisterRec(wd.getSessionId());
						} else if (wd.getCsocket().hasSessionId(wd.getSessionId())) {
							NIOHandler.this.timeOutInvoke(wd);
							Thread.sleep(1L);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public static int getWriteQueueSize() {
		return writeExe.getQueue().size();
	}

	public static int getTimeOutQueueSize() {
		return timeOutExe.getQueue().size();
	}

}
