package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * double类型序列化
 * @author chengang
 *
 */
public class DoubleSerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		long value = Double.doubleToLongBits(((Double) obj).doubleValue());
		outStream.WriteInt64(value);
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		long value = inStream.ReadInt64();
		return Double.valueOf(Double.longBitsToDouble(value));
	}

}
