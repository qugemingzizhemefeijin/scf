package cg.zz.scf.test.server.contract;

import cg.zz.scf.server.contract.annotation.OperationContract;
import cg.zz.scf.server.contract.annotation.ServiceContract;

@ServiceContract
public interface IAppLogService {
	
	@OperationContract
	public int loadByID(long id) throws Exception;

}
