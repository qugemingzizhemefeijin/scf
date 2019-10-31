package cg.zz.scf.protocol.serializer;

import cg.zz.scf.serializer.serializer.Serializer;

public class SCFSerializerV2 extends SerializeBase {
	
	private static final Serializer serializer = new Serializer();

	@Override
	public byte[] serialize(Object obj) throws Exception {
		return serializer.Serialize(obj);
	}

	@Override
	public Object deserialize(byte[] data, Class<?> cls) throws Exception {
		return serializer.Derialize(data, cls);
	}

}
