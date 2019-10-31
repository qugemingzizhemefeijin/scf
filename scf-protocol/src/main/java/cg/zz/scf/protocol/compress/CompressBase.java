package cg.zz.scf.protocol.compress;

import cg.zz.scf.protocol.sfp.enumeration.CompressType;

/**
 * 压缩抽象类
 * @author chengang
 *
 */
public abstract class CompressBase {
	
	private static CompressBase sevenZip = new SevenZip();
	
	private static CompressBase unCompress = new UnCompress();
	
	/**
	 * 根据压缩类型获取压缩类
	 * @param ct - CompressType
	 * @return CompressBase
	 * @throws Exception
	 */
	public static CompressBase getInstance(CompressType ct) throws Exception {
		if (ct == CompressType.UnCompress)
			return unCompress;
		else if(ct == CompressType.SevenZip) {
			return sevenZip;
		}
		throw new Exception("末知的压缩格式");
	}
	
	/**
	 * 解压缩
	 * @param paramArrayOfByte - byte[]
	 * @return byte[]
	 * @throws Exception
	 */
	public abstract byte[] unzip(byte[] paramArrayOfByte) throws Exception;
	
	/**
	 * 压缩
	 * @param paramArrayOfByte - byte[]
	 * @return byte[]
	 * @throws Exception
	 */
	public abstract byte[] zip(byte[] paramArrayOfByte) throws Exception;

}
