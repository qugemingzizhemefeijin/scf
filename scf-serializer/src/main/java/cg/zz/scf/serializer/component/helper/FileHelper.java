package cg.zz.scf.serializer.component.helper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class FileHelper {
	
	/**
	 * 获得指定目录下的文件列表
	 * @param dir - 目录
	 * @param extension - 要排除的文件后缀
	 * @return List<File>
	 */
	public static List<File> getFiles(String dir, String[] extension) {
		File f = new File(dir);
		if (!f.isDirectory()) {
			return null;
		}

		List<File> fileList = new ArrayList<File>();
		getFiles(f, fileList, extension);

		return fileList;
	}
	
	/**
	 * 将f目录下所有的文件添加到fileList中
	 * @param f - 目录File
	 * @param fileList - 文件容器
	 * @param extension - 要排除的后缀
	 */
	private static void getFiles(File f, List<File> fileList, String[] extension) {
		File[] files = f.listFiles();
		if (files == null) {
			return;
		}
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				getFiles(files[i], fileList, extension);
			} else {
				if (!files[i].isFile()) continue;
				String fileName = files[i].getName().toLowerCase();
				boolean isAdd = false;
				if (extension != null) {
					for (String ext : extension) {
						if (fileName.lastIndexOf(ext) == fileName.length() - ext.length()) {
							isAdd = true;
							break;
						}
					}
				}

				if (isAdd)
					fileList.add(files[i]);
			}
		}
	}

}
