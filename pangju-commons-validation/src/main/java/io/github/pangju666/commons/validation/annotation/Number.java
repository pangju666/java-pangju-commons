package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.NumberValidator;
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
@Constraint(validatedBy = NumberValidator.class)
public @interface Number {
	String message() default "数字格式不正确";

	boolean positive() default false;

	boolean decimal() default false;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
