package cg.zz.scf.server.performance.monitorweb;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 异常计数器
 * @author chengang
 *
 */
public final class FrameExCount {
	
	private static AtomicInteger count = new AtomicInteger(0);
	
	public static void messageRecv() {
		count.getAndIncrement();
	}
	
	public static int getCount() {
		return count.get();
	}

	public static void initCount(int i) {
		count.set(i);
	}

}
