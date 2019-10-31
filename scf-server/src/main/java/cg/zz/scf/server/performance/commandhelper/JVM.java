package cg.zz.scf.server.performance.commandhelper;

import java.io.IOException;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;
import cg.zz.scf.server.performance.JVMMonitor;
import cg.zz.scf.server.performance.MonitorGC;
import cg.zz.scf.server.performance.MonitorMemory;

/**
 * jvm监控调用
 * @author chengang
 *
 */
public class JVM extends CommandHelperBase {
	
	private static ILog logger = LogFactory.getLogger(JVM.class);

	@Override
	public Command createCommand(String commandStr) {
		if(commandStr != null && !commandStr.equals("")) {
			String[] args = commandStr.split("\\-");
			if (args[0].trim().equalsIgnoreCase("jvm")) {
				Command entity = new Command();
				entity.setCommandType(CommandType.JVM);
			        entity.setSecond(0);
			        entity.setGroup(0);
			        if (args.length > 1) {
			        	for (int i = 1; i < args.length; i++) {
			        		String s = args[i].trim();
			        		if (s.startsWith("gcutil")) {//查看gc信息
			        			commandStrSet(entity, "gcutil", args[i]);
			        		} else if (s.startsWith("class")) {
			        			commandStrSet(entity, "class", args[i]);
			        		} else if (s.startsWith("gcheap")) {
			        			commandStrSet(entity, "gcheap", args[i]);
			        		} else if (s.startsWith("help")) {//打印帮助信息
			        			entity.setCommand("help");
			        		} else if (s.startsWith("thread")) {
			        			commandStrSet(entity, "thread", args[i]);
			        		} else if (s.startsWith("memory")) {//内存信息
			        			commandStrSet(entity, "memory", args[i]);
			        		} else if (s.startsWith("heap")) {//堆信息
			        			commandStrSet(entity, "heap", args[i]);
			        		} else if (s.startsWith("noheap")) {
			        			commandStrSet(entity, "noheap", args[i]);
			        		}
			        	}
			        }
			        return entity;
			}
		}
		return null;
	}
	
	/**
	 * 设置统计的时常和次数
	 * @param entity - Command
	 * @param sCom - 统计命令
	 * @param sMsg - 信息
	 */
	private void commandStrSet(Command entity, String sCom, String sMsg) {
		entity.setCommand(sCom);
		String[] tcAry = sMsg.trim().split("\\s+");
		if (tcAry.length > 1) {
			try {
				entity.setSecond(Integer.parseInt(tcAry[1].trim()));
			} catch (Exception e) {
				logger.error("jvm input second is error");
			}
			
			if (tcAry.length > 2) {
				try {
					entity.setGroup(Integer.parseInt(tcAry[2].trim()));
				} catch (Exception e) {
					logger.error("jvm input times is error");
				}
			}
		}
	}

	@Override
	public void execCommand(final Command command, final MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.JVM) {
			Clear.setStop(true);
			
			final String cmd = command.getCommand();
			Thread thread = new Thread(){
				public void run() {
					try {
						switch(cmd) {
							case "gcutil" :
								JVM.this.jvmGcutil(command, event);
								break;
							case "class" :
								JVM.this.jvmClass(command, event);
								break;
							case "gcheap" :
								JVM.this.jvmGc(command, event);
								break;
							case "help" : 
								JVM.this.jvmHelp(event);
								break;
							case "thread" : 
								JVM.this.jvmThread(command, event);
								break;
							case "memory" : 
								JVM.this.jvmMemory(command, event);
								break;
							case "heap" :
								JVM.this.jvmHeapMemory(command, event);
								break;
							case "noheap" :
								JVM.this.jvmNoHeapMemory(command, event);
						}
					} catch (Exception ex) {
						JVM.logger.error("jvm command error", ex);
					}
				}
			};
			//开启线程统计
			thread.start();
		}
	}
	
	/**
	 * 统计jvm GCUtil信息
	 * @param command
	 * @param event
	 * @throws IOException
	 */
	private void jvmGcutil(Command command, MessageEvent event) throws IOException {
		boolean loop = true;
		int times = command.getGroup();//统计次数
		
		StringBuilder sb = new StringBuilder();
		sb.append("S\t");
		sb.append("E\t");
		sb.append("O\t");
		sb.append("P\t");
		sb.append("C\t");
		sb.append("YGC\t");
		sb.append("YGCT\t");
		sb.append("FGC\t");
		sb.append("FGCT\t");
		sb.append("GCT\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		DecimalFormat df = new DecimalFormat("0.00");
		while(loop && event.getChannel().isConnected() && Clear.isStop()) {
			MonitorMemory monitorMemory = JVMMonitor.getMemoryUsed();
			MonitorGC monitorGC = JVMMonitor.getGcTime();
			
			StringBuilder strb = new StringBuilder();
			
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
			strb.append(df.format(monitorGC.getyGcTime()));
			strb.append("\t");
			strb.append(monitorGC.getfGcCount());
			strb.append("\t");
			strb.append(df.format(monitorGC.getfGcTime()));
			strb.append("\t");
			strb.append(df.format(monitorGC.getGcTime()));
			strb.append("\r\n");
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if (command.getSecond() != 0) {
				times--;
				if (times <= 0) loop = false;
			} else {
				loop = false;
			}
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 统计JVM的类加载信息
	 * @param command - Command
	 * @param event - MessageEvent
	 * @throws IOException
	 */
	private void jvmClass(Command command, MessageEvent event) throws IOException {
		boolean loop = true;
		int times = command.getGroup();
		StringBuffer sb = new StringBuffer();
		sb.append("Loaded\t");
		sb.append("Unloaded\t");
		sb.append("TotalLoaded\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		while (loop && event.getChannel().isConnected() && Clear.isStop()) {
			StringBuilder strb = new StringBuilder();
			strb.append(JVMMonitor.getLoadedClassCount());
			strb.append("\t");
			strb.append(JVMMonitor.getUnloadedClassCount());
			strb.append("\t\t");
			strb.append(JVMMonitor.getTotalLoadedClassCount());
			strb.append("\r\n");
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if (command.getSecond() != 0) {
				times--;
				if (times <= 0) loop = false;
			} else {
				loop = false;
			}
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * jvm垃圾回收信息
	 * @param command - Command
	 * @param event - MessageEvent
	 * @throws IOException
	 */
	private void jvmGc(Command command, MessageEvent event) throws IOException {
		boolean loop = true;
		int times = command.getGroup();
		
		StringBuilder sb = new StringBuilder();
		sb.append("SC\t");
		sb.append("SU\t");
		sb.append("PC\t");
		sb.append("PU\t");
		sb.append("EC\t");
		sb.append("EU\t");
		sb.append("OC\t");
		sb.append("OU\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		DecimalFormat df = new DecimalFormat("0.0");
		while (loop && event.getChannel().isConnected() && Clear.isStop()) {
			MonitorMemory monitorMemory = JVMMonitor.getMemoryUsed();
			StringBuilder strb = new StringBuilder();
			strb.append(monitorMemory.getSurvivor().getCommitted() / 1024.0D);
			strb.append("\t");
			strb.append(df.format(monitorMemory.getSurvivor().getUsed() / 1024.0D));
			strb.append("\t");
			strb.append(monitorMemory.getPerm().getCommitted() / 1024.0D);
			strb.append("\t");
			strb.append(df.format(monitorMemory.getPerm().getUsed() / 1024.0D));
			strb.append("\t");
			strb.append(monitorMemory.getEden().getCommitted() / 1024.0D);
			strb.append("\t");
			strb.append(df.format(monitorMemory.getEden().getUsed() / 1024.0D));
			strb.append("\t");
			strb.append(monitorMemory.getOld().getCommitted() / 1024.0D);
			strb.append("\t");
			strb.append(df.format(monitorMemory.getOld().getUsed() / 1024.0D));
			strb.append("\r\n");
			
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if (command.getSecond() != 0) {
				times--;
				if (times <= 0) loop = false;
			} else {
				loop = false;
			}
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * jvm帮助信息
	 * @param event - MessageEvent
	 * @throws IOException
	 */
	private void jvmHelp(MessageEvent event) throws IOException {
		StringBuilder sbMsg = new StringBuilder();
		sbMsg.append("*******************************************************************\r\n\n");
		sbMsg.append("jvm [option] [time] [count]\r\n");
		sbMsg.append("\t*[option]:\r\n");
		sbMsg.append("\t\t* -gcutil: detection heap memory usage\r\n");
		sbMsg.append("\t\t* -class : load class\r\n");
		sbMsg.append("\t\t* -gcheap: heap memory used and committed \r\n");
		sbMsg.append("\t\t* -memory:JVM memory used \r\n");
		sbMsg.append("\t\t* -heap  :Virtual Machine heap memory used \r\n");
		sbMsg.append("\t\t* -noheap:Virtual Machine noheap memory used \r\n");
		sbMsg.append("\t\t* -thread: thread counts \r\n");
		sbMsg.append("\t\t* -help  : help\r\n");
		sbMsg.append("\t* time\t : [time] milliseconds apart test again\r\n");
		sbMsg.append("\t* count\t : detection [count] times\r\n");
		sbMsg.append("\t* example: jvm -gcutil\r\n");
		sbMsg.append("\t* example: jvm -gcutil 1000\r\n");
		sbMsg.append("\t* example: jvm -gcutil 1000 5\r\n");
		sbMsg.append("\t* example: jvm -class\r\n");
		sbMsg.append("\t* example: jvm -class 1000\r\n");
		sbMsg.append("\t* example: jvm -class 1000 5\r\n");
		sbMsg.append("\t* example: jvm -gcheap\r\n");
		sbMsg.append("\t* example: jvm -gcheap 1000\r\n");
		sbMsg.append("\t* example: jvm -gcheap 1000 5\r\n");
		sbMsg.append("\t* example: jvm -memory\r\n");
		sbMsg.append("\t* example: jvm -memory 1000\r\n");
		sbMsg.append("\t* example: jvm -memory 1000 5\r\n");
		sbMsg.append("\t* example: jvm -heap\r\n");
		sbMsg.append("\t* example: jvm -heap 1000\r\n");
		sbMsg.append("\t* example: jvm -heap 1000 5\r\n");
		sbMsg.append("\t* example: jvm -noheap\r\n");
		sbMsg.append("\t* example: jvm -noheap 1000\r\n");
		sbMsg.append("\t* example: jvm -noheap 1000 5\r\n");
		sbMsg.append("\t* example: jvm -thread\r\n");
		sbMsg.append("\t* example: jvm -thread 1000\r\n");
		sbMsg.append("\t* example: jvm -thread 1000 5\r\n\n");
		sbMsg.append("*******************************************************************\r\n\n");
		byte[] responseByte = sbMsg.toString().getBytes("utf-8");
		event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
	}
	
	/**
	 * jvm的线程信息
	 * @param command - Command
	 * @param event - MessageEvent
	 * @throws IOException
	 */
	private void jvmThread(Command command, MessageEvent event) throws IOException {
		boolean loop = true;
		int times = command.getGroup();
		
		StringBuilder sb = new StringBuilder();
		sb.append("ATC\t");
		sb.append("PTC\t");
		sb.append("DTC\t");
		sb.append("TSTC\t");
		sb.append("DLC\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		while (loop && event.getChannel().isConnected() && Clear.isStop()) {
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
			strb.append("\r\n");

			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if (command.getSecond() != 0) {
				times--;
				if (times <= 0) loop = false;
			} else {
				loop = false;
			}
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 返回虚拟机的内存使用信息
	 * @param command - Command
	 * @param event - MessageEvent
	 * @throws IOException
	 */
	private void jvmMemory(Command command, MessageEvent event) throws IOException {
		boolean loop = true;
		int times = command.getGroup();
		
		StringBuilder sb = new StringBuilder();
		sb.append("TM\t\t");
		sb.append("UM\t\t");
		sb.append("MUM\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		while (loop && event.getChannel().isConnected() && Clear.isStop()) {
			StringBuilder strb = new StringBuilder();
			strb.append(JVMMonitor.getTotolMemory());
			strb.append("\t\t");
			strb.append(JVMMonitor.getUsedMemory());
			strb.append("\t\t");
			strb.append(JVMMonitor.getMaxUsedMemory());
			strb.append("\r\n");
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if (command.getSecond() != 0) {
				times--;
				if (times <= 0) loop = false;
			} else {
				loop = false;
			}
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * jvm的堆内存信息
	 * @param command - Command
	 * @param event - MessageEvent
	 * @throws IOException
	 */
	private void jvmHeapMemory(Command command, MessageEvent event) throws IOException {
		boolean loop = true;
		int times = command.getGroup();
		StringBuilder sb = new StringBuilder();
		sb.append("I\t\t");
		sb.append("C\t\t");
		sb.append("M\t\t");
		sb.append("U\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		while (loop && event.getChannel().isConnected() && Clear.isStop()) {
			MemoryUsage memoryUsage = JVMMonitor.getJvmHeapMemory();
			StringBuilder strb = new StringBuilder();
			strb.append(memoryUsage.getInit());
			strb.append("\t\t");
			strb.append(memoryUsage.getCommitted());
			strb.append("\t\t");
			strb.append(memoryUsage.getMax());
			strb.append("\t");
			strb.append(memoryUsage.getUsed());
			strb.append("\r\n");
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if (command.getSecond() != 0) {
				times--;
				if (times <= 0) loop = false;
			} else {
				loop = false;
			}
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * jvm非堆内存信息
	 * @param command - Command
	 * @param event - MessageEvent
	 * @throws IOException
	 */
	private void jvmNoHeapMemory(Command command, MessageEvent event) throws IOException {
		boolean loop = true;
		int times = command.getGroup();
		StringBuilder sb = new StringBuilder();
		sb.append("I\t\t");
		sb.append("C\t\t");
		sb.append("M\t\t");
		sb.append("U\r\n");
		event.getChannel().write(ChannelBuffers.copiedBuffer(sb.toString().getBytes("utf-8")));
		
		while (loop && event.getChannel().isConnected() && Clear.isStop()) {
			MemoryUsage memoryUsage = JVMMonitor.getJvmNoHeapMemory();
			StringBuilder strb = new StringBuilder();
			strb.append(memoryUsage.getInit());
			strb.append("\t");
			strb.append(memoryUsage.getCommitted());
			strb.append("\t");
			strb.append(memoryUsage.getMax());
			strb.append("\t");
			strb.append(memoryUsage.getUsed());
			strb.append("\r\n");
			byte[] responseByte = strb.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			if (command.getSecond() != 0) {
				times--;
				if (times <= 0) loop = false;
			} else {
				loop = false;
			}
			try {
				Thread.sleep(command.getSecond());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void messageReceived(SCFContext context) {
		
	}

	@Override
	public void removeChannel(Command commandStr, Channel channel) {
		
	}

	@Override
	public int getChannelCount() {
		return 0;
	}

}
