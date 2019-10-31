package cg.zz.scf.server.filter;

import java.util.Collection;

import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.context.PerformanceCounter;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.StopWatch;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.util.UDPClient;

/**
 * 执行统计的一个udp的发送fitler
 * @author chengang
 *
 */
public class ExecuteTimeFilter implements IFilter {
	
	private static int minRecordTime = 200;
	private static String serviceName;
	private static UDPClient udpClient = null;
	
	private static ILog logger = LogFactory.getLogger(ExecuteTimeFilter.class);
	
	static {
		try {
			String ip = Global.getInstance().getServiceConfig().getString("scf.log.udpserver.ip");
			int port = Global.getInstance().getServiceConfig().getInt("scf.log.udpserver.port");
			minRecordTime = Global.getInstance().getServiceConfig().getInt("scf.log.exectime.limit");
			serviceName = Global.getInstance().getServiceConfig().getString("scf.service.name");
			
			if (ip == null || port <= 0) {
				logger.error("udp ip is null or port is null");
			} else {
				udpClient = UDPClient.getInstrance(ip, port, "utf-8");
				logger.info("udp client is create.");
				logger.info("ip : "+ ip);
				logger.info("port : "+ port);
				logger.info("minRecordTime : "+ minRecordTime);
				logger.info("serviceName : "+ serviceName);
			}
		} catch (Exception ex) {
			logger.error("init ExecuteTimeFilter error", ex);
		}
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		StopWatch sw = context.getStopWatch();
		Collection<PerformanceCounter> pcList = sw.getMapCounter().values();
		
		for (PerformanceCounter pc : pcList) {
			if (pc.getExecuteTime() > minRecordTime) {
				StringBuilder sbMsg = new StringBuilder();
				sbMsg.append(serviceName);
			        sbMsg.append("--");
			        sbMsg.append(pc.getKey());
			        sbMsg.append("--time: ");
			        sbMsg.append(pc.getExecuteTime());

			        sbMsg.append(" [fromIP: ");
			        sbMsg.append(sw.getFromIP());
			        sbMsg.append(";localIP: ");
			        sbMsg.append(sw.getLocalIP() + "]");
			        
			        udpClient.send(sbMsg.toString());
			}
		}
	}
	
	@Override
	public int getPriority() {
		return 0;
	}

}
