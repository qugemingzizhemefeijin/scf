package cg.zz.scf.server.performance.commandhelper;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.MessageEvent;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.performance.Command;
import cg.zz.scf.server.performance.CommandType;

/**
 * help帮助
 * @author chengang
 *
 */
public class Help extends CommandHelperBase {

	@Override
	public Command createCommand(String commandStr) {
		if ("help".equalsIgnoreCase(commandStr)) {
			Command entity = new Command();
			entity.setCommandType(CommandType.Help);
			return entity;
		}

		return null;
	}

	@Override
	public void execCommand(Command command, MessageEvent event) throws Exception {
		if (command.getCommandType() == CommandType.Help) {
			StringBuilder sbMsg = new StringBuilder();
			sbMsg.append("*******************************************************************\r\n\n");

			sbMsg.append("count[|second num|method methodName]\r\n");
			sbMsg.append("\t* show method call times in num seconds\r\n");
			sbMsg.append("\t* second  : in num seconds statistics once (num default 1)\r\n");
			sbMsg.append("\t* method  : for statistics method\r\n");
			sbMsg.append("\t* example : count\r\n");
			sbMsg.append("\t* example : count|second 3\r\n");
			sbMsg.append("\t* example : count|second 3|method getInfo\r\n\n");

			sbMsg.append("time|grep abc[|group num|column -tkda]\r\n");
			sbMsg.append("\t* show method execute time\r\n");
			sbMsg.append("\t* grep   : condition\r\n");
			sbMsg.append("\t* group  : method called num times show statistics once\r\n");
			sbMsg.append("\t* column : show column a->all t->time k->key d->description\r\n");
			sbMsg.append("\t* example: time|grep getInfo\r\n");
			sbMsg.append("\t* example: time|grep getInfo|group 10|column -tk\r\n\n");

			sbMsg.append("jvm [option] [time] [count]\r\n");
			sbMsg.append("\t*[option]:\r\n");
			sbMsg.append("\t\t* -gcutil: detection heap memory usage\r\n");
			sbMsg.append("\t\t* -class : load class\r\n");
			sbMsg.append("\t\t* -gcheap: heap memory used and committed \r\n");
			sbMsg.append("\t\t* -memory:JVM memory used \r\n");
			sbMsg.append("\t\t* -heap  :Virtual Machine heap memory used \r\n");
			sbMsg.append("\t\t* -noheap:Virtual Machine noheap memory used \r\n");
			sbMsg.append("\t\t* -thread: thread counts \r\n");
			sbMsg.append("\t\t* -help  : help\r\n");
			sbMsg.append("\t* time\t : [time] milliseconds apart test again\r\n");
			sbMsg.append("\t* count\t : detection [count] times\r\n");
			sbMsg.append("\t* example: jvm -gcutil\r\n");
			sbMsg.append("\t* example: jvm -gcutil 1000\r\n");
			sbMsg.append("\t* example: jvm -gcutil 1000 5\r\n");
			sbMsg.append("\t* example: jvm -class\r\n");
			sbMsg.append("\t* example: jvm -class 1000\r\n");
			sbMsg.append("\t* example: jvm -class 1000 5\r\n");
			sbMsg.append("\t* example: jvm -gcheap\r\n");
			sbMsg.append("\t* example: jvm -gcheap 1000\r\n");
			sbMsg.append("\t* example: jvm -gcheap 1000 5\r\n");
			sbMsg.append("\t* example: jvm -memory\r\n");
			sbMsg.append("\t* example: jvm -memory 1000\r\n");
			sbMsg.append("\t* example: jvm -memory 1000 5\r\n");
			sbMsg.append("\t* example: jvm -heap\r\n");
			sbMsg.append("\t* example: jvm -heap 1000\r\n");
			sbMsg.append("\t* example: jvm -heap 1000 5\r\n");
			sbMsg.append("\t* example: jvm -noheap\r\n");
			sbMsg.append("\t* example: jvm -noheap 1000\r\n");
			sbMsg.append("\t* example: jvm -noheap 1000 5\r\n");
			sbMsg.append("\t* example: jvm -thread\r\n");
			sbMsg.append("\t* example: jvm -thread 1000\r\n");
			sbMsg.append("\t* example: jvm -thread 1000 5\r\n\n");

			sbMsg.append("exec|top\r\n");
			sbMsg.append("    |netstat -na\r\n");
			sbMsg.append("\t* exec command  (at present only allow:top or netstat)\r\n");
			sbMsg.append("\t* example: exec|top\r\n\n");

			sbMsg.append("control * use for control scf-server\r\n\n");
			sbMsg.append("clear   * stop command\r\n\n");
			sbMsg.append("help    * show help\r\n\n");
			sbMsg.append("quit    * quit monitor\r\n\n");
			sbMsg.append("*******************************************************************\r\n\n");
			byte[] responseByte = sbMsg.toString().getBytes("utf-8");
			event.getChannel().write(ChannelBuffers.copiedBuffer(responseByte));
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

}
