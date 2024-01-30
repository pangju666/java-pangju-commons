package io.github.pangju666.commons.validation.annotation;


import io.github.pangju666.commons.validation.validator.ColorValidator;
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
@Constraint(validatedBy = ColorValidator.class)
public @interface Color {
	String message() default "颜色表达式格式不正确";
	boolean notBlank() default false;
	boolean notEmpty() default false;
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
}
