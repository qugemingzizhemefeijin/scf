package cg.zz.scf.server.contract.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务接口方法注解
 * @author chengang
 *
 */
@Target({ java.lang.annotation.ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationContract {
	
	/**
	 * 方法名称
	 * @return String
	 */
	public String methodName() default AnnotationUtil.DEFAULT_VALUE;
	
}
