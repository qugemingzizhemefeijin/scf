package cg.zz.scf.client.communication.socket;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * 扩展了ByteArrayOutputStream，新增toByteArray方法，可以从已有的buf中获取指定范围的byte数组
 * @author chengang
 *
 */
public class CByteArrayOutputStream extends ByteArrayOutputStream {
	
	/**
	 * 从buf中获取指定范围的byte数组
	 * @param index - 开始位置
	 * @param len - 获取长度
	 * @return byte[]
	 */
	public byte[] toByteArray(int index, int len) {
		return Arrays.copyOfRange(this.buf, index, Math.min(index + len, size()));
	}

}
