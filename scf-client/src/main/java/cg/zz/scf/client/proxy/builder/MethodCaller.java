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
 * 远程方法实际的调用者，通过获取ServiceProxy来调用远程的方法
 * @author chengang
 *
 */
public class MethodCaller {
	
	/**
	 * 空的参数数组
	 */
	private static final Object[] DEFAULT_ARGS = new Object[0];
	
	/**
	 * 服务名称
	 */
	private String serviceName;
	
	/**
	 * 接口名称
	 */
	private String lookup;
	
	/**
	 * 接口版本
	 */
	private String serVersion = "SCF";
	
	/**
	 * 构造方法调用者对象
	 * @param serviceName - 服务名称
	 * @param lookup - 接口名称
	 */
	public MethodCaller(String serviceName, String lookup) {
		this.serviceName = serviceName;
		this.lookup = lookup;
	}
	
	/**
	 * 构造方法调用者对象
	 * @param serviceName - 服务名称
	 * @param lookup - 接口名称
	 * @param serVersion - 接口版本，默认是SCF，也可以指定SCFV2
	 */
	public MethodCaller(String serviceName, String lookup, String serVersion) {
		this.serviceName = serviceName;
		this.lookup = lookup;
		this.serVersion = serVersion;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object doMethodCall(Object[] args, Method methodInfo) throws Exception, Throwable {
		//获取带泛型的参数数组
		Type[] typeAry = methodInfo.getGenericParameterTypes();
		//只获取参数的类型
		Class<?>[] clsAry = methodInfo.getParameterTypes();
		if (args == null) {
			args = DEFAULT_ARGS;
		}
		if (args.length != typeAry.length) {
			throw new Exception("argument count error!");
		}
		
		//服务在这里被初始化的，同时获取远程代理对象
		ServiceProxy proxy = ServiceProxy.getProxy(this.serviceName);
		Parameter[] paras = null;//参数数组
		List<Integer> outParas = new ArrayList<>();//Out参数在参数数组中的索引位置集合，Out对象作用跟ObjectHolder一样，这里用于远程传递值
		
		boolean syn = true;//是否是同步接口
		ReceiveHandler receiveHandler = null;//接收到服务器返回后的处理对象
		int parasLength = 0;//参数数量
		
		//如果最后一个参数类型为ReceiveHandler，则实际的参数类型需要-1，并且调用是异步方法。
		if (typeAry.length >= 1 && (args[typeAry.length - 1] instanceof ReceiveHandler)) {
			syn = false;
			receiveHandler = (ReceiveHandler) args[typeAry.length - 1];
			parasLength = typeAry.length - 1;
		} else {
			parasLength = typeAry.length;
		}
		paras = new Parameter[parasLength];
		for (int i = 0; i < parasLength; i++) {
			if (args[i] instanceof Out) {
				//创建Parameter对象，标记为Out类型
				paras[i] = new Parameter(args[i], clsAry[i], typeAry[i], ParaType.Out);
				outParas.add(i);//记录Out对象的索引位置
			} else {
				//创建Parameter对象，标记为In类型
				paras[i] = new Parameter(args[i], clsAry[i], typeAry[i], ParaType.In);
			}
		}
		
		//返回的类型描述对象，查看了上下代码，好像这个对象没啥用处。
		Parameter returnPara = new Parameter(null, methodInfo.getReturnType(), methodInfo.getGenericReturnType());
		//方法名称
		String methodName = methodInfo.getName();
		OperationContract ann = methodInfo.getAnnotation(OperationContract.class);
		//这里如果方法上标注了OperationContract注解，则会获取标注的方法名称
		//这里有一个BUG，方法名会加上 $前缀
		//但是服务端没有做这个逻辑，结果会导致not found method的错误
		if (ann != null && !ann.methodName().equals(AnnotationUtil.DEFAULT_VALUE)) {
			methodName = "$" + ann.methodName();
		}
		
		//同步方法调用，需要返回结果。
		//这里同步方法的线程等待是在proxy.invoke中实现的
		//同步方法和异步方法的区别只在参数多态来搞的，开源的Gaea不支持异步调用
		if (syn) {
			InvokeResult<Object> result = proxy.invoke(returnPara, this.lookup, methodName, paras, this.serVersion);
			
			//将服务端传递回来的Out对象数据放到客户端对应位置的Out对象中
			if (result != null && result.getOutPara() != null) {
				for(int i = 0; i < outParas.size() && i < result.getOutPara().length; i++) {
					Object op = args[outParas.get(i).intValue()];
					if (op instanceof Out) {
						((Out) op).setOutPara(result.getOutPara()[i]);
					}
				}
			}
			return result.getResult();
		}
		
		//异步方法不需要返回结果，直接调用即可
		//这里实际上我觉得可以返回一个Future类型的对象更好一些
		proxy.invoke(returnPara, this.lookup, methodName, paras, receiveHandler);
		return null;
	}

}
