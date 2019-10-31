package cg.zz.scf.serializer.serializer;

import cg.zz.scf.serializer.component.SCFInStream;
import cg.zz.scf.serializer.component.SCFOutStream;

/**
 * 序列化基础接口
 * @author chengang
 *
 */
public interface ISCFSerializer {
	
	/**
	 * 序列化对象
	 * @param outStream - SCFOutStream
	 */
	public abstract void Serialize(SCFOutStream outStream);

	/**
	 * 反序列化接口
	 * @param inStream - SCFInStream
	 */
	public abstract void Derialize(SCFInStream inStream);

}
