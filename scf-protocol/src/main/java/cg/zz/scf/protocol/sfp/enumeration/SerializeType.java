package cg.zz.scf.protocol.sfp.enumeration;

/**
 * 序列化枚举
 * 
 * @author chengang
 *
 */
public enum SerializeType {

	/**
	 * json
	 */
	JSON(1),
	
	/**
	 * java
	 */
	JAVABinary(2),
	
	/**
	 * xml
	 */
	XML(3),
	
	/**
	 * scf二进制
	 */
	SCFBinary(4),
	
	/**
	 * scf二进制V2
	 */
	SCFBinaryV2(5);

	private final int num;
	
	private SerializeType(int num) {
		this.num = num;
	}

	public int getNum() {
		return this.num;
	}

	public static SerializeType getSerializeType(int num) {
		for (SerializeType type : values()) {
			if (type.getNum() == num) {
				return type;
			}
		}
		return null;
	}

}
