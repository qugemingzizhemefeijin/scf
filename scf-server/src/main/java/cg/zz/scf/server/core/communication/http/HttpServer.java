package cg.zz.scf.server.core.communication.http;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.ChannelGroupFuture;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.contract.server.IServer;
import cg.zz.scf.server.core.proxy.IInvokerHandle;
import cg.zz.scf.server.util.FileHelper;

/**
 * http接口服务
 * @author chengang
 *
 */
public class HttpServer implements IServer {
	
	private static ILog logger = LogFactory.getLogger(HttpServer.class);
	
	private static final ServerBootstrap bootstrap = new ServerBootstrap();
	
	static final ChannelGroup allChannels = new DefaultChannelGroup("SCF-HttpServer");
	
	static IInvokerHandle invokerHandle = null;
	
	static String errorPageHTML = null;

	@Override
	public void start() throws Exception {
		logger.info("-----------------starting http server-----------------");
		
		logger.info("loading invoker...");
		String invoker = Global.getInstance().getServiceConfig().getString("scf.proxy.invoker.implement");
		invokerHandle = (IInvokerHandle)Class.forName(invoker).newInstance();
		
		logger.info("initing url mapping...");
		RequestMapping.init();
		
		logger.info("initing http server...");
		initHttpServer();
		
		logger.info("loading error html...");
		loadErrorHTML();
	}

	@Override
	public void stop() throws Exception {
		logger.info("----------------------------------------------------");
		logger.info("-- http server closing...");
		logger.info("-- channels count : " + allChannels.size());
		ChannelGroupFuture future = allChannels.close();
		logger.info("-- closing all channels...");
		future.awaitUninterruptibly();
		logger.info("-- closed all channels...");
		
		bootstrap.getFactory().releaseExternalResources();
		
		logger.info("-- released external resources");
		logger.info("-- close success !");
		logger.info("----------------------------------------------------");
	}
	
	/**
	 * 加载错误页面信息
	 */
	private void loadErrorHTML() {
		String errorPage = Global.getInstance().getRootPath() + "service/deploy/" + Global.getInstance().getServiceConfig().getString("scf.service.name") + "/error.html";
		File file = new File(errorPage);
		if (file.exists()) {
			try {
				errorPageHTML = FileHelper.getContentByLines(errorPage);
			} catch (IOException e) {
				logger.error(e);
			}
		}
	}
	
	/**
	 * 初始化http服务器
	 * @throws Exception
	 */
	private void initHttpServer() throws Exception {
		boolean tcpNoDelay = true;
		logger.info("-----------------http server config-----------------");
		logger.info("-- listen ip: " + Global.getInstance().getServiceConfig().getString("scf.server.http.listenIP"));
		logger.info("-- port: " + Global.getInstance().getServiceConfig().getInt("scf.server.http.listenPort"));
		logger.info("-- tcpNoDelay: " + tcpNoDelay);
		logger.info("-- receiveBufferSize: " + Global.getInstance().getServiceConfig().getInt("scf.server.http.receiveBufferSize"));
		logger.info("-- sendBufferSize: " + Global.getInstance().getServiceConfig().getInt("scf.server.http.sendBufferSize"));
		logger.info("-- frameMaxLength: " + Global.getInstance().getServiceConfig().getInt("scf.server.http.frameMaxLength"));
		logger.info("-- worker thread count: " + Global.getInstance().getServiceConfig().getInt("scf.server.http.workerCount"));
		logger.info("-----------------------------------------------------");
		
		logger.info(Global.getInstance().getServiceConfig().getString("scf.service.name") + " HttpServer starting...");
		
		bootstrap.setFactory(
			new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), 
			Executors.newCachedThreadPool(), 
			Global.getInstance().getServiceConfig().getInt("scf.server.http.workerCount")));
		
		HttpHandler handler = new HttpHandler();
		bootstrap.setPipelineFactory(new HttpPipelineFactory(handler));
		bootstrap.setOption("child.tcpNoDelay", tcpNoDelay);
		bootstrap.setOption("child.receiveBufferSize", Integer.valueOf(Global.getInstance().getServiceConfig().getInt("scf.server.http.receiveBufferSize")));
		bootstrap.setOption("child.sendBufferSize", Integer.valueOf(Global.getInstance().getServiceConfig().getInt("scf.server.http.sendBufferSize")));
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(Global.getInstance().getServiceConfig().getString("scf.server.http.listenIP"),Global.getInstance().getServiceConfig().getInt("scf.server.http.listenPort"));
			Channel channel = bootstrap.bind(socketAddress);
			allChannels.add(channel);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
