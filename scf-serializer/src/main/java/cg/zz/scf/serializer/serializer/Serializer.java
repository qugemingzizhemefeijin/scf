package cg.zz.scf.serializer.serializer;

import java.nio.charset.Charset;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;
import cg.zz.scf.serializer.component.TypeMap;
import cg.zz.scf.serializer.component.helper.ClassHelper;

/**
 * 序列化
 * @author chengang
 *
 */
public class Serializer {
	
	@Deprecated
	public static String[] JarPath;
	
	private Charset _Encoder = Charset.forName("UTF-8");
	
	static {
		TypeMap.InitTypeMap();

		JarPath = null;
	}
	
	public Serializer() {
		
	}
	
	public Serializer(Charset encoder) {
		this._Encoder = encoder;
	}
	
	@Deprecated
	public static void SetJarPath(String[] jarPath) {
		System.err.println("------------------------------------注意!!!------------------------------------------------------");
		System.err.println("注意!!!指定了JarPath重新扫描jar文件，强烈建议不使用指定JarPath方式扫描jar文件，请及时纠正，以免版本升级后带来系统错误！");
		System.err.println("-----------------------------------------------------------------------------------------------------");
		JarPath = jarPath;
		TypeMap.InitTypeMap();
	}
	
	/**
	 * 将对象序列化成byte[]数组
	 * @param obj - Object
	 * @return byte[]
	 * @throws Exception
	 */
	public byte[] Serialize(Object obj) throws Exception {
		SCFOutStream stream = null;
		try {
			stream = new SCFOutStream();
			stream.Encoder = this._Encoder;
			if (obj == null) {
				SerializerFactory.GetSerializer(null).WriteObject(null, stream);
			} else {
				Class<?> type = obj.getClass();
				if ((obj instanceof ISCFSerializer))
					((ISCFSerializer) obj).Serialize(stream);
				else {
					SerializerFactory.GetSerializer(type).WriteObject(obj, stream);
				}
			}
			byte[] result = stream.toByteArray();
			return result;
		} finally {
			if (stream != null)
				stream.close();
		}
	}

	/**
	 * 将数据反序列为对象
	 * @param buffer - byte[]
	 * @param type - 反序列化成指定的对象
	 * @return Object
	 * @throws Exception
	 */
	public Object Derialize(byte[] buffer, Class<?> type) throws Exception {
		SCFInStream stream = null;
		try {
			stream = new SCFInStream(buffer);
			stream.Encoder = this._Encoder;
			if (ClassHelper.InterfaceOf(type, ISCFSerializer.class)) {
				ISCFSerializer obj = (ISCFSerializer) type.newInstance();
				obj.Derialize(stream);
				return obj;
			}
			return SerializerFactory.GetSerializer(type).ReadObject(stream, type);
		} finally {
			if (stream != null) stream.close();
		}
	}

}
