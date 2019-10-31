package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * float序列化
 * @author chengang
 *
 */
public class FloatSerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		int value = Float.floatToIntBits(((Float) obj).floatValue());
		outStream.WriteInt32(value);
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		int value = inStream.ReadInt32();
		return Float.valueOf(Float.intBitsToFloat(value));
	}

}
