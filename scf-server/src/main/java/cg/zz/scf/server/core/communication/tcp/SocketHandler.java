package cg.zz.scf.server.core.communication.tcp;

import java.nio.ByteBuffer;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;

import cg.zz.scf.protocol.utility.ProtocolConst;
import cg.zz.scf.protocol.utility.ProtocolHelper;
import cg.zz.scf.server.contract.context.ApproveContext;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.SCFChannel;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.SecureContext;
import cg.zz.scf.server.contract.context.ServerType;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.contract.server.IServerHandler;
import cg.zz.scf.server.util.ExceptionHelper;

/**
 * netty event handler
 * @author chengang
 *
 */
public class SocketHandler extends SimpleChannelUpstreamHandler implements IServerHandler {
	
	private static ILog logger = LogFactory.getLogger(SocketHandler.class);
	
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		try {
			logger.debug("message receive");
			ByteBuffer buffer = ((ChannelBuffer)e.getMessage()).toByteBuffer();
			byte[] reciveByte = buffer.array();
			logger.debug("reciveByte.length:" + reciveByte.length);
			
			byte[] headDelimiter = new byte[ProtocolConst.P_START_TAG.length];
			System.arraycopy(reciveByte, 0, headDelimiter, 0, ProtocolConst.P_START_TAG.length);
			
			//检查头信息是否匹配
			if (ProtocolHelper.checkHeadDelimiter(headDelimiter)) {
				byte[] requestBuffer = new byte[reciveByte.length - ProtocolConst.P_START_TAG.length];
				System.arraycopy(reciveByte, ProtocolConst.P_START_TAG.length, requestBuffer, 0, reciveByte.length - ProtocolConst.P_START_TAG.length);
				
				SCFContext scfContext = new SCFContext(requestBuffer, new SCFChannel(e.getChannel()), ServerType.TCP, this);
				SocketServer.invokerHandle.invoke(scfContext);
			} else {
				byte[] response = ExceptionHelper.createErrorProtocol();
				e.getChannel().write(ChannelBuffers.copiedBuffer(response));
				logger.error("protocol error: protocol head not match");
			}
		} catch (Throwable ex) {
			byte[] response = ExceptionHelper.createErrorProtocol();
			e.getChannel().write(response);
			logger.error("SocketHandler invoke error", ex);
		}
	}
	
	public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
		if (e instanceof ChannelStateEvent) {
			logger.debug(e.toString());
		}
		super.handleUpstream(ctx, e);
	}
	
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		SocketServer.allChannels.add(e.getChannel());
		logger.info("new channel open:" + e.getChannel().getRemoteAddress().toString());
		
		//查看是否启动了权限认证
		if (Global.getInstance().getGlobalSecureIsRights()) {
			Global.getInstance().addChannelMap(e.getChannel(), new SecureContext());
		}
		
		if (Global.getInstance().getApproveIsRights()) {
			Global.getInstance().addChannelApproveMap(e.getChannel(), new ApproveContext());
		}
	}
	
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		for (IFilter filter : Global.getInstance().getConnectionFilterList())
			filter.filter(new SCFContext(new SCFChannel(e.getChannel())));
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("unexpected exception from downstream remoteAddress(" + e.getChannel().getRemoteAddress().toString() + ")",e.getCause());
	}
	
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		logger.info("channel is closed:" + e.getChannel().getRemoteAddress().toString());
		e.getChannel().close();
		
		if (Global.getInstance().getGlobalSecureIsRights()) {
			Global.getInstance().removeChannelMap(e.getChannel());
		}
		
		if (Global.getInstance().getApproveIsRights())
			Global.getInstance().removeChannelApproveMap(e.getChannel());
	}

	@Override
	public void writeResponse(SCFContext context) {
		if (context != null && context.getScfResponse() != null) {
			logger.debug("服务端打印返回信息....");
			context.getChannel().write(context.getScfResponse().getResponseBuffer());
		} else {
			context.getChannel().write(new byte[1]);
			logger.error("context is null or response is null in writeResponse");
		}
	}

}
