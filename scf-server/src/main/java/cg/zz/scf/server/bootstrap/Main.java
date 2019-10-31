package cg.zz.scf.server.bootstrap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

import cg.zz.scf.server.bootstrap.signal.OperateSignal;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.IProxyFactory;
import cg.zz.scf.server.contract.context.ServiceConfig;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.init.IInit;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.Log4jConfig;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.contract.log.SystemPrintStream;
import cg.zz.scf.server.contract.server.IServer;
import cg.zz.scf.server.deploy.filemonitor.FileMonitor;
import cg.zz.scf.server.deploy.filemonitor.HotDeployListener;
import cg.zz.scf.server.deploy.filemonitor.NotifyCount;
import cg.zz.scf.server.deploy.hotdeploy.DynamicClassLoader;
import cg.zz.scf.server.deploy.hotdeploy.GlobalClassLoader;
import cg.zz.scf.server.deploy.hotdeploy.ProxyFactoryLoader;
import sun.misc.Signal;

@SuppressWarnings("restriction")
public class Main {
	
	private static final ILog logger = LogFactory.getLogger(Main.class);

	/**
	 * 服务Main方法
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length < 1) {
			throw new IllegalArgumentException("usage: -Dscf.service.name=<service-name> [<other-scf-config>]");
		}
		
		//当前项目启动程序路径
		String userDir = System.getProperty("user.dir");
		//项目根路径
		String rootPath = userDir.substring(0 , userDir.lastIndexOf("/"))+"/";
		//服务名称，必须设置
		String serviceName = "no service name please set it";
		
		Map<String , String> argsMap = new HashMap<String , String>();
		Global.getInstance().setRootPath(rootPath);
		//解析程序启动时候的初始化变量
		for (String arg : args) {
			if(arg.startsWith("-D")) {
				String[] aryArg = arg.split("=");
				if (aryArg.length == 2) {
					if (aryArg[0].equalsIgnoreCase("-Dscf.service.name")) {
						serviceName = aryArg[1];
					}
					argsMap.put(aryArg[0].replaceFirst("-D", ""), aryArg[1]);
				}
			}
		}
		
		//服务的根路径
		String serviceFolderPath = rootPath + "service/deploy/" + serviceName;
		String scfConfigDefaultPath = rootPath + "conf/scf_config.xml";//scf默认配置文件路径
		String scfConfigPath = serviceFolderPath + "/scf_config.xml";//scf自定义配置路径
		String log4jConfigDefaultPath = rootPath + "conf/scf_log4j.xml";//scf默认log4j配置路径
		String log4jConfigPath = serviceFolderPath + "/scf_log4j.xml";//scf自定义log4j配置路径
		
		loadLog4jConfig(log4jConfigPath, log4jConfigDefaultPath, serviceName);
		
		logger.info("+++++++++++++++++++++ staring +++++++++++++++++++++\n");
		
		logger.info("user.dir: " + userDir);
		logger.info("rootPath: " + rootPath);
		logger.info("service scf_config.xml: " + scfConfigPath);
		logger.info("default scf_config.xml: " + scfConfigDefaultPath);
		logger.info("service scf_log4j.xml: " + log4jConfigPath);
		logger.info("default scf_log4j.xml: " + log4jConfigDefaultPath);
		
		logger.info("load service config...");
		ServiceConfig sc = loadServiceConfig(new String[] { scfConfigDefaultPath, scfConfigPath });
		Set<String> keySet = argsMap.keySet();
		for (String key : keySet) {
			logger.info(key + ": " + argsMap.get(key));
			sc.set(key, argsMap.get(key));
		}
		
		if (sc.getString("scf.service.name") == null || sc.getString("scf.service.name").equals("")) {
			logger.info("scf.service.name:" + serviceName);
			sc.set("scf.service.name", serviceName);
		}
		Global.getInstance().setServiceConfig(sc);
		
		logger.info("-----------------loading global jars------------------");
		DynamicClassLoader classLoader = new DynamicClassLoader();
		classLoader.addFolder(new String[] { 
			rootPath + "service/deploy/" + sc.getString("scf.service.name") + "/", 
			rootPath + "service/lib/", 
			rootPath + "lib" });
		
		//加载jar资源到系统的ClassLoader中，也就是sun.misc.Launcher的子类AppClassLoader
		 GlobalClassLoader.addSystemClassPathFolder(new String[] { 
			rootPath + "service/deploy/" + sc.getString("scf.service.name") + "/", 
			rootPath + "service/lib/", 
			rootPath + "lib" });
		 
		 logger.info("-------------------------end-------------------------\n");
		 
		 if(new File(serviceFolderPath).isDirectory() || !serviceName.equalsIgnoreCase("error_service_name_is_null")) {
			 logger.info("--------------------loading proxys-------------------");
			 IProxyFactory proxyFactory = ProxyFactoryLoader.loadProxyFactory(classLoader);
			 Global.getInstance().setProxyFactory(proxyFactory);
			 logger.info("-------------------------end-------------------------\n");
			 
			 logger.info("-----------------loading init beans------------------");
			 loadInitBeans(classLoader, sc);
			 logger.info("-------------------------end-------------------------\n");
		 }
		 
		 logger.info("-----------loading global request filters------------");
		 List<IFilter> requestFilters = loadFilters(classLoader, sc, "scf.filter.global.request");
		for (IFilter filter : requestFilters) {
			Global.getInstance().addGlobalRequestFilter(filter);
		}
		logger.info("-------------------------end-------------------------\n");
		
		logger.info("-----------loading global response filters-----------");
		List<IFilter> responseFilters = loadFilters(classLoader, sc, "scf.filter.global.response");
		for (IFilter filter : responseFilters) {
			Global.getInstance().addGlobalResponseFilter(filter);
		}
		logger.info("-------------------------end-------------------------\n");
		
		logger.info("-----------loading connection filters-----------");
		List<IFilter> connFilters = loadFilters(classLoader, sc, "scf.filter.connection");
		for (IFilter filter : connFilters) {
			Global.getInstance().addConnectionFilter(filter);
		}
		logger.info("-------------------------end-------------------------\n");
		
		logger.info("------------------load secureKey start---------------------");
		if (sc.getString("scf.secure") != null && "true".equalsIgnoreCase(sc.getString("scf.secure"))) {
			logger.info("scf.secure:" + sc.getString("scf.secure"));
			loadSecureKey(sc, serviceFolderPath);
		}
		logger.info("------------------load secureKey end----------------------\n");
		
		logger.info("------------------signal registr start---------------------");
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName != null && osName.indexOf("window") == -1) {
			OperateSignal operateSignalHandler = new OperateSignal();
			Signal sig = new Signal("USR2");
			Signal.handle(sig, operateSignalHandler);
		}
		logger.info("------------------signal registr success----------------------\n");
		
		logger.info("------------------ starting servers -----------------");
		loadServers(classLoader, sc);
		logger.info("-------------------------end-------------------------\n");
		
		//是否可以热部署
		if (sc.getBoolean("scf.hotdeploy")) {
			logger.info("------------------init file monitor-----------------");
			addFileMonitor(rootPath, sc.getString("scf.service.name"));
			logger.info("-------------------------end-------------------------\n");
		}
		
		//注册虚拟机关闭时候的监听事件
		try {
			registerExcetEven();
		} catch (Exception e) {
			logger.error("registerExcetEven error", e);
			System.exit(0);
		}
		
		logger.info("+++++++++++++++++++++ server start success!!! +++++++++++++++++++++\n");
		while (true) Thread.sleep(3600000L);
	}
	
	/**
	 * 获取系统配置
	 * @param cps - 配置文件路径
	 * @return ServiceConfig
	 * @throws Exception
	 */
	private static ServiceConfig loadServiceConfig(String[] cps) throws Exception {
		ServiceConfig sc = ServiceConfig.getServiceConfig(cps);
		if (sc == null) {
			logger.error("ServiceConfig sc is null");
		}
		
		return sc;
	}
	
	/**
	 * 加载log4j配置
	 * @param configPath - 自定义配置文件
	 * @param defaultPath - 默认配置文件
	 * @param serviceName - 服务名称
	 * @throws Exception
	 */
	private static void loadLog4jConfig(String configPath, String defaultPath, String serviceName) throws Exception {
		File fLog4jConfig = new File(configPath);
		//如果文件存在则加载自定义配置，否则加载默认配置
		if (fLog4jConfig.exists()) {
			Log4jConfig.configure(configPath);
			SystemPrintStream.redirectToLog4j();
		} else {
			Log4jConfig.configure(defaultPath);
			String logPath = null;
			Appender appender = Logger.getRootLogger().getAppender("activexAppender");
			if(appender != null && appender instanceof FileAppender) {
				FileAppender fappender = (FileAppender)appender;
				if (!fappender.getFile().contains(serviceName)) {
					logPath = fappender.getFile();
					fappender.setFile(fappender.getFile().substring(0, fappender.getFile().lastIndexOf("/")) + "/" + serviceName + "/" + serviceName + ".log");
					fappender.activateOptions();
					
					System.err.println(fappender.getFile());
				}
			}
			SystemPrintStream.redirectToLog4j();
			try {
				if (logPath != null) {
					File file = new File(logPath);
					file.delete();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 初始化起始Bean
	 * @param classLoader - DynamicClassLoader
	 * @param sc - ServiceConfig
	 * @throws Exception
	 */
	private static void loadInitBeans(DynamicClassLoader classLoader, ServiceConfig sc) throws Exception {
		List<String> initList = sc.getList("scf.init", ",");
		if (initList != null && !initList.isEmpty()) {
			for (String initBeans : initList) {
				try {
					logger.info("load: " + initBeans);
					
					IInit initBean = (IInit)classLoader.loadClass(initBeans).newInstance();
					Global.getInstance().addInit(initBean);
					initBean.init();
				} catch (Exception e) {
					logger.error("init " + initBeans + " error!!!", e);
			    }
			}
		}
	}
	
	/**
	 * 加载权限验证密钥
	 * @param sc - ServiceConfig
	 * @param path - 密钥文件路径
	 * @throws Exception
	 */
	private static void loadSecureKey(ServiceConfig sc, String path) throws Exception {
		File[] file = new File(path).listFiles();
		for (File f : file) {
			String fName = f.getName();
			if (!f.exists() || fName.indexOf("secure") < 0 || !"xml".equalsIgnoreCase(fName.substring(fName.lastIndexOf(".") + 1))) {
				continue;
			}
			ServiceConfig.getSecureConfig(new String[] { f.getPath() });
		}
	}
	
	/**
	 * 加载指定配置的过滤器
	 * @param classLoader - DynamicClassLoader
	 * @param sc - ServiceConfig
	 * @param key - 配置文件中的过滤器key
	 * @return List<IFilter>
	 * @throws Exception
	 */
	private static List<IFilter> loadFilters(DynamicClassLoader classLoader, ServiceConfig sc, String key) throws Exception {
		List<String> filterList = sc.getList(key, ",");
		List<IFilter> instanceList = new ArrayList<IFilter>();
		if (filterList != null) {
			for (String filterName : filterList) {
				try {
					logger.info("load: " + filterName);
					IFilter filter = (IFilter) classLoader.loadClass(filterName).newInstance();
					instanceList.add(filter);
				} catch (Exception e) {
					logger.error("load " + filterName + " error!!!", e);
				}
			}
		}

		return instanceList;
	}
	
	/**
	 * 加载服务，有http telnet tcp 等
	 * @param classLoader - DynamicClassLoader
	 * @param sc - ServiceConfig
	 * @throws Exception
	 */
	private static void loadServers(DynamicClassLoader classLoader, ServiceConfig sc) throws Exception {
		List<String> servers = sc.getList("scf.servers", ",");
		if (servers != null) {
			for (String server : servers) {
				try {
					if (sc.getBoolean(server + ".enable")) {
						logger.info(server + " is starting...");
						IServer serverImpl = (IServer) classLoader.loadClass(sc.getString(server + ".implement")).newInstance();
						Global.getInstance().addServer(serverImpl);
						serverImpl.start();
						logger.info(server + "started success!!!\n");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 添加文件监控系统，每5秒监控一次文件的改动
	 * @param rootPath - 监控的目录
	 * @param serviceName - 服务名称
	 * @throws Exception
	 */
	private static void addFileMonitor(String rootPath, String serviceName) throws Exception {
		FileMonitor.getInstance().addMonitorFile(rootPath + "service/deploy/" + serviceName +"/");
		
		FileMonitor.getInstance().setInterval(5000L);
		FileMonitor.getInstance().setNotifyCount(NotifyCount.Once);
		FileMonitor.getInstance().addListener(new HotDeployListener());
		FileMonitor.getInstance().start();
	}
	
	/**
	 * 注册虚拟机停止后执行的回收资源事件
	 */
	private static void registerExcetEven() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				for (IServer server : Global.getInstance().getServerList()) {
					try {
						server.stop();
					} catch (Exception e) {
						Main.logger.error("stop server error", e);
					}
				}
				try {
					super.finalize();
				} catch (Throwable e) {
					Main.logger.error("super.finalize() error when stop server", e);
				}
			}
		});
	}

}
