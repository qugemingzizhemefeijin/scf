package cg.zz.scf.protocol.sfp.enumeration;

/**
 * 压缩算法枚举
 * 
 * @author chengang
 *
 */
public enum CompressType {

	/**
	 * 不压缩(无意义编号为0)
	 */
	UnCompress(0),

	/**
	 * 7zip
	 */
	SevenZip(1),

	/**
	 * DES加密
	 */
	DES(2);

	private final int num;
	
	private CompressType(int num) {
		this.num = num;
	}

	public int getNum() {
		return num;
	}

	/**
	 * 根据压缩编码获取枚举
	 * @param num - int
	 * @return CompressType
	 * @throws Exception
	 */
	public static CompressType getCompressType(int num) throws Exception {
		for (CompressType type : CompressType.values()) {
			if (type.getNum() == num) {
				return type;
			}
		}
		throw new Exception("末知的压缩格式");
	}

}
