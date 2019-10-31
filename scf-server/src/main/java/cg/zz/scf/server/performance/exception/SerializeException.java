package cg.zz.scf.server.performance.exception;

public class SerializeException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public SerializeException(String message, Throwable cause) {
		super(message, cause);
	}
	
	public SerializeException(String message) {
		super(message);
	}
	
	public SerializeException(Throwable cause) {
		super("serialize exception", cause);
	}
	
	public SerializeException() {
		super("serialize exception");
	}

}
