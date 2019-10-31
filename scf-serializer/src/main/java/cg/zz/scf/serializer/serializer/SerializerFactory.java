package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.exception.DisallowedSerializeException;
import cg.zz.scf.serializer.component.helper.TypeHelper;

/**
 * 序列化工厂
 * @author chengang
 *
 */
class SerializerFactory {
	
	private static final SerializerBase arraySerializer = new ArraySerializer();
	private static final SerializerBase boolSerializer = new BooleanSerializer();
	private static final SerializerBase byteSerializer = new ByteSerializer();
	private static final SerializerBase charSerializer = new CharSerializer();
	private static final SerializerBase dateTimeSerializer = new DateTimeSerializer();
	private static final SerializerBase decimalSerializer = new DecimalSerializer();
	private static final SerializerBase doubleSerializer = new DoubleSerializer();
	private static final SerializerBase enumSerializer = new EnumSerializer();
	private static final SerializerBase floatSerializer = new FloatSerializer();
	private static final SerializerBase int16Serializer = new Int16Serializer();
	private static final SerializerBase int32Serializer = new Int32Serializer();
	private static final SerializerBase int64Serializer = new Int64Serializer();
	private static final SerializerBase keyValueSerializer = new KeyValueSerializer();
	private static final SerializerBase listSerializer = new ListSerializer();
	private static final SerializerBase mapSerializer = new MapSerializer();
	private static final SerializerBase nullSerializer = new NullSerializer();
	private static final SerializerBase objectSerializer = new ObjectSerializer();
	private static final SerializerBase stringSerializer = new StringSerializer();
	
	public static SerializerBase GetSerializer(Class<?> type) throws ClassNotFoundException, DisallowedSerializeException {
		if (type == null)
			return nullSerializer;
		else if (type.isEnum()) {
			return enumSerializer;
		}
		
		int typeId = TypeHelper.GetTypeId(type);
		SerializerBase serializer = null;
		
		switch (typeId) {
			case 0:
			case 1:
				serializer = nullSerializer;
				break;
			case 2:
				serializer = objectSerializer;
				break;
			case 3:
				serializer = boolSerializer;
				break;
			case 4:
				serializer = charSerializer;
				break;
			case 5:
			case 6:
				serializer = byteSerializer;
				break;
			case 7:
			case 8:
				serializer = int16Serializer;
				break;
			case 9:
			case 10:
				serializer = int32Serializer;
				break;
			case 11:
			case 12:
				serializer = int64Serializer;
				break;
			case 13:
				serializer = floatSerializer;
				break;
			case 14:
				serializer = doubleSerializer;
				break;
			case 15:
				serializer = decimalSerializer;
				break;
			case 16:
				serializer = dateTimeSerializer;
				break;
			case 18:
				serializer = stringSerializer;
				break;
			case 19:
			case 20:
			case 21:
				serializer = listSerializer;
				break;
			case 22:
				serializer = keyValueSerializer;
				break;
			case 23:
				serializer = arraySerializer;
				break;
			case 24:
			case 25:
				serializer = mapSerializer;
				break;
			case 17:
			default:
				serializer = objectSerializer;
			}
		return serializer;
	}

}
