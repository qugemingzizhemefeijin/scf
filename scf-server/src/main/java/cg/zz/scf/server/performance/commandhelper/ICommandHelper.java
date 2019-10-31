package cg.zz.scf.server.performance.commandhelper;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.Command;

public interface ICommandHelper {
	
	/**
	 * 创建一个Command
	 * @param commandStr - 接收到的命令行
	 * @return Command
	 */
	public abstract Command createCommand(String commandStr);

	/**
	 * 执行命令行
	 * @param command - Command
	 * @param event - MessageEvent 表示一个接收消息的通知或写消息的请求
	 * @throws Exception
	 */
	public abstract void execCommand(Command command, MessageEvent event) throws Exception;

	/**
	 * 接受到一个SCF请求
	 * @param context - SCFContext
	 */
	public abstract void messageReceived(SCFContext context);

	/**
	 * 移除一个通道
	 * @param commandStr - Command
	 * @param channel - Channel
	 */
	public abstract void removeChannel(Command commandStr, Channel channel);

	/**
	 * 获得通道的数量
	 * @return int
	 */
	public abstract int getChannelCount();

}
