package cg.zz.scf.server.filter;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.StopWatch;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.performance.monitorweb.MonitorCount;

/**
 * 监控过滤器
 * @author chengang
 *
 */
public class MonitorFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(MonitorFilter.class);

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		try {
			if (context != null) {
				StopWatch sw = context.getStopWatch();
				if (sw != null) {
					MonitorCount.messageRecv(sw);
				}
			}
		} catch (Exception mex) {
			logger.info("MonitorCount error" + mex);
		}
	}

}
