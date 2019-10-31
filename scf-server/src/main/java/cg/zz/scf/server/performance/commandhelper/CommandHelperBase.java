package cg.zz.scf.server.performance.commandhelper;

import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;

public abstract class CommandHelperBase implements ICommandHelper {

	protected static ILog logger = LogFactory.getLogger(CommandHelperBase.class);

}
