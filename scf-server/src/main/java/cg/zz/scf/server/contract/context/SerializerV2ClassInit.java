package cg.zz.scf.server.contract.context;

import cg.zz.scf.serializer.serializer.Serializer;
import cg.zz.scf.server.contract.init.IInit;

/**
 * 序列化扫描器初始化V2版本
 * @author chengang
 *
 */
public class SerializerV2ClassInit implements IInit {

	@Override
	public void init() {
		new Serializer();
	}

}
