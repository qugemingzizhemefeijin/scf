package cg.zz.scf.protocol.exception;

public class TimeoutException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8719231946037262862L;

	public TimeoutException() {
		super("服务器端服务调用超时出错!");
	}

	public TimeoutException(String message) {
		super(message);
		setErrCode(ReturnType.TIME_OUT);
	}

}
