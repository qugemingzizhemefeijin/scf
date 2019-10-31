package cg.zz.scf.server.deploy.bytecode;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cg.zz.scf.server.contract.annotation.AnnotationUtil;
import cg.zz.scf.server.contract.annotation.HttpPathParameter;
import cg.zz.scf.server.contract.annotation.HttpRequestMapping;
import cg.zz.scf.server.contract.annotation.OperationContract;
import cg.zz.scf.server.contract.annotation.ServiceBehavior;
import cg.zz.scf.server.contract.annotation.ServiceContract;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.deploy.hotdeploy.DynamicClassLoader;
import cg.zz.scf.server.util.ClassHelper;
import cg.zz.scf.server.util.FileHelper;

/**
 * 类扫描器
 * @author chengang
 *
 */
public final class ScanClass {
	
	private static ILog logger = LogFactory.getLogger(ScanClass.class);
	
	/**
	 * 服务元信息
	 */
	private static ContractInfo contractInfo = null;
	
	/**
	 * 接口类信息列表
	 */
	private static List<ClassInfo> contractClassInfos = null;
	
	/**
	 * 接口实现类信息列表
	 */
	private static List<ClassInfo> behaviorClassInfos = null;
	
	/**
	 * 获取全局唯一的接口信息描述
	 * @param path - 扫描目录路径
	 * @param classLoader - 类加载器
	 * @return ContractInfo
	 * @throws Exception
	 */
	public static ContractInfo getContractInfo(String path, DynamicClassLoader classLoader) throws Exception {
		if (contractInfo == null) {
			synchronized (ScanClass.class) {
				if (contractInfo == null) {
					scan(path, classLoader);
				}
			}
		}
		
		return contractInfo;
	}
	
	/**
	 * 获取所有的接口信息描述列表
	 * @param path - 扫描目录路径
	 * @param classLoader - 类加载器
	 * @return List<ClassInfo>
	 * @throws Exception
	 */
	public static List<ClassInfo> getContractClassInfos(String path, DynamicClassLoader classLoader) throws Exception {
		if (contractInfo == null) {
			synchronized (ScanClass.class) {
				if (contractInfo == null) {
					scan(path, classLoader);
				}
			}
		}
		
		return contractClassInfos;
	}
	
	/**
	 * 获取所有的接口实现类信息描述列表
	 * @param path - 扫描目录路径
	 * @param classLoader - 类加载器
	 * @return List<ClassInfo>
	 * @throws Exception
	 */
	public static List<ClassInfo> getBehaviorClassInfos(String path, DynamicClassLoader classLoader) throws Exception {
		if (contractInfo == null) {
			synchronized (ScanClass.class) {
				if (contractInfo == null) {
					scan(path, classLoader);
				}
			}
		}
		
		return behaviorClassInfos;
	}
	
	/**
	 * 扫描指定路径下的所有的jar包
	 * @param path - 扫描目录路径
	 * @param classLoader - 类加载器
	 * @throws Exception
	 */
	private static void scan(String path, DynamicClassLoader classLoader) throws Exception {
		logger.info("begin scan jar from path:" + path);
		
		List<String> jarPathList = FileHelper.getUniqueLibPath(new String[] { path });
		if (jarPathList == null || jarPathList.isEmpty()) {
			//throw new Exception("no jar fonded from path: " + path);
			logger.warn("no jar fonded from path: " + path);
			return;
		}
		
		contractClassInfos = new ArrayList<ClassInfo>();
		behaviorClassInfos = new ArrayList<ClassInfo>();
		
		for (String jpath : jarPathList) {
			Set<Class<?>> clsSet = null;
			try {
				clsSet = ClassHelper.getClassFromJar(jpath, classLoader);
			} catch (Exception ex) {
				throw ex;
			}
			
			if (clsSet == null || clsSet.isEmpty()) {
				continue;
			}
			
			for (Class<?> cls : clsSet) {
				try {
					ServiceBehavior behavior = cls.getAnnotation(ServiceBehavior.class);
					ServiceContract contract = cls.getAnnotation(ServiceContract.class);
					if(behavior == null && contract == null) {
						continue;
				    }
					
					if (contract != null) {
						ClassInfo ci = contract(cls);
						if (ci != null) contractClassInfos.add(ci);
					} else if (behavior != null) {
						ClassInfo ci = behavior(cls);
						if(ci != null) behaviorClassInfos.add(ci);
					}
				} catch (Exception ex) {
				          throw ex;
			        }
			}
		}
		
		contractInfo = createContractInfo(contractClassInfos, behaviorClassInfos);
		logger.info("finish scan jar");
	}
	
	/**
	 * 将类的元信息加载到ClassInfo中
	 * @param cls - Class
	 * @param ignoreAnnotation - 是否忽略注解，也就是加载所有类
	 * @return ClassInfo
	 */
	protected static ClassInfo contract(Class<?> cls, boolean ignoreAnnotation) {
		if (ignoreAnnotation) {
			ClassInfo ci = new ClassInfo();
			ci.setCls(cls);
			ci.setClassType(ClassType.INTERFACE);
			
			Method[] methods = cls.getDeclaredMethods();
			List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();
			
			for (Method m : methods) {
				if ((Modifier.isPublic(m.getModifiers())) || (Modifier.isProtected(m.getModifiers()))) {
					MethodInfo mi = new MethodInfo();
					mi.setMethod(m);
					
					methodInfos.add(mi);
				}
			}
			
			ci.setMethodList(methodInfos);
			return ci;
		}
		
		return contract(cls);
	}
	
	/**
	 * 解析类的接口方法，将类元信息加载到ClassInfo中
	 * @param cls - Class<?>
	 * @return ClassInfo
	 */
	protected static ClassInfo contract(Class<?> cls) {
		ServiceContract contractAnn = (ServiceContract)cls.getAnnotation(ServiceContract.class);
		
		ClassInfo ci = new ClassInfo();
		ci.setCls(cls);
		ci.setClassType(ClassType.INTERFACE);
		
		//获得类的所有的接口类
		List<Class<?>> interfaceList = getInterfaces(cls);
		List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();
		
		for (Class<?> interfaceCls : interfaceList) {
			Method[] methods = interfaceCls.getDeclaredMethods();
			//如果类的注解是加载全部，则直接将所有的方法都加入到代理列表中。
			if(contractAnn != null && contractAnn.defaultAll()) {
				for (Method m : methods) {
					if ((Modifier.isPublic(m.getModifiers())) || (Modifier.isProtected(m.getModifiers()))) {
						MethodInfo mi = new MethodInfo();
						mi.setMethod(m);
						
						methodInfos.add(mi);
					}
				}
			} else {
				for (Method m : methods) {
					if ((Modifier.isPublic(m.getModifiers())) || (Modifier.isProtected(m.getModifiers()))) {
						OperationContract oc = m.getAnnotation(OperationContract.class);
						if (oc != null) {
							MethodInfo mi = new MethodInfo();
							mi.setMethod(m);
							
							methodInfos.add(mi);
						}
					}
				}
			}
		}
		
		ci.setMethodList(methodInfos);
		return ci;
	}
	
	protected static ClassInfo behavior(Class<?> cls) throws Exception {
		ServiceBehavior behaviorAnn = (ServiceBehavior)cls.getAnnotation(ServiceBehavior.class);
		
		ClassInfo ci = new ClassInfo();
		ci.setCls(cls);
		ci.setClassType(ClassType.CLASS);
		
		//如果命名空间不为null或者默认
		if(behaviorAnn != null && !behaviorAnn.lookUP().equalsIgnoreCase(AnnotationUtil.DEFAULT_VALUE)) {
			ci.setLookUP(behaviorAnn.lookUP());
		} else {
			ci.setLookUP(cls.getSimpleName());
		}
		
		Method[] methods = cls.getDeclaredMethods();
		List<MethodInfo> methodInfos = new ArrayList<MethodInfo>();
		
		for(Method m : methods) {
			//only load public or protected method
			if(Modifier.isPublic(m.getModifiers()) || Modifier.isProtected(m.getModifiers())) {
				MethodInfo mi = new MethodInfo();
				mi.setMethod(m);
				
				HttpRequestMapping requestMappingAnn = m.getAnnotation(HttpRequestMapping.class);
				mi.setHttpRequestMapping(requestMappingAnn);
				
				Class<?>[] paramAry = m.getParameterTypes();
				Type[] types = m.getGenericParameterTypes();
				
				String[] paramNames = ClassHelper.getParamNames(cls, m);
				String[] mapping = new String[paramAry.length]; 
				HttpPathParameter[] paramAnnAry = new HttpPathParameter[paramAry.length];
				
				if(requestMappingAnn != null) {
					Object[][] annotations = ClassHelper.getParamAnnotations(cls, m);
					for(int i=0; i<annotations.length; i++) {
						for(int j=0; j<annotations[i].length; j++) {
							HttpPathParameter paramAnn = null;
							try {
								paramAnn = (HttpPathParameter)annotations[i][j];
							} catch(Exception ex) {
								
							}
							
							paramAnnAry[i] = paramAnn;
							if(paramAnn != null) {
								mapping[i] = paramAnn.mapping();
								break;
							} else {
								mapping[i] = paramNames[i];
							}
						}
					}
					
					for(int i=0; i<paramAry.length; i++) {
						if(mapping[i] == null) {
							mapping[i] = paramNames[i];
						}
					}
				}
				
				ParamInfo[] paramInfoAry = new ParamInfo[paramAry.length];
				for(int i=0; i<paramAry.length; i++) {
					paramInfoAry[i] = new ParamInfo(i,paramAry[i],types[i],paramNames[i],mapping[i],paramAnnAry[i]);
				}
				
				mi.setParamInfoAry(paramInfoAry);
				methodInfos.add(mi);
			}
		}
		
		ci.setMethodList(methodInfos);
		return ci;
	}
	
	private static ContractInfo createContractInfo(List<ClassInfo> contracts, List<ClassInfo> behaviors) {
		ContractInfo contractInfo = new ContractInfo();
		List<SessionBean> sessionBeanList = new ArrayList<SessionBean>();
		
		for(ClassInfo c : contracts) {
			SessionBean bean = new SessionBean();
			bean.setInterfaceClass(c);
			bean.setInterfaceName(c.getCls().getName());
			Map<String, String> implMap = new HashMap<String, String>();
			
			//循环创建接口实现类跟接口类的关联映射表
			//key为接口实现类的lookup或者类名
			//value为接口实现类的名称
			for(ClassInfo b : behaviors) {
				Class<?>[] interfaceAry = b.getCls().getInterfaces();
				for(Class<?> item : interfaceAry) {
					if(item == c.getCls()) {
						implMap.put(b.getLookUP(), b.getCls().getName());
						break;
					}
				}
			}
			
			bean.setInstanceMap(implMap);
			sessionBeanList.add(bean);
		}
		
		contractInfo.setSessionBeanList(sessionBeanList);
		return contractInfo;
	}
	
	/**
	 * 获得类的所有的接口
	 * @param cls - Class
	 * @return List<Class<?>>
	 */
	private static List<Class<?>> getInterfaces(Class<?> cls) {
		List<Class<?>> clsList = new ArrayList<Class<?>>();
		getInterfaces(cls, clsList);
		return clsList;
	}
	
	/**
	 * 将类以及父类的所有的接口放入到clsList集合中
	 * @param cls - Class
	 * @param clsList - List<Class<?>>
	 */
	private static void getInterfaces(Class<?> cls, List<Class<?>> clsList) {
		clsList.add(cls);
		Class<?>[] aryCls = cls.getInterfaces();
		
		if(aryCls != null && aryCls.length > 0) {
			for(Class<?> c : aryCls) {
				getInterfaces(c, clsList);
			}
		}
	}

}
