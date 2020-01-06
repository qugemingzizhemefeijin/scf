package cg.zz.scf.protocol.utility;

import java.io.Serializable;

import cg.zz.scf.serializer.component.annotation.SCFMember;
import cg.zz.scf.serializer.component.annotation.SCFSerializable;

/**
 * KeyValuePair
 * @author chengang
 *
 */
@SuppressWarnings("serial")
@SCFSerializable(name="RpParameter")
public class KeyValuePair implements Serializable {
	
	/**
	 * key名称
	 */
	@SCFMember(name="name", sortId=1)
	private String key;
	
	/**
	 * value值
	 */
	@SCFMember(sortId=2)
	private Object value;
	
	public KeyValuePair() {
		
	}
	
	public KeyValuePair(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

}
