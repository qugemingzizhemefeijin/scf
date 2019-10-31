package cg.zz.scf.server.contract.entity;

/**
 * SCFResponse输出对象
 * @author chengang
 *
 * @param <T>
 */
public class Out<T> {
	
	private T outPara;

	@Deprecated
	public Out(){
		
	}
	
	public Out(T t) {
		this.outPara = t;
	}

	public T getOutPara() {
		return outPara;
	}

	public void setOutPara(T outPara) {
		this.outPara = outPara;
	}

}
