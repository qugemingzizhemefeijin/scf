package cg.zz.scf.protocol.exception;

public class NotFoundServiceException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2786850258160145720L;

	public NotFoundServiceException() {
		super("服务器端找不到指定的服务!");
	}

	public NotFoundServiceException(String message) {
		super(message);
		setErrCode(ReturnType.NOT_FOUND_SERVICE_EXCEPTION);
	}

}
