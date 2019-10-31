package cg.zz.scf.serializer.component.helper;

import cg.zz.scf.serializer.component.TypeMap;
import cg.zz.scf.serializer.component.exception.DisallowedSerializeException;

/**
 * JAVA类型的映射辅助类
 * @author chengang
 *
 */
public class TypeHelper {
	
	public static void InitTypeMap() {
		TypeMap.InitTypeMap();
	}
	
	public static int GetTypeId(Class<?> type) throws DisallowedSerializeException {
		return TypeMap.getTypeId(type);
	}
	
	/**
	 * 根据typeId获得Class
	 * @param typeId - typeId，SCFSerializable注解的name或者Class的简称
	 * @return Class<?>
	 */
	public static Class<?> GetType(int typeId) {
		return TypeMap.getClass(typeId);
	}
	
	/**
	 * 判断是否是原始类型
	 * Long和long都认为是基本类型
	 * @param type - Class<?>
	 * @return boolean
	 */
	public static boolean IsPrimitive(Class<?> type) {
		if (type.isPrimitive()) {
			return true;
		} else if (type == Long.class || type == long.class) {
			return true;
		} else if (type == Integer.class || type == int.class) {
			return true;
		} else if (type == Byte.class || type == byte.class) {
			return true;
		} else if (type == Short.class || type == short.class) {
			return true;
		} else if (type == Character.class || type == char.class) {
			return true;
		} else if (type == Double.class || type == double.class) {
			return true;
		} else if (type == Float.class || type == float.class) {
			return true;
		} else if (type == Boolean.class || type == boolean.class) {
			return true;
		}
		return false;
	}

}
