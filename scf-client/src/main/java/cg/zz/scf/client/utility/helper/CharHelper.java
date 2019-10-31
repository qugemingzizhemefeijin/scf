package cg.zz.scf.client.utility.helper;

/**
 * 字符操作类
 *
 */
public class CharHelper {
	
	/**
	 * 字符截取
	 * @param source
	 * @param startIndex
	 * @param count
	 * @return
	 */
	public static String subString(String source, int startIndex, int count) {
		if (source == null || source.equals("")) {
			return null;
		}
		if (source.length() - startIndex > count) {
			count = source.length() - startIndex;
		}
		if (startIndex <= 0) {
			startIndex = 0;
		}
		return source.substring(startIndex, count);
	}

}
