package cg.zz.scf.client.utility;

public class AutoResetEventO {
	
	public synchronized void set() {
		notify();
	}

	public synchronized boolean waitOne(long time) throws InterruptedException {
		long t = System.currentTimeMillis();
		wait(time);

		return System.currentTimeMillis() - t < time;
	}

}
