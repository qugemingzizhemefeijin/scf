package cg.zz.scf.protocol.sdp;

import cg.zz.scf.serializer.component.annotation.SCFMember;
import cg.zz.scf.serializer.component.annotation.SCFSerializable;

/**
 * Response协议对象
 * @author chengang
 *
 */
@SCFSerializable(name="ResponseProtocol")
public class ResponseProtocol {
	
	/**
	 * 远程方法返回的结果对象
	 */
	@SCFMember(sortId=2)
	private Object result;
	
	/**
	 * Out对象返回值集合
	 */
	@SCFMember(sortId=1)
	private Object[] outpara;
	
	/**
	 * 构造Response协议对象
	 */
	public ResponseProtocol() {
		
	}
	
	/**
	 * 构造Response协议对象
	 * @param result - 返回结果
	 * @param outpara - Out对象集合
	 */
	public ResponseProtocol(Object result, Object[] outpara) {
		this.result = result;
		this.outpara = outpara;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Object[] getOutpara() {
		return outpara;
	}

	public void setOutpara(Object[] outpara) {
		this.outpara = outpara;
	}

}
