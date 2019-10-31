package cg.zz.scf.protocol.exception;

public class JSONException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5653606328447837587L;

	public JSONException() {
		super("服务器端JSON错误!!");
	}

	public JSONException(String message) {
		super(message);
		setErrCode(ReturnType.JSON_EXCEPTION);
	}

}
