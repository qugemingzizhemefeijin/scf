package cg.zz.scf.protocol.exception;

public class WaitTimeoutException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1372593305957117788L;
	private String message = "客户端等待可用连接超时了";

	public WaitTimeoutException() {
	}

	public WaitTimeoutException(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
