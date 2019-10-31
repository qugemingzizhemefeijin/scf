package cg.zz.scf.client.proxy.builder;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import cg.zz.scf.client.proxy.ServiceProxy;
import cg.zz.scf.server.contract.annotation.AnnotationUtil;
import cg.zz.scf.server.contract.annotation.OperationContract;
import cg.zz.scf.server.contract.entity.Out;

/**
 * 方法调用者
 * @author chengang
 *
 */
public class MethodCaller {
	
	private String serviceName;
	private String lookup;
	
	private String serVersion = "SCF";
	
	public MethodCaller(String serviceName, String lookup) {
		this.serviceName = serviceName;
		this.lookup = lookup;
	}
	
	public MethodCaller(String serviceName, String lookup, String serVersion) {
		this.serviceName = serviceName;
		this.lookup = lookup;
		this.serVersion = serVersion;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object doMethodCall(Object[] args, Method methodInfo) throws Exception, Throwable {
		Type[] typeAry = methodInfo.getGenericParameterTypes();
		Class<?>[] clsAry = methodInfo.getParameterTypes();
		if (args == null) {
			args = new Object[0];
		}
		if (args.length != typeAry.length) {
			throw new Exception("argument count error!");
		}
		
		ServiceProxy proxy = ServiceProxy.getProxy(this.serviceName);
		Parameter[] paras = null;
		List<Integer> outParas = new ArrayList<>();
		
		boolean syn = true;
		ReceiveHandler receiveHandler = null;
		int parasLength = 0;
		
		if (typeAry.length >= 1 && (args[typeAry.length - 1] instanceof ReceiveHandler)) {
			syn = false;
			receiveHandler = (ReceiveHandler) args[typeAry.length - 1];
			parasLength = typeAry.length - 1;
		} else {
			parasLength = typeAry.length;
		}
		paras = new Parameter[parasLength];
		for (int i = 0; i < parasLength; i++) {
			if ((args[i] instanceof Out)) {
				paras[i] = new Parameter(args[i], clsAry[i], typeAry[i], ParaType.Out);
				outParas.add(Integer.valueOf(i));
			} else {
				paras[i] = new Parameter(args[i], clsAry[i], typeAry[i], ParaType.In);
			}
		}
		
		Parameter returnPara = new Parameter(null, methodInfo.getReturnType(), methodInfo.getGenericReturnType());
		String methodName = methodInfo.getName();
		OperationContract ann = (OperationContract) methodInfo.getAnnotation(OperationContract.class);
		if (ann != null && !ann.methodName().equals(AnnotationUtil.DEFAULT_VALUE)) {
			methodName = "$" + ann.methodName();
		}
		
		if (syn) {
			InvokeResult<Object> result = null;
			if (this.serVersion.equalsIgnoreCase("SCFV2"))
				result = proxy.invoke(returnPara, this.lookup, methodName, paras, this.serVersion);
			else {
				result = proxy.invoke(returnPara, this.lookup, methodName, paras);
			}
			
			if ((result != null) && (result.getOutPara() != null)) {
				for (int i = 0; (i < outParas.size()) && (i < result.getOutPara().length); i++) {
					Object op = args[((Integer) outParas.get(i)).intValue()];
					if ((op instanceof Out)) {
						((Out) op).setOutPara(result.getOutPara()[i]);
					}
				}
			}
			return result.getResult();
		}
		proxy.invoke(returnPara, this.lookup, methodName, paras, receiveHandler);
		return null;
	}

}
