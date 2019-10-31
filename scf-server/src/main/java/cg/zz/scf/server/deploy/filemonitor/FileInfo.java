package cg.zz.scf.server.deploy.filemonitor;

import java.io.File;

/**
 * 文件信息
 * @author chengang
 *
 */
public class FileInfo {
	
	/**
	 * 上次修改时间
	 */
	private long lastModifyTime;
	
	/**
	 * 文件大小
	 */
	private long fileSize;
	
	/**
	 * 文件路径
	 */
	private String filePath;
	
	/**
	 * 文件名称
	 */
	private String fileName;
	
	/**
	 * 是否存在
	 */
	private boolean exists;
	
	public FileInfo() {
		
	}
	
	public FileInfo(File f) throws Exception {
		if (f != null) {
			this.fileSize = f.length();
			this.lastModifyTime = f.lastModified();
			this.filePath = f.getCanonicalPath();
			this.fileName = f.getName();
		} else {
			throw new Exception("File is null");
		}
	}

	public long getLastModifyTime() {
		return lastModifyTime;
	}

	public void setLastModifyTime(long lastModifyTime) {
		this.lastModifyTime = lastModifyTime;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isExists() {
		return exists;
	}

	public void setExists(boolean exists) {
		this.exists = exists;
	}

}
