package cg.zz.scf.serializer.component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;

import cg.zz.scf.serializer.component.helper.ClassHelper;
import cg.zz.scf.serializer.component.helper.FileHelper;
import cg.zz.scf.serializer.component.helper.StrHelper;
import cg.zz.scf.serializer.serializer.Serializer;

/**
 * Class扫描器
 * @author chengang
 *
 */
@SuppressWarnings("deprecation")
public class ClassScaner {
	
	/**
	 * 当前线程的类加载器
	 */
	private ClassLoader cl = Thread.currentThread().getContextClassLoader();
	
	/**
	 * 类文件中包含的关键字
	 */
	private static final String KEY_WORD = "SCFSerializable";
	
	/**
	 * 扫描指定的包路径
	 * @param basePackages - 包路径
	 * @return Set<Class<?>>
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public Set<Class<?>> scan(String... basePackages) throws URISyntaxException, IOException, ClassNotFoundException {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		if (basePackages != null && basePackages.length > 0 && !StrHelper.isEmptyOrNull(basePackages[0])) {
			for (String pack : basePackages) {
				classes.addAll(scanByPakage(pack));
			}
		} else if ((Serializer.JarPath != null) && (Serializer.JarPath.length > 0)) {
			System.err.println("指定JarPath路径扫描Jar包模式已经过时，请在启动vm参数中设置scf.serializer.basepakage。");
			      for (String path : Serializer.JarPath)
				      classes.addAll(scanByJarPath(path));
		} else {
			System.err.println("开始扫描全部引用jar包，如果扫描过程过长请在启动vm参数中设置scf.serializer.basepakage或者设置scf.serializer.scantype=asyn使用异步模式扫描。");
			classes.addAll(scanByURLClassLoader());
			if (classes.size() == 0) {
				classes.addAll(scanByJarPath(ClassHelper.getJarPath(ClassScaner.class)));
			}
		}
		return classes;
	}
	
	/**
	 * 扫描指定的包，获得所有的Class
	 * @param pack - 包路径
	 * @return Set<Class<?>>
	 * @throws URISyntaxException
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 * @throws ClassNotFoundException
	 */
	public Set<Class<?>> scanByPakage(String pack) throws URISyntaxException, MalformedURLException, FileNotFoundException, ClassNotFoundException {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		
		try {
			Enumeration<URL> dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
			while (dirs.hasMoreElements()) {
				URL url = dirs.nextElement();
				String protocol = url.getProtocol();
				
				if ("file".equals(protocol)) {
					getClassFromURL(url, pack, classes);
				} else {
					if (!"jar".equals(protocol)) continue;
					try {
						JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
						classes.addAll(ClassHelper.GetClassFromJar(jar, KEY_WORD, pack));
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return classes;
	}
	
	/**
	 * 将指定的jarPath目录下的所有的jar文件内的类扫描出来
	 * @param jarPath - String
	 * @return Set<Class<?>>
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Set<Class<?>> scanByJarPath(String jarPath) throws IOException, ClassNotFoundException {
		System.out.println("jarPath:" + jarPath);
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		List<File> jarFiles = FileHelper.getFiles(jarPath, new String[] { "jar" });
		if (jarFiles == null)
			System.err.println("No find jar from path:" + jarPath);
		else {
			for (File f : jarFiles) {
				classes.addAll(ClassHelper.GetClassFromJar(f.getPath(), KEY_WORD));
			}
		}
		return classes;
	}
	
	/**
	 * 从ClassPath中读取所有的Class
	 * @return Set<Class<?>>
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private Set<Class<?>> scanByURLClassLoader() throws URISyntaxException, IOException, ClassNotFoundException {
		Set<Class<?>> classes = new LinkedHashSet<Class<?>>();
		URL[] urlAry = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
		for (URL url : urlAry) {
			if (!url.getPath().equalsIgnoreCase("/")) {
				System.out.println("scanByURLClassLoader:" + URLDecoder.decode(url.getPath(), "utf-8"));
				if (url.getPath().endsWith(".jar"))
					classes.addAll(ClassHelper.GetClassFromJar(URLDecoder.decode(url.getPath(), "utf-8"), KEY_WORD));
				else {
					getClassFromURL(url, "", classes);
				}
			}
		}
		return classes;
	}
	
	/**
	 * 读取jar的Url中的所有的类放入到Set中
	 * @param url - URL
	 * @param basePak - 要扫描的基础包
	 * @param classes - Set<Class<?>>
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void getClassFromURL(URL url, String basePak, Set<Class<?>> classes) throws MalformedURLException, URISyntaxException, FileNotFoundException, IOException, ClassNotFoundException {
		if (url == null) {
			System.err.println("url is null when getClassFromURL");
			return;
		}
		String path = URLDecoder.decode(url.getPath(), "utf-8");
		if ((path == null) || (path.equalsIgnoreCase(""))) {
			System.err.println("path is null when getClassFromURL (url:" + url + ")");
			return;
		}
		
		File f = new File(path);
		if (f.isDirectory()) {
			List<File> files = FileHelper.getFiles(f.getAbsolutePath(), new String[] { "class" });
			for (File file : files) {
				Class<?> c = getClassFromFile(file, url, basePak);
				if (c != null) classes.add(c);
			}
		} else if (f.getName().endsWith(".class")) {
			Class<?> c = getClassFromFile(f, url, basePak);
			if (c != null) classes.add(c);
		}
	}
	
	/**
	 * 从文件中加载Class
	 * @param f - File
	 * @param baseURL - 文件所在的URL
	 * @param basePak - 类所在的基础包
	 * @return Class<?>
	 * @throws ClassNotFoundException
	 * @throws URISyntaxException
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Class<?> getClassFromFile(File f, URL baseURL, String basePak) throws ClassNotFoundException, URISyntaxException, FileNotFoundException, IOException {
		if (!isSerializable(f)) {
			return null;
		}
		
		String filePath = f.getAbsolutePath();
		filePath = filePath.replace("\\", ".");
		String dirPath = baseURL.toURI().getPath();
		if (dirPath.startsWith("/")) {
			dirPath = dirPath.substring(1);
		}
		dirPath = dirPath.replace("/", ".");
		filePath = filePath.replace(dirPath, "");
		if (filePath.endsWith(".class")) {
			filePath = filePath.substring(0, filePath.length() - ".class".length());
		}
		Class<?> c = this.cl.loadClass(basePak + filePath);
		return c;
	}
	
	/**
	 * 查看文件是否是可以序列化
	 * @param f - File
	 * @return boolean
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static boolean isSerializable(File f) throws FileNotFoundException, IOException {
		if (!f.getAbsolutePath().endsWith(".class")) {
			return false;
		}
		boolean result = false;
		StringBuffer sb = new StringBuffer();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(f);
			br = new BufferedReader(fr);
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				sb.append(line);
				if (sb.indexOf(KEY_WORD) > -1) result = true;
			}
		} finally {
			if (fr != null) fr.close();
			if (br != null) br.close();
		}
		
		return result;
	}

}
