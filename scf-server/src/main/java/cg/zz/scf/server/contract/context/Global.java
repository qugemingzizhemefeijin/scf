package cg.zz.scf.server.contract.context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jboss.netty.channel.Channel;

import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.init.IInit;
import cg.zz.scf.server.contract.server.IServer;

/**
 * 全局配置信息
 * @author chengang
 *
 */
public class Global {
	
	/**
	 * 单例
	 */
	private static Global instance = null;
	
	/**
	 * 服务配置项
	 */
	private ServiceConfig serviceConfig = null;
	
	/**
	 * 代理工厂
	 */
	private IProxyFactory proxyFactory = null;
	
	/**
	 * 服务器列表
	 */
	private List<IServer> serverList = new ArrayList<IServer>();
	
	/**
	 * 请求过滤器
	 */
	private List<IFilter> globalRequestFilterList = new ArrayList<IFilter>();
	
	/**
	 * 返回过滤器
	 */
	private List<IFilter> globalResponseFilterList = new ArrayList<IFilter>();
	
	/**
	 * 连接过滤器
	 */
	private List<IFilter> connectionFilterList = new ArrayList<IFilter>();
	
	/**
	 * 初始化列表
	 */
	private List<IInit> initList = new ArrayList<IInit>();
	
	/**
	 * 项目根路径
	 */
	private String rootPath;
	
	/**
	 * 授权文件、对应方法
	 */
	private Map<String,List<String>> secureMap = new HashMap<String,List<String>>();
	
	/**
	 * 各channel对应SecureContext
	 */
	private ConcurrentMap<Channel, SecureContext> channelMap = new ConcurrentHashMap<Channel , SecureContext>();
	
	/**
	 * 服务器运行状态Nomarl正常、Reboot重启
	 */
	private ServerStateType serverState = ServerStateType.Nomarl;
	
	/**
	 * 各通道对应的ApproveContext
	 */
	private ConcurrentHashMap<Channel, ApproveContext> channelApproveMap = new ConcurrentHashMap<Channel, ApproveContext>();
	
	/**
	 * 上下文环境的线程变量
	 */
	private ThreadLocal<SCFContext> threadLocal = new ThreadLocal<SCFContext>();
	
	/**
	 * 单例
	 * @return
	 */
	public static Global getInstance() {
		if (instance == null) {
			synchronized (Global.class) {
				if (instance == null) {
					instance = new Global();
				}
			}
		}
		return instance;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public ServiceConfig getServiceConfig() {
		return serviceConfig;
	}

	public void setServiceConfig(ServiceConfig serviceConfig) {
		this.serviceConfig = serviceConfig;
	}
	
	public ThreadLocal<SCFContext> getThreadLocal() {
		return this.threadLocal;
	}

	public IProxyFactory getProxyFactory() {
		return proxyFactory;
	}

	public void setProxyFactory(IProxyFactory proxyFactory) {
		this.proxyFactory = proxyFactory;
	}

	public List<IServer> getServerList() {
		return serverList;
	}

	public List<IFilter> getGlobalRequestFilterList() {
		return globalRequestFilterList;
	}

	public List<IFilter> getGlobalResponseFilterList() {
		return globalResponseFilterList;
	}

	public List<IInit> getInitList() {
		return initList;
	}
	
	/**
	 * 添加服务器列表
	 * @param server - IServer
	 */
	public void addServer(IServer server) {
		synchronized (this.serverList) {
			this.serverList.add(server);
		}
	}
	
	/**
	 * 添加初始化接口
	 * @param init - IInit
	 */
	public void addInit(IInit init) {
		synchronized (this.initList) {
			this.initList.add(init);
		}
	}
	
	/**
	 * 添加请求过滤器
	 * @param filter - IFilter
	 */
	public void addGlobalRequestFilter(IFilter filter) {
		synchronized (this.globalRequestFilterList) {
			this.globalRequestFilterList.add(filter);
		}
	}

	/**
	 * 移除请求过滤器
	 * @param filter - IFilter
	 */
	public void removeGlobalRequestFilter(IFilter filter) {
		synchronized (this.globalRequestFilterList) {
			this.globalRequestFilterList.remove(filter);
		}
	}

	/**
	 * 添加应答过滤器
	 * @param filter - IFilter
	 */
	public void addGlobalResponseFilter(IFilter filter) {
		synchronized (this.globalResponseFilterList) {
			this.globalResponseFilterList.add(filter);
		}
	}

	/**
	 * 移除应答过滤器
	 * @param filter - IFilter
	 */
	public void removeGlobalResponseFilter(IFilter filter) {
		synchronized (this.globalResponseFilterList) {
			this.globalResponseFilterList.remove(filter);
		}
	}
	
	/**
	 *获的当前channel对应密钥类 
	 * @return SecureContext
	 */
	public SecureContext getGlobalSecureContext(Channel channel){
		if(null != this.channelMap){
			return this.channelMap.get(channel);
		}
		return null;
	}
	
	/**
	 * 获得是否启用权限认证 
	 * @return True or False
	 */
	public boolean getGlobalSecureIsRights() {
		if (this.serviceConfig != null) {
			return "true".equalsIgnoreCase(this.serviceConfig.getString("scf.secure"));
		}
		return false;
	}
	
	/**
	 * 查看是否需要审批
	 * @return true or false
	 */
	public boolean getApproveIsRights() {
		if (this.serviceConfig != null) {
			return "true".equalsIgnoreCase(this.serviceConfig.getString("scf.server.approve"));
		}
		return false;
	}

	public ApproveContext getGlobalAppvoreContext(Channel channel) {
		if (this.channelApproveMap != null) {
			return this.channelApproveMap.get(channel);
		}
		return null;
	}
	
	public void addConnectionFilter(IFilter filter) {
		synchronized (this.connectionFilterList) {
			this.connectionFilterList.add(filter);
		}
	}

	public void removeConnectionFilter(IFilter filter) {
		synchronized (this.connectionFilterList) {
			this.connectionFilterList.remove(filter);
		}
	}
	
	public void removeSecureMap(String key) {
		this.secureMap.remove(key);
	}

	public boolean containsSecureMap(String key) {
		return this.secureMap.containsKey(key);
	}

	public Map<String, List<String>> getSecureMap() {
		return this.secureMap;
	}

	public void addSecureMap(String key, List<String> list) {
		this.secureMap.put(key, list);
	}

	public void addChannelMap(Channel channel, SecureContext context) {
		this.channelMap.put(channel, context);
	}

	public void removeChannelMap(Channel channel) {
		this.channelMap.remove(channel);
	}

	public Map<Channel, SecureContext> getChannelMap() {
		return this.channelMap;
	}
	
	public void addChannelApproveMap(Channel channel, ApproveContext context) {
		this.channelApproveMap.put(channel, context);
	}

	public void removeChannelApproveMap(Channel channel) {
		this.channelApproveMap.remove(channel);
	}

	public Map<Channel, ApproveContext> getChannelApproveMap() {
		return this.channelApproveMap;
	}
	
	public List<IFilter> getConnectionFilterList() {
		return this.connectionFilterList;
	}

	public ServerStateType getServerState() {
		return this.serverState;
	}

	public void setServerState(ServerStateType serverState) {
		this.serverState = serverState;
	}

}
