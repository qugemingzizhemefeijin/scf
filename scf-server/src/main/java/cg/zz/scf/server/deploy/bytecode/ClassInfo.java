package cg.zz.scf.server.deploy.bytecode;

import java.util.List;

/**
 * 类信息
 * @author chengang
 *
 */
public class ClassInfo {

	/**
	 * Class
	 */
	private Class<?> cls;
	
	/**
	 * 方法列表
	 */
	private List<MethodInfo> methodList;
	
	/**
	 * 类型
	 */
	private ClassType classType;
	
	/**
	 * 命名空间
	 */
	private String lookUP;

	public Class<?> getCls() {
		return cls;
	}

	public void setCls(Class<?> cls) {
		this.cls = cls;
	}

	public List<MethodInfo> getMethodList() {
		return methodList;
	}

	public void setMethodList(List<MethodInfo> methodList) {
		this.methodList = methodList;
	}

	public ClassType getClassType() {
		return classType;
	}

	public void setClassType(ClassType classType) {
		this.classType = classType;
	}

	public String getLookUP() {
		return lookUP;
	}

	public void setLookUP(String lookUP) {
		this.lookUP = lookUP;
	}

}
