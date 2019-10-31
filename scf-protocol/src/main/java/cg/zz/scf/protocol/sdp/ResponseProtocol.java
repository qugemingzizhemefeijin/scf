package cg.zz.scf.protocol.sdp;

import cg.zz.scf.serializer.component.annotation.SCFMember;
import cg.zz.scf.serializer.component.annotation.SCFSerializable;

/**
 * Response协议
 * @author chengang
 *
 */
@SCFSerializable(name="ResponseProtocol")
public class ResponseProtocol {
	
	@SCFMember(sortId=2)
	private Object result;
	
	@SCFMember(sortId=1)
	private Object[] outpara;
	
	public ResponseProtocol() {
		
	}
	
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
