package cg.zz.scf.server.performance;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.filter.MonitorRequestFilter;
import cg.zz.scf.server.filter.MonitorResponseFilter;

/**
 * 监控中心
 * @author chengang
 *
 */
public class MonitorCenter {
	
	private static ILog logger = LogFactory.getLogger(MonitorCenter.class);
	
	/**
	 * 线程池
	 */
	private static ExecutorService executor = Executors.newCachedThreadPool();
	
	/**
	 * request
	 */
	private static IFilter monitorRequestFilter = new MonitorRequestFilter();
	
	/**
	 * response
	 */
	private static IFilter monitorResponseFilter = new MonitorResponseFilter();

	/**
	 * 命令行执行工具
	 */
	private static Command command = null;
	
	/**
	 * 接收消息
	 * @param e - MessageEvent
	 * @throws Exception
	 */
	public static void messageReceived(MessageEvent e) throws Exception {
		ByteBuffer buffer = ((ChannelBuffer) e.getMessage()).toByteBuffer();
		byte[] reciveByte = buffer.array();
		String msg = new String(reciveByte, "utf-8");
		if (!msg.equals("|") && !msg.equals("-")) {
			command = Command.create(msg);
		}
		logger.info("command:" + msg + "--commandType:" + command.getCommandType());
		command.exec(e);

		removeChannel(e.getChannel());
	}
	
	/**
	 * 添加监控过滤器
	 */
	public static synchronized void addFilter() {
		if (!Global.getInstance().getGlobalRequestFilterList().contains(monitorRequestFilter)) {
			logger.info("add monitorRequestFilter");
			Global.getInstance().addGlobalRequestFilter(monitorRequestFilter);
		}
		if (!Global.getInstance().getGlobalResponseFilterList().contains(monitorResponseFilter)) {
			logger.info("add monitorResponseFilter");
			Global.getInstance().addGlobalResponseFilter(monitorResponseFilter);
		}
	}
	
	/**
	 * 移除监控过滤器
	 */
	public static synchronized void removeFilter() {
		Global.getInstance().removeGlobalRequestFilter(monitorRequestFilter);
		Global.getInstance().removeGlobalResponseFilter(monitorResponseFilter);
		logger.info("remove monitorRequestFilter");
		logger.info("remove monitorResponseFilter");
	}
	
	/**
	 * 添加一个监控任务
	 * @param context - SCFContext
	 */
	public static void addMonitorTask(final SCFContext context) {
		executor.execute(new Runnable() {
			public void run() {
				if (MonitorCenter.command != null) MonitorCenter.command.messageReceived(context);
			}
		});
	}
	
	/**
	 * 从监控命令中移除一个通道
	 * @param channel - Channel
	 */
	public static void removeChannel(Channel channel) {
		if (command != null) {
			command.removeChannel(channel);

			if (command.getChannelCount() <= 0)
				removeFilter();
		}
	}
	
}
