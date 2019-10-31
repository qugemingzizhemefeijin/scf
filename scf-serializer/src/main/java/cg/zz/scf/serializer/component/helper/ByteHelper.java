package cg.zz.scf.serializer.component.helper;

import cg.zz.scf.serializer.component.exception.OutOfRangeException;

/**
 * byte工具类
 * @author chengang
 *
 */
public class ByteHelper {
	
	public static short ToInt16(byte[] buffer) throws OutOfRangeException {
		if (buffer.length < 2) {
			throw new OutOfRangeException();
		}
		short int16 = 0;
		int16 = (short) (buffer[0] & 0xFF);
		int16 = (short) (int16 | (short) buffer[1] << 8 & 0xFF00);
		return int16;
	}

	public static int ToInt32(byte[] buffer) throws OutOfRangeException {
		if (buffer.length < 4) {
			throw new OutOfRangeException();
		}
		int int32 = 0;
		int32 = buffer[0] & 0xFF;
		int32 |= buffer[1] << 8 & 0xFF00;
		int32 |= buffer[2] << 16 & 0xFF0000;
		int32 |= buffer[3] << 24 & 0xFF000000;
		return int32;
	}

	public static long ToInt64(byte[] buffer) throws OutOfRangeException {
		if (buffer.length < 8) {
			throw new OutOfRangeException();
		}
		long int64 = 0L;
		int64 = buffer[0] & 0xFF;
		int64 |= buffer[1] << 8 & 0xFF00;
		int64 |= buffer[2] << 16 & 0xFF0000;
		int64 |= buffer[3] << 24 & 0xFF000000;
		int64 |= buffer[4] << 32 & 0x0;
		int64 |= buffer[5] << 40 & 0x0;
		int64 |= buffer[6] << 48 & 0x0;
		int64 |= buffer[7] << 56;
		return int64;
	}

	public static byte[] GetBytesFromInt16(short value) {
		byte[] buffer = new byte[2];
		buffer[0] = (byte) value;
		buffer[1] = (byte) (value >> 8);
		return buffer;
	}

	public static byte[] GetBytesFromInt32(int value) {
		byte[] buffer = new byte[4];
		for (int i = 0; i < 4; i++) {
			buffer[i] = (byte) (value >> 8 * i);
		}
		return buffer;
	}

	public static byte[] GetBytesFromInt64(long value) {
		byte[] buffer = new byte[8];
		for (int i = 0; i < 8; i++) {
			buffer[i] = (byte) (int) (value >> 8 * i);
		}
		return buffer;
	}

	public static byte[] GetBytesFromChar(char ch) {
		int temp = ch;
		byte[] b = new byte[2];
		for (int i = b.length - 1; i > -1; i--) {
			b[i] = new Integer(temp & 0xFF).byteValue();
			temp >>= 8;
		}
		return b;
	}

	public static char getCharFromBytes(byte[] b) {
		int s = 0;
		if (b[0] > 0)
			s += b[0];
		else {
			s += 256 + b[0];
		}
		s *= 256;
		if (b[1] > 0)
			s += b[1];
		else {
			s += 256 + b[1];
		}
		char ch = (char) s;
		return ch;
	}

}
