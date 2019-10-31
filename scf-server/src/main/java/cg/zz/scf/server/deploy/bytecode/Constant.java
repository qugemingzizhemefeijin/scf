package cg.zz.scf.server.deploy.bytecode;

import cg.zz.scf.protocol.sdp.RequestProtocol;
import cg.zz.scf.protocol.utility.KeyValuePair;
import cg.zz.scf.server.contract.annotation.OperationContract;
import cg.zz.scf.server.contract.annotation.ServiceBehavior;
import cg.zz.scf.server.contract.context.IProxyFactory;
import cg.zz.scf.server.contract.context.IProxyStub;
import cg.zz.scf.server.contract.context.SCFContext;
import cg.zz.scf.server.contract.context.SCFResponse;
import cg.zz.scf.server.contract.entity.Out;
import cg.zz.scf.server.core.conver.ConvertFacotry;
import cg.zz.scf.server.core.conver.IConvert;
import cg.zz.scf.server.util.ErrorState;
import cg.zz.scf.server.util.ServiceFrameException;

public final class Constant {
	
	/**
	 * service contract config xml
	 */
	public static final String SERVICE_CONTRACT = "serviceframe.xml";

	/**
	 * out parameter name
	 */
	public static final String OUT_PARAM = Out.class.getName();

	/**
	 * IProxyStub class name
	 */
	public static final String IPROXYSTUB_CLASS_NAME = IProxyStub.class.getName();

	/**
	 * GaeaContext class name
	 */
	public static final String SCFCONTEXT_CLASS_NAME = SCFContext.class.getName();

	/**
	 * GaeaRequest class name
	 */
	public static final String SCFRESPONSE_CLASS_NAME = SCFResponse.class.getName();

	/**
	 * ServiceFrameException class name
	 */
	public static final String SERVICEFRAMEEXCEPTION_CLASS_NAME = ServiceFrameException.class.getName();

	/**
	 * Request protocol class name
	 */
	public static final String REQUEST_PROTOCOL_CLASS_NAME = RequestProtocol.class.getName();

	/**
	 * IConvert class name
	 */
	public static final String ICONVERT_CLASS_NAME = IConvert.class.getName();

	/**
	 * ConvertFactory class name
	 */
	public static final String CONVERT_FACTORY_CLASS_NAME = ConvertFacotry.class.getName();

	/**
	 * KeyValuePair protocol class name
	 */
	public static final String KEYVALUEPAIR_PROTOCOL_CLASS_NAME = KeyValuePair.class.getName();

	/**
	 * ErrorState class name
	 */
	public static final String ERRORSTATE_CLASS_NAME = ErrorState.class.getName();

	/**
	 * IProxyFactory class name
	 */
	public static final String IPROXYFACTORY_CLASS_NAME = IProxyFactory.class.getName();

	/**
	 * OperationContract class name
	 */
	public static final String OPERATIONCONTRACT_CLASS_NAME = OperationContract.class.getName();

	/**
	 * ServiceBehavior class name
	 */
	public static final String SERVICEBEHAVIOR_CLASS_NAME = ServiceBehavior.class.getName();

	/**
	 * ServiceContract class name
	 */
	public static final String SERVICECONTRACT_CLASS_NAME = ContractInfo.class.getName();

}
