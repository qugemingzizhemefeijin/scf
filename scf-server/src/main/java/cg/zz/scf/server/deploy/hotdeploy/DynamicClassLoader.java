package cg.zz.scf.server.deploy.hotdeploy;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.util.FileHelper;

/**
 * 类动态加载器
 * @author chengang
 *
 */
public class DynamicClassLoader extends SecureClassLoader {
	
	/**
	 * 日志工具
	 */
	private static ILog logger = LogFactory.getLogger(DynamicClassLoader.class);
	
	/**
	 * jar集合
	 */
	private static List<String> jarList = new ArrayList<String>();
	
	/**
	 * 类的缓存集合
	 */
	private Map<String, Class<?>> classCache = new HashMap<String , Class<?>>();
	
	/**
	 * 默认构造函数
	 */
	public DynamicClassLoader() {
		
	}
	
	/**
	 * 指定委托的父类加载器的构造函数
	 * @param parent - 用于委托的父类加载器
	 */
	public DynamicClassLoader(ClassLoader parent) {
		super(parent);
	}
	
	/**
	 * 根据jar包路径和类名称装载类信息
	 * @param jarPath - jar包路径
	 * @param className - 类名称
	 * @param fromCache - 是否从缓存中查找
	 * @return Class<?>
	 * @throws ClassNotFoundException
	 */
	public Class<?> findClass(String jarPath, String className, boolean fromCache) throws ClassNotFoundException {
		logger.debug("find class jarPath: " + jarPath + "  className: " + className + "  fromCache:" + fromCache);
		
		//判断是否需要从缓存中加载类
		if (fromCache && this.classCache.containsKey(className)) {
			return this.classCache.get(className);
		}
		
		String classPath = className.replace('.', '/').concat(".class");
		byte[] clsByte = null;
		
		if(jarPath == null || jarPath.equals("")) {
			for(String jp : jarList) {
				jarPath = jp;
				clsByte = getClassByte(jp, classPath);
				if(clsByte != null) {
					break;
				}
			}
		} else {
			clsByte = getClassByte(jarPath, classPath);
		}
		
		if (clsByte == null) {
			throw new ClassNotFoundException(className);
		}
		
		URL url = null;
		try {
			url = new URL("file", "", jarPath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return findClass(className, clsByte, url);
	}
	
	/**
	 * 使用指定的二进制名称查找类以及类 的字节码和jar包路径找到类
	 * @param className - 类名称
	 * @param clsByte - 字节码
	 * @param url - jar包路径
	 * @return Class<?>
	 */
	public Class<?> findClass(String className, byte[] clsByte, URL url) {
		Class<?> cls = null;
		try {
			CodeSource cs = new CodeSource(url, (java.security.cert.Certificate[]) null);
			ProtectionDomain pd = new ProtectionDomain(cs, null, this, null);
			cls = super.defineClass(className, clsByte, 0, clsByte.length, pd);
			resolveClass(cls);
			classCache.put(className, cls);
		} catch (Exception e) {
			logger.error("define class error" , e);
		}
		
		return cls;
	}
	
	/**
	 * 装载类
	 * @param jarPath - jar包路径
	 * @param className - 类名称
	 * @return Class<?>
	 * @throws ClassNotFoundException
	 */
	public Class<?> findClass(String jarPath, String className) throws ClassNotFoundException {
		return findClass(jarPath, className, true);
	}
	
	/**
	 * 装载类
	 */
	public Class<?> findClass(String className) throws ClassNotFoundException {
		return findClass("", className, true);
	}
	
	/**
	 * 装载类
	 * @param className - 类名称
	 * @param fromCache - 是否从缓存中查找
	 * @return Class<?>
	 * @throws ClassNotFoundException
	 */
	public Class<?> findClass(String className, boolean fromCache) throws ClassNotFoundException {
		return findClass("", className, fromCache);
	}
	
	/**
	 * 清空所有的缓存
	 */
	public void clearAllClassCache(){
		logger.info("clear class cache:");
		try {
			Iterator<Entry<String, Class<?>>> it = classCache.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, Class<?>> entry = it.next();
				logger.debug("-----key:" + entry.getKey() + "  value:" + entry.getValue().getName());
			}
		} catch(Exception ex) {
			logger.error(ex);
		}
		classCache.clear();
	}
	
	/**
	 * 将指定url添加到jarList中
	 * @param url - jar地址
	 */
	public void addURL(String url) {
		if(!jarList.contains(url)){
			jarList.add(url);
		}
	}
	
	/**
	 * 添加指定目录下的所有的jar到jarList中
	 * @param dirs - 目录地址
	 * @throws IOException
	 */
	public void addFolder(String... dirs) throws IOException {
		List<String> jarList = FileHelper.getUniqueLibPath(dirs);
		for(String jar : jarList) {
			addURL(jar);
		}
	}
	
	public List<String> getJarList(){
		return jarList;
	}
	
	/**
	 * 获取jar包的内的Class的字节码
	 * @param jarPath - jar文件路径
	 * @param classPath - Class类路径
	 * @return byte[]
	 */
	private byte[] getClassByte(String jarPath, String classPath) {
		JarFile jarFile = null;
		InputStream input = null;
		byte[] clsByte = null;
		
		try {
			jarFile = new JarFile(jarPath);  // read jar
			JarEntry entry = jarFile.getJarEntry(classPath); // read class file
			if(entry != null) {
				logger.debug("get class:" + classPath + "  from:" + jarPath);
				input = jarFile.getInputStream(entry);
				clsByte = new byte[input.available()];
				input.read(clsByte);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(input != null) {
				try{input.close();}catch(IOException e) {e.printStackTrace();}
			}
			if(jarFile != null) {
				try{jarFile.close();}catch(IOException e) {e.printStackTrace();}
			}
		}
		
		return clsByte;
	}

}
