package cg.zz.scf.serializer.component.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({java.lang.annotation.ElementType.FIELD})
@Documented
public @interface SCFMember {
	
	/**
	 * 名称
	 * @return String
	 */
	String name() default "";
	
	/**
	 * 排序顺序
	 * @return int
	 */
	int sortId() default -1;

}
