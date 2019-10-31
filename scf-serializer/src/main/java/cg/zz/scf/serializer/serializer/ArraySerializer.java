package cg.zz.scf.serializer.serializer;

import java.lang.reflect.Array;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.exception.StreamException;
import cg.zz.scf.serializer.component.helper.ByteHelper;
import cg.zz.scf.serializer.component.helper.ClassHelper;
import cg.zz.scf.serializer.component.helper.StrHelper;
import cg.zz.scf.serializer.component.helper.TypeHelper;

/**
 * 数组序列化
 * @author chengang
 *
 */
public class ArraySerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		if (obj == null) {
			SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
			return;
		}
		
		Class<?> type = ClassHelper.GetClassForName(obj.getClass().getCanonicalName().replace("[]", StrHelper.EmptyString));
		int typeId = TypeHelper.GetTypeId(type);

		outStream.WriteInt32(typeId);
		if (outStream.WriteRef(obj)) {
			return;
		}
		
		//判断数组的组件类型是否是基本类型
		if (obj.getClass().getComponentType().isPrimitive()) {
			if (type == Character.class) {
				char[] charArray = (char[]) obj;
				outStream.WriteInt32(charArray.length);
				for (char item : charArray) {
					SerializerFactory.GetSerializer(Character.TYPE).WriteObject(Character.valueOf(item), outStream);
				}
				charArray = (char[]) null;
				return;
			} else if (type == Short.class) {
				short[] shortArray = (short[]) obj;
				outStream.WriteInt32(shortArray.length);
				for (short item : shortArray) {
					SerializerFactory.GetSerializer(Short.TYPE).WriteObject(Short.valueOf(item),outStream);
				}
				shortArray = (short[]) null;
				return;
			} else if (type == Integer.class) {
				int[] intArray = (int[]) obj;
				outStream.WriteInt32(intArray.length);
				for (int intItem : intArray) {
					SerializerFactory.GetSerializer(Integer.TYPE).WriteObject(Integer.valueOf(intItem), outStream);
				}
				intArray = (int[]) null;
				return;
			} else if (type == Float.class) {
				float[] floatArray = (float[]) obj;
				outStream.WriteInt32(floatArray.length);
				for (float item : floatArray) {
					SerializerFactory.GetSerializer(Float.TYPE).WriteObject(Float.valueOf(item),outStream);
				}
				floatArray = (float[]) null;
				return;
			} else if (type == Long.class) {
				long[] longArray = (long[]) obj;
				outStream.WriteInt32(longArray.length);
				for (long item : longArray) {
					SerializerFactory.GetSerializer(Long.TYPE).WriteObject(Long.valueOf(item),outStream);
				}
				longArray = (long[]) null;
				return;
			} else if (type == Double.class) {
				double[] doubleArray = (double[]) obj;
				outStream.WriteInt32(doubleArray.length);
				for (double item : doubleArray) {
					SerializerFactory.GetSerializer(Double.TYPE).WriteObject(Double.valueOf(item),	outStream);
				}
				doubleArray = (double[]) null;
				return;
			} else if (type == Boolean.class) {
				boolean[] booleanArray = (boolean[]) obj;
				outStream.WriteInt32(booleanArray.length);
				for (boolean item : booleanArray) {
					SerializerFactory.GetSerializer(Boolean.TYPE).WriteObject(Boolean.valueOf(item), outStream);
				}
				booleanArray = (boolean[]) null;
				return;
			} else if (type == Byte.class) {
				byte[] byteArray = (byte[]) obj;
				outStream.WriteInt32(byteArray.length);
				outStream.write(byteArray);
				return;
			}
		}
		
		if (type == Byte.class) {
			Byte[] src = (Byte[]) obj;
			byte[] buf = new byte[src.length];
			for (int i = 0; i < src.length; i++) {
				buf[i] = src[i].byteValue();
			}
			outStream.WriteInt32(buf.length);
			outStream.write(buf);
			return;
		}
		
		Object[] array = (Object[]) obj;
		outStream.WriteInt32(array.length);
		
		//如果类型不是基本类型
		if (!TypeHelper.IsPrimitive(type)) {
			for (Object item : array)
				if (item == null) {
					SerializerFactory.GetSerializer(null).WriteObject(null, outStream);
				} else {
					Class<?> itemType = item.getClass();
					int itemTypeId = TypeHelper.GetTypeId(itemType);
					outStream.WriteInt32(itemTypeId);
					SerializerFactory.GetSerializer(itemType).WriteObject(item, outStream);
				}
		} else {
			for (Object item : array) {
				SerializerFactory.GetSerializer(item.getClass()).WriteObject(item, outStream);
			}
		}
	}

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
		if (len > inStream.MAX_DATA_LEN) {//最多支持10M的数据包
			throw new StreamException("Data length overflow.");
		}
		
		Class<?> type = TypeHelper.GetType(typeId);
		if (type == null) {
			throw new ClassNotFoundException("Cannot find class with typId,target class:" + defType.getName() + ",typeId:" + typeId);
		}
		
		if (type == Byte.class) {
			byte[] buffer = new byte[len];
			inStream.SafeRead(buffer);
			return buffer;
		}
		
		//如果要转换的为基本类型的数组
		if (defType != null && defType.getComponentType() != null && defType.getComponentType().isPrimitive()) {
			if (defType == char[].class) {
				char[] charArray = new char[len];
				for (int i = 0; i < len; i++) {
					short data = inStream.ReadInt16();
					byte[] buffer = ByteHelper.GetBytesFromInt16(data);
					charArray[i] = ByteHelper.getCharFromBytes(buffer);
				}
				return charArray;
			} else if (defType == short[].class) {
				short[] shortArray = new short[len];
				for (int i = 0; i < len; i++) {
					short shortValue = inStream.ReadInt16();
					shortArray[i] = shortValue;
				}
				return shortArray;
			} else if (defType == float[].class) {
				float[] floatArray = new float[len];
				for (int i = 0; i < len; i++) {
					int floatValue = inStream.ReadInt32();
					floatArray[i] = Float.intBitsToFloat(floatValue);
				}
				return floatArray;
			} else if (defType == long[].class) {
				long[] longArray = new long[len];
				for (int i = 0; i < len; i++) {
					longArray[i] = inStream.ReadInt64();
				}
				return longArray;
			} else if (defType == double[].class) {
				double[] doubleArray = new double[len];
				for (int i = 0; i < len; i++) {
					long doubleValue = inStream.ReadInt64();
					doubleArray[i] = Double.longBitsToDouble(doubleValue);
				}
				return doubleArray;
			} else if (defType == int[].class) {
				int[] intArray = new int[len];
				for (int i = 0; i < len; i++) {
					int intValue = inStream.ReadInt32();
					intArray[i] = intValue;
				}
				return intArray;
			} else if (defType == boolean[].class) {
				boolean[] booleanArray = new boolean[len];
				for (int i = 0; i < len; i++) {
					booleanArray[i] = inStream.read() > 0;
				}
				return booleanArray;
			} else if (defType == byte[].class) {
				byte[] buffer = new byte[len];
				inStream.SafeRead(buffer);
				return buffer;
			}
		}
		
		Object[] array = (Object[])Array.newInstance(type, len);
		if (!TypeHelper.IsPrimitive(type)) {//如果流中的数据类型不为基本类型，则需要循环解析数组的每个项目
			for (int i = 0; i < len; i++) {
				int itemTypeId = inStream.ReadInt32();
				if (itemTypeId == 0) {
					array[i] = null;
				} else {
					Class<?> itemType = TypeHelper.GetType(itemTypeId);
					if (itemType == null) {
						throw new ClassNotFoundException("Cannot find class with typId,target class:" + type.getName() + ",typeId:" + itemTypeId);
					}
					Object value = SerializerFactory.GetSerializer(itemType).ReadObject(inStream,type);
					array[i] = value;
				}
			}
		} else {
			for (int i = 0; i < len; i++) {
				Object value = SerializerFactory.GetSerializer(type).ReadObject(inStream, type);
				array[i] = value;
			}
		}
		
		inStream.SetRef(hashcode, array);
		return array;
	}

}
