package cg.zz.scf.server.performance;

import java.net.SocketAddress;

import org.jboss.netty.channel.Channel;

/**
 * 监控通道
 * @author chengang
 *
 */
public class MonitorChannel {
	
	/**
	 * 通道
	 */
	private Channel channel;
	
	/**
	 * socket地址
	 */
	private SocketAddress socketAddress;
	
	/**
	 * 命令
	 */
	private Command command;
	
	/**
	 * 统计消息次数
	 */
	private int convergeCount;
	
	/**
	 * 统计时间合计
	 */
	private long convergeTime;
	
	/**
	 * 开始时间
	 */
	private long beginTime;
	
	public MonitorChannel() {
		
	}
	
	public MonitorChannel(Command command, Channel channel, SocketAddress socketAddress) {
		this.command = command;
		this.channel = channel;
		this.socketAddress = socketAddress;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public SocketAddress getSocketAddress() {
		return socketAddress;
	}

	public void setSocketAddress(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}

	public Command getCommand() {
		return command;
	}

	public void setCommand(Command command) {
		this.command = command;
	}

	public int getConvergeCount() {
		return convergeCount;
	}

	public void setConvergeCount(int convergeCount) {
		this.convergeCount = convergeCount;
	}

	public long getConvergeTime() {
		return convergeTime;
	}

	public void setConvergeTime(long convergeTime) {
		this.convergeTime = convergeTime;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

}
