package cg.zz.scf.server.performance;

/**
 * 垃圾回收的信息
 * @author chengang
 *
 */
public class MonitorGC {
	
	/**
	 * 年轻代回收次数
	 */
	private long yGcCount;
	
	/**
	 * 年轻代回收消耗的时间
	 */
	private long yGcTime;
	
	/**
	 * 年老代回收次数
	 */
	private long fGcCount;
	
	/**
	 * 年老代回收消耗的时间
	 */
	private long fGcTime;
	
	/**
	 * 垃圾回收总消耗的时间
	 */
	private long gcTime;

	public long getyGcCount() {
		return yGcCount;
	}

	public void setyGcCount(long yGcCount) {
		this.yGcCount = yGcCount;
	}

	public long getyGcTime() {
		return yGcTime;
	}

	public void setyGcTime(long yGcTime) {
		this.yGcTime = yGcTime;
	}

	public long getfGcCount() {
		return fGcCount;
	}

	public void setfGcCount(long fGcCount) {
		this.fGcCount = fGcCount;
	}

	public long getfGcTime() {
		return fGcTime;
	}

	public void setfGcTime(long fGcTime) {
		this.fGcTime = fGcTime;
	}

	public long getGcTime() {
		return gcTime;
	}

	public void setGcTime(long gcTime) {
		this.gcTime = gcTime;
	}

}
