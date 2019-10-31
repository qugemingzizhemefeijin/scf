package cg.zz.scf.protocol.compress;

/**
 * 不做任何压缩
 * @author chengang
 *
 */
public class UnCompress extends CompressBase {

	@Override
	public byte[] unzip(byte[] buffer) throws Exception {
		return buffer;
	}

	@Override
	public byte[] zip(byte[] buffer) throws Exception {
		return buffer;
	}

}
