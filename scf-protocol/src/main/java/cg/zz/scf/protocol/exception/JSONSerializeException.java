package cg.zz.scf.protocol.exception;

public class JSONSerializeException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1345850871133701013L;

	public JSONSerializeException() {
		super("服务器端数据JSON序列化错误!");
	}

	public JSONSerializeException(String message) {
		super(message);
		setErrCode(ReturnType.JSON_SERIALIZE_EXCEPTION);
	}

}
