package cg.zz.scf.protocol.exception;

public class ParaException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 645820794419786011L;

	public ParaException() {
		super("服务器端方法调用参数错误!");
	}

	public ParaException(String message) {
		super(message);
		setErrCode(ReturnType.PARA_EXCEPTION);
	}

}
