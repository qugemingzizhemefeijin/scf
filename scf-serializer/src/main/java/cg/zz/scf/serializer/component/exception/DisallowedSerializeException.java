package cg.zz.scf.serializer.component.exception;

/**
 * 无效的序列化类型错误
 * @author chengang
 *
 */
public class DisallowedSerializeException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9118366975662794946L;
	
	/**
	 * 错误描述信息
	 */
	private String msg;
	
	public DisallowedSerializeException(String message) {
		this.msg = message;
	}

	public DisallowedSerializeException() {
		this.msg = "This type disallowed serialize,please add SCFSerializable attribute to the type.";
	}

	public DisallowedSerializeException(Class<?> type) {
		this.msg = ("This type disallowed serialize,please add SCFSerializable attribute to the type.type:" + type.getName());
	}

	public String getMessage() {
		return this.msg;
	}

}
