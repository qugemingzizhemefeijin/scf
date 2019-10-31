package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * int类型序列化
 * @author chengang
 *
 */
public class Int32Serializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		outStream.WriteInt32(((Integer)obj).intValue());
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		return Integer.valueOf(inStream.ReadInt32());
	}

}
