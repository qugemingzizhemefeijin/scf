package cg.zz.scf.server.contract.http;

public class HttpThreadLocal {
	
	//HttpContext线程变量
	private static ThreadLocal<HttpContext> httplocal = new ThreadLocal<HttpContext>();
	
	private static HttpThreadLocal httpthreadlocal;
	
	public static synchronized HttpThreadLocal getInstance() {
		if (httpthreadlocal == null) {
			httpthreadlocal = new HttpThreadLocal();
		}
		return httpthreadlocal;
	}
	
	public HttpContext get() {
		return httplocal.get();
	}

	public void set(HttpContext context) {
		httplocal.set(context);
	}

	public void remove() {
		httplocal.remove();
	}

}
