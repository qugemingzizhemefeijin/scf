package cg.zz.scf.server.util;

/**
 * Endianness tools for:
 * 
 * convert int to byte[]
 * convert byte[] to int
 */
public class EndiannessHelper {
	
	/**
	 * byte array to int (little endian)
	 * @param buf
	 * @return
	 */
	public static int bytesToIntLittleEndian(byte buf[]) {
		return buf[0] & 0xff | 
				((buf[1] << 8) & 0xff00) |
				((buf[2] << 16) & 0xff0000) |
				((buf[3] << 24) & 0xff000000);
	}

	/**
	 * int to byte array (little endian)
	 * @param n
	 * @return
	 */
	public static byte[] intToBytesLittleEndian(int n) {
		byte[] buf = new byte[4];
		buf[0] = (byte) (0xff & n);
		buf[1] = (byte) ((0xff00 & n) >> 8);
		buf[2] = (byte) ((0xff0000 & n) >> 16);
		buf[3] = (byte) ((0xff000000 & n) >> 24);
		return buf;
	}
	
	/**
	 * byte array to int (big endian)
	 * @param buf
	 * @return
	 */
	public static int bytesToIntBigEndian(byte[] buf) {
		return ((buf[0] << 24) & 0xff000000) | 
				((buf[1] << 16) & 0xff0000) |
				((buf[2] << 8) & 0xff00) |
				(buf[3] & 0xff);
	}
	
	/**
	 * int to byte array (big endian)
	 * @param n
	 * @return
	 */
	public static byte[] intToBytesBigEndian(int n) {
		byte[] buf = new byte[4];
		buf[0] = (byte) ((0xff000000 & n) >> 24);
		buf[1] = (byte) ((0xff0000 & n) >> 16);
		buf[2] = (byte) ((0xff00 & n) >> 8);
		buf[3] = (byte) (0xff & n);
		return buf;
	}

}
