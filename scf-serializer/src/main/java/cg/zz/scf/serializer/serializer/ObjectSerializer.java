package cg.zz.scf.serializer.serializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.annotation.SCFMember;
import cg.zz.scf.serializer.component.annotation.SCFNotMember;
import cg.zz.scf.serializer.component.annotation.SCFSerializable;
import cg.zz.scf.serializer.component.exception.ClassNoMatchException;
import cg.zz.scf.serializer.component.exception.DisallowedSerializeException;
import cg.zz.scf.serializer.component.helper.StrHelper;
import cg.zz.scf.serializer.component.helper.TypeHelper;

/**
 * 其他的类
 * @author Administrator
 *
 */
public class ObjectSerializer extends SerializerBase {
	
	private static Map<Class<?>, TypeInfo> TypeInfoMap = new HashMap<Class<?>, TypeInfo>();

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		if (obj == null) {
			SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
			return;
		}
		Class<?> type = obj.getClass();
		TypeInfo typeInfo = GetTypeInfo(type);
		outStream.WriteInt32(typeInfo.TypeId);
		if (outStream.WriteRef(obj)) {
			return;
		}
		
		for (Field f : typeInfo.Fields) {
			Object value = f.get(obj);
			if (value == null) {
				SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
			} else if (value instanceof ISCFSerializer) {
				((ISCFSerializer) value).Serialize(outStream);
			} else {
				Class<?> valueType = value.getClass();
				outStream.WriteInt32(TypeHelper.GetTypeId(valueType));
				SerializerFactory.GetSerializer(valueType).WriteObject(value, outStream);
			}
		}
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		int typeId = inStream.ReadInt32();
		if (typeId == 0) {
			return null;
		}
		Class<?> type = TypeHelper.GetType(typeId);
		
		if (type == null) {
			throw new ClassNotFoundException("Cannot find class with typId,target class:" + defType.getName() + ",typeId:" + typeId);
		}
		if (!defType.isAssignableFrom(type) && defType != type) {
			throw new ClassNoMatchException("Class not match!class:" + type.getName() + ",require " + defType.getName());
		}
		byte isRef = (byte) inStream.read();
		int hashcode = inStream.ReadInt32();
		if (isRef > 0) {
			return inStream.GetRef(hashcode);
		}
		TypeInfo typeInfo = GetTypeInfo(type);
		Object obj = type.newInstance();
		for (Field f : typeInfo.Fields) {
			if ((inStream == null) || (inStream.available() == 0)) {
				break;
			}
			int ptypeId = inStream.ReadInt32();
			if (ptypeId == 0) {
				f.set(obj, null);
			} else {
				Class<?> ptype = TypeHelper.GetType(ptypeId);
				if (ptype == null) {
					throw new ClassNotFoundException("Cannot find class with typId,target class: " + f.getType().getName() + ",typeId:" + ptypeId);
				}
				if (ISCFSerializer.class.isAssignableFrom(ptype)) {
					ISCFSerializer value = (ISCFSerializer) ptype.newInstance();
					value.Derialize(inStream);
					f.set(obj, value);
				} else {
					Object value = SerializerFactory.GetSerializer(ptype).ReadObject(inStream,f.getType());
					if (value != null) f.set(obj, value);
				}
			}
		}
		inStream.SetRef(hashcode, obj);
		return obj;
	}
	
	private TypeInfo GetTypeInfo(Class<?> type) throws ClassNotFoundException, DisallowedSerializeException {
		if (TypeInfoMap.containsKey(type)) {
			return (TypeInfo) TypeInfoMap.get(type);
		}
		SCFSerializable cAnn = (SCFSerializable) type.getAnnotation(SCFSerializable.class);
		if (cAnn == null) {
			throw new DisallowedSerializeException();
		}
		int typeId = TypeHelper.GetTypeId(type);
		TypeInfo typeInfo = new TypeInfo(typeId);
		
		ArrayList<Field> fields = new ArrayList<Field>();
		Class<?> temType = type;
		//此处将所有的字符包括父类的字段都存储起来
		while (true) {
			Field[] fs = temType.getDeclaredFields();
			for (Field f : fs) {
				fields.add(f);
			}
			Class<?> superClass = temType.getSuperclass();
			if (superClass == null) {
				break;
			}
			temType = superClass;
		}
		
		Map<Integer , Field> mapFildes = new HashMap<Integer , Field>();
		List<Integer> indexIds = new ArrayList<Integer>();
		//是否序列化所有字段
		if (cAnn.defaultAll()) {
			for (Field f : fields) {
				//如果字段不需要序列化，则跳过
				SCFNotMember ann = (SCFNotMember)f.getAnnotation(SCFNotMember.class);
			        if (ann != null) {
			        	continue;
			        }
			        f.setAccessible(true);//设置字段为取消 Java 语言访问检查
			        Integer indexId = Integer.valueOf(StrHelper.GetHashcode(f.getName().toLowerCase()));
			        mapFildes.put(indexId, f);
			        indexIds.add(indexId);
			}
		} else {
			for (Field f : fields) {
				SCFMember ann = (SCFMember) f.getAnnotation(SCFMember.class);
				if ((ann == null) || (ann.name().startsWith("$"))) {
					continue;
				}
				f.setAccessible(true);
				
				String name = ann.name();
				if (ann.name() == null || ann.name().length() == 0) {
					name = f.getName();
				}
				
				/*
				 * 2011-6-28修改，支持服务器端增加字段客户端不需要更新功能
				 */
				Integer indexId = 0;
				if (name.startsWith("#")) {
					indexId = Integer.MAX_VALUE;
					mapFildes.put(indexId, f);
				} else {
					indexId = StrHelper.GetHashcode(name.toLowerCase());
					mapFildes.put(indexId, f);
					indexIds.add(indexId);
				}
			}
		}
		
		//将indexIds里面的对象按照hashCode由小到大排序
		for (int i = 0 , len = indexIds.size(); i < len; i++) {
			for (int j = i + 1; j < len; j++) {
				Integer item = (Integer) indexIds.get(j);
				if (((Integer) indexIds.get(i)).intValue() > item.intValue()) {
					indexIds.set(j, (Integer) indexIds.get(i));
					indexIds.set(i, item);
				}
			}
		}
		for (Integer index : indexIds) {
			typeInfo.Fields.add((Field) mapFildes.get(index));
		}
		TypeInfoMap.put(type, typeInfo);
		return typeInfo;
	}

}
