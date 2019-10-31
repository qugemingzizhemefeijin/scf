package cg.zz.scf.server.deploy.bytecode;

import java.lang.reflect.Type;

import cg.zz.scf.server.contract.annotation.HttpPathParameter;

/**
 * 方法参数信息
 * @author chengang
 *
 */
public class ParamInfo {
	
	/**
	 * 参数次序
	 */
	private int index;
	
	/**
	 * Class
	 */
	private Class<?> cls;
	
	/**
	 * 参数类型
	 */
	private Type type;
	
	/**
	 * 参数名称
	 */
	private String name;
	
	/**
	 * 映射
	 */
	private String mapping;
	
	/**
	 * 注解
	 */
	private HttpPathParameter httpPathParameter;
	
	public ParamInfo() {
		
	}
	
	/**
	 * 构造方法参数信息
	 * @param index - 参数位置
	 * @param cls - 参数Class
	 * @param type - 参数类型
	 * @param name - 参数名称
	 * @param mapping - 参数映射名称
	 * @param httpPathParameter - 参数注解对象
	 */
	public ParamInfo(int index, Class<?> cls, Type type, String name, String mapping, HttpPathParameter httpPathParameter) {
		this.index = index;
		this.cls = cls;
		this.type = type;
		this.name = name;
		this.mapping = mapping;
		this.httpPathParameter = httpPathParameter;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Class<?> getCls() {
		return cls;
	}

	public void setCls(Class<?> cls) {
		this.cls = cls;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMapping() {
		return mapping;
	}

	public void setMapping(String mapping) {
		this.mapping = mapping;
	}

	public HttpPathParameter getHttpPathParameter() {
		return httpPathParameter;
	}

	public void setHttpPathParameter(HttpPathParameter httpPathParameter) {
		this.httpPathParameter = httpPathParameter;
	}

}
