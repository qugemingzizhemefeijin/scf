package cg.zz.test.client;

import cg.zz.scf.client.proxy.builder.ProxyFactory;
import cg.zz.scf.test.server.contract.IAppLogService;

public class Test {
	
	static {
		String path = Test.class.getClassLoader().getResource("").getPath();
		if(path.startsWith("/")) {
			path = path.substring(1);
		}
		
		//必须要设置此值
		System.setProperty("scf.client.config.path", path + "scf.config");
	}
	
	public static IAppLogService getAppLogService() {
		String url = "tcp://mimi/AppLogService";
		IAppLogService service = ProxyFactory.create(IAppLogService.class, url);
		return service;
	}

	public static void main(String[] args) throws Exception {
		System.err.println("服务开始啦");
		IAppLogService log = getAppLogService();
		System.err.println(log.loadByID(100L));
		System.err.println(log.loadByID(100L));
		System.err.println(log.loadByID(100L));
		System.err.println(log.loadByID(100L));
		System.err.println(log.loadByID(100L));
		System.in.read();
	}

}
