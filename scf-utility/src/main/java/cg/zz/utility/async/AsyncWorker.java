package cg.zz.utility.async;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cg.zz.utility.jsr166.LinkedTransferQueue;
import cg.zz.utility.jsr166.TransferQueue;
import cg.zz.utility.tools.InetAddressUtil;

/**
 * 异步执行者
 * @author chengang
 *
 */
public class AsyncWorker extends Thread {
	
	private static final Log logger = LogFactory.getLog(AsyncWorker.class);
	
	/**
	 * 获取队列中的数据默认超时时间
	 */
	private static final long POLL_DEFAULT_TIMEOUT = 1500L;
	
	/**
	 * 线程工厂名称
	 */
	final String threadFactoryName;
	
	/**
	 * 本地ID
	 */
	static String localIp = InetAddressUtil.getIpMixed();
	
	/**
	 * 共享的任务队列
	 */
	private final TransferQueue<AsyncTask> taskQueue;
	
	/**
	 * 任务执行器
	 */
	private final Executor executor;
	
	/**
	 * 是否结束
	 */
	private boolean isStop = false;

	/**
	 * 超时开关
	 */
	private boolean timeoutEffect = false;
	
	AsyncWorker(Executor executor, boolean timeoutEffect, String threadFactoryName) {
		this.taskQueue = new LinkedTransferQueue<AsyncTask>();
		this.executor = executor;
		this.timeoutEffect = timeoutEffect;
		this.threadFactoryName = threadFactoryName;
	}

	@Override
	public void run() {
		if(this.timeoutEffect) {
			while(!isStop) {
				execTimeoutTask();
			}
		} else {
			while(!isStop) {
				execNoTimeLimitTask();
			}
		}
	}
	
	/**
	 * 添加任务
	 * @param task - AsyncTask
	 */
	void addTask(AsyncTask task) {
		this.taskQueue.offer(task);
	}

	/**
	 * 停止工作线程(stop is final)
	 */
	void end() {
		this.isStop = true;
		logger.info("-------------------async workder is stop-------------------");
	}
	
	/**
	 * 执行不超时的任务
	 */
	private void execNoTimeLimitTask() {
		AsyncTask task = null;
		try {
			task = (AsyncTask)this.taskQueue.poll(POLL_DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
			if (null != task) {
				//查看任务是否超过了qtimeout
				if (System.currentTimeMillis() - task.getAddTime() > task.getQtimeout()) {
					task.getHandler().exceptionCaught(new TimeoutException(this.threadFactoryName + " async task timeout!" + " Host ip:" + localIp));
					return;
				}
				//如果队列
				if (task.getInQueueTime() != -1 && (System.currentTimeMillis() - task.getAddTime() >= task.getInQueueTime())) {
					logger.error(this.threadFactoryName + " The task inQueue time :" + (System.currentTimeMillis() - task.getAddTime()));
				}
				Object obj = task.getHandler().run();
				task.getHandler().messageReceived(obj);
			}
		} catch (InterruptedException e) {
			
		} catch (Throwable e) {
			if (task != null) task.getHandler().exceptionCaught(e);
		}
	}
	
	/**
	 * 有超时时间的任务
	 */
	private void execTimeoutTask() {
		try {
			final AsyncTask task = (AsyncTask)this.taskQueue.poll(POLL_DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
			if (null != task) {
				//查看任务是否超过了qtimeout
				if (System.currentTimeMillis() - task.getAddTime() > task.getQtimeout()) {
					task.getHandler().exceptionCaught(new TimeoutException(this.threadFactoryName + "async task timeout!" + " Host ip:" + localIp));
					return;
				}
				final CountDownLatch cdl = new CountDownLatch(1);
				this.executor.execute(new Runnable(){

					@Override
					public void run() {
						try {
							Object obj = task.getHandler().run();
							task.getHandler().messageReceived(obj);
						} catch (Throwable ex) {
							task.getHandler().exceptionCaught(ex);
						} finally {
							cdl.countDown();
						}
					}
					
				});
				
				//设置超时等待时间
				cdl.await(getTimeout(task.getTimeout(), this.taskQueue.size()), TimeUnit.MILLISECONDS);
				if (cdl.getCount() > 0L) {
					task.getHandler().exceptionCaught(new TimeoutException("async task timeout!"));
				}
			} else {
				logger.error("execTimeoutTask take task is null");
			}
		} catch (InterruptedException e) {
			logger.error("");
		} catch (Throwable e) {
			logger.error("get task from poll error", e);
		}
	}
	
	/**
	 * 获得超时时间(队列越长，超时时间越短，最长不会超过初始给定的值)
	 * @param timeout - 任务超时时间
	 * @param queueLen - 队列长度
	 * @return int
	 */
	private int getTimeout(int timeout, int queueLen) {
		if ((queueLen <= 0) || (timeout < 5)) {
			return timeout;
		}

		float rad = (float) (timeout - timeout * 0.006D * queueLen);
		int result = (int) rad < 5 ? 5 : (int) rad;
		if (queueLen > 100) {
			logger.warn("async task,queueLen:" + queueLen + ",fact timeout:" + result + ",original timeout:" + timeout);
		}

		return result;
	}

}
