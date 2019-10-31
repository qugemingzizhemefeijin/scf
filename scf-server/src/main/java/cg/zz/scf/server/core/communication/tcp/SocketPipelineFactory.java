package cg.zz.scf.server.core.communication.tcp;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;

import cg.zz.scf.protocol.utility.ProtocolConst;

/**
 * netty Pipeline Factory
 * @author chengang
 *
 */
public class SocketPipelineFactory implements ChannelPipelineFactory {
	
	private final ChannelHandler handler;
	private int frameMaxLength;
	
	public SocketPipelineFactory(ChannelHandler handler, int frameMaxLength) {
		this.handler = handler;
		this.frameMaxLength = frameMaxLength;
	}
	
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		
		ChannelBuffer buf = ChannelBuffers.directBuffer(ProtocolConst.P_END_TAG.length);
		buf.writeBytes(ProtocolConst.P_END_TAG);
		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(this.frameMaxLength, true, buf));
		
		pipeline.addLast("handler", this.handler);
		
		return pipeline;
	}

}
