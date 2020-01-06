package cg.zz.utility.async;

/**
 * 异步调用处理接口
 * @author chengang
 *
 */
public abstract interface IAsyncHandler {
	
	/**
	 * 在接收到客户端请求的时候，执行方法调用操作
	 * @return Object
	 * @throws Throwable
	 */
	public abstract Object run() throws Throwable;
	
	/**
	 * 消息接收
	 * @param paramObject - Object
	 */
	public abstract void messageReceived(Object paramObject);
	
	/**
	 * 异常捕获
	 * @param paramThrowable - Throwable
	 */
	public abstract void exceptionCaught(Throwable paramThrowable);

}
