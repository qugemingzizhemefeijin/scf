package cg.zz.scf.protocol.exception;

public class ChannelClosedException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7421801487828987366L;

	public ChannelClosedException() {
		this("Channel连接已经断开了!");
	}

	public ChannelClosedException(String message) {
		super(message);
	}

}
