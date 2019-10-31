package cg.zz.scf.serializer.component.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.TYPE})
@Documented
public @interface SCFSerializable {
	
	/**
	 * 名称
	 * @return String
	 */
	String name() default "";

	/**
	 * 是否全部
	 * @return True or False
	 */
	boolean defaultAll() default false;

}
