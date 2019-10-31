package cg.zz.scf.server.contract.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpPathParameter {
	
	/**
	 * 参数映射名称
	 * @return String
	 */
	public String mapping();
	
	/**
	 * 参数类型
	 * @return HttpParameterType
	 */
	public HttpParameterType paramType() default HttpParameterType.PathParameter;

}
