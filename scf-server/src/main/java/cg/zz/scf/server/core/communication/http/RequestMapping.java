package cg.zz.scf.server.core.communication.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cg.zz.scf.server.contract.annotation.HttpParameterType;
import cg.zz.scf.server.contract.annotation.HttpRequestMethod;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.core.conver.IConvert;
import cg.zz.scf.server.core.conver.JavaConvert;
import cg.zz.scf.server.core.conver.JsonConvert;
import cg.zz.scf.server.deploy.bytecode.ClassInfo;
import cg.zz.scf.server.deploy.bytecode.MethodInfo;
import cg.zz.scf.server.deploy.bytecode.ParamInfo;
import cg.zz.scf.server.deploy.bytecode.ScanClass;
import cg.zz.scf.server.deploy.hotdeploy.DynamicClassLoader;
import cg.zz.scf.server.util.JsonHelper;

public class RequestMapping {
	
	private static ILog logger = LogFactory.getLogger(RequestMapping.class);
	
	private static Map<String, Controler> urlMap = new HashMap<>();
	
	private static IConvert javaConvert = new JavaConvert();
	
	private static IConvert jsonConvert = new JsonConvert();
	
	private static Pattern patternParam = Pattern.compile("\\{(.+?)\\}");
	
	public static Action getAction(HttpContext context) throws Exception {
		logger.debug("http-method:" + context.getMethod());
		logger.debug("uri:" + context.getUri());
		String regUrl = getKey(context.getUri());
		logger.debug("reg-url:" + regUrl);
		
		if ((regUrl == null) || (regUrl.equalsIgnoreCase(""))) {
			throw new HttpException("url not match!!!", 404, null);
		}
		
		Controler controler = (Controler)urlMap.get(regUrl);
		
		Action action = getAction(context.getMethod(), controler);
		
		Pattern pattern = Pattern.compile(regUrl);
		Matcher matcher = pattern.matcher(context.getUri());
		if (matcher.find()) {
			for (Parameter para : action.getParamList()) {
				if (para.getParaType() == HttpParameterType.PathParameter) {
					logger.debug("url para-mapping:" + para.getMapping() + "--value:" + matcher.group(para.getUrlParaIndex() + 1));
					
					String value = matcher.group(para.getUrlParaIndex() + 1);
					para.setValue(value);
				}
			}
		}
		
		if (context.getContentBuffer() != null && context.getContentBuffer().length > 0) {
			String content = new String(context.getContentBuffer(), Global.getInstance().getServiceConfig().getString("scf.encoding"));
			KeyValue[] kvs = (KeyValue[])JsonHelper.toJava(content, KeyValue[].class);
			for (KeyValue kv : kvs) {
				for (Parameter para : action.getParamList()) {
					if (para.getMapping().equalsIgnoreCase(kv.getKey())) {
						logger.debug("content para-mapping:" + para.getMapping() + "--value:" + kv.getValue());
						
						String value = kv.getValue();
						para.setValue(value);
						break;
					}
				}
			}
		}
		
		return action;
	}
	
	protected static String getKey(String uri) {
		Set<String> set = urlMap.keySet();
		for (String reg : set) {
			if (uri.matches(reg)) {
				return reg;
			}
		}
		
		return null;
	}
	
	protected static int getUrlParaIndex(String uri, String mapping) throws Exception {
		Matcher matcher = patternParam.matcher(uri);
		
		int index = 0;
		while (matcher.find()) {
			if (matcher.group(1).equalsIgnoreCase(mapping)) {
				return index;
			}
			index++;
		}
		throw new Exception("not mapping");
	}
	
	protected static int getContentParaIndex(ParamInfo[] piAry, ParamInfo para) {
		int index = 0;
		
		for(ParamInfo pi : piAry) {
			if(pi == para) return index;
			else if(pi.getHttpPathParameter() != null && pi.getHttpPathParameter().paramType() == HttpParameterType.ContentParameter) {
				index++;
			}
		}
		
		return index;
	}
	
	protected static Action getAction(HttpRequestMethod method, Controler controler) {
		Action action = null;
		switch (method) {
			case DELETE:
				action = controler.getGetAction();
				break;
			case GET:
				action = controler.getPostAction();
				break;
			case PUT:
				action = controler.getDeleteAction();
				break;
			case HEAD:
				action = controler.getPutAction();
				break;
			case POST:
				action = controler.getHeadAction();
				break;
			default:
				action = controler.getGetAction();
		}
		
		return action;
	}
	
	protected static String createRegexUrl(String sourceUrl, ParamInfo[] piAry) {
		String regUrl = sourceUrl.toLowerCase();
		for (ParamInfo pi : piAry) {
			if(pi.getHttpPathParameter() == null || pi.getHttpPathParameter().paramType() == HttpParameterType.PathParameter) {
				if(pi.getCls() == byte.class || pi.getCls() == Byte.class
						|| pi.getCls() == short.class || pi.getCls() == Short.class
						|| pi.getCls() == int.class || pi.getCls() == Integer.class
						|| pi.getCls() == long.class || pi.getCls() == Long.class
						|| pi.getCls() == double.class || pi.getCls() == Double.class){
					regUrl = regUrl.replaceFirst("\\{"+ pi.getMapping().toLowerCase() +"\\}", "(\\\\d+)");
				} else {
					regUrl = regUrl.replaceFirst("\\{"+ pi.getMapping().toLowerCase() +"\\}", "(\\\\w+)");
				}
			}
		}
		return regUrl;
	}
	
	protected static Object convertValue(String type, String value) throws Exception {
		Object obj = null;
		if (type.equalsIgnoreCase("String"))
			obj = value;
		else if ((type.equalsIgnoreCase("int")) || (type.equalsIgnoreCase("Integer")))
			obj = Integer.valueOf(javaConvert.convertToint(value));
		else if (type.equalsIgnoreCase("long"))
			obj = Long.valueOf(javaConvert.convertTolong(value));
		else if (type.equalsIgnoreCase("short"))
			obj = Short.valueOf(javaConvert.convertToshort(value));
		else if (type.equalsIgnoreCase("float"))
			obj = Float.valueOf(javaConvert.convertTofloat(value));
		else if (type.equalsIgnoreCase("boolean"))
			obj = Boolean.valueOf(javaConvert.convertToboolean(value));
		else if (type.equalsIgnoreCase("double"))
			obj = Double.valueOf(javaConvert.convertTodouble(value));
		else if ((type.equalsIgnoreCase("char")) || (type.equalsIgnoreCase("Character")))
			obj = Character.valueOf(javaConvert.convertTochar(value));
		else if (type.equalsIgnoreCase("byte"))
			obj = Byte.valueOf(javaConvert.convertTobyte(value));
		else {
			obj = jsonConvert.convertToT(value, Class.forName(type));
		}
		
		return obj;
	}
	
	public static void init() throws Exception {
		logger.info("begin init http request mapping...");
		
		urlMap.clear();
		
		StringBuffer sbMsg = new StringBuffer();
		
		String servicePath = Global.getInstance().getRootPath() + "/service/deploy/" + Global.getInstance().getServiceConfig().getString("scf.service.name");
		
		DynamicClassLoader classLoader = new DynamicClassLoader();
		classLoader.addFolder(new String[] { servicePath });
		
		List<ClassInfo> ciList = ScanClass.getBehaviorClassInfos(servicePath, classLoader);
		for (ClassInfo ci : ciList) {
			List<MethodInfo> miList = ci.getMethodList();
			for(MethodInfo mi : miList) {
				if(mi.getHttpRequestMapping() != null) {
					String uri = mi.getHttpRequestMapping().uri();
				        String regUrl = createRegexUrl(uri, mi.getParamInfoAry());
				        Controler controler = urlMap.get(regUrl);
				        if (controler == null) {
				        	controler = new Controler();
				        }
				        
				        sbMsg.append("\n----------------- url mapping -----------------");
				        sbMsg.append("\nurl:" + uri);
				        sbMsg.append("\nreg-url:" + regUrl);
				        sbMsg.append("\nlookUP:");
				        sbMsg.append(ci.getLookUP());
				        sbMsg.append("\nmethodName:");
				        sbMsg.append(mi.getMethod().getName());
				        
				        Action action = new Action();
				        action.setLookup(ci.getLookUP());
				        action.setMethodName(mi.getMethod().getName());
				        
				        List<Parameter> paramList = new ArrayList<>();
				        ParamInfo[] piAry = mi.getParamInfoAry();
				        for(int i=0; i<piAry.length; i++) {
				        	String paraTypeName = piAry[i].getType().toString().replaceFirst("class ", "");
						if(paraTypeName.startsWith("[")){
							paraTypeName = piAry[i].getCls().getCanonicalName();
						}
						
						sbMsg.append("\n\nparaTypeName:");
						sbMsg.append(paraTypeName);
						sbMsg.append("\nmapping:");
						sbMsg.append(piAry[i].getMapping());
						
						if (piAry[i].getHttpPathParameter() != null && piAry[i].getHttpPathParameter().paramType() == HttpParameterType.ContentParameter) {
							paramList.add(new Parameter(paraTypeName,piAry[i].getMapping(),null,0,getContentParaIndex(piAry, piAry[i]),piAry[i].getIndex(),HttpParameterType.ContentParameter));
							
							sbMsg.append("\ncontentParamIndex:");
							sbMsg.append(getContentParaIndex(piAry, piAry[i]));
							sbMsg.append("\nparamType:");
							sbMsg.append(HttpParameterType.ContentParameter);
						} else {
							paramList.add(new Parameter(paraTypeName,piAry[i].getMapping(),null,getUrlParaIndex(uri, piAry[i].getMapping()),0,piAry[i].getIndex(),HttpParameterType.PathParameter));
							
							sbMsg.append("\nurlParamIndex:");
							sbMsg.append(getUrlParaIndex(uri, piAry[i].getMapping()));
							sbMsg.append("\nparamType:");
							sbMsg.append(HttpParameterType.PathParameter);
						}
						sbMsg.append("\nmethodParamIndex:");
						sbMsg.append(piAry[i].getIndex());
				        }
				        
				        action.setParamList(paramList);
				        
				        HttpRequestMethod[] methodAry = mi.getHttpRequestMapping().method();
				        for (HttpRequestMethod method : methodAry) {
				        	sbMsg.append("\nhttpMethod:");
				                sbMsg.append(method);
				                
						if (method == HttpRequestMethod.GET)
							controler.setGetAction(action);
						else if (method == HttpRequestMethod.POST)
							controler.setPostAction(action);
						else if (method == HttpRequestMethod.DELETE)
							controler.setDeleteAction(action);
						else if (method == HttpRequestMethod.HEAD)
							controler.setHeadAction(action);
						else if (method == HttpRequestMethod.PUT) {
							controler.setPutAction(action);
						}
				        }
				        urlMap.put(regUrl, controler);
				        
				        sbMsg.append("\n\n");
				}
			}
			
			logger.debug(sbMsg.toString());
			
			logger.info("finish init http request mapping!");
		}
	}

}
