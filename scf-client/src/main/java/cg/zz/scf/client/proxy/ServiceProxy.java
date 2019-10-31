package cg.zz.scf.client.proxy;

import java.io.IOException;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

public class ServiceProxy {
	
	private static final ILog logger = LogFactory.getLogger(ServiceProxy.class);
	
	private int sessionId = 1;
	
	/**
	 * 超时重连次数
	 */
	private int requestTime = 0;
	
	/**
	 * IO服务切换次数
	 */
	private int ioreconnect = 0;
	
	/**
	 * 超时重新发送次数
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
	
	private static final Object lockerSessionID = new Object();
	private static final HashMap<String, ServiceProxy> Proxys = new HashMap<>();
	private static ConcurrentHashMap<String, ServerChoose> methodServer = new ConcurrentHashMap<>();
	
	private ServiceProxy(String serviceName) throws Exception {
		this.config = ServiceConfig.GetConfig(serviceName);
		this.dispatcher = new Dispatcher(this.config);
		
		this.requestTime = this.config.getSocketPool().getReconnectTime();
		int serverCount = 1;
		
		if (this.dispatcher.GetAllServer() != null && this.dispatcher.GetAllServer().size() > 0) {
			serverCount = this.dispatcher.GetAllServer().size();
		}
		
		this.ioreconnect = (serverCount - 1);
		this.count = this.requestTime;
		
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
					server.getScoketpool().destroy();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 根据服务名称获得一个服务代理
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
	
	public InvokeResult<Object> invoke(Parameter returnType, String typeName, String methodName, Parameter[] paras) throws Exception, Throwable {
		logger.info("invoke==============1");
		long watcher = System.currentTimeMillis();
		List<KeyValuePair> listPara = new ArrayList<>();
		for (Parameter p : paras) {
			listPara.add(new KeyValuePair(p.getSimpleName(), p.getValue()));
		}
		RequestProtocol requestProtocol = new RequestProtocol(typeName, methodName, listPara);
		Protocol sendP = new Protocol(createSessionId(),(byte)this.config.getServiceid(),SDPType.Request,CompressType.UnCompress,this.config.getProtocol().getSerializerType(),PlatformType.Java,requestProtocol);
		logger.info("sendP.getSessionID() : "+sendP.getSessionID());
		
		Protocol receiveP = null;
		Server server = null;
		String[] methodPara = getMethodPara(typeName, methodName, paras);
		for (int i = 0; i <= this.count; i++) {
			server = getKeyServer(methodPara);
			
			if (server == null) {
				logger.error("cannot get server");
				throw new Exception("cannot get server");
			}
			
			try {
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
				createDead(server);

				throw uaex;
			} catch (Throwable ex) {
				logger.error("invoke other Exception", ex);
				throw ex;
			}
		}
		
		if (receiveP == null) {
			throw new Exception("userdatatype error!");
		}
		
		if (receiveP.getSdpType() == SDPType.Response) {
			ResponseProtocol rp = (ResponseProtocol)receiveP.getSdpEntity();
			logger.debug("invoke time:" + (System.currentTimeMillis() - watcher) + "ms");
			return new InvokeResult<Object>(rp.getResult(), rp.getOutpara());
		} else if (receiveP.getSdpType() == SDPType.Reset) {
			logger.info(server.getName() + " server is reboot,system will change normal server!");
			createReboot(server);
			return invoke(returnType, typeName, methodName, paras);
		} else if (receiveP.getSdpType() == SDPType.Exception) {
			ExceptionProtocol ep = (ExceptionProtocol)receiveP.getSdpEntity();
			throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
		}
		throw new Exception("userdatatype error!");
	}
	
	public InvokeResult<Object> invoke(Parameter returnType, String typeName, String methodName, Parameter[] paras, String serVersion) throws Exception, Throwable {
		logger.info("invoke==============2");
		long watcher = System.currentTimeMillis();
		List<KeyValuePair> listPara = new ArrayList<>();
		for (Parameter p : paras) {
			listPara.add(new KeyValuePair(p.getSimpleName(), p.getValue()));
		}
		RequestProtocol requestProtocol = new RequestProtocol(typeName, methodName, listPara);
		SerializeType serializerType = SerializeType.SCFBinary;
		if (serVersion.equalsIgnoreCase("SCFV2"))
			serializerType = SerializeType.SCFBinaryV2;
		else if (serVersion.equalsIgnoreCase("SCF")) {
			serializerType = SerializeType.SCFBinary;
		}
		Protocol sendP = new Protocol(createSessionId(), (byte) this.config.getServiceid(), SDPType.Request,CompressType.UnCompress, serializerType, PlatformType.Java, requestProtocol);
		
		Protocol receiveP = null;
		Server server = null;
		String[] methodPara = getMethodPara(typeName, methodName, paras);
		for (int i = 0; i <= this.count; i++) {
			server = getKeyServer(methodPara);
			
			if (server == null) {
				logger.error("cannot get server");
				throw new Exception("cannot get server");
			}
			
			try {
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
				createDead(server);

				throw uaex;
			} catch (Throwable ex) {
				logger.error("invoke other Exception", ex);
				throw ex;
			}
		}
		
		if (receiveP == null) {
			throw new Exception("userdatatype error!");
		}
		
		if (receiveP.getSdpType() == SDPType.Response) {
			ResponseProtocol rp = (ResponseProtocol) receiveP.getSdpEntity();
			logger.debug("invoke time:" + (System.currentTimeMillis() - watcher) + "ms");
			return new InvokeResult<Object>(rp.getResult(), rp.getOutpara());
		} else if (receiveP.getSdpType() == SDPType.Reset) {
			logger.info(server.getName() + " server is reboot,system will change normal server!");
			createReboot(server);
			return invoke(returnType, typeName, methodName, paras);
		} else if (receiveP.getSdpType() == SDPType.Exception) {
			ExceptionProtocol ep = (ExceptionProtocol) receiveP.getSdpEntity();
			throw ThrowErrorHelper.throwServiceError(ep.getErrorCode(), ep.getErrorMsg());
		}
		
		throw new Exception("userdatatype error!");
	}
	
	public void invoke(Parameter returnType, String typeName, String methodName, Parameter[] paras, ReceiveHandler rh) throws Exception, Throwable {
		logger.info("invoke==============2");
		List<KeyValuePair> listPara = new ArrayList<>();
		for (Parameter p : paras) {
			listPara.add(new KeyValuePair(p.getSimpleName(), p.getValue()));
		}
		RequestProtocol requestProtocol = new RequestProtocol(typeName, methodName, listPara);
		Protocol sendP = new Protocol(createSessionId(), (byte) this.config.getServiceid(), SDPType.Request,CompressType.UnCompress, this.config.getProtocol().getSerializerType(),PlatformType.Java, requestProtocol);
		
		Server server = null;
		String[] methodPara = getMethodPara(typeName, methodName, paras);
		for (int i = 0; i <= this.count; i++) {
			server = getKeyServer(methodPara);
			if (server == null) {
				logger.error("cannot get server");
				throw new Exception("cannot get server");
			}
			try {
				rh.setServer(server);
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
	 * 创建协议
	 * @param hp - HandclaspProtocol
	 * @return Protocol
	 * @throws Exception
	 */
	public Protocol createProtocol(HandclaspProtocol hp) throws Exception {
		Protocol sendRightsProtocol = new Protocol(createSessionId(), (byte) this.config.getServiceid(),
				SDPType.Request, CompressType.UnCompress,
				this.config.getProtocol().getSerializerType(), PlatformType.Java, hp);
		return sendRightsProtocol;
	}
	
	/**
	 * 创建一个认证协议
	 * @param ap - 名称
	 * @return Protocol
	 * @throws Exception
	 */
	public Protocol createApproveProtocol(String ap) throws Exception {
		Protocol sendRightsProtocol = new Protocol(createSessionId(), (byte) this.config.getServiceid(),
				SDPType.Request, CompressType.UnCompress,
				this.config.getProtocol().getSerializerType(), PlatformType.Java, ap);
		return sendRightsProtocol;
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
		if (spList != null)
			for (ServiceProxy sp : spList)
				sp.destroy();
	}
	
	/**
	 * 创建sessionId
	 * @return int
	 */
	private int createSessionId() {
		synchronized (lockerSessionID) {
			if (this.sessionId > SCFConst.MAX_SESSIONID) {
				this.sessionId = 1;
			}
			return this.sessionId++;
		}
	}
	
	/**
	 * 设置服务信息
	 * @param lookup - 表
	 * @param methodName - 方法名称
	 * @param para - 参数列表
	 * @param serverName - 服务名数组
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
			if (!methodServer.containsKey(sb.toString())) methodServer.put(sb.toString(), sc);
		} else {
			logger.error("serverName is null");
			throw new Exception("para or serverName is null");
		}
	}
	
	/**
	 * 设置服务信息
	 * @param lookup - 表
	 * @param methodName - 方法名称
	 * @param serverName - 服务名列表
	 * @throws Exception
	 */
	public static void setServer(String lookup, String methodName, String[] serverName) throws Exception {
		if (serverName != null) {
			ServerChoose sc = new ServerChoose(serverName.length, serverName);

			StringBuffer sb = new StringBuffer();
			sb.append(lookup);
			sb.append(methodName);

			if (!methodServer.containsKey(sb.toString())) methodServer.put(sb.toString(), sc);
		} else {
			logger.error("serverName is null");
			throw new Exception("para or serverName is null");
		}
	}
	
	/**
	 * 设置服务信息
	 * @param lookup - 表
	 * @param serverName - 服务名列表
	 * @throws Exception
	 */
	public static void setServer(String lookup, String[] serverName) throws Exception {
		if (serverName != null) {
			ServerChoose sc = new ServerChoose(serverName.length, serverName);

			StringBuffer sb = new StringBuffer();
			sb.append(lookup);

			if (!methodServer.containsKey(sb.toString())) methodServer.put(sb.toString(), sc);
		} else {
			logger.error("serverName is null");
			throw new Exception("para or serverName is null");
		}
	}
	
	/**
	 * 获得方法参数信息
	 * @param lookup - 表
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
	 * 根据methodServer的key获得服务
	 * @param key - String[]
	 * @return Server
	 */
	private Server getKeyServer(String[] key) {
		Server server = null;
		for (int i = 0; i < key.length; i++) {
			if (methodServer.containsKey(key[i])) {
				server = this.dispatcher.GetServer(methodServer.get(key[i]));
				if (server != null) {
					break;
				}
			}
		}
		if (server == null) {
			server = this.dispatcher.GetServer();
		}
		
		if (server.getState() == ServerState.Dead && !server.isTesting()) {
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
