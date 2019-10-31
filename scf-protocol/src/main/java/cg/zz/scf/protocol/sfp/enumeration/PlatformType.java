package cg.zz.scf.protocol.sfp.enumeration;

/**
 * 平台类型枚举
 * @author chengang
 *
 */
public enum PlatformType {

	Dotnet(0),
	Java(1),
	C(2);

	private final int num;
	
	private PlatformType(int num) {
		this.num = num;
	}

	public int getNum() {
		return num;
	}

	public static PlatformType getPlatformType(int num) {
		for (PlatformType type : PlatformType.values()) {
			if (type.getNum() == num) {
				return type;
			}
		}
		return null;
	}

}
