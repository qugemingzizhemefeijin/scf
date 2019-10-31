package cg.zz.scf.protocol.exception;

public class NotFoundMethodException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3714106202809139151L;

	public NotFoundMethodException() {
		super("服务器端找不到指定的调用方法!!");
	}

	public NotFoundMethodException(String message) {
		super(message);
		setErrCode(ReturnType.NOT_FOUND_METHOD_EXCEPTION);
	}

}
