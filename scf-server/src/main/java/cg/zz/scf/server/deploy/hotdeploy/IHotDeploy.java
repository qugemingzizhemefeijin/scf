package cg.zz.scf.server.deploy.hotdeploy;

import cg.zz.scf.server.core.proxy.IInvokerHandle;

/**
 * 热部署加载执行接口
 * @author chengang
 *
 */
public interface IHotDeploy {
	
	/**
	 * 设置同步的调用执行器
	 * @param handle - IInvokerHandle
	 */
	public abstract void setSyncInvokerHandle(IInvokerHandle handle);

	/**
	 * 设置异步的调用执行器
	 * @param handle - IInvokerHandle
	 */
	public abstract void setAsyncInvokerHandle(IInvokerHandle handle);

}
