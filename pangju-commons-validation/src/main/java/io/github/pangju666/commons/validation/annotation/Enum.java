package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.EnumValidator;
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
@Constraint(validatedBy = EnumValidator.class)
public @interface Enum {
	String message() default "不是有效的枚举变量名称";

	Class<? extends java.lang.Enum> enumClass();

	boolean ignoreCase() default true;

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
