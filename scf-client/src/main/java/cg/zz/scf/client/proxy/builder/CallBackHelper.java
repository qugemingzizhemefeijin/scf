package cg.zz.scf.client.proxy.builder;

public class CallBackHelper {
	
	public static CallBackExecutor getInstance() {
		return Cb.callbackexecutor;
	}

	public static class Cb {
		public static CallBackExecutor callbackexecutor = new CallBackExecutor();
	}

}
