package cg.zz.scf.server.deploy.bytecode;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import cg.zz.scf.server.contract.context.IProxyFactory;
import cg.zz.scf.server.contract.context.IProxyStub;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.deploy.hotdeploy.DynamicClassLoader;

public class CreateManager {
	
	private static ILog logger = LogFactory.getLogger(CreateManager.class);
	
	/**
	 * 创建代理工厂
	 * @param serviceRootPath - 服务的根路径
	 * @param classLoader - DynamicClassLoader
	 * @return IProxyFactory
	 * @throws Exception
	 */
	public IProxyFactory careteProxy(String serviceRootPath, DynamicClassLoader classLoader) throws Exception {
		//此文件是配置需要扫描的接口方法，如果没有此文件则需要扫描所有的serviceRootPath下的jar包
		String configPath = serviceRootPath + "/" + "serviceframe.xml";
		File file = new File(configPath);
		ContractInfo serviceContract = null;
		
		if (file != null && file.exists()) {
			serviceContract = ContractConfig.loadContractInfo(configPath, classLoader);
		} else {
			//扫描注定目录下所有的jar包
			serviceContract = ScanClass.getContractInfo(serviceRootPath + "/", classLoader);
		}
		
		long time = System.currentTimeMillis();
		
		//创建所有的代理类
		List<ClassFile> localProxyList = new ProxyClassCreater().createProxy(classLoader, serviceContract, time);
		logger.info("proxy class buffer creater finish!!!");
		
		//创建代理工厂类
		ClassFile cfProxyFactory = new ProxyFactoryCreater().createProxy(classLoader, serviceContract, time);
		logger.info("proxy factory buffer creater finish!!!");
		
		//装载类并且创建每个代理类的实体
		List<IProxyStub> localProxyAry = new ArrayList<IProxyStub>();
		for (ClassFile cf : localProxyList) {
			Class<?> cls = classLoader.findClass(cf.getClsName(), cf.getClsByte(), null);
			logger.info("dynamic load class:" + cls.getName());
			
			localProxyAry.add((IProxyStub)cls.newInstance());
		}
		
		//装载并且实例化代理工厂类
		Class<?> proxyFactoryCls = classLoader.findClass(cfProxyFactory.getClsName(), cfProxyFactory.getClsByte(), null);
		Constructor<?> constructor = proxyFactoryCls.getConstructor(new Class[] { List.class });
		IProxyFactory pfInstance = (IProxyFactory)constructor.newInstance(new Object[] { localProxyAry });
		logger.info("crate ProxyFactory instance!!!");
		
		return pfInstance;
	}

}
