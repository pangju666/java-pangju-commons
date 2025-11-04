package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.NanoIdsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * NanoId集合校验注解
 * <p>验证集合中元素是否为有效的NanoId：
 * <ul>
 *     <li>当allMatch=true时，所有元素必须为有效的NanoId</li>
 *     <li>当allMatch=false时，至少一个元素为有效的NanoId</li>
 * </ul></p>
 *
 * <p>
 * 支持的类型是 {@code Collection<? extends CharSequence>}。{@code null}或空集合视为有效。
 * </p>
 *
 * @author pangju666
 * @see NanoId
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {NanoIdsValidator.class})
public @interface NanoIds {
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
	 * 是否要求所有元素匹配（默认true）
	 * <p>false表示只要任一元素匹配即通过</p>
	 *
	 * @since 1.0.0
	 */
	boolean allMatch() default true;

	/**
	 * 校验失败时的默认消息
	 */
	String message() default "集合中存在无效的NanoId";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
