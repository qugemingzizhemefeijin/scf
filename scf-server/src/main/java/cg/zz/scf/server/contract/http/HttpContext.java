package cg.zz.scf.server.contract.http;

/**
 * 请求的Http上下文
 * @author chengang
 *
 */
public class HttpContext {
	
	/**
	 * request
	 */
	private HttpRequest request;
	
	/**
	 * response
	 */
	private HttpResponse response;

	public HttpRequest getRequest() {
		return request;
	}

	public void setRequest(HttpRequest request) {
		this.request = request;
	}

	public HttpResponse getResponse() {
		return response;
	}

	public void setResponse(HttpResponse response) {
		this.response = response;
	}

}
