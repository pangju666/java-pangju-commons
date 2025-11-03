package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.XssValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * XSS防护校验注解
 * <p>验证字符串是否包含HTML内容</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {XssValidator.class})
public @interface Xss {
	/**
	 * 校验失败时的默认消息
	 */
	String message() default "存在html字符串";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
