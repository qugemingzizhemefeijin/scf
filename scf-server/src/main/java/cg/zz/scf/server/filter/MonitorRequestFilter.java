package cg.zz.scf.server.filter;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;

/**
 * 监控的请求过滤器
 * @author chengang
 *
 */
public class MonitorRequestFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(MonitorRequestFilter.class);

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		logger.debug("MonitorRequestFilter set monitor true");
		context.setMonitor(true);
	}

}
