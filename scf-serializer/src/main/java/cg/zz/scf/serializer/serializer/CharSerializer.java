package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.helper.ByteHelper;

/**
 * char序列化
 * @author chengang
 *
 */
public class CharSerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		byte[] bs = ByteHelper.GetBytesFromChar(((Character) obj).charValue());
		for (byte b : bs)
			outStream.WriteByte(b);
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		short data = inStream.ReadInt16();
		byte[] buffer = ByteHelper.GetBytesFromInt16(data);
		return Character.valueOf(ByteHelper.getCharFromBytes(buffer));
	}

}
