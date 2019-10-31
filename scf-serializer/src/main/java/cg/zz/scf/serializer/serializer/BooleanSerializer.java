package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * 序列化boolean
 * @author chengang
 *
 */
public class BooleanSerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		byte value = 0;
		if (((Boolean) obj).booleanValue()) {
			value = 1;
		}
		outStream.WriteByte(value);
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		if (inStream.read() > 0) return Boolean.valueOf(true);
		return Boolean.valueOf(false);
	}

}
