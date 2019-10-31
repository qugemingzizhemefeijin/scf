package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.helper.TypeHelper;

/**
 * 枚举类型序列化
 * @author chengang
 *
 */
public class EnumSerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		int typeId = TypeHelper.GetTypeId(obj.getClass());
		outStream.WriteInt32(typeId);
		String value = obj.toString();
		SerializerFactory.GetSerializer(String.class).WriteObject(value, outStream);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		int typeId = inStream.ReadInt32();
		Class type = TypeHelper.GetType(typeId);
		if (type == null) {
			throw new ClassNotFoundException("Cannot find class with typId,target class:" + defType.getName() + ",typeId:" + typeId);
		}
		String value = (String) SerializerFactory.GetSerializer(String.class).ReadObject(inStream, defType);
		return Enum.valueOf(type, value);
	}

}
