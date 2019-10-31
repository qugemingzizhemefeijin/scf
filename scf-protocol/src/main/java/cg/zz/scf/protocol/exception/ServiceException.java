package cg.zz.scf.protocol.exception;

public class ServiceException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4204550150352540357L;

	public ServiceException() {
		super("服务器端服务出错!");
	}

	public ServiceException(String message) {
		super(message);
		setErrCode(ReturnType.SERVICE_EXCEPTION);
	}

}
