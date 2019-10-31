package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * 16位int[short]序列化
 * @author chengang
 *
 */
public class Int16Serializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		outStream.WriteInt16(((Short)obj).shortValue());
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		return Short.valueOf(inStream.ReadInt16());
	}

}
