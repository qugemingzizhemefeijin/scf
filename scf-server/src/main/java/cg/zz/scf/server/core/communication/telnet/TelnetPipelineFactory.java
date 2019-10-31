package cg.zz.scf.server.core.communication.telnet;

import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;

/**
 * 为一个新的Channel创建一个新的ChannelPipeline. 
 * @author chengang
 *
 */
public class TelnetPipelineFactory implements ChannelPipelineFactory {
	
	/**
	 * 处理或拦截一个ChannelEvent并发送一个 ChannelEvent到ChannelPipeline 里的下一个处理器 . 
	 */
	private final ChannelHandler handler;
	
	/**
	 * 解码的帧长度，超过这个长度将抛出异常 TooLongFrameException
	 */
	private int frameMaxLength;
	
	public TelnetPipelineFactory(ChannelHandler handler, int frameMaxLength) {
		this.handler = handler;
		this.frameMaxLength = frameMaxLength;
	}

	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline pipeline = Channels.pipeline();
		pipeline.addLast("framer",	new DelimiterBasedFrameDecoder(this.frameMaxLength, Delimiters.lineDelimiter()));
		pipeline.addLast("handler", this.handler);
		return pipeline;
	}

}
