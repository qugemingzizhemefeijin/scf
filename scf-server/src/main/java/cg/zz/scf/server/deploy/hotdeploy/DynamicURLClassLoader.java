package cg.zz.scf.server.deploy.hotdeploy;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import cg.zz.scf.server.util.FileHelper;

/**
 * URLClassLoader动态类加载工具类
 * @author chengang
 *
 */
public class DynamicURLClassLoader {
	
	/**
	 * addURL的方法句柄
	 */
	private static Method addURL;
	
	/**
	 * 加载器
	 */
	private URLClassLoader classLoader;
	
	static {
		try {
			addURL = URLClassLoader.class.getDeclaredMethod("addURL", new Class[] { URL.class });
		} catch (Exception e) {
			e.printStackTrace();
		}
		addURL.setAccessible(true);
	}
	
	/**
	 * 构造函数
	 * @throws MalformedURLException
	 */
	public DynamicURLClassLoader() throws MalformedURLException {
		this.classLoader = new URLClassLoader(new URL[] { new URL("file", "", "") });
	}

	/**
	 * 添加一个URL到加载器中
	 * @param url - URL
	 * @throws Exception
	 */
	public void addURL(URL url) throws Exception {
		addURL.invoke(this.classLoader, new Object[] { url });
	}

	/**
	 * 添加一个jar文件路径到加载器中
	 * @param path - 文件路径
	 * @throws Exception
	 */
	public void addURL(String path) throws Exception {
		URL url = new URL("file", "", path);
		addURL(url);
	}

	/**
	 * 添加一个目录下的jar文件到类加载器中
	 * @param dirs - 目录
	 * @throws Exception
	 */
	public void addFolder(String[] dirs) throws Exception {
		List<String> jarList = FileHelper.getUniqueLibPath(dirs);
		for (String jar : jarList)
			addURL(jar);
	}

	/**
	 * 装载指定的类
	 * @param className - 类全名
	 * @return Class<?>
	 * @throws ClassNotFoundException
	 */
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		return this.classLoader.loadClass(className);
	}

}
