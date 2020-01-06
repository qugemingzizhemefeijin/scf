package cg.zz.scf.serializer.component.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 主要用于扫描Jar包，获取Class类集合，判断Class类是否实现了指定的接口等工具方法
 * @author chengang
 *
 */
public final class ClassHelper {
	
	private static final Map<String , Class<?>> BASIC_CLASS_MAP = new HashMap<>();
	
	static {
		BASIC_CLASS_MAP.put("boolean", Boolean.class);
		BASIC_CLASS_MAP.put("char", Character.class);
		BASIC_CLASS_MAP.put("byte", Byte.class);
		BASIC_CLASS_MAP.put("short", Short.class);
		BASIC_CLASS_MAP.put("int", Integer.class);
		BASIC_CLASS_MAP.put("long", Long.class);
		BASIC_CLASS_MAP.put("float", Float.class);
		BASIC_CLASS_MAP.put("double", Double.class);
	}
	
	/**
	 * 从jar包中获取所有的类Class
	 * @param jarPath - jar路径
	 * @return Set<Class<?>>
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Set<Class<?>> GetClassFromJar(String jarPath) throws IOException, ClassNotFoundException {
		JarFile jarFile = new JarFile(jarPath);
		return GetClassFromJar(jarFile, "", "");
	}

	/**
	 * 从jar包中获取所有的类Class
	 * @param jarPath - jar路径
	 * @param keyword - jar中文件包含的关键字
	 * @return Set<Class<?>>
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Set<Class<?>> GetClassFromJar(String jarPath, String keyword) throws IOException, ClassNotFoundException {
		JarFile jarFile = new JarFile(jarPath);
		return GetClassFromJar(jarFile, keyword, "");
	}

	/**
	 * 从jar包中获取所有的类Class
	 * @param jarFile - jar路径
	 * @param keyword - jar中文件包含的关键字
	 * @param basePakage - 要扫描的基础包。如com.pv.util中的包
	 * @return Set<Class<?>>
	 */
	public static Set<Class<?>> GetClassFromJar2(JarFile jarFile, String keyword, String basePakage) {
		String packageDirName = basePakage.replace('.', '/');
		Enumeration<JarEntry> entries = jarFile.entries();
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		while (entries.hasMoreElements()) {
			try {
				JarEntry jarEntry = (JarEntry) entries.nextElement();
				String name = jarEntry.getName();
				if (name.charAt(0) == '/') {
					name = name.substring(1);
				}
				if (!StrHelper.isEmptyOrNull(packageDirName) && !name.startsWith(packageDirName)) {
					continue;
				}
				if (!name.endsWith(".class") || name.contains("com/bj58/spat/scf/serializer") || !checkJarEntry(jarFile, jarEntry, keyword))
					continue;
				String className = name.replaceAll(".class", StrHelper.EmptyString).replaceAll("/", ".");
				Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(className);

				if (type != null) {
					classes.add(type);
				}
			} catch (Throwable ex) {
				ex.printStackTrace();
			}
		}
		return classes;
	}

	/**
	 * 从jar包中获取所有的类Class
	 * @param jarFile - jar路径
	 * @param keyword - jar中文件包含的关键字
	 * @param basePakage - 要扫描的基础包。如com.pv.util中的包
	 * @return Set<Class<?>>
	 */
	public static Set<Class<?>> GetClassFromJar(JarFile jarFile, String keyword, String basePakage) throws IOException {
		Boolean recursive = true;//是否递归
		String packageName = basePakage;
		String packageDirName = basePakage.replace('.', '/');
		Enumeration<JarEntry> entries = jarFile.entries();
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		while (entries.hasMoreElements()) {
			try {
				// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
				JarEntry entry = (JarEntry) entries.nextElement();
				String name = entry.getName();
				// 如果是以/开头的
				if (name.charAt(0) == '/') {
					// 获取后面的字符串
					name = name.substring(1);
				}
				// 如果前半部分和定义的包名相同
				if (name.startsWith(packageDirName)) {
					int idx = name.lastIndexOf('/');
					// 如果以"/"结尾 是一个包
					if (idx != -1) {
						// 获取包名 把"/"替换成"."
						packageName = name.substring(0, idx).replace('/', '.');
					}
					// 如果可以迭代下去 并且是一个包
					if ((idx != -1) || recursive) {
						// 如果是一个.class文件 而且不是目录
						if (name.endsWith(".class") && !entry.isDirectory()) {
							// 检测entry是否符合要求
							if (!ClassHelper.checkJarEntry(jarFile, entry, keyword)) {
								continue;
							}
							// 去掉后面的".class" 获取真正的类名
							String className = name.substring(packageName.length() + 1,name.length() - 6);
							try {
								// 添加到classes
								Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(packageName + "." + className);
								classes.add(c);
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (NoClassDefFoundError e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		return classes;
	}

	/**
	 * 检查jar包是否符合条件
	 * @param jarFile - jar文件
	 * @param entry - jar中的Class
	 * @param keyWord - 要检查的关键字
	 * @return boolean
	 * @throws IOException
	 */
	public static boolean checkJarEntry(JarFile jarFile, JarEntry entry, String keyWord) throws IOException {
		if ((keyWord == null) || (keyWord.equals(""))) {
			return true;
		}
		InputStream input = null;
		InputStreamReader isr = null;
		BufferedReader reader = null;
		try {
			input = jarFile.getInputStream(entry);
			isr = new InputStreamReader(input);
			reader = new BufferedReader(isr);
			StringBuffer sb = new StringBuffer();
			boolean result = false;
			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				sb.append(line);
				if (sb.indexOf(keyWord) > -1) {
					result = true;
				}
			}
			return result;
		} finally {
			if (input != null) {
				input.close();
			}
			if (isr != null) {
				isr.close();
			}
			if (reader != null)
				reader.close();
		}
	}

	/**
	 * 判断type是否实现了interfaceType接口
	 * @param type - Class<?>
	 * @param interfaceType - Class<?>
	 * @return boolean
	 */
	public static boolean InterfaceOf(Class<?> type, Class<?> interfaceType) {
		if (type == null) {
			return false;
		}
		Class<?>[] interfaces = type.getInterfaces();
		for (Class<?> c : interfaces) {
			if (c.equals(interfaceType)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 根据名称获得对应的Class
	 * 如果类型是基本类型，则返回与之对应的对象类型
	 * @param name - String
	 * @return Class<?>
	 * @throws ClassNotFoundException
	 */
	public static Class<?> GetClassForName(String name) throws ClassNotFoundException {
		Class<?> cls = BASIC_CLASS_MAP.get(name);
		if(cls != null) {
			return cls;
		}
		return Class.forName(name);
	}

	/**
	 * 根据Class获得他所属的jar的文件路径
	 * @param type - Class<?>
	 * @return String
	 */
	public static String getJarPath(Class<?> type) {
		String path = type.getProtectionDomain().getCodeSource().getLocation().getPath();
		path = path.replaceFirst("file:/", "");
		path = path.replaceAll("!/", "");
		path = path.replaceAll("\\\\", "/");
		path = path.substring(0, path.lastIndexOf("/"));
		if (path.substring(0, 1).equalsIgnoreCase("/")) {
			String osName = System.getProperty("os.name").toLowerCase();
			if (osName.indexOf("window") >= 0) path = path.substring(1);
		}
		try {
			return URLDecoder.decode(path, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(ClassHelper.class.getName()).log(Level.SEVERE, null, ex);
		}
		return path;
	}

	/**
	 * 获得当前jar包的名称
	 * @param c - Class
	 * @return String
	 */
	public static String getCurrJarName(Class<?> c) {
		String filePath = c.getProtectionDomain().getCodeSource().getLocation().getFile();

		filePath = filePath.replaceFirst("file:/", "");
		filePath = filePath.replaceAll("!/", "");
		filePath = filePath.replaceAll("\\\\", "/");
		filePath = filePath.substring(filePath.lastIndexOf("/") + 1);
		return filePath;
	}

}
