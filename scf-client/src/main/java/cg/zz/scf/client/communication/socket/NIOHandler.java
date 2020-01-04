package cg.zz.scf.client.communication.socket;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;

/**
 * 异步消息的处理类
 * @author chengang
 *
 */
public class NIOHandler {
	
	private static final ILog logger = LogFactory.getLogger(NIOHandler.class);

	/**
	 * 单例，其实如果用轻量级的Juice代码会更优雅
	 */
	private static final NIOHandler handler = new NIOHandler();
	
	/**
	 * 最多存储的异步回调数量
	 */
	final int q_size = 30000;
	
	/**
	 * 异步消息发送队列
	 */
	private static final ThreadPoolExecutor writeExe = new ThreadPoolExecutor(1, 1, 1500L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), new ThreadRenameFactory("NIOHandler-Send-Thread"));

	/**
	 * 消息超时队列
	 */
	private static final ThreadPoolExecutor timeOutExe = new ThreadPoolExecutor(1, 1, 1500L, TimeUnit.MILLISECONDS,
			new LinkedBlockingQueue<Runnable>(), new ThreadRenameFactory("NIOHandler-TimeOut-Thread"));

	public static NIOHandler getInstance() {
		return handler;
	}
	
	/**
	 * 将消息放入异步队列中发送（同时放入到超时队列中），队列最多支持3W条消息
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
	
	/**
	 * 将异步消息放入到异步发送队列中
	 * @param wd - WindowData
	 */
	private void sendInvoke(final WindowData wd) {
		writeExe.execute(new Runnable() {
			public void run() {
				try {
					if (wd != null) {
						//这里弄一个if判断不是更直观？？
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
	
	/**
	 * 将异步消息放入到超时处理队列中
	 * @param wd - WindowData
	 */
	private void timeOutInvoke(final WindowData wd) {
		timeOutExe.execute(new Runnable() {
			public void run() {
				try {
					if (wd != null) {
						//判断如果消息超时了
						if (System.currentTimeMillis() - wd.getTimestamp() > wd.getCsocket().getTimeOut(NIOHandler.getWriteQueueSize())) {
							//构造一个异步超时的异常
							String exceptionMsg = "ServiceName:[" + wd.getCsocket().getServiceName() + "],ServiceIP:[" + wd.getCsocket().getServiceIP() + "],Receive data timeout or error!timeout:" + (System.currentTimeMillis() - wd.getTimestamp());
							wd.getReceiveHandler().callBack(new Exception(exceptionMsg));
							//注销消息，不维护WindowData
							wd.getCsocket().unregisterRec(wd.getSessionId());
						} else if (wd.getCsocket().hasSessionId(wd.getSessionId())) {//如果异步消息还被socket维护着，则重新放入到队列中
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
	
	/**
	 * 获取待发送消息的队列成员数量
	 * @return int
	 */
	public static int getWriteQueueSize() {
		return writeExe.getQueue().size();
	}

	/**
	 * 获取超时消息处理队列成员数量
	 * @return int
	 */
	public static int getTimeOutQueueSize() {
		return timeOutExe.getQueue().size();
	}

}
