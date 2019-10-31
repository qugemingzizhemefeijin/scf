package cg.zz.scf.server.performance.commandhelper;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;

/**
 * 回车换行命令
 * @author chengang
 *
 */
public class CRLF extends CommandHelperBase {

	@Override
	public void messageReceived(SCFContext context) {
		
	}

	@Override
	public int getChannelCount() {
		return 0;
	}

	@Override
	public Command createCommand(String commandStr) {
		if (commandStr == null || commandStr.equalsIgnoreCase("")) {
			Command entity = new Command();
			entity.setCommandType(CommandType.CRLF);
			return entity;
		}
		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.CRLF)
			logger.debug("cr & lf");
	}

	@Override
	public void removeChannel(Command commandStr, Channel channel) {
		
	}

}
