package cg.zz.scf.server.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件操作工具类
 * @author chengang
 *
 */
public final class FileHelper {
	
	/**
	 * 创建文件并且默认写入指定的内容
	 * @param filePath - 文件路径
	 * @param content - 内容
	 * @throws IOException
	 */
	public static void createFile(String filePath, String content) throws IOException {
		FileWriter writer = null;
		try {
			writer = new FileWriter(filePath);
			writer.write(content);
		} catch(IOException ex) {
			throw ex;
		} finally {
			if(writer != null) {
				writer.close();
			}
		}
	}
	
	/**
	 * 创建一个目录
	 * @param path - 目录地址
	 */
	public static void createFolder(String path){
		File file = new File(path);
		file.mkdirs();
	}
	
	/**
	 * 删除一个文件
	 * @param path - 文件地址
	 */
	public static void deleteFile(String path) {
		File file = new File(path);
		file.deleteOnExit();
	}
	
	/**
	 * 获得文件集合
	 * @param dir - 目录地址
	 * @param extension - 要获取的文件后缀
	 * @return List<File>
	 */
	public static List<File> getFiles(String dir, String... extension) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			return null;
		}
		
		List<File> fileList = new ArrayList<File>();
		getFiles(f, fileList, extension);
		
		return fileList;
	}
	
	/**
	 *  返回多个目录下的不重复名称的文件路径
	 * @param dirs - 目录
	 * @return List<String>
	 * @throws IOException
	 */
	public static List<String> getUniqueLibPath(String... dirs) throws IOException {
		List<String> jarList = new ArrayList<String>();
		List<String> fileNameList = new ArrayList<String>();
		
		for(String dir : dirs) {
			List<File> fileList = FileHelper.getFiles(dir, "rar", "jar", "war", "ear");
			if(fileList != null) {
				for(File file : fileList) {
					if(!fileNameList.contains(file.getName())) {
						jarList.add(file.getCanonicalPath());
						fileNameList.add(file.getName());
					}
				}
			}
		}
		
		return jarList;
	}
	
	/**
	 * 获取添加的文件放入fileList集合中
	 * @param f - 扫描的文件或目录
	 * @param fileList - 放入的集合
	 * @param extension - 扫描的文件后缀
	 */
	private static void getFiles(File f, List<File> fileList, String... extension) {
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getFiles(files[i], fileList, extension);
			} else if (files[i].isFile()) {
				String fileName = files[i].getName().toLowerCase();
				boolean isAdd = false;
				if(extension != null) {
					for(String ext : extension) {
						if (fileName.lastIndexOf(ext) == fileName.length() - ext.length()){
							isAdd = true;
							break;
						}
					}
				}
				
				if(isAdd) {
					fileList.add(files[i]);
				}
			}
		}
	}
	
	/**
	 * 按行读取文件
	 * @param path - 读取的文件路径
	 * @return String
	 * @throws IOException
	 */
	public static String getContentByLines(String path) throws IOException {
		File file = new File(path);
		if(!file.exists()) {
	        	throw new IOException("file not exist:" + path);
	        }
		
		BufferedReader reader = null;
		StringBuilder sbContent = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = reader.readLine()) != null) {
				sbContent.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {reader.close();} catch (IOException e) {e.printStackTrace();}
			}
		}
		
		return sbContent.toString();
	}

}
