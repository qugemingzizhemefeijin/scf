package cg.zz.scf.server.performance.monitorweb;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 丢弃消息的统计类
 * @author chengang
 *
 */
public class AbandonCount {
	
	/**
	 * 原子Integer
	 */
	private static AtomicInteger count = new AtomicInteger(0);

	/**
	 * 统计数据+1
	 */
	public static void messageRecv() {
		count.getAndIncrement();
	}

	/**
	 * 得到总数
	 * @return int
	 */
	public static int getCount() {
		return count.get();
	}

	/**
	 * 初始化统计数据
	 * @param i - int
	 */
	public static void initCount(int i) {
		count.set(i);
	}

}
