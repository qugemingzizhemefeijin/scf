package cg.zz.scf.server.deploy.bytecode;

import java.util.Map;

/**
 * sessionBean
 * @author chengang
 *
 */
public class SessionBean {

	/**
	 * 接口名称
	 */
	private String interfaceName;
	
	/**
	 * 接口实现类与接口类的映射关系
	 * key为接口实现类的lookup或者类简称
	 * value为接口实现类的全名称
	 */
	private Map<String, String> instanceMap;
	
	/**
	 * 接口类信息
	 */
	private ClassInfo interfaceClass;
	
	public SessionBean() {
		
	}
	
	public SessionBean(String interfaceName, Map<String, String> instanceMap, ClassInfo interfaceClass) {
		this.interfaceName = interfaceName;
		this.instanceMap = instanceMap;
		this.interfaceClass = interfaceClass;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public Map<String, String> getInstanceMap() {
		return instanceMap;
	}

	public void setInstanceMap(Map<String, String> instanceMap) {
		this.instanceMap = instanceMap;
	}

	public ClassInfo getInterfaceClass() {
		return interfaceClass;
	}

	public void setInterfaceClass(ClassInfo interfaceClass) {
		this.interfaceClass = interfaceClass;
	}

}
