package cg.zz.utility.async;

/**
 * 异步任务
 * @author chengang
 *
 */
public class AsyncTask {
	
	/**
	 * 默认超时时间
	 */
	private static final int DEFAULT_TIME_OUT = 3000;
	
	/**
	 * 任务执行的超时时间
	 */
	private int timeout;
	
	/**
	 * 在队列中的超时时间
	 */
	private int qtimeout;
	
	/**
	 * 任务添加时间
	 */
	private long addTime;
	
	/**
	 * 在队列中的时间
	 */
	private int inQueueTime;
	
	/**
	 * 异步调用处理类
	 */
	private IAsyncHandler handler;
	
	/**
	 * 构造异步任务
	 * @param timeout - 超时时间，会抛出TimeoutException异常(单位：豪秒)
	 * @param handler - 执行句柄
	 */
	public AsyncTask(int timeout, IAsyncHandler handler) {
		if (timeout < 0) {
			timeout = DEFAULT_TIME_OUT;
		}
		
		this.timeout = timeout;
		this.qtimeout = (timeout * 3 / 2 + 1);
		this.handler = handler;
		this.addTime = System.currentTimeMillis();
	}
	
	/**
	 * 构造异步任务
	 * @param timeout - 超时时间，会抛出TimeoutException异常(单位：豪秒)
	 * @param handler - 执行句柄
	 * @param inQueueTime - 允许在队列的时间（如果超过这个时间，会打印警告语句）
	 */
	public AsyncTask(int timeout, IAsyncHandler handler, int inQueueTime) {
		if (timeout < 0) {
			timeout = DEFAULT_TIME_OUT;
		}
		
		this.timeout = timeout;
		this.qtimeout = (timeout * 3 / 2 + 1);
		this.handler = handler;
		this.addTime = System.currentTimeMillis();
		this.inQueueTime = inQueueTime;
	}
	
	/**
	 * 构造异步任务
	 * @param handler - 执行句柄
	 */
	public AsyncTask(IAsyncHandler handler) {
		this.timeout = DEFAULT_TIME_OUT;
		this.qtimeout = (this.timeout * 3 / 2 + 1);
		this.handler = handler;
		this.addTime = System.currentTimeMillis();
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getQtimeout() {
		return qtimeout;
	}

	public void setQtimeout(int qtimeout) {
		this.qtimeout = qtimeout;
	}

	public long getAddTime() {
		return addTime;
	}

	public void setAddTime(long addTime) {
		this.addTime = addTime;
	}

	public int getInQueueTime() {
		return inQueueTime;
	}

	public void setInQueueTime(int inQueueTime) {
		this.inQueueTime = inQueueTime;
	}

	public IAsyncHandler getHandler() {
		return handler;
	}

	public void setHandler(IAsyncHandler handler) {
		this.handler = handler;
	}

}
