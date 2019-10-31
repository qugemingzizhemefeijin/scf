package cg.zz.scf.server.performance.commandhelper;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;

/**
 * 管理和控制
 * @author chengang
 *
 */
public class Control extends CommandHelperBase {

	@Override
	public Command createCommand(String commandStr) {
		if("control".equalsIgnoreCase(commandStr)) {
			Command entity = new Command();
			entity.setCommandType(CommandType.Control);
			return entity;
		}
		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if(command.getCommandType() == CommandType.Control) {
			String msg = "error: at present not allow\r\n";
			byte[] responseByte = msg.getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
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
