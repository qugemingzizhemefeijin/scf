package cg.zz.scf.server.core.communication.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;

import cg.zz.scf.server.contract.annotation.HttpRequestMethod;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.contract.server.IServerHandler;
import cg.zz.scf.server.util.ExceptionHelper;
import cg.zz.scf.server.util.JsonHelper;

/**
 * Netty中有个注释@interface ChannelPipelineCoverage，它表示被注释的ChannelHandler是否能添加到多个ChannelPipeline
 * 中，其可选的值是”all”和”one”。”all”表示ChannelHandler是无状态的，可被多个ChannelPipeline共享，而”one”
 * 表示ChannelHandler只作用于单个ChannelPipeline中。但ChannelPipelineCoverage只是个注释而已，并没有实际的检查作用。
 * 对于ChannelHandler是”all”还是”one”，还是根据逻辑需要而定。比如，像解码请求handler，因为可能解码的数据不完整，
 * 需要等待下一次读事件来了之后再继续解析，所以解码请求handler就需要是”one”的（否则多个Channel共享数据就乱了）。
 * 而像业务逻辑处理hanlder通常是”all”的。
 * @author Administrator
 *
 */
@SuppressWarnings("deprecation")
@ChannelPipelineCoverage("one")
public class HttpHandler extends SimpleChannelUpstreamHandler implements IServerHandler {
	
	private static ILog logger = LogFactory.getLogger(HttpHandler.class);
	
	@SuppressWarnings("unused")
	private static volatile int connectedCount = 0;
	private static final Charset utf8 = Charset.forName("UTF-8");
	private volatile boolean readingChunks;
	private volatile org.jboss.netty.handler.codec.http.HttpRequest request;
	private volatile HttpContext context = new HttpContext();
	private volatile byte[] byteContent = null;
	private volatile int contentLength = 0;
	private volatile int receiveLength = 0;
	
	/**
	 * 接受到消息
	 */
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		//如果不是块读取
		if (!this.readingChunks) {
			this.request = ((org.jboss.netty.handler.codec.http.HttpRequest)e.getMessage());
			String requestUri = this.request.getUri();
			if (requestUri.lastIndexOf("favicon.ico") > 0) {
				logger.info("this url is favicon.ico");
			        return;
			}
			
			this.context.setUri(requestUri);
			
			if (this.request.getMethod() == HttpMethod.GET)
				this.context.setMethod(HttpRequestMethod.GET);
			else if (this.request.getMethod() == HttpMethod.POST)
				this.context.setMethod(HttpRequestMethod.POST);
			else if (this.request.getMethod() == HttpMethod.DELETE)
				this.context.setMethod(HttpRequestMethod.DELETE);
			else if (this.request.getMethod() == HttpMethod.PUT)
				this.context.setMethod(HttpRequestMethod.PUT);
			else if (this.request.getMethod() == HttpMethod.HEAD) {
				this.context.setMethod(HttpRequestMethod.HEAD);
			}
			
			Map<String , List<String>> headers = new HashMap<>();
			if (!this.request.getHeaderNames().isEmpty()) {
				for (String name : this.request.getHeaderNames()) {
					List<String> vList = new ArrayList<>();
					for (String value : this.request.getHeaders(name)) {
						vList.add(value);
					}
					headers.put(name, vList);
				}
			}
			this.context.setHeaders(headers);
			
			QueryStringDecoder queryStringDecoder = new QueryStringDecoder(this.request.getUri(), utf8);
			Map<String , List<String>> params = queryStringDecoder.getParameters();
			this.context.setParams(params);
			
			this.context.setFromIP(e.getChannel().getRemoteAddress().toString());
			this.context.setToIP(e.getChannel().getLocalAddress().toString());
			
			if (this.request.isChunked()) {
				this.readingChunks = true;
			} else {
				this.contentLength = (int)this.request.getContentLength();
				if (this.contentLength > 0) {
					this.byteContent = new byte[this.contentLength];
					ChannelBuffer cb = this.request.getContent();
					if (cb != null) {
						cb.getBytes(0, this.byteContent);
					}
				}
				
				this.context.setContentBuffer(this.byteContent);
				invoke(this.context, e);
			}
		} else {
			HttpChunk chunk = (HttpChunk)e.getMessage();
			ChannelBuffer cb = chunk.getContent();
			if (cb != null && cb.capacity() > 0) {
				cb.getBytes(0, this.byteContent, this.receiveLength, cb.capacity());
				this.receiveLength += cb.capacity();
			}
			
			if (chunk.isLast()) {
				this.readingChunks = false;
				
				this.context.setContentBuffer(this.byteContent);
				invoke(this.context, e);
			}
		}
	}
	
	private void invoke(HttpContext httpContext, MessageEvent e) {
		try {
			
		} catch (Throwable ex) {
			logger.error("http request error!!!", ex);
			
			HttpResponseStatus status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
			if ((ex instanceof HttpException) && ((HttpException)ex).getErrorCode() == 404) {
				status = HttpResponseStatus.NOT_FOUND;
			}
			
			if (HttpServer.errorPageHTML != null) {
				writeResponse(ChannelBuffers.copiedBuffer(HttpServer.errorPageHTML, utf8), e.getChannel(),status);
			} else {
				String errorMsg = ExceptionHelper.getStackTrace(ex);
				writeResponse(ChannelBuffers.copiedBuffer(errorMsg, utf8), e.getChannel(),status);
			}
		}
	}
	
	private void writeResponse(ChannelBuffer buffer, Channel channel, HttpResponseStatus status) {
		//判断连接是否关闭了
		boolean close = 
			"close".equalsIgnoreCase(this.request.getHeader("Connection")) || (
			this.request.getProtocolVersion().equals(HttpVersion.HTTP_1_0) && 
			!"keep-alive".equalsIgnoreCase(this.request.getHeader("Connection")));
		
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);
		
		response.setContent(buffer);
		if (status != HttpResponseStatus.OK) {
			response.setHeader("Content-Type", "text/html; charset=utf-8");
		} else {
			response.setHeader("Content-Type", "text/plain; charset=utf-8");
		}
		
		response.setHeader("Content-Length", String.valueOf(buffer.readableBytes()));
		response.setHeader("Connection", "close");
		response.setHeader("Server", "SCF");
		
		ChannelFuture future = channel.write(response);
		if (close) future.addListener(ChannelFutureListener.CLOSE);
	}
	
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		connectedCount += 1;
	}
	
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.error("http channel exception(" + e.getChannel().getRemoteAddress().toString() +")", e.getCause());
		e.getChannel().close();
	}
	
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) {
		connectedCount -= 1;
		e.getChannel().close();
	}

	@Override
	public void writeResponse(SCFContext context) {
		try {
			String jsonStr = JsonHelper.toJsonString(context.getScfResponse().getReturnValue());
			context.getScfResponse().setResponseBuffer(jsonStr.getBytes(Global.getInstance().getServiceConfig().getString("scf.encoding")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		HttpResponseStatus status = HttpResponseStatus.OK;
		ChannelBuffer buffer = null;
		if (context.getError() != null) {
			status = HttpResponseStatus.INTERNAL_SERVER_ERROR;
		}
		if (HttpServer.errorPageHTML != null && context.getError() != null) {
			buffer = ChannelBuffers.copiedBuffer(HttpServer.errorPageHTML, "utf-8");
		} else {
			buffer = ChannelBuffers.copiedBuffer(context.getScfResponse().getResponseBuffer());
		}
		
		writeResponse(buffer, context.getChannel().getNettyChannel(), status);
	}

}
