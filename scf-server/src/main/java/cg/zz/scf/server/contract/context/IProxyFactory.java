package cg.zz.scf.server.contract.context;

/**
 * 代理工厂
 * @author chengang
 *
 */
public abstract interface IProxyFactory {
	
	/**
	 * 获得一个代理方法
	 * @param lookup - 获得一个指定的代理
	 * @return IProxyStub
	 */
	public abstract IProxyStub getProxy(String lookup);

}
