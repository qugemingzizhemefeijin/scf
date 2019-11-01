package cg.zz.scf.client.proxy.builder;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;

/**
 * 服务接口请求生成的动态代理类真正执行的代理对象
 * @author chengang
 *
 */
public class ProxyStandard implements InvocationHandler, Serializable, IProxyStandard {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5767110984962101022L;
	
	private Class<?> interfaceClass;
	private MethodCaller methodCaller;
	private String lookup;
	private ILog logger = LogFactory.getLogger(ProxyStandard.class);
	
	/**
	 * 构造动态代理对象
	 * @param interfaceClass - 代理的接口
	 * @param serviceName - 服务名称
	 * @param lookup - 接口名称
	 */
	public ProxyStandard(Class<?> interfaceClass, String serviceName, String lookup) {
		this.lookup = lookup;
		this.interfaceClass = interfaceClass;
		this.methodCaller = new MethodCaller(serviceName, lookup);
	}
	
	/**
	 * 构造动态代理对象
	 * @param interfaceClass - 代理的接口
	 * @param serviceName - 服务名称
	 * @param lookup - 接口名称
	 * @param serVersion - 接口版本，默认是SCF，也可以指定SCFV2
	 */
	public ProxyStandard(Class<?> interfaceClass, String serviceName, String lookup, String serVersion) {
		this.lookup = lookup;
		this.interfaceClass = interfaceClass;
		this.methodCaller = new MethodCaller(serviceName, lookup, serVersion);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		long start = System.currentTimeMillis();
		Object obj = null;
		//调用真正的代理
		obj = this.methodCaller.doMethodCall(args, method);
		long end = System.currentTimeMillis();
		long total = end - start;
		//如果代理方法执行超过了200毫秒，则发出日志警告
		if (total > 200L) {
			this.logger.warn("interface:" + this.interfaceClass.getName() + ";class:" + this.lookup + ";method:" + method.getName() + ";invoke time :" + total);
		}
		return obj;
	}

}
