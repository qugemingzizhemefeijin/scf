package cg.zz.scf.client.proxy.builder;

/**
 * 调用返回的时候，用于从Exception/Response/Reset等协议对象中获取返回对象和OutPara对象并组装使用<br/>
 * 
 * 异步回调：cg.zz.scf.client.proxy.builder.ReceiveHandler.notify<br/>
 * 同步回调：cg.zz.scf.client.proxy.builder.MethodCaller.doMethodCall<br/>
 * @author chengang
 *
 * @param <T>
 */
public class InvokeResult<T> {
	
	/**
	 * 远程调用返回的结果
	 */
	private T Result;
	
	/**
	 * 方法参数中Out参数的返回数据
	 */
	private Object[] OutPara;

	public InvokeResult(T result, Object[] outPara) {
		this.Result = result;
		this.OutPara = outPara;
	}

	public Object[] getOutPara() {
		return this.OutPara;
	}

	public void setOutPara(Object[] OutPara) {
		this.OutPara = OutPara;
	}

	public T getResult() {
		return this.Result;
	}

	public void setResult(T Result) {
		this.Result = Result;
	}

}
