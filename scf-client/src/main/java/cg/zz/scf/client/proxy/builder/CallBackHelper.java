package cg.zz.scf.client.proxy.builder;

/**
 * 异步消息处理辅助类（这个类好像没啥用）
 * @author chengang
 *
 */
public class CallBackHelper {
	
	/**
	 * 异步消息回调处理单例线程池（典型的通过内部类实现单子类的懒加载）
	 * @return CallBackExecutor
	 */
	public static CallBackExecutor getInstance() {
		return Cb.callbackexecutor;
	}

	public static class Cb {
		public static CallBackExecutor callbackexecutor = new CallBackExecutor();
	}

}
