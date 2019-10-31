package cg.zz.scf.server.performance.monitorweb;

import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.performance.JVMMonitor;
import cg.zz.scf.server.performance.MonitorGC;
import cg.zz.scf.server.performance.MonitorMemory;

/**
 * 监控jvm运行状态
 * @author chengang
 *
 */
public class MonitorJVM {
	
	private static ILog logger = LogFactory.getLogger(MonitorJVM.class);
	
	private MonitorUDPClient udp;
	private String serviceName;
	
	public MonitorJVM(MonitorUDPClient udp, String serviceName) {
		this.udp = udp;
		this.serviceName = serviceName;
	}
	
	public void jvmGc() {
		if (this.udp == null) {
			return;
		}
		
		MonitorMemory monitorMemory = JVMMonitor.getMemoryUsed();
		StringBuffer strb = new StringBuffer();
		strb.append(monitorMemory.getSurvivor().getCommitted());
		strb.append("\t");
		strb.append(monitorMemory.getSurvivor().getUsed());
		strb.append("\t");
		strb.append(monitorMemory.getPerm().getCommitted());
		strb.append("\t");
		strb.append(monitorMemory.getPerm().getUsed());
		strb.append("\t");
		strb.append(monitorMemory.getEden().getCommitted());
		strb.append("\t");
		strb.append(monitorMemory.getEden().getUsed());
		strb.append("\t");
		strb.append(monitorMemory.getOld().getCommitted());
		strb.append("\t");
		strb.append(monitorMemory.getOld().getUsed());
		strb.append("\t");
		strb.append(this.serviceName);
		
		try {
			byte[] responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.Gc);
			this.udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send jvmGc error");
			e.printStackTrace();
		}
	}
	
	public void jvmGCUtil() {
		MonitorMemory monitorMemory = JVMMonitor.getMemoryUsed();
		MonitorGC monitorGC = JVMMonitor.getGcTime();
		DecimalFormat df = new DecimalFormat("0.00");
		StringBuffer strb = new StringBuffer();
		strb.append(df.format(monitorMemory.getSurvivor().getPercentage()));
		strb.append("\t");
		strb.append(df.format(monitorMemory.getEden().getPercentage()));
		strb.append("\t");
		strb.append(df.format(monitorMemory.getOld().getPercentage()));
		strb.append("\t");
		strb.append(df.format(monitorMemory.getPerm().getPercentage()));
		strb.append("\t");
		strb.append(df.format(monitorMemory.getCodeCache().getPercentage()));
		strb.append("\t");
		strb.append(monitorGC.getyGcCount());
		strb.append("\t");
		strb.append(monitorGC.getyGcTime());
		strb.append("\t");
		strb.append(monitorGC.getfGcCount());
		strb.append("\t");
		strb.append(monitorGC.getfGcTime());
		strb.append("\t");
		strb.append(monitorGC.getGcTime());
		strb.append("\t");
		strb.append(this.serviceName);
		
		try {
			byte[] responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.GCUtil);
			this.udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send jvmGCUtil error");
			e.printStackTrace();
		}
	}
	
	public void jvmThreadCount() {
		StringBuilder strb = new StringBuilder();
		strb.append(JVMMonitor.getAllThreadsCount());
		strb.append("\t");
		strb.append(JVMMonitor.getPeakThreadCount());
		strb.append("\t");
		strb.append(JVMMonitor.getDaemonThreadCount());
		strb.append("\t");
		strb.append(JVMMonitor.getTotalStartedThreadCount());
		strb.append("\t");
		strb.append(JVMMonitor.getDeadLockCount());
		strb.append("\t");
		strb.append(this.serviceName);
		try {
			byte[] responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.ThreadCount);
			this.udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send jvmThreadCount error");
			e.printStackTrace();
		}
	}
	
	public void jvmClassCount() {
		StringBuffer strb = new StringBuffer();
		strb.append(JVMMonitor.getLoadedClassCount());
		strb.append("\t");
		strb.append(JVMMonitor.getUnloadedClassCount());
		strb.append("\t");
		strb.append(JVMMonitor.getTotalLoadedClassCount());
		strb.append("\t");
		strb.append(this.serviceName);
		try {
			byte[] responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.ClassCount);
			this.udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send jvmClassCount error");
			e.printStackTrace();
		}
	}
	
	public void jvmMemory() {
		StringBuilder strb = new StringBuilder();
		strb.append(JVMMonitor.getTotolMemory());
		strb.append("\t");
		strb.append(JVMMonitor.getUsedMemory());
		strb.append("\t");
		strb.append(JVMMonitor.getMaxUsedMemory());
		strb.append("\t");
		strb.append(this.serviceName);
		try {
			byte[] responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.Memory);
			this.udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send JVMMemory error");
			e.printStackTrace();
		}
	}
	
	public void jvmHeapMemory() {
		MemoryUsage memoryUsage = JVMMonitor.getJvmHeapMemory();
		StringBuilder strb = new StringBuilder();
		strb.append(memoryUsage.getInit());
		strb.append("\t");
		strb.append(memoryUsage.getCommitted());
		strb.append("\t");
		strb.append(memoryUsage.getMax());
		strb.append("\t");
		strb.append(memoryUsage.getUsed());
		strb.append("\t");
		strb.append(this.serviceName);
		try {
			byte[] responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.HeapMemory);
			this.udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send JVMMemory error");
			e.printStackTrace();
		}
	}
	
	public void jvmNoHeapMemory() {
		MemoryUsage memoryUsage = JVMMonitor.getJvmNoHeapMemory();
		StringBuilder strb = new StringBuilder();
		strb.append(memoryUsage.getInit());
		strb.append("\t");
		strb.append(memoryUsage.getCommitted());
		strb.append("\t");
		strb.append(memoryUsage.getMax());
		strb.append("\t");
		strb.append(memoryUsage.getUsed());
		strb.append("\t");
		strb.append(this.serviceName);
		try {
			byte[] responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.NoHeapMemory);
			this.udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send JVMMemory error");
			e.printStackTrace();
		}
	}
	
	public void jvmLoad() {
		StringBuilder strb = new StringBuilder();
		strb.append(JVMMonitor.getSystemLoad());
		strb.append("\t");
		strb.append(this.serviceName);
		try {
			byte[] responseByte = strb.toString().getBytes("utf-8");
			MonitorProtocol protocol = new MonitorProtocol(MonitorType.jvm, JVMExType.Load);
			this.udp.send(protocol.dataCreate(responseByte));
		} catch (Exception e) {
			logger.error("send JVMMemory error");
			e.printStackTrace();
		}
	}

}
