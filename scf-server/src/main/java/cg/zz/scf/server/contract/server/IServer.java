package cg.zz.scf.server.contract.server;

/**
 * 服务器接口
 * @author chengang
 *
 */
public abstract interface IServer {
	
	/**
	 * 开启服务器
	 * @throws Exception
	 */
	public abstract void start() throws Exception;
	
	/**
	 * 关闭服务器
	 * @throws Exception
	 */
	public abstract void stop() throws Exception;

}
