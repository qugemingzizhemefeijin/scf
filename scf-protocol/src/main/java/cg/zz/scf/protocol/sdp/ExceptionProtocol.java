package cg.zz.scf.protocol.sdp;

import cg.zz.scf.serializer.component.annotation.SCFMember;
import cg.zz.scf.serializer.component.annotation.SCFSerializable;

/**
 * 错误信息协议
 * @author chengang
 *
 */
@SCFSerializable(name="ExceptionProtocol")
public class ExceptionProtocol {
	
	/**
	 * 错误码
	 */
	@SCFMember(sortId=1)
	private int errorCode;
	
	/**
	 * 目的IP
	 */
	@SCFMember(sortId=2)
	 private String toIP;

	/**
	 * 来源ID
	 */
	 @SCFMember(sortId=3)
	 private String fromIP;

	 /**
	  * 错误信息
	  */
	 @SCFMember(sortId=4)
	 private String errorMsg;

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getToIP() {
		return toIP;
	}

	public void setToIP(String toIP) {
		this.toIP = toIP;
	}

	public String getFromIP() {
		return fromIP;
	}

	public void setFromIP(String fromIP) {
		this.fromIP = fromIP;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

}
