package cg.zz.scf.server.contract.filter;

import cg.zz.scf.server.contract.context.SCFContext;

/**
 * 过滤器接口
 * @author chengang
 *
 */
public abstract interface IFilter {
	
	/**
	 * 执行优先级
	 * @return int
	 */
	public abstract int getPriority();
	
	/**
	 * 过滤方法
	 * @param context - SCFContext
	 * @throws Exception
	 */
	public abstract void filter(SCFContext context) throws Exception;

}
