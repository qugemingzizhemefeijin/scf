package cg.zz.scf.server.performance.commandhelper;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;

/**
 * 非法的命令输入
 * @author chengang
 *
 */
public class Illegal extends CommandHelperBase {

	@Override
	public Command createCommand(String commandStr) {
		Command entity = new Command();
		entity.setCommandType(CommandType.Illegal);
		return entity;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.Illegal) {
			String msg = "error: bad command please input again\r\n";
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
