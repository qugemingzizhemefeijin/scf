package cg.zz.scf.server.util;

import java.io.Serializable;

import cg.zz.scf.protocol.sdp.RequestProtocol;
import cg.zz.scf.protocol.utility.KeyValuePair;

/**
 * SCF服务异常类
 * @author chengang
 *
 */
@SuppressWarnings("serial")
public class ServiceFrameException extends Exception implements Serializable {
	
	/**
	 * 错误类型枚举
	 */
	private ErrorState state;
	
	/**
	 * 错误描述
	 */
	private String errorMsg;
	
	/**
	 * 错误来源ID
	 */
	private String fromIP;
	
	/**
	 * 错误返回ID
	 */
	private String toIP;
	
	/**
	 * RequestProtocol协议对象
	 */
	private Object sdp;
	
	public ServiceFrameException(String errorMsg, String fromIP, String toIP, Object sdp, ErrorState state, Throwable cause) {
		super(errorMsg, cause);
		this.setState(state);
		this.setErrorMsg(errorMsg);
		this.setFromIP(fromIP);
		this.setToIP(toIP);
		this.setSdp(sdp);
	}
	
	public ServiceFrameException(String errorMsg, ErrorState state, Throwable cause){
	        this(errorMsg, "", "", null, state, cause);
	}
	
	public ServiceFrameException(String errorMsg, ErrorState state){
		this(errorMsg, "", "", null, state, null);
	}
	
	public ServiceFrameException(ErrorState state, Throwable cause){
		this("", "", "", null, state, cause);
	}
	
	public ServiceFrameException(ErrorState state){
		this("", "", "", null, state, null);
	}

	@Override
	public void printStackTrace() {
		System.out.println("-------------------------begin-----------------------------");
		System.out.println("fromIP:" + this.getFromIP());
		System.out.println("toIP:" + this.getToIP());
		System.out.println("state:" + this.getState().toString());
		System.out.println("errorMsg:" + this.getErrorMsg());
		System.out.println("MessageBodyBase:");
		
		if(sdp.getClass() == RequestProtocol.class) {
			RequestProtocol request = (RequestProtocol)this.sdp;
			System.out.println("Server.Lookup:" + request.getLookup());
			System.out.println("Server.MethodName:" + request.getMethodName());
			System.out.println("Server.ParaKVList:");
			for(KeyValuePair kv : request.getParaKVList()){
				System.out.println("key:" + kv.getKey() + "---value:"+kv.getValue());
			}
		}
		super.printStackTrace();
		System.out.println("--------------------------end------------------------------");
	}

	public ErrorState getState() {
		return state;
	}

	public void setState(ErrorState state) {
		this.state = state;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public String getFromIP() {
		return fromIP;
	}

	public void setFromIP(String fromIP) {
		this.fromIP = fromIP;
	}

	public String getToIP() {
		return toIP;
	}

	public void setToIP(String toIP) {
		this.toIP = toIP;
	}

	public Object getSdp() {
		return sdp;
	}

	public void setSdp(Object sdp) {
		this.sdp = sdp;
	}

}
