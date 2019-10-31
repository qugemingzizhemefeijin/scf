package cg.zz.scf.server.contract.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 服务接口注解
 * @author chengang
 *
 */
@Target({ java.lang.annotation.ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ServiceContract {
	
	/**
	 * 是否所有
	 * @return True or False
	 */
	public boolean defaultAll() default false;
	
}
