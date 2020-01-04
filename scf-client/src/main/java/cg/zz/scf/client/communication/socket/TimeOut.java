package cg.zz.scf.client.communication.socket;

/**
 * 此类无用了
 * @author chengang
 *
 */
@Deprecated
public class TimeOut {
	
	private int sessionId;
	private long time;
	private WindowData wd;
	private CSocket cSocket;
	
	public TimeOut(int sessionId, WindowData wd, CSocket cSocket) {
		this.sessionId = sessionId;
		this.wd = wd;
		this.cSocket = cSocket;
		this.time = System.currentTimeMillis();
	}
	
	public int getSessionId() {
		return this.sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public long getTime() {
		return this.time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public WindowData getWd() {
		return this.wd;
	}

	public void setWd(WindowData wd) {
		this.wd = wd;
	}

	public CSocket getcSocket() {
		return this.cSocket;
	}

	public void setcSocket(CSocket cSocket) {
		this.cSocket = cSocket;
	}

}
