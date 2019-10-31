package cg.zz.scf.protocol.exception;

public class RemoteException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6291998107598672145L;
	private int errCode;

	public int getErrCode() {
		return this.errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public RemoteException(int errCode, String msg) {
		super(msg);
		this.errCode = errCode;
	}

	public RemoteException(String msg) {
		this(-1, msg);
	}

}
