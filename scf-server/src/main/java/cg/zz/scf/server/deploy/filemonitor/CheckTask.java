package cg.zz.scf.server.deploy.filemonitor;

import java.io.File;
import java.util.TimerTask;

import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;

/**
 * 文件检查的任务
 * @author chengang
 *
 */
public class CheckTask extends TimerTask {
	
	private static ILog logger = LogFactory.getLogger(CheckTask.class);

	@Override
	public void run() {
		try {
			if (FileMonitor.fileList != null) {
				boolean isChange = false;
				for (FileInfo fInfo : FileMonitor.fileList) {
					File f = new File(fInfo.getFilePath());
					fInfo.setExists(f.exists());
					if (f.exists()) {
						long length = f.length();
						long modifyTime = f.lastModified();

						if (modifyTime == fInfo.getLastModifyTime() && length == fInfo.getFileSize()) continue;
						logger.info("file change:" + f.getAbsolutePath());
						logger.info("newLength:" + length);
						logger.info("oldLength:" + fInfo.getFileSize());
						logger.info("newModifyTime:" + modifyTime);
						logger.info("oldModifyTime:" + fInfo.getLastModifyTime());

						fInfo.setFileSize(length);
						fInfo.setLastModifyTime(modifyTime);

						isChange = true;

						if (FileMonitor.notifyCount == NotifyCount.EachChangeFile)
							FileMonitor.getInstance().fireFilesChangedEvent(fInfo);
					} else {
						isChange = true;

						if (FileMonitor.notifyCount == NotifyCount.EachChangeFile) {
							FileMonitor.getInstance().fireFilesChangedEvent(fInfo);
						}
					}
				}

				if ((FileMonitor.notifyCount == NotifyCount.Once) && (isChange))
					FileMonitor.getInstance().fireFilesChangedEvent(null);
			}
		} catch (Exception e) {
			logger.error("check file error", e);
		}
	}

}
