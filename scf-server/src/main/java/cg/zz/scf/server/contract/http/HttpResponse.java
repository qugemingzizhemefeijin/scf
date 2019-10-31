package cg.zz.scf.server.contract.http;

import java.util.HashMap;
import java.util.Map;

/**
 * HttpResponse
 * @author chengang
 *
 */
public class HttpResponse {
	
	/**
	 * header头映射表
	 */
	private Map<String, String> headers = new HashMap<String, String>();
	
	/**
	 * 内容类型
	 */
	private String contentType;
	
	/**
	 * 返回码
	 */
	private int code;

	/**
	 * 写入内容
	 * @param content - 内容
	 */
	public void write(String content) {
		write(content, "utf-8");
	}

	public void write(String content, String encoding) {
	}

	public void write(byte[] buffer) {
	}

	public void addHeader(String key, String value) {
		this.headers.put(key, value);
	}

	public Map<String, String> getHeaders() {
		return this.headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getCode() {
		return this.code;
	}

	public void setCode(int code) {
		this.code = code;
	}

}
