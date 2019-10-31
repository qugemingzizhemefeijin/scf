package cg.zz.scf.server.core.communication.telnet;

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

/**
 * telnet服务
 * @author chengang
 *
 */
public class TelnetServer implements IServer {
	
	private static ILog logger = LogFactory.getLogger(TelnetServer.class);
	
	/**
	 * 该启动器只针对面向连接的传输(如TCP/IP)和本地传输. 若是有无连接传输则使用 ServerBootstrap 代替.
	 * 如果你正使用一个不接受传入连接且不通过创建子channel接受消息的无连接传输如UDP/IP，千万不要使用该帮助类. 
	 */
	private static final ServerBootstrap bootstrap = new ServerBootstrap();
	
	/**
	 * 一个包含打开通道和为通道提供各种各样批量操作的线程安全集合.使用ChannelGroup,你可以把通道分类到一个有意义的群组中
	 * (如. 基于每服务或每状态.) 一个已经关闭的通道会被自动的从集合中移除,所以你不需要担心已经添加到该集合的通道的生命周期.
	 * 一个通道可以属于多个通道组. 
	 */
	public static final ChannelGroup allChannels = new DefaultChannelGroup("58ControlServer");

	@Override
	public void start() throws Exception {
		String telnetIP = Global.getInstance().getServiceConfig().getString("scf.server.telnet.listenIP");
		int telnetPort = Global.getInstance().getServiceConfig().getInt("scf.server.telnet.listenPort");
		if (telnetIP == null || telnetIP.equalsIgnoreCase("") || telnetIP.equalsIgnoreCase("0.0.0.0")) {
			telnetIP = Global.getInstance().getServiceConfig().getString("scf.server.tcp.listenIP");
		}
		if (telnetPort == 0) {
			int port = Global.getInstance().getServiceConfig().getInt("scf.server.tcp.listenPort");
			telnetPort = Reverse(Reverse(port) + 1);
		}
		
		logger.info("----------------telnet server config------------------");
		logger.info("-- telnet server listen ip: " + telnetIP);
		logger.info("-- telnet server port: " + telnetPort);
		logger.info("------------------------------------------------------");
		
		//设置一个用来处理I/O操作的ChannelFactory ，这个方法只能被调用一次，
		//如果通道工厂已经通过构造函数设置了，该方法会抛出异常
		//NioServerSocketChannelFactory 用于创建基于NIO的服务端ServerSocketChannel的ServerSocketChannelFactory .
		//它利用引入了NIO非阻塞I/O模式有效率的服务大数量的并发连接. 
		bootstrap.setFactory(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(),Executors.newCachedThreadPool()));
		//设置ChannelPipelineFactory. 调用该方法会废止该启动器当前的"pipeline"属性. 
		//随后调用 getPipeline() 和 getPipelineAsMap() 方法就会抛出 IllegalStateException异常. 
		bootstrap.setPipelineFactory(new TelnetPipelineFactory(new TelnetHandler(), Global.getInstance().getServiceConfig().getInt("scf.server.telnet.frameMaxLength")));
		
		//这里设置tcpNoDelay和keepAlive参数，前面的child前缀必须要加上，用来指明这个参数将被应用到接收到的
		//Channels，而不是设置的ServerSocketChannel.
		bootstrap.setOption("child.tcpNoDelay", true);
		//设置接收缓冲区大小
		bootstrap.setOption("child.receiveBufferSize", Global.getInstance().getServiceConfig().getInt("scf.server.telnet.receiveBufferSize"));
		//设置发送缓冲区大小
		bootstrap.setOption("child.sendBufferSize", Global.getInstance().getServiceConfig().getInt("scf.server.telnet.sendBufferSize"));
		
		try {
			InetSocketAddress socketAddress = new InetSocketAddress(telnetIP, telnetPort);
			Channel channel = bootstrap.bind(socketAddress);
			allChannels.add(channel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		logger.info("----------------------------------------------------");
		logger.info("-- telnet Server closing...");
		logger.info("-- channels count : " + allChannels.size());
		
		//关闭该组所有通道.如果通道已经被连接到一个远程端或已绑定到一个本地地址,它会自动断开和取消绑定.
		//返回当该操作完成所有通道时能收到通知的ChannelGroupFuture
		ChannelGroupFuture future = allChannels.close();
		//等待该future完成.该方法会捕捉InterruptedException并安静丢弃.
		future.awaitUninterruptibly();

		//释放该工厂依赖的外部资源.
		bootstrap.getFactory().releaseExternalResources();

		logger.info("-- close success !");
		logger.info("----------------------------------------------------");
	}
	
	/**
	 * 将数字反转如19090变为9091
	 * @param num - int
	 * @return int
	 */
	public static int Reverse(int num) {
		int re = 0;
		for (; num != 0; num /= 10) {
			re = re * 10 + num % 10;
		}
		return re;
	}

}
