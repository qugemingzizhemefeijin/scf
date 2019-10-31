package cg.zz.scf.client.proxy.builder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代理工厂
 * @author chengang
 *
 */
public class ProxyFactory {
	
	private static Map<String , Object> cache = new ConcurrentHashMap<>();
	
	/**
	 * Factory for Proxy
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
			proxy = createStandardProxy(strUrl, type, SerVersion);
			if (proxy != null) {
				cache.put(key, proxy);
			}
		}
		
		return (T)proxy;
	}
	
	/**
	 * 创建代理类
	 * @param strUrl - 服务地址
	 * @param type - 代理类类型
	 * @return Object
	 */
	private static Object createStandardProxy(String strUrl, Class<?> type) {
		String serviceName = "";
		String lookup = "";
		strUrl = strUrl.replace("tcp://", "");
		String[] splits = strUrl.split("/");
		if (splits.length == 2) {
			serviceName = splits[0];
			lookup = splits[1];
		}
		
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
		String serviceName = "";
		String lookup = "";
		strUrl = strUrl.replace("tcp://", "");
		String[] splits = strUrl.split("/");
		if (splits.length == 2) {
			serviceName = splits[0];
			lookup = splits[1];
		}
		InvocationHandler handler = new ProxyStandard(type, serviceName, lookup, SerVersion);
		return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { type },handler);
	}

}
