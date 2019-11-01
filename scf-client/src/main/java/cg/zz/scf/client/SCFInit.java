package cg.zz.scf.client;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import cg.zz.scf.client.proxy.ServiceProxy;
import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;
import cg.zz.scf.serializer.serializer.Serializer;

public class SCFInit {
	
	/**
	 * 配置文件路径
	 */
	protected static String DEFAULT_CONFIG_PATH;
	
	private static ILog logger = LogFactory.getLogger(SCFInit.class);
	
	static {
		DEFAULT_CONFIG_PATH = System.getProperty("scf.client.config.path");
		if (DEFAULT_CONFIG_PATH == null) {
			DEFAULT_CONFIG_PATH = System.getProperty("scf.config.path");
		}
		if (DEFAULT_CONFIG_PATH == null) {
			DEFAULT_CONFIG_PATH = getJarPath(SCFConst.class) + "/scf.config";
		}
		registerExcetEven();
	}
	
	/**
	 * 初始化信息
	 * @param configPath - 配置文件路径
	 * @param jarPaths - 要扫描的jar包
	 */
	@Deprecated
	public static void init(String configPath, String[] jarPaths) {
		DEFAULT_CONFIG_PATH = configPath;
		SCFConst.CONFIG_PATH = configPath;
		Serializer.SetJarPath(jarPaths);
		logger.debug("scf.config配置文件路径为:" + SCFConst.CONFIG_PATH);
	}
	
	/**
	 * 初始化信息
	 * @param configPath - 配置文件路径
	 */
	public static void init(String configPath) {
		DEFAULT_CONFIG_PATH = configPath;
		SCFConst.CONFIG_PATH = configPath;
		logger.debug("scf.config配置文件路径为:" + SCFConst.CONFIG_PATH);
	}
	
	/**
	 * 获得SCFConst Client包下的scfconfig的路径
	 * @param type - Class
	 * @return String
	 */
	private static String getJarPath(Class<?> type) {
		String path = type.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.replaceFirst("file:/", "");
		path = path.replaceAll("!/", "");
		path = path.replaceAll("\\\\", "/");
		path = path.substring(0, path.lastIndexOf("/"));
		if (path.substring(0, 1).equalsIgnoreCase("/")) {
			String osName = System.getProperty("os.name").toLowerCase();
			if (osName.indexOf("window") >= 0)
				path = path.substring(1);
		}
		try {
			return URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(SCFConst.class.getName()).log(Level.SEVERE, null, ex);
		}
		return path;
	}
	
	/**
	 * 注册系统停机的钩子
	 */
	private static void registerExcetEven() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				ServiceProxy.destroyAll();
			}
		});
	}

}
