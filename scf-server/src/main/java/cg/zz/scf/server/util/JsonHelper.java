package cg.zz.scf.server.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * json工具类
 * @author chengang
 *
 */
public class JsonHelper {
	
	/**
	 * 将对象转换成json字符串
	 * @param obj - Object
	 * @return String
	 */
	public static String toJsonString(Object obj) throws Exception {
		if (obj == null) {
			return "null";
		}
		
		return JSON.toJSONString(obj);
	}
	
	/**
	 * 将字符串转换成指定的类
	 * @param text - String
	 * @param clazz - Class
	 * @return T
	 */
	public static <T> T toJava(String json , Class<T> clazz) {
		return JSON.parseObject(json, clazz);
	}
	
	/**
	 * 将字符串转换成指定的容器类
	 * @param text - String
	 * @param clazz - Class
	 * @return Object
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object toJava(String json , Class<?> containClass, Class<?> itemClass) throws Exception {
		Object bean = null;
		if ((containClass == List.class) || (containClass == ArrayList.class))
			bean = new ArrayList();
		else if (containClass == Vector.class)
			bean = new Vector();
		else if ((containClass == Set.class) || (containClass == HashSet.class)) {
			bean = new HashSet();
		} else {
			throw new Exception("containClass must is (ArrayList Vector HashSet)");
		}
		
		JSONArray jsonArray = JSONArray.parseArray(json);
		if(jsonArray != null && !jsonArray.isEmpty()) {
			for (int i = 0 , size = jsonArray.size(); i < size; i++) {
				Object item = jsonArray.getObject(i, itemClass);
				
				((Collection)bean).add(item);
			}
		}
		
		return bean;
	}
	
	/**
	 * 将字符串转换成FastJson对象
	 * @param json - String
	 * @return JSONObject
	 */
	public static JSONObject toJsonObject(String json) {
		return JSONObject.parseObject(json);
	}
	
	/**
	 * 将字符串转换成FastJson对象
	 * @param json - String
	 * @return JSONArray
	 */
	public static JSONArray toJsonArray(String json) {
		return JSONObject.parseArray(json);
	}

}
