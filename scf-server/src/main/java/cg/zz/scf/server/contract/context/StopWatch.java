package cg.zz.scf.server.contract.context;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class StopWatch {
	
	/**
	 * 监控时间
	 */
	private Date monitorTime;
	
	/**
	 * 方法名称
	 */
	private String methodName;
	
	/**
	 * 命名空间
	 */
	private String lookup;
	
	/**
	 * 来源IP
	 */
	private String fromIP;
	
	/**
	 * 本地IP
	 */
	private String localIP;
	
	/**
	 * 监控计数器
	 */
	private Map<String, PerformanceCounter> mapCounter = new HashMap<String , PerformanceCounter>();
	
	/**
	 * 开始默认计数器
	 */
	public void start() {
		this.mapCounter.put("default", new PerformanceCounter("", "", System.currentTimeMillis()));
	}
	
	/**
	 * 暂停默认计数器
	 */
	public void stop() {
		PerformanceCounter pc = this.mapCounter.get("default");
		if (pc != null) pc.setEndTime(System.currentTimeMillis());
	}
	
	/**
	 * 重置所有计数器
	 */
	public void reset() {
		this.mapCounter.clear();
	}
	
	/**
	 * 开始指定名称的计数器
	 * @param key - 名称
	 * @param description - 描述
	 */
	public void startNew(String key, String description) {
		this.mapCounter.put(key, new PerformanceCounter(key, description, System.currentTimeMillis()));
	}
	
	/**
	 * 暂时指定名称的计数器
	 * @param key - 名称
	 */
	public void stop(String key) {
		PerformanceCounter pc = this.mapCounter.get(key);
		if (pc != null) pc.setEndTime(System.currentTimeMillis());
	}
	
	public Map<String, PerformanceCounter> getMapCounter() {
		return this.mapCounter;
	}
	
	public Date getMonitorTime() {
		return this.monitorTime;
	}
	
	public void setMonitorTime(Date monitorTime) {
		this.monitorTime = monitorTime;
	}
	
	public String getMethodName() {
		return this.methodName;
	}
	
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public void setLookup(String lookup) {
		this.lookup = lookup;
	}
	
	public String getLookup() {
		return this.lookup;
	}
	
	public String getFromIP() {
		return this.fromIP;
	}
	
	public void setFromIP(String fromIP) {
		this.fromIP = fromIP;
	}

	public String getLocalIP() {
		return this.localIP;
	}

	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

}
