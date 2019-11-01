package cg.zz.scf.protocol.sdp;

import java.util.List;

import cg.zz.scf.protocol.utility.KeyValuePair;
import cg.zz.scf.serializer.component.annotation.SCFMember;
import cg.zz.scf.serializer.component.annotation.SCFSerializable;

/**
 * request协议对象
 * @author chengang
 *
 */
@SCFSerializable(name="RequestProtocol")
public class RequestProtocol {
	
	/**
	 * 接口名
	 */
	@SCFMember(sortId=1)
	private String lookup;
	
	/**
	 * 方法名称
	 */
	@SCFMember(sortId=2)
	private String methodName;
	
	/**
	 * 参数列表
	 */
	@SCFMember(sortId=3)
	private List<KeyValuePair> paraKVList;
	
	/**
	 * 默认构造函数
	 */
	public RequestProtocol() {
		
	}
	
	/**
	 * 创建Request协议对象
	 * @param lookup - 接口名
	 * @param methodName - 方法名称
	 * @param paraKVList - 参数列表
	 */
	public RequestProtocol(String lookup, String methodName, List<KeyValuePair> paraKVList) {
		this.lookup = lookup;
		this.methodName = methodName;
		this.paraKVList = paraKVList;
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

	public List<KeyValuePair> getParaKVList() {
		return paraKVList;
	}

	public void setParaKVList(List<KeyValuePair> paraKVList) {
		this.paraKVList = paraKVList;
	}

}
