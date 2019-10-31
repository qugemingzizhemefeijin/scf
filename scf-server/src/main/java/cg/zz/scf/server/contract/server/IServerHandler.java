package cg.zz.scf.server.contract.server;

import cg.zz.scf.server.contract.context.SCFContext;

/**
 * 服务器处理器类
 * @author chengang
 *
 */
public abstract interface IServerHandler {
	
	/**
	 * 写入返回信息
	 * @param context - SCFContext
	 */
	public abstract void writeResponse(SCFContext context);

}
