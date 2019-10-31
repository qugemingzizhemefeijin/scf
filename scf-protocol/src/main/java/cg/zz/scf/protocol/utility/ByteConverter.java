package cg.zz.scf.protocol.utility;

public class ByteConverter {

	/**
	 * byte array to int (little endian)
	 * @param buf - byte[]
	 * @return int
	 */
	public static int bytesToIntLittleEndian(byte[] buf) {
		return buf[0] & 0xFF | buf[1] << 8 & 0xFF00 | buf[2] << 16
				& 0xFF0000 | buf[3] << 24 & 0xFF000000;
	}
	
	/**
	 * byte array to int (little endian)
	 * @param buf - byte[]
	 * @param offset - 偏移量
	 * @return int
	 */
	public static int bytesToIntLittleEndian(byte[] buf, int offset) {
		return buf[offset] & 0xFF | buf[(offset + 1)] << 8 & 0xFF00
				| buf[(offset + 2)] << 16 & 0xFF0000
				| buf[(offset + 3)] << 24 & 0xFF000000;
	}

	/**
	 * byte array to int (big endian)
	 * @param buf - byte[]
	 * @param offset - 偏移量
	 * @return int
	 */
	public static int bytesToIntBigEndian(byte[] buf, int offset) {
		return buf[offset] << 24 & 0xFF000000 | buf[(offset + 1)] << 16
				& 0xFF0000 | buf[(offset + 2)] << 8 & 0xFF00
				| buf[(offset + 3)] & 0xFF;
	}
	
	/**
	 * int to byte array (little endian)
	 * @param n - int
	 * @return byte[]
	 */
	public static byte[] intToBytesLittleEndian(int n) {
		byte[] buf = new byte[4];
		buf[0] = (byte) (0xFF & n);
		buf[1] = (byte) ((0xFF00 & n) >> 8);
		buf[2] = (byte) ((0xFF0000 & n) >> 16);
		buf[3] = (byte) ((0xFF000000 & n) >> 24);
		return buf;
	}

	/**
	 * byte array to int (big endian)
	 * @param buf - byte[]
	 * @return int
	 */
	public static int bytesToIntBigEndian(byte[] buf) {
		return buf[0] << 24 & 0xFF000000 | buf[1] << 16 & 0xFF0000
				| buf[2] << 8 & 0xFF00 | buf[3] & 0xFF;
	}

	/**
	 * int to byte array (big endian)
	 * @param n - int
	 * @return byte[]
	 */
	public static byte[] intToBytesBigEndian(int n) {
		byte[] buf = new byte[4];
		buf[0] = (byte) ((0xFF000000 & n) >> 24);
		buf[1] = (byte) ((0xFF0000 & n) >> 16);
		buf[2] = (byte) ((0xFF00 & n) >> 8);
		buf[3] = (byte) (0xFF & n);
		return buf;
	}

	/**
	 * byte array to short (little endian)
	 * @param buf - byte[]
	 * @param offset - 偏移量
	 * @return short
	 */
	public static short bytesToShortLittleEndian(byte[] buf, int offset) {
		return (short) (buf[offset] << 8 & 0xFF00 | buf[(offset + 1)] & 0xFF);
	}

	/**
	 * byte array to short (big endian)
	 * @param buf - byte[]
	 * @param offset - 偏移量
	 * @return short
	 */
	public static short bytesToShortBigEndian(byte[] buf, int offset) {
		return (short) (buf[offset] & 0xFF | buf[(offset + 1)] << 8 & 0xFF00);
	}

	/**
	 * short to byte array (big endian)
	 * @param n - short
	 * @return byte[]
	 */
	public static byte[] shortToBytesBigEndian(short n) {
		byte[] buf = new byte[2];
		for (int i = 0; i < buf.length; i++) {
			buf[i] = (byte) (n >> 8 * i);
		}
		return buf;
	}

	/**
	 * byte array to short (little endian)
	 * @param buffer - byte[]
	 * @return short
	 */
	public static short byteToShortLittleEndian(byte[] buffer) {
		short int16 = 0;
		int16 = (short) (buffer[0] & 0xFF);
		int16 = (short) (int16 | (short) buffer[1] << 8 & 0xFF00);
		return int16;
	}

	/**
	 * short to byte array (little endian)
	 * @param n - short
	 * @return byte[]
	 */
	public static byte[] shortToBytesLittleEndian(short n) {
		byte[] buf = new byte[2];
		for (int i = 0; i < buf.length; i++) {
			buf[(buf.length - i - 1)] = (byte) (n >> 8 * i);
		}
		return buf;
	}

}
