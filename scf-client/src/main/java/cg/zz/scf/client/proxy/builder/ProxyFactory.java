package cg.zz.scf.client.proxy.builder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理工厂，这里维护的是接口的 URL地址与接口动态代理生成的实体对象的映射关系，真正的远程调用在ServiceProxy中生成对应的
 * @author chengang
 *
 */
public class ProxyFactory {
	
	/**
	 * key=接口URL，如：tcp://mimi/AppLogService，value=JDK动态代理后生成的类实体，实际调用的是ProxyStandard的invoke方法中实现的。
	 */
	private static Map<String , Object> cache = new ConcurrentHashMap<>();
	
	/**
	 * Factory for Proxy，这里就是服务初始化的入口
	 * @param type - 服务类类型
	 * @param strUrl - 服务地址
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> type, String strUrl) {
		String key = strUrl.toLowerCase();
		Object proxy = null;
		if (cache.containsKey(key)) {
			proxy = cache.get(key);
		}
		if (proxy == null) {
			//创建代理类，这里应该可以加个锁，毕竟这样子更符合单例
			proxy = createStandardProxy(strUrl, type);
			if (proxy != null) {
				cache.put(key, proxy);
			}
		}
		
		return (T)proxy;
	}
	
	/**
	 * Factory for Proxy
	 * @param type - 服务类类型
	 * @param strUrl - 服务地址
	 * @param SerVersion - 版本
	 * @return T
	 */
	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> type, String strUrl, String SerVersion) {
		String key = strUrl.toLowerCase();
		Object proxy = null;
		if (cache.containsKey(key)) {
			proxy = cache.get(key);
		}
		if (proxy == null) {
			//创建代理类，这里应该可以加个锁，毕竟这样子更符合单例
			proxy = createStandardProxy(strUrl, type, SerVersion);
			if (proxy != null) {
				cache.put(key, proxy);
			}
		}
		
		return (T)proxy;
	}
	
	/**
	 * 创建代理类
	 * @param strUrl - 服务地址，如：tcp://mimi/AppLogService
	 * @param type - 代理类类型
	 * @return Object
	 */
	private static Object createStandardProxy(String strUrl, Class<?> type) {
		//服务名称 , 接口名称
		String serviceName = null , lookup = null;
		
		//从strUrl中解析出服务名和方法名，如注释中的则解析出 serviceName=mimi，lookup=AppLogService
		strUrl = strUrl.replace("tcp://", "");
		String[] splits = strUrl.split("/");
		if (splits.length == 2) {
			serviceName = splits[0];
			lookup = splits[1];
		}
		
		if(serviceName == null || lookup == null) {
			throw new RuntimeException("resolve service error , strUrl = " + strUrl + ",serviceName = " + serviceName + " , lookup = " + lookup);
		}
		
		//通过JDK动态代理生成代理对象，这里生成的代理对象还不是远程代理对象，只是简单的服务调用代理对象。。
		//真正的远程代理对象在里面的MethodCaller中的doMethodCall方法中会获取。
		InvocationHandler handler = new ProxyStandard(type, serviceName, lookup);
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { type },handler);
	}
	
	/**
	 * 创建代理类
	 * @param strUrl - 服务地址
	 * @param type - 代理类类型
	 * @param SerVersion - 服务版本
	 * @return Object
	 */
	private static Object createStandardProxy(String strUrl, Class<?> type, String SerVersion) {
		//服务名称 , 接口名称
		String serviceName = null , lookup = null;
		
		//从strUrl中解析出服务名和方法名，如注释中的则解析出 serviceName=mimi，lookup=AppLogService
		strUrl = strUrl.replace("tcp://", "");
		String[] splits = strUrl.split("/");
		if (splits.length == 2) {
			serviceName = splits[0];
			lookup = splits[1];
		}
		
		if(serviceName == null || lookup == null) {
			throw new RuntimeException("resolve service error , strUrl = " + strUrl + ",serviceName = " + serviceName + " , lookup = " + lookup);
		}
		
		//通过JDK动态代理生成代理对象，这里生成的代理对象还不是远程代理对象，只是简单的服务调用代理对象。。
		//真正的远程代理对象在里面的MethodCaller中的doMethodCall方法中会获取。
		InvocationHandler handler = new ProxyStandard(type, serviceName, lookup, SerVersion);
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { type },handler);
	}

}
