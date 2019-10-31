package cg.zz.scf.serializer.serializer;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.exception.ClassNoMatchException;
import cg.zz.scf.serializer.component.exception.StreamException;
import cg.zz.scf.serializer.component.helper.TypeHelper;

/**
 * 列表序列化
 * @author chengang
 *
 */
public class ListSerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		if (obj == null) {
			SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
		}
		
		List<?> list = (List<?>) obj;
		int typeId = TypeHelper.GetTypeId(List.class);
		outStream.WriteInt32(typeId);
		if (outStream.WriteRef(obj)) {
			return;
		}
		outStream.WriteInt32(list.size());
		
		for (Object item : list) {
			if (item == null) {
				SerializerFactory.GetSerializer(null).WriteObject(item, outStream);
			} else {
				Class<?> itemType = item.getClass();
				outStream.WriteInt32(TypeHelper.GetTypeId(itemType));
				SerializerFactory.GetSerializer(itemType).WriteObject(item, outStream);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
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
		if (type != List.class) {
			throw new ClassNoMatchException("Class must be list!type:" + type.getName());
		}
		//类修饰符
		int modifier = defType.getModifiers();
		//判断类defType不是抽象类，并且不是接口，并且必须是List的子类
		if (!Modifier.isAbstract(modifier) && !Modifier.isInterface(modifier) && List.class.isAssignableFrom(defType)) {
			type = defType;
		} else {
			type = ArrayList.class;
			//如果defType是不是type的超类或者超接口，抛出异常
			if (!defType.isAssignableFrom(type)) {
				throw new ClassNoMatchException("Defind type and value type not match !defind type:" + defType.getName() + ",value type:" + type.getName());
			}
		}
		
		List list = (List)type.newInstance();
		for (int i = 0; i < len; i++) {
			int itemTypeId = inStream.ReadInt32();
			if (itemTypeId == 0) {
				list.add(null);
			} else {
				Class<?> itemType = TypeHelper.GetType(itemTypeId);
				if (itemType == null) {
					throw new ClassNotFoundException("Cannot find class with typId,target class:(list[item]),typeId:" + itemTypeId);
				}
				Object value = SerializerFactory.GetSerializer(itemType).ReadObject(inStream, itemType);
				list.add(value);
			}
		}
		inStream.SetRef(hashcode, list);
		return list;
	}

}
