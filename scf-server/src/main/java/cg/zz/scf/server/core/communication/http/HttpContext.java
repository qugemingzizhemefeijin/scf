package cg.zz.scf.server.core.communication.http;

import java.util.List;
import java.util.Map;

import cg.zz.scf.server.contract.annotation.HttpRequestMethod;

public class HttpContext {
	
	private String uri;
	
	private HttpRequestMethod method;
	
	private byte[] contentBuffer;
	
	private String fromIP;
	
	private String toIP;
	
	private Map<String, List<String>> headers;
	
	private Map<String, List<String>> params;

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public HttpRequestMethod getMethod() {
		return method;
	}

	public void setMethod(HttpRequestMethod method) {
		this.method = method;
	}

	public byte[] getContentBuffer() {
		return contentBuffer;
	}

	public void setContentBuffer(byte[] contentBuffer) {
		this.contentBuffer = contentBuffer;
	}

	public String getFromIP() {
		return fromIP;
	}

	public void setFromIP(String fromIP) {
		this.fromIP = fromIP;
	}

	public String getToIP() {
		return toIP;
	}

	public void setToIP(String toIP) {
		this.toIP = toIP;
	}

	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	public Map<String, List<String>> getParams() {
		return params;
	}

	public void setParams(Map<String, List<String>> params) {
		this.params = params;
	}

}
