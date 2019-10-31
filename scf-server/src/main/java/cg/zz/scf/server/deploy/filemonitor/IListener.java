package cg.zz.scf.server.deploy.filemonitor;

/**
 * 文件监听器
 * @author chengang
 *
 */
public interface IListener {
	
	/**
	 * 当文件发生更改时触发
	 * @param fileInfo - FileInfo
	 */
	public void fileChanged(FileInfo fileInfo);

}
