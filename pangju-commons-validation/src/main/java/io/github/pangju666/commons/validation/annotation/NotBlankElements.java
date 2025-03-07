package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.NotBlankElementsValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Constraint(validatedBy = NotBlankElementsValidator.class)
public @interface NotBlankElements {
	String message() default "集合中存在空白的值";

	boolean allMatch() default true;

	/**
	 * 集合是否必须不为 null 或空集合
	 */
	boolean notEmpty() default false;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
