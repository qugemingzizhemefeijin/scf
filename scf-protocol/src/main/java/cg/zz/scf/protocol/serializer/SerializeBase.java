package cg.zz.scf.protocol.serializer;

import java.nio.charset.Charset;

import cg.zz.scf.protocol.sfp.enumeration.SerializeType;

/**
 * 序列化抽象类
 * @author chengang
 *
 */
public abstract class SerializeBase {
	
	private static SCFSerialize scfSerialize = new SCFSerialize();
	
	private static JSONSerialize jsonSerialize = new JSONSerialize();
	
	private static SCFSerializerV2 scfSerializev2 = new SCFSerializerV2();
	
	public static SerializeBase getInstance(SerializeType serializeType) throws Exception {
		if (serializeType == SerializeType.SCFBinary)
			return scfSerialize;
		else if (serializeType == SerializeType.JSON)
			return jsonSerialize;
		else if (serializeType == SerializeType.SCFBinaryV2) {
			return scfSerializev2;
		}
		throw new Exception("末知的序列化算法");
	}
	
	/**
	 * 序列化
	 * @param paramObject - Object
	 * @return byte[]
	 * @throws Exception
	 */
	public abstract byte[] serialize(Object paramObject) throws Exception;
	
	/**
	 * 反序列化
	 * @param paramArrayOfByte - byte[]
	 * @param clazz - 转换的Class类型
	 * @return Object
	 * @throws Exception
	 */
	public abstract Object deserialize(byte[] paramArrayOfByte, Class<?> clazz) throws Exception;
	
	private Charset encoder;

	public Charset getEncoder() {
		return encoder;
	}

	public void setEncoder(Charset encoder) {
		this.encoder = encoder;
	}

}
