package cg.zz.scf.server.deploy.filemonitor;

import cg.zz.scf.serializer.component.helper.TypeHelper;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.IProxyFactory;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.deploy.hotdeploy.DynamicClassLoader;
import cg.zz.scf.server.deploy.hotdeploy.ProxyFactoryLoader;

/**
 * 热部署监听器
 * @author chengang
 *
 */
public class HotDeployListener implements IListener {
	
	private static ILog logger = LogFactory.getLogger(HotDeployListener.class);

	@Override
	public void fileChanged(FileInfo fileInfo) {
		logger.info("service file is change!!! ");
		
		try {
			logger.info("begin hot deploy scf...");
			
			DynamicClassLoader classLoader = new DynamicClassLoader();
			classLoader.addFolder(new String[] {
				Global.getInstance().getRootPath() + "service/deploy/" + Global.getInstance().getServiceConfig() .getString("scf.service.name") + "/",
				Global.getInstance().getRootPath() + "service/lib/",
				Global.getInstance().getRootPath() + "lib" });
			
			IProxyFactory proxyFactory = ProxyFactoryLoader.loadProxyFactory(classLoader);
			if (proxyFactory != null) {
				Global.getInstance().setProxyFactory(proxyFactory);
				logger.info("change context class loader");
				Thread.currentThread().setContextClassLoader(proxyFactory.getClass().getClassLoader());
				logger.info("init serializer type map");
				TypeHelper.InitTypeMap();
				logger.info("notice gc");
				System.gc();
				logger.info("hot deploy service success!!!");
			} else {
				logger.error("IInvokerHandle is null when hotDeploy!!!");
			}
			
			logger.info("finish hot deploy!!!");
		} catch (Exception e) {
			logger.error("create proxy error", e);
		}
	}

}
