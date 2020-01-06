package cg.zz.scf.serializer.component.helper;

/**
 * 字符串工具类
 * @author chengang
 *
 */
public class StrHelper {
	
	/**
	 * 空字符串
	 */
	public final static String EmptyString = "";
	
	/**
	 * 获得字符串的hashCode
	 * @param str - 字符串
	 * @return int
	 */
	public static int GetHashcode(String str) {
		int hash1 = 5381;
		int hash2 = hash1;
		int len = str.length();
		for (int i = 0; i < len; i++) {
			int c = str.charAt(i);
			hash1 = (hash1 << 5) + hash1 ^ c;
			i++;
			if (i >= len) {
				break;
			}
			c = str.charAt(i);
			hash2 = (hash2 << 5) + hash2 ^ c;
		}
		return hash1 + hash2 * 1566083941;
	}

	/**
	 * 判断字符串是否是空
	 * @param str - String
	 * @return boolean
	 */
	public static boolean isEmptyOrNull(String str) {
		return str == null || "".equals(str);
	}

}
