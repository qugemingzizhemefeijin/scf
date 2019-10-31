package cg.zz.scf.server.core.communication.http;

public class HttpException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String errorMsg;
	
	private int errorCode;
	
	public HttpException(String errorMsg, int errorCode, Throwable cause) {
		super(errorMsg, cause);
		setErrorMsg(errorMsg);
		setErrorCode(errorCode);
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

}
