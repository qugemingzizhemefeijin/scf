package cg.zz.scf.server.core.communication.http;

import java.util.List;

import cg.zz.scf.server.contract.annotation.HttpRequestMethod;

public class Action {
	
	private HttpRequestMethod mothod;
	
	private String lookup;
	
	private String methodName;
	
	private List<Parameter> paramList;

	public HttpRequestMethod getMothod() {
		return mothod;
	}

	public void setMothod(HttpRequestMethod mothod) {
		this.mothod = mothod;
	}

	public String getLookup() {
		return lookup;
	}

	public void setLookup(String lookup) {
		this.lookup = lookup;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public List<Parameter> getParamList() {
		return paramList;
	}

	public void setParamList(List<Parameter> paramList) {
		this.paramList = paramList;
	}

}
