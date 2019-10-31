package cg.zz.scf.protocol.exception;

public class NetException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2664280646471652558L;

	public NetException() {
		super("服务器端网络错误!");
	}

	public NetException(String message) {
		super(message);
		setErrCode(ReturnType.NET);
	}

}
