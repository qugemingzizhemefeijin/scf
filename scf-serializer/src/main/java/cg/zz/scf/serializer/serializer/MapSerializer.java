package cg.zz.scf.serializer.serializer;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.exception.ClassNoMatchException;
import cg.zz.scf.serializer.component.exception.StreamException;
import cg.zz.scf.serializer.component.helper.TypeHelper;

/**
 * Map序列化
 * @author chengang
 *
 */
public class MapSerializer extends SerializerBase {

	@SuppressWarnings("rawtypes")
	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		if (obj == null) {
			SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
		}
		
		int typeId = TypeHelper.GetTypeId(Map.class);
		outStream.WriteInt32(typeId);
		if (outStream.WriteRef(obj)) {
			return;
		}
		Map map = (Map)obj;
		outStream.WriteInt32(map.size());
		Iterator iterator = map.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry) iterator.next();
			Class keyType = entry.getKey().getClass();
			int keyTypeId = TypeHelper.GetTypeId(keyType);
			outStream.WriteInt32(keyTypeId);
			SerializerFactory.GetSerializer(keyType).WriteObject(entry.getKey(), outStream);
			
			Object value = entry.getValue();
			if (value == null) {
				SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
			} else {
				Class valueType = value.getClass();
				int valueTypeId = TypeHelper.GetTypeId(valueType);
				outStream.WriteInt32(valueTypeId);
				SerializerFactory.GetSerializer(valueType).WriteObject(value, outStream);
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		int typeId = inStream.ReadInt32();
		if (typeId == 0) {
			return null;
		}
		byte isRef = (byte) inStream.read();
		int hashcode = inStream.ReadInt32();
		if (isRef > 0) {
			return inStream.GetRef(hashcode);
		}
		int len = inStream.ReadInt32();
		inStream.getClass();
		if (len > inStream.MAX_DATA_LEN) {
			throw new StreamException("Data length overflow.");
		}
		Class<?> type = TypeHelper.GetType(typeId);
		if (type == null) {
			throw new ClassNotFoundException("Cannot find class with typId,target class:" + defType.getName() + ",typeId:" + typeId);
		}
		if (type != Map.class) {
			throw new ClassNoMatchException("Class must be map!type:" + type.getName());
		}
		//如果目标defType不是抽象类，不是接口并且是Map的子类
		int modifier = defType.getModifiers();
		if (!Modifier.isAbstract(modifier) && !Modifier.isInterface(modifier) && Map.class.isAssignableFrom(defType)) {
			type = defType;
		} else {
			type = HashMap.class;
			if (!defType.isAssignableFrom(type)) {
				throw new ClassNoMatchException("Defind type and value type not match !defind type:" + defType.getName() + ",value type:" + type.getName());
			}
		}
		
		Map<Object,Object> map = (Map<Object,Object>)type.newInstance();
		for (int i = 0; i < len; i++) {
			int keyTypeId = inStream.ReadInt32();
			Class<?> keyType = TypeHelper.GetType(keyTypeId);
			if (keyType == null) {
				throw new ClassNotFoundException("Cannot find class with typId,target class:map[key],typeId:" + keyTypeId);
			}
			Object key = SerializerFactory.GetSerializer(keyType).ReadObject(inStream, keyType);

			int valueTypeId = inStream.ReadInt32();
			Class<?> valueType = TypeHelper.GetType(valueTypeId);

			Object value = null;
			if (valueType != null) {
				value = SerializerFactory.GetSerializer(valueType).ReadObject(inStream, valueType);
			}

			map.put(key, value);
		}
		inStream.SetRef(hashcode, map);
		return map;
	}

}
