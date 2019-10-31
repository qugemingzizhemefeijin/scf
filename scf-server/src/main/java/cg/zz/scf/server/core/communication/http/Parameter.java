package cg.zz.scf.server.core.communication.http;

import cg.zz.scf.server.contract.annotation.HttpParameterType;

public class Parameter {
	
	private String type;
	private String mapping;
	private Object value;
	private int urlParaIndex;
	private int contentParaIndex;
	private int methodParaIndex;
	private HttpParameterType paraType;
	
	public Parameter() {}
	
	public Parameter(String type, String mapping, Object value, int urlParaIndex, int contentParaIndex, int methodParaIndex, HttpParameterType paraType) {
		this.type = type;
		this.mapping = mapping;
		this.value = value;
		this.urlParaIndex = urlParaIndex;
		this.contentParaIndex = contentParaIndex;
		this.methodParaIndex = methodParaIndex;
		this.paraType = paraType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public int getUrlParaIndex() {
		return urlParaIndex;
	}

	public void setUrlParaIndex(int urlParaIndex) {
		this.urlParaIndex = urlParaIndex;
	}

	public int getContentParaIndex() {
		return contentParaIndex;
	}

	public void setContentParaIndex(int contentParaIndex) {
		this.contentParaIndex = contentParaIndex;
	}

	public int getMethodParaIndex() {
		return methodParaIndex;
	}

	public void setMethodParaIndex(int methodParaIndex) {
		this.methodParaIndex = methodParaIndex;
	}

	public HttpParameterType getParaType() {
		return paraType;
	}

	public void setParaType(HttpParameterType paraType) {
		this.paraType = paraType;
	}

}
