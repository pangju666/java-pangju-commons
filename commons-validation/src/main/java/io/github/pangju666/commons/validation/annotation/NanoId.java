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
 * NanoId校验注解
 * <p>验证字符串是否为有效的ObjectId</p>
 *
 * <p>
 * 支持的类型是 {@code CharSequence}。{@code null} 视为有效，空白字符串视为无效。
 * </p>
 *
 * @author pangju666
 * @see io.github.pangju666.commons.lang.id.NanoId
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {XssValidator.class})
public @interface NanoId {
	/**
	 * 随机字符表
	 */
	char[] alphabet() default {'_', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f',
		'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B',
		'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
		'Y', 'Z'};

	/**
	 * id长度
	 */
	int size() default 21;

	/**
	 * 校验失败时的默认消息
	 */
	String message() default "无效的NanoId";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
