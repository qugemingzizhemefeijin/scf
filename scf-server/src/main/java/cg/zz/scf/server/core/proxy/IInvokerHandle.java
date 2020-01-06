package cg.zz.scf.server.core.proxy;

import cg.zz.scf.server.contract.context.SCFContext;

/**
 * 执行器接口
 * @author chengang
 *
 */
public interface IInvokerHandle {
	
	/**
	 * 调用方法
	 * @param context - SCFContext
	 * @throws Exception
	 */
	public void invoke(SCFContext context) throws Exception;

}
