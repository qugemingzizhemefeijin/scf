package cg.zz.scf.server.deploy.hotdeploy;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * class工具类
 * @author chengang
 *
 */
public final class ClassHelper {
	
	/**
	 * 获得指定jar包下的所有类的Class对象
	 * @param jarPath - jar路径
	 * @param regex - 匹配正则[暂时无用]
	 * @return Set<Class<?>>
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Set<Class<?>> getClassFromJar(String jarPath, String[] regex) throws IOException,ClassNotFoundException {
		JarFile jarFile = new JarFile(jarPath);
		Enumeration<JarEntry> entries = jarFile.entries();
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = (JarEntry) entries.nextElement();
			String name = jarEntry.getName();
			if (name.endsWith(".class")) {
				String className = name.replaceAll(".class", "").replaceAll("/", ".");
				Class<?> type = null;
				try {
					type = Class.forName(className);
				} catch (Throwable localThrowable) {
				}
				if (type != null) {
					classes.add(type);
				}
			}
		}
		jarFile.close();
		return classes;
	}

}
