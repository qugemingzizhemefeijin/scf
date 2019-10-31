package cg.zz.scf.protocol.exception;

public class RebootException extends RemoteException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9160157907128639769L;

	public RebootException() {
		super("服务正在重启!");
	}

	public RebootException(String message) {
		super(message);
		setErrCode(ReturnType.REBOOT_EXCEPTION);
	}

}
