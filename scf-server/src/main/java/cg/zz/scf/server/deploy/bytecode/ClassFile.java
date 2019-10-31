package cg.zz.scf.server.deploy.bytecode;

/**
 * class类信息
 * @author chengang
 *
 */
public class ClassFile {
	
	/**
	 * 类名
	 */
	private String clsName;
	
	/**
	 * 类字节码
	 */
	private byte[] clsByte;

	/**
	 * 构造函数
	 * @param clsName - 类名
	 * @param clsByte - 类字节码
	 */
	public ClassFile(String clsName, byte[] clsByte) {
		this.clsName = clsName;
		this.clsByte = clsByte;
	}

	public String getClsName() {
		return this.clsName;
	}

	public void setClsName(String clsName) {
		this.clsName = clsName;
	}

	public byte[] getClsByte() {
		return this.clsByte;
	}

	public void setClsByte(byte[] clsByte) {
		this.clsByte = clsByte;
	}

}
