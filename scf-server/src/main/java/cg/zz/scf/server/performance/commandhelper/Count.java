package cg.zz.scf.server.performance.commandhelper;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.PerformanceCounter;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.StopWatch;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;
import cg.zz.scf.server.performance.MonitorCenter;
import cg.zz.scf.server.performance.MonitorChannel;

/**
 * 统计调用次数
 * @author chengang
 *
 */
public class Count extends CommandHelperBase {
	
	private static List<MonitorChannel> taskList = new ArrayList<MonitorChannel>();

	@Override
	public Command createCommand(String commandStr) {
		if(commandStr != null && !commandStr.equals("")) {
			String[] args = commandStr.split("\\|");
			if (args[0].trim().equalsIgnoreCase("count")) {
				Command entity = new Command();
				entity.setCommandType(CommandType.Count);
			        entity.setSecond(1);
			        entity.setMethod("#all#");
			        if (args.length > 1) {
			        	for (int i = 1; i < args.length; i++) {
			        		if (args[i].trim().startsWith("second")) {
			        			entity.setSecond(Integer.parseInt(args[i].trim().replaceFirst("second ", "").trim()));
			        		} else if (args[i].trim().startsWith("method")) {
			        			entity.setMethod(args[i].trim().replaceFirst("method ", "").trim());
			        		}
			        	}
			        }
			        return entity;
			}
		}
		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.Count) {
			//添加监控过滤器
			MonitorCenter.addFilter();
			logger.info("add count monitor channel:" + event.getChannel().getRemoteAddress());
			for (int i = 0; i < taskList.size(); i++) {
				if (taskList.get(i).getChannel().equals(event.getChannel()) || !taskList.get(i).getChannel().isOpen()) {
					taskList.remove(i);
				}
			}
			taskList.add(new MonitorChannel(command, event.getChannel(), event.getChannel().getRemoteAddress()));
		}
	}

	@Override
	public void messageReceived(SCFContext context) {
		if (taskList.size() <= 0) {
			return;
		}
		StopWatch sw = context.getStopWatch();
		if (sw != null) {
			Map<String, PerformanceCounter> mapCounter = sw.getMapCounter();
			StringBuilder sbAllMsg = new StringBuilder("#all#");
			for(Map.Entry<String, PerformanceCounter> me : mapCounter.entrySet()) {
				PerformanceCounter pc = me.getValue();
				sbAllMsg.append("key:");
			        sbAllMsg.append(pc.getKey());
			}
			
			try {
				String allMsg = sbAllMsg.toString();
				for (MonitorChannel mc : taskList) {
					long now = System.currentTimeMillis();
					//如果监控时间到了，则返回数据
					if (now - mc.getBeginTime() > mc.getCommand().getSecond() * 1000) {
						String msg = mc.getCommand().getMethod() + "  " + String.valueOf(mc.getConvergeCount()) + "\r\n";
						byte[] responseByte = msg.getBytes("utf-8");
						mc.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
						mc.setBeginTime(now);
						mc.setConvergeCount(0);
					} else if (allMsg.indexOf(mc.getCommand().getMethod()) >= 0) {//如果是监控的指定的方法，则方法的调用次数累计+1
						mc.setConvergeCount(mc.getConvergeCount() + 1);
					}
				}
			} catch (UnsupportedEncodingException e) {
				logger.error("send monitor data", e);
			}
		}
	}

	@Override
	public void removeChannel(Command command, Channel channel) {
		if (command.getCommandType() != CommandType.Count) {
			for (int i = 0; i < taskList.size(); i++) {
				if (taskList.get(i).getChannel().equals(channel) || !taskList.get(i).getChannel().isOpen()) {
				          taskList.remove(i);
				}
			}
		}
	}

	@Override
	public int getChannelCount() {
		//将关闭的Channel移除
		for (int i = 0; i < taskList.size(); i++) {
			if (!(taskList.get(i)).getChannel().isOpen()) {
				taskList.remove(i);
			}
		}
		return taskList.size();
	}

}
