package cg.zz.scf.client.proxy.builder;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import cg.zz.scf.client.utility.logger.ILog;
import cg.zz.scf.client.utility.logger.LogFactory;

public class ProxyStandard implements InvocationHandler, Serializable, IProxyStandard {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5767110984962101022L;
	
	private Class<?> interfaceClass;
	private MethodCaller methodCaller;
	private String lookup;
	private ILog logger = LogFactory.getLogger(ProxyStandard.class);
	
	public ProxyStandard(Class<?> interfaceClass, String serviceName, String lookup) {
		this.lookup = lookup;
		this.interfaceClass = interfaceClass;
		this.methodCaller = new MethodCaller(serviceName, lookup);
	}
	
	public ProxyStandard(Class<?> interfaceClass, String serviceName, String lookup, String serVersion) {
		this.lookup = lookup;
		this.interfaceClass = interfaceClass;
		this.methodCaller = new MethodCaller(serviceName, lookup, serVersion);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		long start = System.currentTimeMillis();
		Object obj = null;
		obj = this.methodCaller.doMethodCall(args, method);
		long end = System.currentTimeMillis();
		long total = end - start;
		if (total > 200L) {
			this.logger.warn("interface:" + this.interfaceClass.getName() + ";class:" + this.lookup + ";method:" + method.getName() + ";invoke time :" + total);
		}
		return obj;
	}

}
