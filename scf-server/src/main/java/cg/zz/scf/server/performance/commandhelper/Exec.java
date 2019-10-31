package cg.zz.scf.server.performance.commandhelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;

/**
 * 执行命令
 * @author chengang
 *
 */
public class Exec extends CommandHelperBase {

	@Override
	public Command createCommand(String commandStr) {
		if(commandStr != null && !commandStr.equals("")) {
			String[] args = commandStr.split("\\|");
			if (args[0].trim().equalsIgnoreCase("exec")) {
				String execStr = commandStr.replaceFirst("exec\\|", "");
				if (execStr.startsWith("netstat") || execStr.startsWith("top")) {
					Command entity = new Command();
					entity.setCommandType(CommandType.Exec);
					if (execStr.equalsIgnoreCase("top"))
						entity.setCommand("top -bn 1");
					else {
						entity.setCommand(execStr);
					}
					return entity;
				}
			}
		}
		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.Exec) {
			Runtime rt = Runtime.getRuntime();
			Process proc = null;
			String execStr = null;
			try {
				String osName = System.getProperty("os.name");
				execStr = command.getCommand();
				
				if (osName.toLowerCase().startsWith("windows") && command.getCommand().equalsIgnoreCase("top")) {
					execStr = System.getenv("windir") + "\\system32\\wbem\\wmic.exe process get Caption," + "KernelModeTime,UserModeTime,ThreadCount";
				}
				
				logger.info("exec command:" + execStr);
				
				proc = rt.exec(execStr);
				
				StringBuilder sbMsg = new StringBuilder();
				StreamHelper errorStream = new StreamHelper(proc.getErrorStream(), sbMsg);
			        StreamHelper outputStream = new StreamHelper(proc.getInputStream(), sbMsg);
			        errorStream.start();
			        outputStream.start();
			        
				Thread.sleep(2000L);
			        proc.waitFor();
			        
			        byte[] responseByte = sbMsg.toString().getBytes("utf-8");
			        event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
			} catch (Exception e) {
				logger.error("exec command error", e);
			} finally {
				if(proc != null) proc.destroy();
			}
		}
	}

	@Override
	public void messageReceived(SCFContext context) {
		
	}

	@Override
	public void removeChannel(Command commandStr, Channel channel) {
		
	}

	@Override
	public int getChannelCount() {
		return 0;
	}
	
	/**
	 * 异步启动线程进行读取系统命令返回的信息
	 * 为什么要另外启动线程呢？
	 * 是因为系统为每个进程分配了固定大小的一个缓冲空间。
	 * 如果不开启另一个线程的化，如果缓冲空间满了。可能会造成主线程假死。
	 * @author chengang
	 *
	 */
	private class StreamHelper extends Thread {
		private InputStream inStream;
		private StringBuilder sbMsg;
		
		StreamHelper(InputStream inStream, StringBuilder sbMsg) {
			this.inStream = inStream;
			this.sbMsg = sbMsg;
		}

		@Override
		public void run() {
			InputStreamReader streamReader = null;
			BufferedReader bufferReader = null;
			try {
				streamReader = new InputStreamReader(this.inStream);
				bufferReader = new BufferedReader(streamReader);
				String line = null;
				while ((line = bufferReader.readLine()) != null) {
					this.sbMsg.append(line);
					this.sbMsg.append("\r\n");
				}
			} catch (IOException ex) {
				Exec.logger.error("read stream from exec error", ex);
			} finally {
				if (bufferReader != null) {
					try {
						bufferReader.close();
					} catch (IOException ex) {
						Exec.logger.error("close BufferedReader error when exec command", ex);
					}
				}
				if (streamReader != null) {
					try {
						streamReader.close();
					} catch (IOException ex) {
						Exec.logger.error("close InputStreamReader error when exec command", ex);
					}
				}
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException ex) {
						Exec.logger.error("close InputStream error when exec command", ex);
					}
				}
			}
		}
	}

}
