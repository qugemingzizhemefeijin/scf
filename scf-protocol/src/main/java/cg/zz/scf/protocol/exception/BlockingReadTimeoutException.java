package cg.zz.scf.protocol.exception;

import java.io.InterruptedIOException;

public class BlockingReadTimeoutException extends InterruptedIOException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6874380221437586597L;

	public BlockingReadTimeoutException() {
		super("客户端读取数据超时了！");
	}

	public BlockingReadTimeoutException(String message, Throwable cause) {
		super(message);
		initCause(cause);
	}

	public BlockingReadTimeoutException(String message) {
		super(message);
	}

	public BlockingReadTimeoutException(Throwable cause) {
		initCause(cause);
	}

}
