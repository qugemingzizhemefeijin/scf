package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * 64位int[long]序列化
 * @author chengang
 *
 */
public class Int64Serializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		outStream.WriteInt64(((Long)obj).longValue());
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		return Long.valueOf(inStream.ReadInt64());
	}

}
