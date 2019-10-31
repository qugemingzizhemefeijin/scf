package cg.zz.scf.server.deploy.hotdeploy;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.util.FileHelper;
import sun.misc.Launcher;

/**
 * 用于加载jar包
 * @author chengang
 *
 */
@SuppressWarnings("restriction")
public class GlobalClassLoader {
	
	private static ILog logger = LogFactory.getLogger(GlobalClassLoader.class);
	
	/**
	 * 反射获取URLClassLoader的addUrl方法句柄
	 */
	private static Method addURL;
	
	private static URLClassLoader system;
	
	private static URLClassLoader ext;
	
	static{
		try {
			addURL = URLClassLoader.class.getDeclaredMethod("addURL",new Class[] {URL.class });
		} catch (Exception e) {
			e.printStackTrace();
		}
		addURL.setAccessible(true);
		
		system = (URLClassLoader)getSystemClassLoader();
		ext = (URLClassLoader)getExtClassLoader();
	}
	
	/**
	 * 返回系统类加载器
	 * @return ClassLoader
	 */
	public static ClassLoader getSystemClassLoader() {
		return ClassLoader.getSystemClassLoader();
	}
	
	/**
	 * 返回委托的父类加载器。
	 * @return ClassLoader
	 */
	public static ClassLoader getExtClassLoader() {
		return getSystemClassLoader().getParent();
	}
	
	/**
	 * 将指定的 URL 添加到 URL 列表中，以便搜索类和资源。反射调用addURL方法
	 * @param url - 资源路径
	 * @throws Exception
	 */
	public static void addURL2SystemClassLoader(URL url) throws Exception {
		try {
			logger.info("append jar to classpath:" + url.toString());
			addURL.invoke(system, new Object[] { url });
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 将指定的 URL 添加到 URL 列表中，以便搜索类和资源。反射调用addURL方法
	 * @param url - 资源路径
	 * @throws Exception
	 */
	public static void addURL2ExtClassLoader(URL url) throws Exception {
		try {
			logger.info("append jar to classpath:" + url.toString());
			addURL.invoke(ext, new Object[] { url });
		} catch (Exception e) {
			throw e;
		}
	}
	
	/**
	 * 将文件路径的转换成URL添加到URL列表中，以便搜索类和资源。
	 * @param path - 文件路径
	 * @throws Exception
	 */
	public static void addSystemClassPath(String path) throws Exception {
		try {
			URL url = new URL("file", "", path);
			addURL2SystemClassLoader(url);
		} catch (MalformedURLException e) {
			throw e;
		}
	}
	
	/**
	 * 将文件路径的转换成URL添加到URL列表中，以便搜索类和资源。
	 * @param path - 文件路径
	 * @throws Exception
	 */
	public static void addExtClassPath(String path) throws Exception {
		try {
			URL url = new URL("file", "", path);
			addURL2ExtClassLoader(url);
		} catch (MalformedURLException e) {
			throw e;
		}
	}
	
	/**
	 * 将多个目录下的资源加载到虚拟机
	 * @param dirs - 目录
	 * @throws Exception
	 */
	public static void addSystemClassPathFolder(String... dirs) throws Exception {
		List<String> jarList = FileHelper.getUniqueLibPath(dirs);
		for (String jar : jarList) {
			addSystemClassPath(jar);
		}
	}
	
	/**
	 * 将多个目录下的资源加载到虚拟机
	 * @param dirs - 目录
	 * @throws Exception
	 */
	public static void addURL2ExtClassLoaderFolder(String... dirs) throws Exception {
		List<String> jarList = FileHelper.getUniqueLibPath(dirs);
		for (String jar : jarList) {
			addExtClassPath(jar);
		}
	}
	
	public static URL[] getBootstrapURLs() {
		return Launcher.getBootstrapClassPath().getURLs();
	}
	
	/**
	 * 返回用于加载类和资源的 URL 搜索路径。
	 * @return URL[]
	 */
	public static URL[] getSystemURLs() {
		return system.getURLs();
	}
	
	/**
	 * 返回用于加载类和资源的 URL 搜索路径。
	 * @return URL[]
	 */
	public static URL[] getExtURLs() {
		return ext.getURLs();
	}

}
