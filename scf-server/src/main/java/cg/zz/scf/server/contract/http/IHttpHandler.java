package cg.zz.scf.server.contract.http;

import cg.zz.scf.server.contract.annotation.OperationContract;
import cg.zz.scf.server.contract.annotation.ServiceContract;

/**
 * Http操作接口
 * @author chengang
 *
 */
@ServiceContract
public abstract interface IHttpHandler {
	
	/**
	 * get请求
	 * @param context - HttpContext
	 */
	@OperationContract
	public abstract void get(HttpContext context);

	/**
	 * post请求
	 * @param context - HttpContext
	 */
	@OperationContract
	public abstract void post(HttpContext context);

	/**
	 * put请求
	 * @param context - HttpContext
	 */
	@OperationContract
	public abstract void put(HttpContext context);

	/**
	 * delete请求
	 * @param context - HttpContext
	 */
	@OperationContract
	public abstract void delete(HttpContext context);

	/**
	 * head请求
	 * @param context - HttpContext
	 */
	@OperationContract
	public abstract void head(HttpContext context);

}
