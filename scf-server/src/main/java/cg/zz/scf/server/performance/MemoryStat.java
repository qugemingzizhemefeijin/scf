package cg.zz.scf.server.performance;

/**
 * 内存状态
 * @author chengang
 *
 */
public class MemoryStat {
	
	/**
	 * 表示 Java 虚拟机在启动期间从操作系统请求的用于内存管理的初始内存容量（以字节为单位）。
	 */
	private long init;
	
	/**
	 * 表示当前已经使用的内存量（以字节为单位）。 
	 */
	private long used;
	
	/**
	 * 表示保证可以由 Java 虚拟机使用的内存量（以字节为单位）。
	 */
	private long committed;
	
	/**
	 * 表示可以用于内存管理的最大内存量（以字节为单位）。
	 */
	private long max;
	
	/**
	 * 内存使用百分比
	 */
	private double percentage;

	public long getInit() {
		return init;
	}

	public void setInit(long init) {
		this.init = init;
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}

	public long getCommitted() {
		return committed;
	}

	public void setCommitted(long committed) {
		this.committed = committed;
	}

	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		this.max = max;
	}

	public double getPercentage() {
		return percentage;
	}

	public void setPercentage(double percentage) {
		this.percentage = percentage;
	}

}
