package cg.zz.scf.client.utility;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AutoResetEvent {
	
	CountDownLatch cdl;
	
	public AutoResetEvent() {
		this.cdl = new CountDownLatch(1);
	}
	
	public AutoResetEvent(int waitCount) {
		this.cdl = new CountDownLatch(waitCount);
	}
	
	public void set() {
		this.cdl.countDown();
	}

	public boolean waitOne(long time) throws InterruptedException {
		return this.cdl.await(time, TimeUnit.MILLISECONDS);
	}

}
