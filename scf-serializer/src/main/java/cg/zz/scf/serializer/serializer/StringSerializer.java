package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.exception.StreamException;
import cg.zz.scf.serializer.component.helper.ByteHelper;
import cg.zz.scf.serializer.component.helper.StrHelper;

/**
 * 字符串序列化
 * @author chengang
 *
 */
public class StringSerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		if (outStream.WriteRef(obj)) {
			return;
		}
		
		byte[] buffer = obj.toString().getBytes(outStream.Encoder);
		byte[] bLen = ByteHelper.GetBytesFromInt32(buffer.length);
		byte[] bytes = new byte[buffer.length + 4];
		System.arraycopy(bLen, 0, bytes, 0, 4);
		System.arraycopy(buffer, 0, bytes, 4, buffer.length);
		outStream.write(bytes);
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		int isRef = (byte) inStream.read();
		int hashcode = inStream.ReadInt32();
		if (isRef > 0) {
			Object obj = inStream.GetRef(hashcode);
			if (obj == null) {
				return StrHelper.EmptyString;
			}
			return obj;
		}
		int len = inStream.ReadInt32();
		inStream.getClass();
		if (len > inStream.MAX_DATA_LEN) {
			throw new StreamException("Data length overflow.");
		}
		if (len == 0) {
			inStream.SetRef(hashcode, StrHelper.EmptyString);
			return StrHelper.EmptyString;
		}
		
		byte[] buffer = new byte[len];
		inStream.SafeRead(buffer);
		String str = new String(buffer, inStream.Encoder);
		inStream.SetRef(hashcode, str);
		return str;
	}

}
