package cg.zz.scf.server.performance;

/**
 * 监控内存
 * @author chengang
 *
 */
public class MonitorMemory {
	
	/**
	 * 老年代内存
	 */
	private MemoryStat old;
	
	/**
	 * 伊甸园内存
	 */
	private MemoryStat eden;
	
	/**
	 * 交换区内存
	 */
	private MemoryStat survivor;
	
	/**
	 * 本地代码内存
	 */
	private MemoryStat codeCache;
	
	/**
	 * 永久带内存
	 */
	private MemoryStat perm;

	public MemoryStat getOld() {
		return old;
	}

	public void setOld(MemoryStat old) {
		this.old = old;
	}

	public MemoryStat getEden() {
		return eden;
	}

	public void setEden(MemoryStat eden) {
		this.eden = eden;
	}

	public MemoryStat getSurvivor() {
		return survivor;
	}

	public void setSurvivor(MemoryStat survivor) {
		this.survivor = survivor;
	}

	public MemoryStat getCodeCache() {
		return codeCache;
	}

	public void setCodeCache(MemoryStat codeCache) {
		this.codeCache = codeCache;
	}

	public MemoryStat getPerm() {
		return perm;
	}

	public void setPerm(MemoryStat perm) {
		this.perm = perm;
	}

}
