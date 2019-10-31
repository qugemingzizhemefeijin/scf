package cg.zz.scf.server.performance;

import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.commandhelper.CRLF;
import cg.zz.scf.server.performance.commandhelper.Clear;
import cg.zz.scf.server.performance.commandhelper.Control;
import cg.zz.scf.server.performance.commandhelper.Count;
import cg.zz.scf.server.performance.commandhelper.Exec;
import cg.zz.scf.server.performance.commandhelper.Help;
import cg.zz.scf.server.performance.commandhelper.ICommandHelper;
import cg.zz.scf.server.performance.commandhelper.Illegal;
import cg.zz.scf.server.performance.commandhelper.JVM;
import cg.zz.scf.server.performance.commandhelper.Quit;
import cg.zz.scf.server.performance.commandhelper.Time;

/**
 * 执行telnet命令工具
 * @author chengang
 *
 */
public class Command {
	
	/**
	 * 命令类型
	 */
	private CommandType commandType;
	
	/**
	 * 命令
	 */
	private String command;
	
	/**
	 * 过滤的列表，大部分监控都不需要。除了time
	 */
	private List<String> grep;
	
	/**
	 * 统计次数
	 */
	private int group;
	
	/**
	 * 显示统计信息列，大部分监控都不需要。除了time
	 */
	private List<ShowColumn> columnList;
	
	/**
	 * 统计秒数
	 */
	private int second;
	
	/**
	 * 命令当前的方法
	 */
	private String method;
	
	private static List<ICommandHelper> helperList = new ArrayList<ICommandHelper>();
	
	static {
		helperList.add(new CRLF());
		helperList.add(new Quit());
		helperList.add(new Count());
		helperList.add(new Exec());
		helperList.add(new Time());
		helperList.add(new JVM());
		helperList.add(new Clear());
		helperList.add(new Help());
		helperList.add(new Control());
		helperList.add(new Illegal());
	}
	
	/**
	 * 根据传递过来的命令创建合适的Command
	 * @param command - String
	 * @return Command
	 */
	public static Command create(String command) {
		Command entity = null;
		command = command.trim();
		for (ICommandHelper cc : helperList) {
			entity = cc.createCommand(command);
			if (entity != null) {
				break;
			}
		}
		if (entity == null) {
			entity = new Command();
			entity.setCommandType(CommandType.Illegal);
		}
		return entity;
	}
	
	/**
	 * 执行helperList中支持的命令
	 * @param event - MessageEvent
	 * @throws Exception
	 */
	public void exec(MessageEvent event) throws Exception {
		for (ICommandHelper cc : helperList)
			cc.execCommand(this, event);
	}
	
	/**
	 * 删除helperList中所有的命令包含channel的通道
	 * @param channel - Channel
	 */
	public void removeChannel(Channel channel) {
		for (ICommandHelper cc : helperList)
			cc.removeChannel(this, channel);
	}
	
	/**
	 * 接收SCF的数据
	 * @param context - SCFContext
	 */
	public void messageReceived(SCFContext context) {
		for (ICommandHelper cc : helperList)
			cc.messageReceived(context);
	}
	
	/**
	 * 获得helperList中的命令通道总数
	 * @return int
	 */
	public int getChannelCount() {
		int count = 0;
		for (ICommandHelper cc : helperList) {
			count += cc.getChannelCount();
		}
		return count;
	}

	public CommandType getCommandType() {
		return commandType;
	}

	public void setCommandType(CommandType commandType) {
		this.commandType = commandType;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public List<String> getGrep() {
		return grep;
	}

	public void setGrep(List<String> grep) {
		this.grep = grep;
	}

	public int getGroup() {
		return group;
	}

	public void setGroup(int group) {
		this.group = group;
	}

	public List<ShowColumn> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<ShowColumn> columnList) {
		this.columnList = columnList;
	}

	public int getSecond() {
		return second;
	}

	public void setSecond(int second) {
		this.second = second;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public static List<ICommandHelper> getHelperList() {
		return helperList;
	}

	public static void setHelperList(List<ICommandHelper> helperList) {
		Command.helperList = helperList;
	}

}
