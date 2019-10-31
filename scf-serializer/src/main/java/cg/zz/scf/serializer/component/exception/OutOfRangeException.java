package cg.zz.scf.serializer.component.exception;

public class OutOfRangeException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8890485233698970004L;
	
	/**
	 * 错误信息
	 */
	private String msg;

	public OutOfRangeException(String message) {
		this.msg = message;
	}

	public OutOfRangeException() {
		this.msg = "Out range exeception.";
	}

	public String getMessage() {
		return this.msg;
	}

}
