package cg.zz.scf.serializer.serializer;

import java.math.BigDecimal;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * 数字类型
 * @author chengang
 *
 */
public class DecimalSerializer extends SerializerBase {

	@Override
	public void WriteObject(Object obj, SCFOutStream outStream) throws Exception {
		SerializerFactory.GetSerializer(String.class).WriteObject(obj.toString(), outStream);
	}

	@Override
	public Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception {
		Object value = SerializerFactory.GetSerializer(String.class).ReadObject(inStream, String.class);
		if (value != null) {
			return new BigDecimal(value.toString());
		}
		return BigDecimal.ZERO;
	}

}
