package cg.zz.scf.server.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public final class SystemUtils {
	
	/**
	 * 锁
	 */
	private static final ReentrantLock lock = new ReentrantLock();
	
	/**
	 * sessionID
	 */
	private static final AtomicInteger sessionID = new AtomicInteger(0);
	
	/**
	 * 最大sessionID
	 */
	private static final long MAX_SESSIONID = 1 << 30;
	
	/**
	 * 获得系统并发线程数量
	 * @return int
	 */
	public static int getSystemThreadCount() {
		int cpus = getCpuProcessorCount();
		int result = cpus - 1;
		return result == 0 ? 1 : result;
	}
	
	/**
	 * 获得CPU数量
	 * @return int
	 */
	public static int getCpuProcessorCount() {
		return Runtime.getRuntime().availableProcessors();
	}
	
	/**
	 * 获得CPU数量，如果CPU<7个则n=CPU数量，并且不大于6个
	 * @return int
	 */
	public static int getHalfCpuProcessorCount() {
		int cpu = getCpuProcessorCount();
		int n = cpu / 2;
		if (cpu < 7) {
			n = cpu;
		}
		return n > 6 ? 6 : n;
	}
	
	/**
	 * 创建sessionID
	 * @return int
	 */
	public static int createSessionId() {
		try {
			lock.lock();
			int sID = sessionID.getAndIncrement();
			if (sessionID.getAndIncrement() > MAX_SESSIONID) {
				sessionID.set(1);
			}
			int i = sID;
			return i;
		} finally {
			lock.unlock();
		}
	}

}
