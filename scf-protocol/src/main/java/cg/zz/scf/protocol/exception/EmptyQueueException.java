package cg.zz.scf.protocol.exception;

public class EmptyQueueException extends RuntimeException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4040234702663985015L;

	public EmptyQueueException(String err) {
		super(err);
	}

}
