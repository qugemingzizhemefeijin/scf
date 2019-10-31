package cg.zz.scf.server.deploy.bytecode;

import java.lang.reflect.Method;

import cg.zz.scf.server.contract.annotation.HttpRequestMapping;

/**
 * 方法信息
 * @author chengang
 *
 */
public class MethodInfo {
	
	/**
	 * 方法
	 */
	private Method method;
	
	/**
	 * 参数
	 */
	private ParamInfo[] paramInfoAry;
	
	/**
	 * http映射
	 */
	private HttpRequestMapping httpRequestMapping;

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	public ParamInfo[] getParamInfoAry() {
		return paramInfoAry;
	}

	public void setParamInfoAry(ParamInfo[] paramInfoAry) {
		this.paramInfoAry = paramInfoAry;
	}

	public HttpRequestMapping getHttpRequestMapping() {
		return httpRequestMapping;
	}

	public void setHttpRequestMapping(HttpRequestMapping httpRequestMapping) {
		this.httpRequestMapping = httpRequestMapping;
	}

}
