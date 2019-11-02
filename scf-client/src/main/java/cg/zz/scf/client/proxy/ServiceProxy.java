package cg.zz.scf.client.proxy;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cg.zz.scf.client.SCFConst;
import cg.zz.scf.client.configuration.ServiceConfig;
import cg.zz.scf.client.loadbalance.Dispatcher;
import cg.zz.scf.client.loadbalance.Server;
import cg.zz.scf.client.loadbalance.component.ServerChoose;
import cg.zz.scf.client.loadbalance.component.ServerState;
import cg.zz.scf.client.proxy.builder.InvokeResult;
import cg.zz.scf.client.proxy.builder.Parameter;
import cg.zz.scf.client.proxy.builder.ReceiveHandler;
import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;
import cg.zz.scf.protocol.exception.RebootException;
import cg.zz.scf.protocol.exception.ThrowErrorHelper;
import cg.zz.scf.protocol.exception.TimeoutException;
import cg.zz.scf.protocol.sdp.ExceptionProtocol;
import cg.zz.scf.protocol.sdp.HandclaspProtocol;
import cg.zz.scf.protocol.sdp.RequestProtocol;
import cg.zz.scf.protocol.sdp.ResponseProtocol;
import cg.zz.scf.protocol.sfp.enumeration.CompressType;
import cg.zz.scf.protocol.sfp.enumeration.PlatformType;
import cg.zz.scf.protocol.sfp.enumeration.SDPType;
import cg.zz.scf.protocol.sfp.enumeration.SerializeType;
import cg.zz.scf.protocol.sfp.v1.Protocol;
import cg.zz.scf.protocol.utility.KeyValuePair;

/**
 * 服务请求代理类，通过获取Server对象调用request方法，将消息发出。<br/>
 * 同时在调用getProxy方法时候，会检查serviceName是否被初始化，如果未初始化，则进行初始化并创建Socket连接池等。<br/>
 * 这个类的作用就是维护 服务于实际调用接口的一个映射关系，如我们的服务地址：tcp://mimi/AppLogService，则服务名是 mimi，服务的lookup是AppLogService。<br/>
 * 最终获取到ServerChoose，其中维护了接口对应的服务集合，然后调用Dispatcher.GetServer()方法来获取其中的一个Server对象，通过Server对象获取连接池中的一个连接来最终获取到数据。
 * @author chengang
 *
 */
public class ServiceProxy {
	
	private static final ILog logger = LogFactory.getLogger(ServiceProxy.class);
	
	/**
	 * 其实就是消息ID，每次自增+1
	 */
	private int sessionId = 1;
	
	/**
	 * 连接超时时间
	 */
	private int requestTime = 0;
	
	/**
	 * 请求重试次数
	 */
	private int ioreconnect = 0;
	
	/**
	 * 超时重新发送次数/超时时间
	 */
	private int count = 0;
	
	/**
	 * 服务配置
	 */
	private ServiceConfig config;
	
	/**
	 * 服务的调度器
	 */
	private Dispatcher dispatcher;
	
	/**
	 * 获得一个服务的锁，防止创建多个服务
	 */
	private static final Object locker = new Object();
	
	/**
	 * 对消息ID自增的锁
	 */
	private static final Object lockerSessionID = new Object();
	
	/**
	 * 服务名称与服务代理的映射关系
	 */
	private static final Map<String, ServiceProxy> Proxys = new HashMap<>();
	
	/**
	 * Key是接口+方法+参数等组合体，具体参考setServer方法，value是对应的服务器名称。好像setServer没有地方调用过，也就是一直维护的是个空Map
	 */
	private static ConcurrentHashMap<String, ServerChoose> methodServer = new ConcurrentHashMap<>();
	
	/**
	 * 构造ServiceProxy
	 * @param serviceName - 服务名称
	 * @throws Exception
	 */
	private ServiceProxy(String serviceName) throws Exception {
		//读取配置文件
		this.config = ServiceConfig.GetConfig(serviceName);
		//服务器请求分派器
		this.dispatcher = new Dispatcher(this.config);
		
		//请求重试的超时时间
		this.requestTime = this.config.getSocketPool().getReconnectTime();
		int serverCount = 1;
		//获取可用服务的数量
		if (this.dispatcher.GetAllServer() != null && this.dispatcher.GetAllServer().size() > 0) {
			serverCount = this.dispatcher.GetAllServer().size();
		}
		
		//默认的重试次数是 服务数量-1
		this.ioreconnect = (serverCount - 1);
		this.count = this.requestTime;
		
		//count=0的前提就是没有配置requestTime=0并且serverCount=1，否则至少是那两者的1个值
		//结果下面的invoke代码，如果设置了requestTime的值，可能会造成无限重试。。。。
		if (this.ioreconnect > this.requestTime) this.count = this.ioreconnect;
	}
	
	/**
	 * 销毁所有的服务连接
	 */
	private void destroy() {
		List<Server> serverList = this.dispatcher.GetAllServer();
		if (serverList != null) {
			for (Server server : serverList) {
				try {
					//销毁服务的连接池
					server.getScoketpool().destroy();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 根据服务名称获得一个服务代理，这个其实就是整个程序的入口了。。当调用一个服务的时候，如果没有被初始化，则在这里初始化
	 * @param serviceName - 服务名称
	 * @return {@link ServiceProxy}
	 * @throws Exception
	 */
	public static ServiceProxy getProxy(String serviceName) throws Exception {
		ServiceProxy p = Proxys.get(serviceName.toLowerCase());
		if (p == null) {
			synchronized (locker) {
				p = Proxys.get(serviceName.toLowerCase());
				if (p == null) {
					p = new ServiceProxy(serviceName);
					Proxys.put(serviceName.toLowerCase(), p);
				}
			}
		}
		return p;
	}
	
	/**
	 * 同步调用远程接口。这里逻辑包括调用服务选择，协议封装，超时重试处理，同步等逻辑
	 * @param returnType - 被调用方法的返回参数信息
	 * @param lookup - 接口名称
	 * @param methodName - 调用的方法名
	 * @param paras - 请求参数集合
	 * @param serVersion - 请求的SCF版本号
	 * @return InvokeResult<Object>
	 * @throws Exception
	 * @throws Throwable
	 */
	public InvokeResult<Object> invoke(Parameter returnType, String lookup, String methodName, Parameter[] paras, String serVersion) throws Exception, Throwable {
		long watcher = System.currentTimeMillis();
		List<KeyValuePair> listPara = new ArrayList<>();
		for (Parameter p : paras) {
			listPara.add(new KeyValuePair(p.getSimpleName(), p.getValue()));
		}
		//封装request协议
		RequestProtocol requestProtocol = new RequestProtocol(lookup, methodName, listPara);
		//判断序列化版本号
		SerializeType serializerType = SerializeType.SCFBinary;
		if (serVersion.equalsIgnoreCase("SCFV2")) {
			serializerType = SerializeType.SCFBinaryV2;
		} //else if (serVersion.equalsIgnoreCase("SCF")) {//这个代码有点多余
		//	serializerType = SerializeType.SCFBinary;
		//}
		
		//封装通信协议
		Protocol sendP = new Protocol(createSessionId(), (byte) this.config.getServiceid(), SDPType.Request,CompressType.UnCompress, serializerType, PlatformType.Java, requestProtocol);
		
		Protocol receiveP = null;
		Server server = null;
		//将接口，方法，参数依次拼接为字符串放入一个长度为3的数组中
		String[] methodPara = getMethodPara(lookup, methodName, paras);
		for (int i = 0; i <= this.count; i++) {
			//选择一个服务器
			server = getKeyServer(methodPara);
			
			if (server == null) {
				logger.error("cannot get server");
				throw new Exception("cannot get server");
			}
			
			try {
				//发出请求并获取返回值。。下面就是一堆乱起八遭的判断，没啥解释的了
				receiveP = server.request(sendP);
			} catch (IOException io) {
				if (this.count == 0 || i == this.ioreconnect) {
					throw io;
				}
				if (i < this.count && i < this.ioreconnect) {
					logger.error(server.getName() + " server has IOException,system will change normal server!");
				}
			} catch (RebootException rb) {
				createReboot(server);
				if (this.count == 0 || i == this.ioreconnect) {
					throw new IOException("connect fail!");
				}
				if (i < this.count && i < this.ioreconnect) {
					logger.error(server.getName() + " server has reboot,system will change normal server!");
				}
			} catch (TimeoutException toex) {
				if (this.count == 0 || i == this.requestTime) {
					throw toex;
				}
				if (i < this.count && i < this.requestTime) {
					logger.error(server.getName() + " server has TimeoutException,system will change normal server!");
				}
			} catch (UnresolvedAddressException uaex) {
				//将服务设置为死亡状态
				createDead(server);

				throw uaex;
			} catch (Throwable ex) {
				logger.error("invoke other Exception", ex);
				throw ex;
			}
		}
		//没有获取到结果，肯定是出现问题了。。
		if (receiveP == null) {
			throw new Exception("userdatatype error!");
		}
		
		if (receiveP.getSdpType() == SDPType.Response) {//服务端返回应答
			ResponseProtocol rp = (ResponseProtocol) receiveP.getSdpEntity();
			//打印远程方法实际调用的时间
			logger.debug("invoke time:" + (System.currentTimeMillis() - watcher) + "ms");
			return new InvokeResult<Object>(rp.getResult(), rp.getOutpara());
		} else if (receiveP.getSdpType() == SDPType.Reset) {//服务端状态为正在重启
			logger.info(server.getName() + " server is reboot,system will change normal server!");
			//将服务设置为重启状态
			createReboot(server);
			return invoke(returnType, lookup, methodName, paras , serVersion);
		} else if (receiveP.getSdpType() == SDPType.Exception) {//服务端返回异常
			ExceptionProtocol ep = (ExceptionProtocol) receiveP.getSdpEntity();
			throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
		}
		
		throw new Exception("userdatatype error!");
	}
	
	/**
	 * 异步调用远程接口。这里逻辑包括调用服务选择，协议封装，超时重试处理等逻辑。
	 * @param returnType - 被调用方法的返回参数信息
	 * @param lookup - 接口名称
	 * @param methodName - 调用的方法名
	 * @param paras - 请求参数集合
	 * @param rh - 异步回调处理类
	 * @throws Exception
	 * @throws Throwable
	 */
	public void invoke(Parameter returnType, String lookup, String methodName, Parameter[] paras, ReceiveHandler rh) throws Exception, Throwable {
		List<KeyValuePair> listPara = new ArrayList<>();
		for (Parameter p : paras) {
			listPara.add(new KeyValuePair(p.getSimpleName(), p.getValue()));
		}
		//封装request协议
		RequestProtocol requestProtocol = new RequestProtocol(lookup, methodName, listPara);
		//封装通信协议
		Protocol sendP = new Protocol(createSessionId(), (byte) this.config.getServiceid(), SDPType.Request,CompressType.UnCompress, this.config.getProtocol().getSerializerType(),PlatformType.Java, requestProtocol);
		
		Server server = null;
		//将接口，方法，参数依次拼接为字符串放入一个长度为3的数组中
		String[] methodPara = getMethodPara(lookup, methodName, paras);
		for (int i = 0; i <= this.count; i++) {
			//选择一个服务器
			server = getKeyServer(methodPara);
			if (server == null) {
				logger.error("cannot get server");
				throw new Exception("cannot get server");
			}
			
			try {
				//ReceiveHandler维护住Server对象，为回调使用
				rh.setServer(server);
				//requestAsync会将协议对象转化成byte数组，并封装到WindowData对象中并传递给异步队列执行发送逻辑
				server.requestAsync(sendP, rh);
			} catch (IOException io) {
				if (this.count == 0 || i == this.ioreconnect) {
					throw io;
				}
				if (i < this.count && i < this.ioreconnect) {
					logger.error(server.getName() + " server has IOException,system will change normal server!");
				}
			} catch (RebootException rb) {
				createReboot(server);
				if (this.count == 0 || i == this.ioreconnect) {
					throw new IOException("connect fail!");
				}
				if (i < this.count && i < this.ioreconnect) {
					logger.error(server.getName() + " server has reboot,system will change normal server!");
				}
			} catch (TimeoutException toex) {
				if (this.count == 0 || i == this.requestTime) {
					throw toex;
				}
				if (i < this.count && i < this.requestTime) {
					logger.error(server.getName() + " server has TimeoutException,system will change normal server!");
				}
			} catch (UnresolvedAddressException uaex) {
				//将服务设置为死亡状态
				createDead(server);
				throw uaex;
			} catch (Throwable ex) {
				logger.error("invoke other Exception", ex);
				throw ex;
			}
		}
	}
	
	/**
	 * 设置服务重启
	 * @param server - Server
	 * @throws Exception
	 * @throws Throwable
	 */
	private void createReboot(Server server) throws Exception, Throwable {
		server.createReboot();
	}
	
	/**
	 * 设置服务已僵死
	 * @param server - Server
	 * @throws Exception
	 * @throws Throwable
	 */
	private void createDead(Server server) throws Exception, Throwable {
		server.createDead();
	}
	
	/**
	 * 创建DES密钥交换协议
	 * @param hp - HandclaspProtocol
	 * @return Protocol
	 * @throws Exception
	 */
	public Protocol createProtocol(HandclaspProtocol hp) throws Exception {
		return new Protocol(createSessionId(), (byte) this.config.getServiceid(), SDPType.Request, CompressType.UnCompress, this.config.getProtocol().getSerializerType(), PlatformType.Java, hp);
	}
	
	/**
	 * 创建一个字符密钥协议
	 * @param ap - 名称
	 * @return Protocol
	 * @throws Exception
	 */
	public Protocol createApproveProtocol(String ap) throws Exception {
		return new Protocol(createSessionId(), (byte) this.config.getServiceid(), SDPType.Request, CompressType.UnCompress, this.config.getProtocol().getSerializerType(), PlatformType.Java, ap);
	}
	
	/**
	 * 根据服务名称获得服务的信息
	 * 如端口号，地址等信息
	 * @param name - 服务名称
	 * @return String
	 */
	public String getServer(String name) {
		Server server = this.dispatcher.GetServer(name);
		if (server == null) {
			return "";
		}
		return server.toString();
	}
	
	/**
	 * 销毁所有的服务代理
	 */
	public static void destroyAll() {
		Collection<ServiceProxy> spList = Proxys.values();
		if (spList != null) {
			for (ServiceProxy sp : spList) {
				sp.destroy();
			}
		}
	}
	
	/**
	 * 创建sessionId
	 * @return int
	 */
	private int createSessionId() {
		synchronized (lockerSessionID) {
			//大于最大值，则重置为1，这个地方应该是可以使用原子自增的，性能可能会好点。
			if (this.sessionId > SCFConst.MAX_SESSIONID) {
				this.sessionId = 1;
			}
			return this.sessionId++;
		}
	}
	
	/**
	 * 设置接口信息与服务的映射关系。此方法没找到调用的地方
	 * @param lookup - 接口名
	 * @param methodName - 方法名称
	 * @param para - 参数列表
	 * @param serverName - 服务列表
	 * @throws Exception
	 */
	public static void setServer(String lookup, String methodName, List<String> para, String[] serverName) throws Exception {
		if (serverName != null) {
			ServerChoose sc = new ServerChoose(serverName.length, serverName);
			StringBuffer sb = new StringBuffer();
			sb.append(lookup);
			sb.append(methodName);
			if (para != null) {
				for (String str : para) {
					sb.append(str);
				}
			}
			
			String key = sb.toString();
			if (!methodServer.containsKey(key)) {
				methodServer.put(key, sc);
			}
		} else {
			logger.error("serverName is null");
			throw new Exception("para or serverName is null");
		}
	}
	
	/**
	 * 设置接口信息与服务的映射关系。此方法没找到调用的地方
	 * @param lookup - 接口名 
	 * @param methodName - 方法名称
	 * @param serverName - 服务列表
	 * @throws Exception
	 */
	public static void setServer(String lookup, String methodName, String[] serverName) throws Exception {
		if (serverName != null) {
			ServerChoose sc = new ServerChoose(serverName.length, serverName);

			StringBuffer sb = new StringBuffer();
			sb.append(lookup);
			sb.append(methodName);

			String key = sb.toString();
			if (!methodServer.containsKey(key)) {
				methodServer.put(key, sc);
			}
		} else {
			logger.error("serverName is null");
			throw new Exception("para or serverName is null");
		}
	}
	
	/**
	 * 设置接口信息与服务的映射关系。此方法没找到调用的地方
	 * @param lookup - 接口名 
	 * @param serverName - 服务列表
	 * @throws Exception
	 */
	public static void setServer(String lookup, String[] serverName) throws Exception {
		if (serverName != null) {
			ServerChoose sc = new ServerChoose(serverName.length, serverName);

			StringBuffer sb = new StringBuffer();
			sb.append(lookup);

			String key = sb.toString();
			if (!methodServer.containsKey(key)) {
				methodServer.put(key, sc);
			}
		} else {
			logger.error("serverName is null");
			throw new Exception("para or serverName is null");
		}
	}
	
	/**
	 * 获得方法的描述信息
	 * @param lookup - 接口名
	 * @param methodName - 方法名
	 * @param paras - 参数信息
	 * @return String[] 0 lookup , 1 lookup+方法名,2 lookup+方法名+参数信息
	 */
	public String[] getMethodPara(String lookup, String methodName, Parameter[] paras) {
		String[] str = new String[3];
		StringBuilder sb = new StringBuilder();
		sb.append(lookup);
		
		str[2] = sb.toString();
		sb.append(methodName);
		
		str[1] = sb.toString();
		if (paras != null && paras.length == 0) {
			sb.append("null");
		}
		for (Parameter p : paras) {
			sb.append(p.getSimpleName());
		}
		str[0] = sb.toString();
		return str;
	}
	
	/**
	 * 根据methodServer的key获得服务器
	 * @param key - String[]
	 * @return Server
	 */
	private Server getKeyServer(String[] key) {
		Server server = null;
		//这个循环是白循环，看源码的地方没有任何地方调用setServer方法，则methodServer肯定是个空的Map
		for (int i = 0; i < key.length; i++) {
			if (methodServer.containsKey(key[i])) {
				server = this.dispatcher.GetServer(methodServer.get(key[i]));
				if (server != null) {
					break;
				}
			}
		}
		//实际执行的是此处代码
		if (server == null) {
			server = this.dispatcher.GetServer();
		}
		
		//如果服务器死亡，并且非测试，则进行一次测试，看连接是否通顺
		if (server.getState() == ServerState.Dead && !server.isTesting()) {
			//如果能够正常连接或者正在测试中，则设置状态为测试状态。否则不修改状态，只是把testing属性设置为false
			if (server.testing()) {
				server.setTesting(true);
				server.setState(ServerState.Testing);
			} else {
				server.setTesting(false);
			}
		}
		
		return server;
	}

}
