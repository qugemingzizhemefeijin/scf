package cg.zz.scf.protocol.exception;

public class ProtocolException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2379318952762692304L;

	public ProtocolException() {
		super("服务器端协议出错!");
	}

	public ProtocolException(String message) {
		super(message);
		setErrCode(ReturnType.PROTOCOL);
	}

}
