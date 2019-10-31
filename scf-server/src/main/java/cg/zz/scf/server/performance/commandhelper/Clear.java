package cg.zz.scf.server.performance.commandhelper;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;

/**
 * clear命令，清空当前数据
 * @author chengang
 *
 */
public class Clear extends CommandHelperBase {
	
	private volatile static boolean stop = false;
	
	public static void setStop(boolean stop) {
		Clear.stop = stop;
	}

	public static boolean isStop() {
		return stop;
	}

	@Override
	public Command createCommand(String commandStr) {
		if (commandStr != null && !commandStr.equalsIgnoreCase("") && commandStr.equalsIgnoreCase("clear")) {
			Command entity = new Command();
			entity.setCommandType(CommandType.Clear);
			return entity;
		}

		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.Clear) {
			logger.info("clear monitor");
			setStop(false);
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
