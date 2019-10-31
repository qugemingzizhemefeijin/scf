package cg.zz.scf.server.core.communication.tcp;

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

/**
 * socket服务
 * @author chengang
 *
 */
public class SocketServer implements IServer {
	
	private static ILog logger = LogFactory.getLogger(SocketServer.class);
	
	/**
	 * netty ServerBootstrap
	 */
	private static final ServerBootstrap bootstrap = new ServerBootstrap();
	
	/**
	 * record all channel
	 */
	static final ChannelGroup allChannels = new DefaultChannelGroup("SCF-SockerServer");
	
	/**
	 * invoker handle
	 */
	static IInvokerHandle invokerHandle = null;

	/**
	 * start netty server
	 */
	@Override
	public void start() throws Exception {
		logger.info("loading invoker...");
		String invoker = Global.getInstance().getServiceConfig().getString("scf.proxy.invoker.implement");
		invokerHandle = (IInvokerHandle) Class.forName(invoker).newInstance();
		logger.info("initing server...");
		initSocketServer();
	}
	
	/**
	 * 初始化socket server
	 * @throws Exception 
	 */
	private void initSocketServer() throws Exception {
		boolean tcpNoDelay = true;
		logger.info("-- socket server config --");
		logger.info("-- listen ip: " + Global.getInstance().getServiceConfig().getString("scf.server.tcp.listenIP"));
		logger.info("-- port: " + Global.getInstance().getServiceConfig().getInt("scf.server.tcp.listenPort"));
		logger.info("-- tcpNoDelay: " + tcpNoDelay);
		logger.info("-- receiveBufferSize: " + Global.getInstance().getServiceConfig().getInt("scf.server.tcp.receiveBufferSize"));
		logger.info("-- sendBufferSize: " + Global.getInstance().getServiceConfig().getInt("scf.server.tcp.sendBufferSize"));
		logger.info("-- frameMaxLength: " + Global.getInstance().getServiceConfig().getInt("scf.server.tcp.frameMaxLength"));
		logger.info("-- worker thread count: " + Global.getInstance().getServiceConfig().getInt("scf.server.tcp.workerCount"));
		logger.info("--------------------------");
		
		logger.info(Global.getInstance().getServiceConfig().getString("scf.service.name") + " SocketServer starting...");
		
		bootstrap.setFactory(
			new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), 
			Executors.newCachedThreadPool(), 
			Global.getInstance().getServiceConfig().getInt("scf.server.tcp.workerCount")));
		
		SocketHandler handler = new SocketHandler();
		
		bootstrap.setPipelineFactory(new SocketPipelineFactory(handler,Global.getInstance().getServiceConfig().getInt("scf.server.tcp.frameMaxLength")));
		bootstrap.setOption("child.tcpNoDelay", tcpNoDelay);
		bootstrap.setOption("child.receiveBufferSize", Integer.valueOf(Global.getInstance().getServiceConfig().getInt("scf.server.tcp.receiveBufferSize")));
		bootstrap.setOption("child.sendBufferSize", Integer.valueOf(Global.getInstance().getServiceConfig().getInt("scf.server.tcp.sendBufferSize")));
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(Global.getInstance().getServiceConfig().getString("scf.server.tcp.listenIP"),Global.getInstance().getServiceConfig().getInt("scf.server.tcp.listenPort"));
			Channel channel = bootstrap.bind(socketAddress);
			allChannels.add(channel);
		} catch (Exception e) {
			logger.error("init socket server error", e);
			System.exit(1);
		}
	}

	@Override
	public void stop() throws Exception {
		logger.info("----------------------------------------------------");
		logger.info("-- socket server closing...");
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

}
