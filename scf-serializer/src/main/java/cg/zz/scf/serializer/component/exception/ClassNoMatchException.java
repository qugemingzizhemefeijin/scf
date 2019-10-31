package cg.zz.scf.serializer.component.exception;

public class ClassNoMatchException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6763559547703460331L;
	
	/**
	 * 错误信息
	 */
	private String msg;

	public ClassNoMatchException(String message) {
		this.msg = message;
	}

	public ClassNoMatchException() {
		this.msg = "Class error.";
	}

	public String getMessage() {
		return this.msg;
	}

}
