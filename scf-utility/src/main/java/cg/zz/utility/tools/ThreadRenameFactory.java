package cg.zz.utility.tools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * 线程池线程创建工厂
 * 
 * @author chengang
 *
 */
public class ThreadRenameFactory implements ThreadFactory {

	static final AtomicInteger poolNumber = new AtomicInteger(1);
	final ThreadGroup group;
	final AtomicInteger threadNumber = new AtomicInteger(1);
	final String namePrefix;

	public ThreadRenameFactory(String threadNamePrefix) {
		SecurityManager s = System.getSecurityManager();
		this.group = (s != null ? s.getThreadGroup() : Thread.currentThread().getThreadGroup());
		this.namePrefix = (threadNamePrefix + "-pool-" + poolNumber.getAndIncrement() + "-tid-");
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread t = new Thread(this.group, r, this.namePrefix + this.threadNumber.getAndIncrement(), 0L);
		if (t.isDaemon()) {
			t.setDaemon(false);
		}
		if (t.getPriority() != 5) {
			t.setPriority(5);
		}
		return t;
	}

}
