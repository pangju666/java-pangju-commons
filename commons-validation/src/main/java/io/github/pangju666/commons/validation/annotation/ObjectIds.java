package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.ObjectIdsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * ObjectId集合校验注解
 * <p>验证集合中元素是否为有效的ObjectId：
 * <ul>
 *     <li>当allMatch=true时，所有元素必须为有效的ObjectId</li>
 *     <li>当allMatch=false时，至少一个元素为有效的ObjectId</li>
 * </ul></p>
 *
 * <p>
 * 支持的类型是 {@code Collection<? extends CharSequence>
 * }。{@code null}或空集合视为有效。
 * </p>
 *
 * @author pangju666
 * @see ObjectId
 * @since 1.0.0
 */
@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = {ObjectIdsValidator.class})
public @interface ObjectIds {
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
	String message() default "集合中存在无效的ObjectId";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
