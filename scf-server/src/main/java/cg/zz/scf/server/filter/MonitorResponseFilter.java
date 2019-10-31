package cg.zz.scf.server.filter;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.performance.MonitorCenter;

/**
 * 监控的返回过滤器
 * @author chengang
 *
 */
public class MonitorResponseFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(MonitorRequestFilter.class);

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		logger.debug("MonitorResponseFilter addMonitorTask");
		MonitorCenter.addMonitorTask(context);
	}

}
