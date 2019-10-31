package cg.zz.scf.client.utility.helper;

/**
 * 系统帮助类
 * @author Administrator
 *
 */
public class SystemUtils {
	
	/**
	 * 获得系统能够开启的线程数，根据可以开启的最大线程数-1来计算，最少为1
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
	 * 根据CPU计算系统可以开启的最大线程数
	 * 如果cpu>6，则为6个，否则=cpu数量
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

}
