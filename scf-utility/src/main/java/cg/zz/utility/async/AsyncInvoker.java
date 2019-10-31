package cg.zz.utility.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cg.zz.utility.tools.ThreadRenameFactory;

/**
 * 异步调用器
 * @author chengang
 *
 */
public class AsyncInvoker {
	
	/**
	 * Round Robin
	 */
	private int rr = 0;
	
	/**
	 * 工作线程
	 */
	private AsyncWorker[] workers = null;
	
	/**
	 * 获取AsyncInvoker实例(默认工作线程数为CPU的个数)
	 * @return AsyncInvoker
	 */
	public static AsyncInvoker getInstance() {
		int cpuCount = Runtime.getRuntime().availableProcessors();
		return new AsyncInvoker(cpuCount, false, null);
	}
	
	/**
	 * 获取AsyncInvoker实例
	 * @param workerCount - 工作线程数
	 * @return AsyncInvoker
	 */
	public static AsyncInvoker getInstance(int workerCount) {
		return new AsyncInvoker(workerCount, false, null);
	}
	
	/**
	 * 获取AsyncInvoker实例
	 * @param workerCount - 工作线程数
	 * @param timeoutEffect - 是否启用调用超时
	 * @return AsyncInvoker
	 */
	public static AsyncInvoker getInstance(int workerCount, boolean timeoutEffect) {
		return new AsyncInvoker(workerCount, timeoutEffect, null);
	}

	/**
	 * 获取AsyncInvoker实例
	 * @param workerCount - 工作线程个数
	 * @param timeoutEffect - 是否启用调用超时
	 * @param threadFactoryName - 线程池名称
	 * @return AsyncInvoker
	 */
	public static AsyncInvoker getInstance(int workerCount, boolean timeoutEffect, String threadFactoryName) {
		return new AsyncInvoker(workerCount, timeoutEffect, threadFactoryName);
	}
	
	/**
	 * 构造函数
	 * @param workerCount - 工作线程个数
	 * @param timeoutEffect - 是否启用调用超时
	 * @param threadFactoryName - 线程池名称
	 */
	private AsyncInvoker(int workerCount, boolean timeoutEffect, String threadFactoryName) {
		if (threadFactoryName == null) {
			threadFactoryName = "";
		}
		this.workers = new AsyncWorker[workerCount];

		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadRenameFactory(threadFactoryName + "async task thread"));

		for (int i = 0; i < this.workers.length; i++) {
			this.workers[i] = new AsyncWorker(executor, timeoutEffect, threadFactoryName);
			this.workers[i].setDaemon(true);
			this.workers[i].setName("async task worker[" + i + "]");
			this.workers[i].start();
		}
	}
	
	/**
	 * 执行异步任务(无阻塞立即返回,当前版本只实现轮询分配,下个版本增加工作线程间的任务窃取)
	 * @param task - AsyncTask
	 */
	@Deprecated
	public void run(AsyncTask task) {
		if (this.rr > 10000) {
			this.rr = 0;
		}
		int idx = this.rr % this.workers.length;
		this.workers[idx].addTask(task);
		this.rr += 1;
	}
	
	/**
	 * 执行异步任务(无阻塞立即返回,当前版本只实现轮询分配,下个版本增加工作线程间的任务窃取)
	 * @param timeOut - 超时时间
	 * @param handler - 任务handler
	 */
	public void run(int timeOut, IAsyncHandler handler) {
		AsyncTask task = new AsyncTask(timeOut, handler);
		if (this.rr > 10000) {
			this.rr = 0;
		}
		int idx = this.rr % this.workers.length;
		this.workers[idx].addTask(task);
		this.rr += 1;
	}
	
	/**
	 * 执行异步任务(无阻塞立即返回,当前版本只实现轮询分配,下个版本增加工作线程间的任务窃取)
	 * @param timeOut - 超时时间
	 * @param inQueue - 队列时间
	 * @param handler - 任务handler
	 */
	public void run(int timeOut, int inQueue, IAsyncHandler handler) {
		AsyncTask task = new AsyncTask(timeOut, handler, inQueue);
		if (this.rr > 10000) {
			this.rr = 0;
		}
		int idx = this.rr % this.workers.length;
		this.workers[idx].addTask(task);
		this.rr += 1;
	}
	
	/**
	 * 停止所有工作线程
	 */
	public void stop() {
		for (AsyncWorker worker : workers) {
			worker.end();
		}
	}

}
