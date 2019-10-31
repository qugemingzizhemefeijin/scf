package cg.zz.scf.server.contract.context;

import java.net.InetSocketAddress;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;

import cg.zz.scf.protocol.utility.ProtocolConst;
import cg.zz.scf.server.util.ExceptionHelper;

/**
 * scf通道
 * @author chengang
 *
 */
public class SCFChannel {
	
	/**
	 * netty通道
	 */
	private Channel nettyChannel;
	
	/**
	 * 远程IP
	 */
	private String remoteIP;
	
	/**
	 * 远程端口
	 */
	private int remotePort;
	
	/**
	 * 本地IP
	 */
	private String localIP;
	
	/**
	 * 本地端口
	 */
	private int localPort;
	
	public SCFChannel() {
		
	}
	
	/**
	 * 传入netty通道获取IP和端口信息
	 * @param nettyChannel - Channel
	 */
	public SCFChannel(Channel nettyChannel) {
		this.nettyChannel = nettyChannel;
		
		InetSocketAddress remoteAddress = (InetSocketAddress)nettyChannel.getRemoteAddress();
		this.remoteIP = remoteAddress.getAddress().getHostAddress();
		this.remotePort = remoteAddress.getPort();
		
		InetSocketAddress localAddress = (InetSocketAddress)nettyChannel.getLocalAddress();
		this.localIP = localAddress.getAddress().getHostAddress();
		this.localPort = localAddress.getPort();
	}
	
	/**
	 * 关闭netty通道
	 */
	public void close() {
		if(nettyChannel != null) nettyChannel.close();
	}
	
	public void write(byte[] buffer) {
		if (buffer == null) {
			buffer = ExceptionHelper.createErrorProtocol();
		}
		this.nettyChannel.write(ChannelBuffers.copiedBuffer(new byte[][] { ProtocolConst.P_START_TAG, buffer,ProtocolConst.P_END_TAG }));
	}

	public Channel getNettyChannel() {
		return nettyChannel;
	}

	public void setNettyChannel(Channel nettyChannel) {
		this.nettyChannel = nettyChannel;
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public String getLocalIP() {
		return localIP;
	}

	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

}
