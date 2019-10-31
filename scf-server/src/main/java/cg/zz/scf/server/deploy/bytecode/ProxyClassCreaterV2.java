package cg.zz.scf.server.deploy.bytecode;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import cg.zz.scf.server.contract.annotation.AnnotationUtil;
import cg.zz.scf.server.contract.annotation.OperationContract;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.deploy.hotdeploy.DynamicClassLoader;

/**
 * 代理类创建者V2
 * @author chengang
 *
 */
public class ProxyClassCreaterV2 {
	
	private static ILog logger = LogFactory.getLogger(ProxyClassCreater.class);
	
	public List<ClassFile> createProxy(DynamicClassLoader classLoader, ContractInfo serviceContract, long time) throws Exception {
		logger.info("begin create dynamic proxy v2...");
		List<ClassFile> clsList = new ArrayList<ClassFile>();
		
		for (SessionBean sessionBean : serviceContract.getSessionBeanList()) {
			//必须是接口类
			if (sessionBean.getInterfaceClass() != null) {
				//此instanceMap类的key为实现类的名称或者指定的lookup
				//value为接口实现类的全名
				Iterator<Entry<String, String>> it = sessionBean.getInstanceMap().entrySet().iterator();
				while (it.hasNext()) {
					Entry<String , String> entry = it.next();
					String lookup = entry.getKey().toString();
					String implClassName = entry.getValue().toString();
					
					String proxyClassName = lookup + "ProxyStub" + time;
					logger.info("create => " + proxyClassName);
					logger.info("implClassName:" + implClassName);
					
					ClassPool pool = ClassPool.getDefault();
					
					List<String> jarList = classLoader.getJarList();
					for (String jar : jarList) {
						pool.appendClassPath(jar);
					}
					
					CtClass ctProxyClass = pool.makeClass(proxyClassName, null);
					
					CtClass localProxy = pool.getCtClass(Constant.IPROXYSTUB_CLASS_NAME);
					ctProxyClass.addInterface(localProxy);
					
					CtField proxyField = CtField.make("private static " + sessionBean.getInterfaceName() +
							" serviceProxy = new " + implClassName + "();", ctProxyClass);
					ctProxyClass.addField(proxyField);
					
					List<MethodInfo> methodList = sessionBean.getInterfaceClass().getMethodList();
					
					for (MethodInfo mi : methodList) {
						Method m = mi.getMethod();
						logger.debug("create method:" + m.getName());
						String methodStr = createMethods(proxyClassName, mi);
						logger.debug("method(" + m.getName() + ") source code:" + methodStr);
						CtMethod methodItem = CtMethod.make(methodStr, ctProxyClass);
						ctProxyClass.addMethod(methodItem);
					}
					
					String invokeMethod = createInvoke(proxyClassName, methodList);
					logger.debug("create invoke method:" + invokeMethod);
					CtMethod invoke = CtMethod.make(invokeMethod, ctProxyClass);
					ctProxyClass.addMethod(invoke);

					clsList.add(new ClassFile(proxyClassName, ctProxyClass.toBytecode()));
				}
			}
		}
		
		logger.info("create dynamic proxy success!!!");
		return clsList;
	}
	
	/**
	 * 创建调用方法
	 * @param className - 被代理的类名
	 * @param miList - 代理的方法集合
	 * @return String
	 */
	private String createInvoke(String className, List<MethodInfo> miList) {
		StringBuilder sb = new StringBuilder();
		sb.append("public " + Constant.SCFRESPONSE_CLASS_NAME + " invoke(" + Constant.SCFCONTEXT_CLASS_NAME + " context) throws " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + " {");
		sb.append("String methodName = ((" + Constant.REQUEST_PROTOCOL_CLASS_NAME + ")context.getScfRequest().getProtocol().getSdpEntity()).getMethodName();");
		for (MethodInfo mi : miList) {
		      String methodName = getMethodName(mi);

		      sb.append("if(methodName.equalsIgnoreCase(\"");
		      sb.append(methodName);
		      sb.append("\")){return ");
		      sb.append(methodName);
		      sb.append("(context);}");
		}
		sb.append("throw new " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + "(\"method:" + className + ".invoke--msg:not found method (\"+methodName+\")\", context.getChannel().getRemoteIP(), context.getChannel().getLocalIP(), context.getScfRequest().getProtocol().getSdpEntity(), " + Constant.ERRORSTATE_CLASS_NAME + ".NotFoundMethodException, null);");
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 创建要代理的方法
	 * @param className - 被代理的类名
	 * @param mi - 方法
	 * @return String
	 */
	public String createMethods(String className, MethodInfo mi) {
		String methodName = getMethodName(mi);
		
		StringBuilder sb = new StringBuilder();
		sb.append("public " + Constant.SCFRESPONSE_CLASS_NAME + " ");
		sb.append(methodName);
		sb.append("(" + Constant.SCFCONTEXT_CLASS_NAME + " context) throws " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + " {");

		//装载序列化解析器
		defineConvertRequest(sb);

		//装载方法参数
		definaMethodParam(sb, className, mi);

		//设置调用的方法参数值
		setValueToParam(sb, className, mi);

		//调用真正的方法
		invokeRealMethod(sb, className, mi);

		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * 装载序列化解析器
	 * @param sb - StringBuilder
	 */
	private void defineConvertRequest(StringBuilder sb) {
		sb.append(Constant.ICONVERT_CLASS_NAME + " convert = " + Constant.CONVERT_FACTORY_CLASS_NAME + ".getConvert(context.getScfRequest().getProtocol());");
		sb.append(Constant.REQUEST_PROTOCOL_CLASS_NAME + " request = (" + Constant.REQUEST_PROTOCOL_CLASS_NAME + ")context.getScfRequest().getProtocol().getSdpEntity();");
		sb.append("java.util.List listKV = request.getParaKVList();");
	}
	
	/**
	 * 装载方法参数
	 * @param sb - StringBuilder
	 * @param className - 要代理的类名
	 * @param mi - MethodInfo
	 */
	private void definaMethodParam(StringBuilder sb, String className, MethodInfo mi) {
		Class<?>[] mType = mi.getMethod().getParameterTypes();
		Type[] mGenericType = mi.getMethod().getGenericParameterTypes();
		
		sb.append("java.util.List listOutPara = new java.util.ArrayList();");
		
		for (int i = 0; i < mGenericType.length; i++) {
			String paraName = mGenericType[i].toString().replaceFirst("class ", "");
			boolean isOutPara = false;
			if (paraName.startsWith(Constant.OUT_PARAM)) {
				isOutPara = true;
			}

			if (paraName.startsWith("[")) {
				paraName = mType[i].getCanonicalName();
			}
			if (isOutPara) {
				sb.append(Constant.OUT_PARAM + " arg" + i);
			} else {
				sb.append(paraName.replaceAll("\\<.*\\>", ""));
				sb.append(" arg" + i);
			}

			if (!isOutPara) {
				paraName = paraName.replaceAll("java.util.", "").replaceAll("java.lang.", "");

				if (paraName.equals("long"))
					sb.append(" = 0L;");
				else if (paraName.equals("float"))
					sb.append(" = 0F;");
				else if (paraName.equals("double"))
					sb.append(" = 0D;");
				else if (paraName.equals("int"))
					sb.append(" = 0;");
				else if (paraName.equals("short"))
					sb.append(" = (short)0;");
				else if (paraName.equals("byte"))
					sb.append(" = (byte)0;");
				else if (paraName.equals("boolean"))
					sb.append(" = false;");
				else if (paraName.equals("char")) {
					sb.append(" = (char)'\\0';");
				} else if (paraName.equals("Long"))
					sb.append(" = new Long(\"0\");");
				else if (paraName.equals("Float"))
					sb.append(" = new Float(\"0\");");
				else if (paraName.equals("Double"))
					sb.append(" = new Double(\"0\");");
				else if (paraName.equals("Integer"))
					sb.append(" = new Integer(\"0\");");
				else if (paraName.equals("Short"))
					sb.append(" = new Short(\"0\");");
				else if (paraName.equals("Byte"))
					sb.append(" = new Byte(\"0\");");
				else if (paraName.equals("Boolean"))
					sb.append(" = new Boolean(\"false\");");
				else if (paraName.equals("Character"))
					sb.append(" = new Character((char)'\\0');");
				else
					sb.append(" = null;");
			} else {
				sb.append(" = new " + Constant.OUT_PARAM);
				sb.append("();");
			}

			if (isOutPara) sb.append("listOutPara.add(arg" + i + ");");
		}
	}
	
	/**
	 * 设置参数的值
	 * @param sb - StringBuilder
	 * @param className - 被代理的类
	 * @param mi - MethodInfo
	 */
	private void setValueToParam(StringBuilder sb, String className, MethodInfo mi) {
		Class<?>[] mType = mi.getMethod().getParameterTypes();
		Type[] mGenericType = mi.getMethod().getGenericParameterTypes();
		String methodName = getMethodName(mi);

		if (mGenericType.length > 0) {
			sb.append("try {");
		}
		for (int i = 0; i < mGenericType.length; i++) {
			String paraName = mGenericType[i].toString().replaceFirst("class ", "");
			boolean isOutPara = false;
			if (paraName.startsWith(Constant.OUT_PARAM)) {
				isOutPara = true;
			}

			if (paraName.startsWith("[")) {
				paraName = mType[i].getCanonicalName();
			}

			String pn = paraName.replaceAll("java.util.", "").replaceAll("java.lang.", "");
			if (!isOutPara) {
				if ((pn.equalsIgnoreCase("String")) || (pn.equalsIgnoreCase("int"))
					|| (pn.equalsIgnoreCase("Integer")) || (pn.equalsIgnoreCase("long"))
					|| (pn.equalsIgnoreCase("short")) || (pn.equalsIgnoreCase("float"))
					|| (pn.equalsIgnoreCase("boolean")) || (pn.equalsIgnoreCase("double"))
					|| (pn.equalsIgnoreCase("char")) || (pn.equalsIgnoreCase("Character"))
					|| (pn.equalsIgnoreCase("byte"))) {
					
					sb.append("arg" + i);
					sb.append(" = convert.convertTo" + pn + "(((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(" + i + ")).getValue());");
				} else {
					sb.append("arg" + i);
					sb.append(" = (" + paraName.replaceAll("\\<.*\\>", "") + ")convert.convertToT(((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(" + i + ")).getValue(), ");
					sb.append(paraName.replaceAll("<.*?>", "") + ".class");

					if ((paraName.indexOf("java.util.List") >= 0)
						|| (paraName.indexOf("java.util.ArrayList") >= 0)
						|| (paraName.indexOf("java.util.Vector") >= 0)
						|| (paraName.indexOf("java.util.Set") >= 0)
						|| (paraName.indexOf("java.util.HashSet") >= 0)) {
						
						sb.append(", ");
						sb.append(paraName.replaceAll("java.util.List<", "")
							.replaceAll("java.util.ArrayList<", "")
							.replaceAll("java.util.Vector<", "")
							.replaceAll("java.util.Set<", "")
							.replaceAll("java.util.HashSet<", "")
							.replaceAll(">", ""));

						sb.append(".class");
					}
					sb.append(");");
				}
			} else {
				String outType = paraName.replaceAll(Constant.OUT_PARAM + "<", "");
				outType = outType.substring(0, outType.length() - 1);

				String outpn = outType.replaceAll("java.util.", "").replaceAll("java.lang.", "");

				if ((outpn.equalsIgnoreCase("String")) || (outpn.equalsIgnoreCase("Integer"))
					|| (outpn.equalsIgnoreCase("Long"))
					|| (outpn.equalsIgnoreCase("Short"))
					|| (outpn.equalsIgnoreCase("Float"))
					|| (outpn.equalsIgnoreCase("Boolean"))
					|| (outpn.equalsIgnoreCase("Double"))
					|| (outpn.equalsIgnoreCase("Character"))
					|| (outpn.equalsIgnoreCase("Byte"))) {
					
					sb.append("arg" + i);
					sb.append(".setOutPara(convert.convertTo" + outpn + "(((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(" + i + ")).getValue()));");
				} else {
					sb.append("arg" + i);
					sb.append(".setOutPara((" + outType + ")convert.convertToT(((" + Constant.KEYVALUEPAIR_PROTOCOL_CLASS_NAME + ")listKV.get(" + i + ")).getValue(), " + outType + ".class");

					if ((outType.indexOf("java.util.List") >= 0)
						|| (outType.indexOf("java.util.ArrayList") >= 0)
						|| (outType.indexOf("java.util.Vector") >= 0)
						|| (outType.indexOf("java.util.Set") >= 0)
						|| (outType.indexOf("java.util.HashSet") >= 0)) {
						
						sb.append(", ");
						sb.append(outType.replaceAll("java.util.List<", "")
							.replaceAll("java.util.ArrayList<", "")
							.replaceAll("java.util.Vector<", "")
							.replaceAll("java.util.Set<", "")
							.replaceAll("java.util.HashSet<", "")
							.replaceAll(">", ""));

						sb.append(".class");
					}
					sb.append("));");
				}
			}
		}

		if (mGenericType.length > 0) {
			sb.append("} catch (Exception e) {");
			sb.append("throw new " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + "(\"method:" + className + "." + methodName + "--msg:parse scfRequest error\", context.getChannel().getRemoteIP(), context.getChannel().getLocalIP(), context.getScfRequest().getProtocol().getSdpEntity(), " + Constant.ERRORSTATE_CLASS_NAME + ".ParaException, e);");
			sb.append("}");
		}
	}
	
	/**
	 * 调用真正的方法
	 * @param sb - StringBuilder
	 * @param className - 被代理的类
	 * @param mi - MethodInfo
	 */
	private void invokeRealMethod(StringBuilder sb, String className, MethodInfo mi) {
		Type[] mGenericType = mi.getMethod().getGenericParameterTypes();
		String methodName = getMethodName(mi);

		Class<?> classReturn = mi.getMethod().getReturnType();
		Type typeReturn = mi.getMethod().getGenericReturnType();
		String returnValueType = typeReturn.toString().replaceFirst("class ", "");
		if (returnValueType.startsWith("[")) {
			returnValueType = classReturn.getCanonicalName();
		}

		sb.append("try {");
		if (!returnValueType.equalsIgnoreCase("void")) {
			sb.append(returnValueType.replaceAll("\\<.*\\>", "") + " returnValue = ");
		}

		sb.append("serviceProxy.");
		sb.append(mi.getMethod().getName());
		sb.append("(");

		for (int i = 0; i < mGenericType.length; i++) {
			sb.append("arg");
			sb.append(i);
			if (i != mGenericType.length - 1) {
				sb.append(", ");
			}
		}
		sb.append(");");

		if (!returnValueType.equalsIgnoreCase("void"))
			sb.append("return new " + Constant.SCFRESPONSE_CLASS_NAME + "(returnValue");
		else {
			sb.append("return new " + Constant.SCFRESPONSE_CLASS_NAME + "(null");
		}
		sb.append(", listOutPara);");
		sb.append("} catch (Exception e) {");
		sb.append("throw new " + Constant.SERVICEFRAMEEXCEPTION_CLASS_NAME + "(\"method:" + className + "." + methodName + "--msg:invoke real service error\", context.getChannel().getRemoteIP(), context.getChannel().getLocalIP(), context.getScfRequest().getProtocol().getSdpEntity(), " + Constant.ERRORSTATE_CLASS_NAME + ".ServiceException, e);");
		sb.append("}");
	}
	
	/**
	 * 获得执行方法的名称
	 * @param mi - MethodInfo
	 * @return String
	 */
	private String getMethodName(MethodInfo mi) {
		String methodName = "";
		OperationContract oc = (OperationContract) mi.getMethod().getAnnotation(OperationContract.class);
		if (oc == null) {
			methodName = mi.getMethod().getName();
		} else if (!oc.methodName().equalsIgnoreCase(AnnotationUtil.DEFAULT_VALUE))
			methodName = oc.methodName();
		else {
			methodName = mi.getMethod().getName();
		}

		return methodName;
	}
	
	/**
	 * 得到一个精简的参数名称
	 * @param paraName - 参数原名称
	 * @return String
	 */
	public String getSimpleParaName(String paraName) {
		if (paraName.indexOf(".") > 0) {
			paraName = paraName.replaceAll(" ", "");
			String[] pnAry = paraName.split("");

			List<String> originalityList = new ArrayList<String>();
			List<String> replaceList = new ArrayList<String>();

			String tempP = "";
			for (int i = 0; i < pnAry.length; i++) {
				if (pnAry[i].equalsIgnoreCase("<")) {
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if (pnAry[i].equalsIgnoreCase(">")) {
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if (pnAry[i].equalsIgnoreCase(",")) {
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if (i == pnAry.length - 1) {
					originalityList.add(tempP);
					replaceList.add(tempP.substring(tempP.lastIndexOf(".") + 1));
					tempP = "";
				} else if ((!pnAry[i].equalsIgnoreCase("[")) && (!pnAry[i].equalsIgnoreCase("]"))) {
					tempP = tempP + pnAry[i];
				}

			}

			for (int i = 0; i < replaceList.size(); i++) {
				paraName = paraName.replaceAll((String) originalityList.get(i),(String) replaceList.get(i));
			}
			return paraName;
		}
		return paraName;
	}

}
