package cg.zz.scf.serializer.component.exception;

public class StreamException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8925160195081590343L;
	
	/**
	 * 错误信息
	 */
	private String msg;

	public StreamException(String message) {
		this.msg = message;
	}

	public StreamException() {
		this.msg = "Stream error.";
	}

	public String getMessage() {
		return this.msg;
	}

}
