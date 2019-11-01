package cg.zz.scf.server.contract.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 此注解标注在接口方法上，代表远程调用的方法
 * @author chengang
 *
 */
@Target({ java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationContract {
	
	/**
	 * 对应远程的方法名称，如果此值不为AnnotationUtil.DEFAULT_VALUE，则会强制在方法名前面加$字符
	 * @return String
	 */
	public String methodName() default AnnotationUtil.DEFAULT_VALUE;
	
}