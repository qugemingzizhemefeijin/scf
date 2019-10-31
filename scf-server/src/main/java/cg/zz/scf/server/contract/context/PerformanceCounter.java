package cg.zz.scf.server.contract.context;

/**
 * 性能计数器
 * @author chengang
 *
 */
public class PerformanceCounter {
	
	/**
	 * 计数器名称
	 */
	private String key;
	
	/**
	 * 计数器描述
	 */
	private String description;
	
	/**
	 * 开始时间
	 */
	private long startTime;
	
	/**
	 * 结束时间
	 */
	private long endTime;
	
	public PerformanceCounter() {
		
	}
	
	/**
	 * 构造监控计时器
	 * @param key - String
	 * @param description - 描述
	 * @param startTime - 开始时间
	 */
	public PerformanceCounter(String key, String description, long startTime) {
		this.key = key;
		this.description = description;
		this.startTime = startTime;
	}
	
	/**
	 * 返回执行时间
	 * @return long
	 */
	public long getExecuteTime() {
		return this.endTime - this.startTime;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

}
