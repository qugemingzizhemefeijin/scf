package cg.zz.scf.serializer.component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import cg.zz.scf.serializer.component.helper.ByteHelper;

public class SCFOutStream extends ByteArrayOutputStream {
	
	/**
	 * 编码
	 */
	public Charset Encoder = Charset.forName("UTF-8");
	
	/**
	 * 写入存储的对象池
	 */
	private Map<Integer, Object> _RefPool = new HashMap<Integer, Object>();
	
	private int hashCode = 1000;
	
	/**
	 * 存储对象和hashCode
	 */
	private Map<Object, Integer> _objMap = new HashMap<Object, Integer>();
	
	public boolean WriteRef(Object obj) throws IOException {
		if (obj == null) {
			WriteByte((byte) 1);
			WriteInt32(0);
			return true;
		}
		int objHashcode = getHashCode(obj);
		if (this._RefPool.containsKey(Integer.valueOf(objHashcode))) {
			WriteByte((byte) 1);
			WriteInt32(objHashcode);
			return true;
		}
		this._RefPool.put(Integer.valueOf(objHashcode), obj);
		WriteByte((byte) 0);
		WriteInt32(objHashcode);
		return false;
	}

	public void WriteByte(byte value) throws IOException {
		write(new byte[] { value });
	}

	public void WriteInt16(short value) throws IOException {
		byte[] buffer = ByteHelper.GetBytesFromInt16(value);
		write(buffer);
	}

	public void WriteInt32(int value) throws IOException {
		byte[] buffer = ByteHelper.GetBytesFromInt32(value);
		write(buffer);
	}

	public void WriteInt64(long value) throws IOException {
		byte[] buffer = ByteHelper.GetBytesFromInt64(value);
		write(buffer);
	}

	private int getHashCode(Object obj) {
		if (obj == null) {
			return 0;
		}
		if ((this._objMap.containsKey(obj)) && (obj == this._objMap.get(obj))) {
			return ((Integer) this._objMap.get(obj)).intValue();
		}
		this._objMap.put(obj, Integer.valueOf(++this.hashCode));
		return this.hashCode;
	}

}
