package cg.zz.scf.serializer.serializer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 类型字段信息
 * @author chengang
 *
 */
public class TypeInfo {
	
	/**
	 * 类型ID
	 */
	public int TypeId;
	
	/**
	 * 字段列表
	 */
	public List<Field> Fields = new ArrayList<Field>();

	public TypeInfo(int typeId) {
		this.TypeId = typeId;
	}

}
