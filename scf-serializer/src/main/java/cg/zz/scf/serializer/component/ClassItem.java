package cg.zz.scf.serializer.component;

/**
 * Class类型对应编号
 * @author chengang
 *
 */
public class ClassItem {
	
	/**
	 * 对应的Class
	 */
	private Class<?>[] Types;
	
	/**
	 * 对应编号
	 */
	private int TypeId;

	public ClassItem(int typeids, Class<?>... types) {
		this.Types = types;
		this.TypeId = typeids;
	}

	public Class<?> getType() {
		return this.Types[0];
	}

	public Class<?>[] getTypes() {
		return this.Types;
	}

	public int getTypeId() {
		return this.TypeId;
	}

}
