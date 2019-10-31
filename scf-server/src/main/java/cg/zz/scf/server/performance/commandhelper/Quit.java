package cg.zz.scf.server.performance.commandhelper;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;

/**
 * 退出quit
 * @author chengang
 *
 */
public class Quit extends CommandHelperBase {

	@Override
	public Command createCommand(String commandStr) {
		if("quit".equalsIgnoreCase(commandStr)) {
			Command entity = new Command();
			entity.setCommandType(CommandType.Quit);
			return entity;
		}
		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.Quit) {
			logger.info("quit monitor");
			event.getChannel().close();
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
