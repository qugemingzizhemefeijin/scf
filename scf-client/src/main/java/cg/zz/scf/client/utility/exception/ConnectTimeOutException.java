package cg.zz.scf.client.utility.exception;

public class ConnectTimeOutException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2679650526476666891L;
	private String msg;

	public ConnectTimeOutException(String message) {
		this.msg = message;
	}

	public ConnectTimeOutException() {
		this.msg = "Connect TimeOut Exception.";
	}

	public String getMessage() {
		return this.msg;
	}

}
