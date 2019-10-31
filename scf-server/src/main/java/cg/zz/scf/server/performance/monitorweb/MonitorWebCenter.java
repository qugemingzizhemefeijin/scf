package cg.zz.scf.server.performance.monitorweb;

import java.io.IOException;
import java.util.Map;

import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.contract.server.IServer;
import cg.zz.scf.server.performance.exception.SerializeException;

/**
 * web监控中心
 * @author chengang
 *
 */
public class MonitorWebCenter implements IServer {
	
	static ILog logger = LogFactory.getLogger(MonitorWebCenter.class);
	
	static MonitorUDPClient udp;

	@Override
	public void start() throws Exception {
		logger.info("----------------monitor server start------------------");
		logger.info("-- monitor server send ip: " +Global.getInstance().getServiceConfig().getString("scf.server.monitor.sendIP"));
		logger.info("-- monitor server port: " + Global.getInstance().getServiceConfig().getInt("scf.server.monitor.sendPort"));
		logger.info("------------------------------------------------------");
		udp = MonitorUDPClient.getInstrance(Global.getInstance().getServiceConfig().getString("scf.server.monitor.sendIP"),Global.getInstance().getServiceConfig().getInt("scf.server.monitor.sendPort"), "utf-8");
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					MonitorWebCenter.control(MonitorWebCenter.udp);
				} catch (Exception e) {
					MonitorWebCenter.logger.error("init monitor server error");
					e.printStackTrace();
				}
			}
			
		});
		
		thread.setName("SCF Monitor UDP send Thread");
		thread.start();
	}

	@Override
	public void stop() throws Exception {
		udp.close();
	}
	
	public static void control(MonitorUDPClient udp) throws Exception {
		MonitorCount mc = new MonitorCount();
		MonitorProtocol countP = new MonitorProtocol(MonitorType.count, (short)0);
		MonitorProtocol abandonP = new MonitorProtocol(MonitorType.abandon, (short)0);
		MonitorProtocol frameExP = new MonitorProtocol(MonitorType.frameEx, (short)0);
		String serviceName = Global.getInstance().getServiceConfig().getString("scf.service.name");
		MonitorJVM mjvm = new MonitorJVM(udp, serviceName);
		int sendtime = Global.getInstance().getServiceConfig().getInt("scf.server.monitor.timeLag");
		
		while (true) {
			try {
				Thread.sleep(sendtime < 3000 ? 0 : sendtime - 3000);
				
				mc.initMCount();
				Thread.sleep(1000L);
				String sendStr = getSendStr(serviceName, "count", mc.getCount());
				if (sendStr != null) {
					udp.send(countP.dataCreate(sendStr.getBytes()));
				}
				
				getMaxCount(MonitorCount.getFromIP(), serviceName);
				
				FrameExCount.initCount(0);
				Thread.sleep(1000L);
				sendStr = getSendStr(serviceName, "frameex", FrameExCount.getCount());
				if (sendStr != null) {
					udp.send(frameExP.dataCreate(sendStr.getBytes()));
				}
				
				AbandonCount.initCount(0);
				Thread.sleep(1000L);
				sendStr = getSendStr(serviceName, "abandon", AbandonCount.getCount());
				if (sendStr != null) {
					udp.send(abandonP.dataCreate(sendStr.getBytes()));
				}
				
				mjvm.jvmGc();
			        mjvm.jvmGCUtil();
			        mjvm.jvmThreadCount();
			        mjvm.jvmMemory();
			        mjvm.jvmHeapMemory();
			        mjvm.jvmNoHeapMemory();
			        mjvm.jvmLoad();
			} catch (Exception ex) {
				logger.error("control method error" + ex);
			}
		}
	}
	
	/**
	 * 发送每个客户端的服务调用次数
	 * @param map - Map<String ,Integer>
	 * @param serviceName - String 服务名称
	 */
	public static void getMaxCount(Map<String, Integer> map, String serviceName) {
		MonitorProtocol protocol = new MonitorProtocol(MonitorType.count, (short)0);
		for(Map.Entry<String, Integer> me : map.entrySet()) {
			String ip = me.getKey();
			Integer count = me.getValue();
			
			StringBuffer sb = new StringBuffer();
			sb.append("ip:\t");
			sb.append((String) ip);
			sb.append("\t");
			sb.append((Integer) count);
			sb.append("\t");
			sb.append(serviceName);
			
			try {
				udp.send(protocol.dataCreate(sb.toString().getBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SerializeException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 发送服务被调用的次数
	 * @param serviceName
	 * @param sendType
	 * @param count
	 * @return
	 */
	public static String getSendStr(String serviceName, String sendType, int count) {
		StringBuffer sb = new StringBuffer();
		sb.append(sendType);
		sb.append("\t");
		sb.append(count);
		sb.append("\t");
		sb.append(serviceName);
		return sb.toString();
	}

}
