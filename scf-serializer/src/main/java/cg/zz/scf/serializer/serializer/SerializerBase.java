package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * 序列化的抽象类
 * @author chengang
 *
 */
public abstract class SerializerBase {
	
	/**
	 * 序列化写入
	 * @param obj - Object
	 * @param outStream - SCFOutStream
	 * @throws Exception
	 */
	public abstract void WriteObject(Object obj, SCFOutStream outStream) throws Exception;

	/**
	 * 序列化读取
	 * @param inStream - SCFInStream
	 * @param defType - Class<?>
	 * @return Object
	 * @throws Exception
	 */
	public abstract Object ReadObject(SCFInStream inStream, Class<?> defType) throws Exception;

}
