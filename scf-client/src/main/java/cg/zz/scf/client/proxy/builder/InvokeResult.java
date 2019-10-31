package cg.zz.scf.client.proxy.builder;

public class InvokeResult<T> {
	
	private T Result;
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
