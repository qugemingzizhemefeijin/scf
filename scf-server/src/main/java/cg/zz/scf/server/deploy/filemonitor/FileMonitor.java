package cg.zz.scf.server.deploy.filemonitor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.util.FileHelper;

/**
 * 检查jar文件是否有变动的工具类
 * @author chengang
 *
 */
public final class FileMonitor {
	
	private static ILog logger = LogFactory.getLogger(FileMonitor.class);
	
	/**
	 * 文件更改通知的监听器列表
	 */
	private static List<IListener> listenerList = new ArrayList<IListener>();

	/**
	 * 需要检查的文件列表
	 */
	static List<FileInfo> fileList = new ArrayList<FileInfo>();

	/**
	 * check interval, default:1000ms
	 */
	private static long interval = 1000L;

	/**
	 * 定时器
	 */
	private static Timer timer = null;

	static NotifyCount notifyCount = null;
	static FileMonitor monitor;
	
	/**
	 * 获得单例
	 * @return FileMonitor
	 */
	public static FileMonitor getInstance() {
		if (monitor == null) {
			synchronized (FileMonitor.class) {
				if (monitor == null) {
					monitor = new FileMonitor();
				}
			}
		}
		return monitor;
	}
	
	public void start() {
		timer = new Timer();
		//延时1秒后每个1秒执行一次
		timer.schedule(new CheckTask(), 1000L, interval);
	}
	
	public void addListener(IListener listener) {
		listenerList.add(listener);
	}

	public void addMonitorFile(FileInfo fileInfo) {
		fileList.add(fileInfo);
	}

	public void addMonitorFile(String dir) throws Exception {
		List<File> fList = FileHelper.getFiles(dir, new String[] { "jar", "ear", "war", "xml" });
		for (File file : fList) {
			logger.info("add monitor file:" + file.getAbsolutePath());
			fileList.add(new FileInfo(file));
		}
	}
	
	public void fireFilesChangedEvent(FileInfo fi) {
		logger.info("listenerList size : " + listenerList.size());
		for (int i = 0; i < listenerList.size(); i++)
			((IListener) listenerList.get(i)).fileChanged(fi);
	}

	public List<FileInfo> getMonitoredFiles() {
		return fileList;
	}
	
	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		FileMonitor.interval = interval;
	}

	public void setNotifyCount(NotifyCount notifyCount) {
		FileMonitor.notifyCount = notifyCount;
	}

	public NotifyCount getNotifyCount() {
		return notifyCount;
	}

}
