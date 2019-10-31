package cg.zz.scf.client.utility.helper;

/**
 * 数组操作类
 *
 */
public class ArrayHelper {
	
	/**
	 * 判断两个byte数组是否相等
	 * @param array1 - 数组
	 * @param array2 - 数组
	 * @return boolean
	 */
	public static boolean equals(byte[] array1, byte[] array2) {
		if (array1 == array2) {
			return true;
		}
		if (array1.length != array2.length) {
			return false;
		}
		for (int i = 0; i < array1.length; i++) {
			if (array1[i] != array2[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 判断数组指定偏移下标后的元素是否跟另外一个数组相等
	 * @param array1 - 数组
	 * @param offset - 偏移下标
	 * @param array2 - 数组
	 * @return boolean
	 */
	public static boolean equals(byte[] array1, int offset, byte[] array2) {
		if (array1 == array2 && offset == 0) {
			return true;
		}
		if (array1.length - offset < array2.length) {
			return false;
		}
		for (int i = 0; i < array2.length; i++) {
			if (array1[(i + offset)] != array2[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 数组截取
	 * @param source - 数组
	 * @param start - 开始位置
	 * @param len - 截取长度
	 * @return byte[]
	 */
	public static byte[] subArray(byte[] source, int start, int len) {
		if (start < 0) {
			start = 0;
		}
		if (len < 0 || len > source.length) {
			return null;
		}
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++) {
			result[i] = source[(start + i)];
		}
		return result;
	}
	
	/**
	 * 将数组元素左移
	 * @param array
	 * @param count
	 */
	public static void leftMove(byte[] array, int count) {
		for (int i = 0; i < array.length; i++) {
			int target = i - count;
			if (target < 0) {
				continue;
			}
			array[target] = array[i];
			array[i] = 0;
		}
	}

}
