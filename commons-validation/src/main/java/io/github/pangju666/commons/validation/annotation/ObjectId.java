package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.ObjectIdValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * ObjectId校验注解
 * <p>验证字符串是否为有效的ObjectId</p>
 *
 * <p>
 * 支持的类型是 {@code CharSequence}。{@code null} 视为有效，空白字符串视为无效。
 * </p>
 *
 * @author pangju666
 * @see org.bson.types.ObjectId
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {ObjectIdValidator.class})
public @interface ObjectId {
	/**
	 * 校验失败时的默认消息
	 */
	String message() default "无效的ObjectId";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
