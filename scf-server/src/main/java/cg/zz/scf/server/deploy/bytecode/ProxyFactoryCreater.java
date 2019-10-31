package cg.zz.scf.server.deploy.bytecode;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import cg.zz.scf.server.contract.context.Global;
import cg.zz.scf.server.contract.log.ILog;
import cg.zz.scf.server.contract.log.LogFactory;
import cg.zz.scf.server.deploy.hotdeploy.DynamicClassLoader;

/**
 * 代理工厂创建类
 * @author chengang
 *
 */
public class ProxyFactoryCreater {
	
	private static ILog logger = LogFactory.getLogger(ProxyFactoryCreater.class);
	
	/**
	 * 创建ClassFile字节码类
	 * @param classLoader - 加载类
	 * @param serviceContract - ContractInfo
	 * @param time - 时间
	 * @return ClassFile
	 * @throws Exception
	 */
	public ClassFile createProxy(DynamicClassLoader classLoader, ContractInfo serviceContract, long time) throws Exception {
		String pfClsName = "ProxyFactory" + time;
		logger.info("begin create ProxyFactory:" + pfClsName);
		ClassPool pool = ClassPool.getDefault();
		List<String> jarList = classLoader.getJarList();
		for (String jar : jarList) {
			pool.appendClassPath(jar);
		}

		CtClass ctProxyClass = pool.makeClass(pfClsName, null);

		CtClass proxyFactory = pool.getCtClass(Constant.IPROXYFACTORY_CLASS_NAME);
		ctProxyClass.addInterface(proxyFactory);

		StringBuilder sbBody = new StringBuilder();
		sbBody.append("public " + Constant.IPROXYSTUB_CLASS_NAME + " getProxy(String lookup) {");
		StringBuilder sbConstructor = new StringBuilder();
		sbConstructor.append("{");
		int proxyCount = 0;
		for (SessionBean sessionBean : serviceContract.getSessionBeanList()) {
			Iterator<Entry<String, String>> it = sessionBean.getInstanceMap().entrySet().iterator();
			while (it.hasNext()) {
				Entry<String , String> entry = it.next();
				String lookup = entry.getKey().toString();

				sbBody.append("if(lookup.equalsIgnoreCase(\"" + lookup + "\")){");
				sbBody.append("return proxy");
				sbBody.append(lookup);
				sbBody.append(Global.getInstance().getServiceConfig().getString("scf.service.name"));
				sbBody.append(";}");

				sbConstructor.append("proxy");
				sbConstructor.append(lookup);
				sbConstructor.append(Global.getInstance().getServiceConfig().getString("scf.service.name"));
				sbConstructor.append("=(");
				sbConstructor.append(Constant.IPROXYSTUB_CLASS_NAME);
				sbConstructor.append(")$1.get(");
				sbConstructor.append(proxyCount);
				sbConstructor.append(");");

				CtField proxyField = CtField.make("private " + Constant.IPROXYSTUB_CLASS_NAME + " proxy" + lookup + Global.getInstance().getServiceConfig() .getString("scf.service.name") + " = null;", ctProxyClass);

				ctProxyClass.addField(proxyField);

				proxyCount++;
			}
		}
		sbBody.append("return null;}}");
		sbConstructor.append("}");

		CtMethod methodItem = CtMethod.make(sbBody.toString(), ctProxyClass);
		ctProxyClass.addMethod(methodItem);

		CtConstructor cc = new CtConstructor(new CtClass[] { pool.get("java.util.List") }, ctProxyClass);
		cc.setBody(sbConstructor.toString());
		ctProxyClass.addConstructor(cc);

		logger.debug("ProxyFactory source code:" + sbBody.toString());

		logger.info("create ProxyFactory success!!!");

		return new ClassFile(pfClsName, ctProxyClass.toBytecode());
	}

}
