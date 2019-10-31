package cg.zz.scf.server.performance;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * jvm监控
 * @author chengang
 *
 */
public class JVMMonitor {
	
	//用于操作系统的管理接口，Java 虚拟机在此操作系统上运行。 
	private static final OperatingSystemMXBean bean = ManagementFactory.getOperatingSystemMXBean();
	
	//Java 虚拟机线程系统的管理接口。
	private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
	
	//Java 虚拟机的内存系统的管理接口。
	private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
	
	//返回 Java 虚拟机试图使用的最大内存量
	private static final long maxMemory = Runtime.getRuntime().maxMemory();
	
	//用于 Java 虚拟机的类加载系统的管理接口。
	private static final ClassLoadingMXBean classLoadingBean = ManagementFactory.getClassLoadingMXBean();
	
	//返回 Java 虚拟机中的 GarbageCollectorMXBean[用于 Java 虚拟机的垃圾回收的管理接口] 对象列表
	private static final List<GarbageCollectorMXBean> garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
	
	//内存池的管理接口列表。堆内存和非堆内存都在此列表中
	private static final List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
	
	private static final Set<String> edenSpace = new HashSet<>();
	private static final Set<String> survivorSpace = new HashSet<>();
	private static final Set<String> oldSpace = new HashSet<>();
	private static final Set<String> permSpace = new HashSet<>();
	private static final Set<String> codeCacheSpace = new HashSet<>();
	private static final List<String> youngGenCollectorNames = new ArrayList<>();
	private static final List<String> fullGenCollectorNames = new ArrayList<>();
	
	static {
		//此处初始化的是可能在不同版本的jvm中，虚拟机的一些名称叫法不一样
		edenSpace.add("Eden Space");
		edenSpace.add("PS Eden Space");
		edenSpace.add("Par Eden Space");
		edenSpace.add("Par Eden Space");
		edenSpace.add("PS Eden Space");
		
		survivorSpace.add("Survivor Space");
		survivorSpace.add("PS Survivor Space");
		survivorSpace.add("Par Survivor Space");
		survivorSpace.add("Par survivor Space");
		survivorSpace.add("PS Survivor Space");
		
		oldSpace.add("Tenured Gen");
		oldSpace.add("PS Old Gen");
		oldSpace.add("CMS Old Gen");
		oldSpace.add("Tenured Gen  Gen");
		oldSpace.add("PS Old Gen");
		
		permSpace.add("Perm Gen");
		permSpace.add("PS Perm Gen");
		permSpace.add("CMS Perm Gen");
		permSpace.add("Perm Gen");
		permSpace.add("PS Perm Gen");
		
		codeCacheSpace.add("Code Cache");

		youngGenCollectorNames.add("Copy");

		youngGenCollectorNames.add("ParNew");

		youngGenCollectorNames.add("PS Scavenge");

		fullGenCollectorNames.add("MarkSweepCompact");

		fullGenCollectorNames.add("PS MarkSweep");

		fullGenCollectorNames.add("ConcurrentMarkSweep");
	}
	
	/**
	 * 获得JVM的线程的ID
	 * @return String
	 */
	public static String getProcessorsId() {
		return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}
	
	/**
	 * 返回平台的服务信息
	 * @return MBeanServer
	 */
	public static MBeanServer getPlatformMBeanServer() {
		return ManagementFactory.getPlatformMBeanServer();
	}
	
	/**
	 * 返回最后一分钟内系统加载平均值
	 * @return double
	 */
	public static double getSystemLoad() {
		double sl = bean.getSystemLoadAverage();
		if (sl < 0.0d) {
			return 0.0d;
		}
		return sl;
	}
	
	/**
	 * 返回 Java 虚拟机可以使用的处理器数目
	 * @return int
	 */
	public static int getAvailableProcessors() {
		return bean.getAvailableProcessors();
	}
	
	/**
	 * 获得文件描述符信息
	 * @return String
	 */
	public static String getFileDescriptor() {
		try {
			String[] attributeNames = { "MaxFileDescriptorCount", "OpenFileDescriptorCount" };
			ObjectName name = new ObjectName("java.lang:type=OperatingSystem");
			
			AttributeList attributes = getPlatformMBeanServer().getAttributes(name, attributeNames);
			StringBuilder sb = new StringBuilder();
			int i = 0;
			for (int len = attributes.size(); i < len; i++) {
				if (sb.length() > 0) {
					sb.append("\r\n");
				}
				sb.append(attributes.get(i).toString().replace(" = ", ":"));
			}
			return sb.toString();
		} catch (MalformedObjectNameException e) {
			return "MaxFileDescriptorCount:0\r\nOpenFileDescriptorCount:0";
		} catch (NullPointerException e) {
			return "MaxFileDescriptorCount:0\r\nOpenFileDescriptorCount:0";
		} catch (InstanceNotFoundException e) {
			return "MaxFileDescriptorCount:0\r\nOpenFileDescriptorCount:0";
		} catch (ReflectionException e) {
			
		}
		return "MaxFileDescriptorCount:0\r\nOpenFileDescriptorCount:0";
	}
	
	/**
	 * 返回活动线程的当前数目，包括守护线程和非守护线程。
	 * @return int
	 */
	public static int getAllThreadsCount() {
		return threadBean.getThreadCount();
	}
	
	/**
	 * 返回自从 Java 虚拟机启动或峰值重置以来峰值活动线程计数。
	 * @return int
	 */
	public static int getPeakThreadCount() {
		return threadBean.getPeakThreadCount();
	}
	
	/**
	 * 返回活动守护线程的当前数目。
	 * @return int
	 */
	public static int getDaemonThreadCount() {
		return threadBean.getDaemonThreadCount();
	}
	
	/**
	 * 返回自从 Java 虚拟机启动以来创建和启动的线程总数目。
	 * @return int
	 */
	public static long getTotalStartedThreadCount() {
		return threadBean.getTotalStartedThreadCount();
	}
	
	/**
	 * 找到处于死锁状态（等待获取对象监视器）的线程的周期。
	 * @return int
	 */
	public static int getDeadLockCount() {
		long[] deadLockIds = threadBean.findMonitorDeadlockedThreads();
		if (deadLockIds == null) {
			return 0;
		}
		return deadLockIds.length;
	}
	
	/**
	 * 返回用于对象分配的堆的当前内存使用量。
	 * @return MemoryUsage
	 */
	public static MemoryUsage getJvmHeapMemory() {
		return memoryMXBean.getHeapMemoryUsage();
	}
	
	/**
	 * 返回 Java 虚拟机使用的非堆内存的当前内存使用量。
	 * @return MemoryUsage
	 */
	public static MemoryUsage getJvmNoHeapMemory() {
		return memoryMXBean.getNonHeapMemoryUsage();
	}
	
	/**
	 * 返回 Java 虚拟机中的内存总量。
	 * @return long 字节
	 */
	public static long getTotolMemory() {
		return Runtime.getRuntime().totalMemory();
	}
	
	/**
	 * 返回 Java 虚拟机中的已使用的内存总量。
	 * @return long 字节
	 */
	public static long getUsedMemory() {
		long totalMemory = Runtime.getRuntime().totalMemory();
		return totalMemory - Runtime.getRuntime().freeMemory();
	}
	
	/**
	 * 返回 Java 虚拟机试图使用的最大内存量
	 * @return long
	 */
	public static long getMaxUsedMemory() {
		return maxMemory;
	}
	
	/**
	 * 返回自 Java 虚拟机开始执行到目前已经加载的类的总数。
	 * @return long
	 */
	public static long getTotalLoadedClassCount() {
		return classLoadingBean.getTotalLoadedClassCount();
	}
	
	/**
	 * 返回当前加载到 Java 虚拟机中的类的数量。
	 * @return int
	 */
	public static int getLoadedClassCount() {
		return classLoadingBean.getLoadedClassCount();
	}
	
	/**
	 * 返回自 Java 虚拟机开始执行到目前已经卸载的类的总数。
	 * @return long
	 */
	public static long getUnloadedClassCount() {
		return classLoadingBean.getUnloadedClassCount();
	}
	
	/**
	 * 返回垃圾回收的信息
	 * @return MonitorGC
	 */
	public static MonitorGC getGcTime() {
		MonitorGC monitorGC = new MonitorGC();
		for (GarbageCollectorMXBean bean : garbageCollectorMXBeans) {
			if (youngGenCollectorNames.contains(bean.getName())) {
				monitorGC.setyGcCount(bean.getCollectionCount());
				monitorGC.setyGcTime(bean.getCollectionTime());
			} else if (fullGenCollectorNames.contains(bean.getName())) {
				monitorGC.setfGcCount(bean.getCollectionCount());
				monitorGC.setfGcTime(bean.getCollectionTime());
			}
		}
		
		monitorGC.setGcTime(monitorGC.getfGcTime() + monitorGC.getyGcTime());
		return monitorGC;
	}
	
	/**
	 * 获得java堆与非堆内存信息
	 * @return Map<String , MemoryUsage>
	 */
	public static Map<String, MemoryUsage> getMemoryPoolCollectionUsage() {
		Map<String , MemoryUsage> gcMemory = new HashMap<String , MemoryUsage>();
		for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
			String name = memoryPoolMXBean.getName();
			if (edenSpace.contains(name)) {
				gcMemory.put("eden", memoryPoolMXBean.getCollectionUsage());
			} else if (survivorSpace.contains(name)) {
				gcMemory.put("survivor", memoryPoolMXBean.getCollectionUsage());
			} else if (oldSpace.contains(name)) {
				gcMemory.put("old", memoryPoolMXBean.getCollectionUsage());
			} else if (permSpace.contains(name)) {
				gcMemory.put("perm", memoryPoolMXBean.getCollectionUsage());
			} else if (codeCacheSpace.contains(name)) {
				gcMemory.put("codeCache", memoryPoolMXBean.getCollectionUsage());
			}
		}
		return gcMemory;
	}
	
	/**
	 * 获得java堆和非堆内存的使用信息
	 * @return Map<String, MemoryUsage>
	 */
	public static Map<String, MemoryUsage> getMemoryPoolUsage() {
		Map<String, MemoryUsage> gcMemory = new HashMap<String, MemoryUsage>();
		for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
			String name = memoryPoolMXBean.getName();
			if (edenSpace.contains(name)) {
				gcMemory.put("eden", memoryPoolMXBean.getUsage());
			} else if (survivorSpace.contains(name)) {
				gcMemory.put("survivor", memoryPoolMXBean.getUsage());
			} else if (oldSpace.contains(name)) {
				gcMemory.put("old", memoryPoolMXBean.getUsage());
			} else if (permSpace.contains(name)) {
				gcMemory.put("perm", memoryPoolMXBean.getUsage());
			} else if (codeCacheSpace.contains(name)) {
				gcMemory.put("codeCache", memoryPoolMXBean.getUsage());
			}
		}
		return gcMemory;
	}
	
	/**
	 * 获得java虚拟机的堆与非堆的内存使用信息
	 * @return MonitorMemory
	 */
	public static MonitorMemory getMemoryUsed() {
		MonitorMemory monitorMemory = new MonitorMemory();
		for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
			String name = memoryPoolMXBean.getName();
			if (edenSpace.contains(name)) {
				monitorMemory.setEden(mUsageToMStat(memoryPoolMXBean));
			} else if (survivorSpace.contains(name)) {
				monitorMemory.setSurvivor(mUsageToMStat(memoryPoolMXBean));
			} else if (oldSpace.contains(name)) {
				monitorMemory.setOld(mUsageToMStat(memoryPoolMXBean));
			} else if (permSpace.contains(name)) {
				monitorMemory.setPerm(mUsageToMStat(memoryPoolMXBean));
			} else if (codeCacheSpace.contains(name)) {
				monitorMemory.setCodeCache(mUsageToMStat(memoryPoolMXBean));
			}
		}
		return monitorMemory;
	}
	
	/**
	 * 获得内存的信息
	 * @param memoryPoolMXBean - MemoryPoolMXBean
	 * @return MemoryStat
	 */
	public static MemoryStat mUsageToMStat(MemoryPoolMXBean memoryPoolMXBean) {
		MemoryStat memoryStat = new MemoryStat();
		memoryStat.setCommitted(memoryPoolMXBean.getUsage().getCommitted());
		memoryStat.setInit(memoryPoolMXBean.getUsage().getInit());
		memoryStat.setMax(memoryPoolMXBean.getUsage().getMax());
		memoryStat.setUsed(memoryPoolMXBean.getUsage().getUsed());
		memoryStat.setPercentage(memoryPoolMXBean.getUsage().getUsed() / memoryPoolMXBean.getUsage().getInit() * 100.0D);
		return memoryStat;
	}
	
	public static void main(String[] args) {
		System.err.println(getDeadLockCount());
	}

}
