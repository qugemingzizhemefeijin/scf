package cg.zz.scf.test.server.components;

import cg.zz.scf.server.contract.annotation.ServiceBehavior;
import cg.zz.scf.test.server.contract.IAppLogService;

@ServiceBehavior
public class AppLogService implements IAppLogService {

	@Override
	public int loadByID(long id) throws Exception {
		System.err.println("==============");
		return 1000;
	}

}
