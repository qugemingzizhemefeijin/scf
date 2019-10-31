package cg.zz.scf.server.contract.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpRequest
 * @author chengang
 *
 */
public class HttpRequest {
	
	/**
	 * 来源IP
	 */
	private String fromIP;
	
	/**
	 * 目标IP
	 */
	private String toIP;
	
	/**
	 * uri
	 */
	private String uri;
	
	/**
	 * 内容
	 */
	private byte[] content;
	
	/**
	 * header头映射表
	 */
	private Map<String, String> headers = new HashMap<String, String>();
	
	/**
	 * header头映射表
	 */
	private Map<String, List<String>> headers_;
	
	public String getQueryString(String key) {
		return null;
	}
	
	public Map<String, List<String>> getHeaders_() {
		return this.headers_;
	}

	public void setHeaders_(Map<String, List<String>> headers_) {
		this.headers_ = headers_;
	}

	public String getFromIP() {
		return this.fromIP;
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public void setFromIP(String fromIP) {
		this.fromIP = fromIP;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return this.uri;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public byte[] getContent() {
		return this.content;
	}

	public void setToIP(String toIP) {
		this.toIP = toIP;
	}

	public String getToIP() {
		return this.toIP;
	}

}
