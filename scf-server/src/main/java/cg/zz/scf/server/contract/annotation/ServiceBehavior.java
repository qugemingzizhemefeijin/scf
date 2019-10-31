package cg.zz.scf.server.contract.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务实现类注解
 * @author chengang
 *
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceBehavior {
	
	/**
	 * 命名空间
	 * @return String
	 */
	public String lookUP() default AnnotationUtil.DEFAULT_VALUE;
	
}
