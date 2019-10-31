package cg.zz.scf.serializer.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import cg.zz.scf.serializer.component.exception.StreamException;
import cg.zz.scf.serializer.component.helper.ByteHelper;

public class SCFInStream extends ByteArrayInputStream {
	
	//最大支持数据为10M
	public final int MAX_DATA_LEN = 1024 * 1024 * 10;
	
	/**
	 * 编码
	 */
	public Charset Encoder = Charset.forName("UTF-8");
	
	/**
	 * 缓存池
	 */
	private Map<Integer, Object> _RefPool = new HashMap<Integer, Object>();
	
	public SCFInStream(byte[] buffer) {
		super(buffer);
	}

	public SCFInStream(byte[] buffer, int offset, int length) {
		super(buffer, offset, length);
	}
	
	public void SafeRead(byte[] buffer) throws StreamException, IOException {
		if (read(buffer) != buffer.length) {
			throw new StreamException();
		}
	}

	public Object GetRef(int hashcode) {
		if (hashcode == 0) {
			return null;
		}
		return this._RefPool.get(Integer.valueOf(hashcode));
	}

	public void SetRef(int hashcode, Object obj) {
		this._RefPool.put(Integer.valueOf(hashcode), obj);
	}

	public short ReadInt16() throws Exception {
		byte[] buffer = new byte[2];
		if (read(buffer) != 2) {
			throw new StreamException();
		}
		return ByteHelper.ToInt16(buffer);
	}

	public int ReadInt32() throws Exception {
		byte[] buffer = new byte[4];
		if (read(buffer) != 4) {
			throw new StreamException();
		}
		return ByteHelper.ToInt32(buffer);
	}

	public long ReadInt64() throws Exception {
		byte[] buffer = new byte[8];
		if (read(buffer) != 8) {
			throw new StreamException();
		}
		return ByteHelper.ToInt64(buffer);
	}

}
