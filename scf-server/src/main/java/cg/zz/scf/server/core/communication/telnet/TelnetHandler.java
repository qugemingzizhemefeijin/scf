package cg.zz.scf.server.core.communication.telnet;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.performance.MonitorCenter;

/**
 * telnet处理器
 * ChannelPipelineCoverage 此注解用于说明处理器的类型，告诉这种类型的处理器是否能够被多余1 //个channel共享
 * 其可选的值是”all”和”one”
 * @author chengang
 *
 */
@SuppressWarnings("deprecation")
@ChannelPipelineCoverage(ChannelPipelineCoverage.ALL)
public class TelnetHandler extends SimpleChannelUpstreamHandler {
	
	private static ILog logger = LogFactory.getLogger(TelnetHandler.class);
	
	/**
	 * 当一个从远端发来的消息对象(如: ChannelBuffer)被接收时调用.
	 */
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		try {
			logger.debug("control message receive");
			MonitorCenter.messageReceived(e);
		} catch (Exception ex) {
			logger.error("control msg error", ex);
		}
	}
	
	/**
	 *  处理一个指定的上游事件.
	 */
	@Override
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if ((e instanceof ChannelStateEvent)) {
			logger.info("control event--" + e.toString());
		}
		super.handleUpstream(ctx, e);
	}
	
	/**
	 * 当一个Channel打开,但还没有绑定和连接时被调用.
	 */
	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		TelnetServer.allChannels.add(e.getChannel());
		logger.info("new control channel open:" + e.getChannel().getRemoteAddress().toString());
	}
	
	/**
	 *  当一个Channel打开并且绑定到本地地址和已连接到远程地址时被调用.
	 */
	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		logger.info("new control channel conected:" + e.getChannel().getRemoteAddress().toString());
	}
	
	/**
	 * 当一个I/O线程或ChannelHandler抛出异常时被调用.
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("control channel exception(" + e.getChannel().getRemoteAddress().toString() + ")",e.getCause());
		MonitorCenter.removeChannel(e.getChannel());
		e.getChannel().close();
	}
	
	/**
	 * 当一个Channel被关闭且它所有关联的资源被释放时调用.
	 */
	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.info("channel is closed:" + e.getChannel().getRemoteAddress().toString());
		MonitorCenter.removeChannel(e.getChannel());
		e.getChannel().close();
	}

}
