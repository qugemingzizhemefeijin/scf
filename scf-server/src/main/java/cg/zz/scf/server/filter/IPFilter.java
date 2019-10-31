package cg.zz.scf.server.filter;

import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.filter.IFilter;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.util.IPTable;

/**
 * IP过滤器，如果IP不是指定的白名单，则禁止通信
 * @author chengang
 *
 */
public class IPFilter implements IFilter {
	
	private static ILog logger = LogFactory.getLogger(IPFilter.class);

	@Override
	public int getPriority() {
		return 100;
	}

	@Override
	public void filter(SCFContext context) throws Exception {
		if (IPTable.isAllow(context.getChannel().getRemoteIP())) {
			logger.info("new channel conected:" + context.getChannel().getRemoteIP());
		} else {
			logger.error("forbid ip not allow connect:" + context.getChannel().getRemoteIP());
			context.getChannel().close();
		}
	}

}
