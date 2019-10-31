package cg.zz.scf.server.performance.monitorweb;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cg.zz.scf.server.contract.context.StopWatch;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;

/**
 * 监控来源IP的调用次数
 * @author chengang
 *
 */
public class MonitorCount {
	
	static ILog logger = LogFactory.getLogger(MonitorCount.class);
	private static AtomicInteger count = new AtomicInteger(0);
	private static Map<String, Integer> fromIP = new ConcurrentHashMap<>();
	
	public static void messageRecv(StopWatch sw) {
		if (sw == null) {
			return;
		}
		
		count.getAndIncrement();
		String ip = sw.getFromIP();
		int countIP = 0;
		if (fromIP.containsKey(ip)) {
			countIP = ((Integer)fromIP.get(ip)).intValue() + 1;
			fromIP.put(ip, Integer.valueOf(countIP));
		} else {
			fromIP.put(ip, Integer.valueOf(1));
		}
	}
	
	public int getCount() {
		return count.get();
	}
	
	public static void initCount(int i) {
		count.set(i);
	}
	
	public void initMCount() {
		if (fromIP != null) {
			fromIP.clear();
		}
		initCount(0);
	}
	
	public static Map<String, Integer> getFromIP() {
		return fromIP;
	}

}
