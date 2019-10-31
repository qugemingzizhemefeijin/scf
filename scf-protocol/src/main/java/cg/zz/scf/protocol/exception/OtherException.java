package cg.zz.scf.protocol.exception;

public class OtherException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9071504569138506759L;

	public OtherException() {
		super("服务器端其他错误!");
	}

	public OtherException(String message) {
		super(message);
		setErrCode(ReturnType.OTHER_EXCEPTION);
	}

}
