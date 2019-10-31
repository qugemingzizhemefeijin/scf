package cg.zz.scf.server.deploy.hotdeploy;

import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.IProxyFactory;
import cg.zz.scf.server.deploy.bytecode.CreateManager;

/**
 * 装载并且实例化代理工厂
 * @author chengang
 *
 */
public class ProxyFactoryLoader {
	
	/**
	 * 获得一个代理工厂对象
	 * @param classLoader - DynamicClassLoader
	 * @return IProxyFactory
	 * @throws Exception
	 */
	public static IProxyFactory loadProxyFactory(DynamicClassLoader classLoader) throws Exception {
		String serviceRootPath = Global.getInstance().getRootPath() + "service/deploy/" + Global.getInstance().getServiceConfig().getString("scf.service.name");
		
		CreateManager cm = new CreateManager();
		return cm.careteProxy(serviceRootPath,classLoader);
	}

}
