package cg.zz.scf.server.contract.context;

import cg.zz.scf.server.util.ServiceFrameException;

/**
 * 代理接口
 * @author chengang
 *
 */
public abstract interface IProxyStub {
	
	public abstract SCFResponse invoke(SCFContext paramSCFContext) throws ServiceFrameException;

}
