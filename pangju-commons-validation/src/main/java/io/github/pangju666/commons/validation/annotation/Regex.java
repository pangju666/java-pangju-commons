package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.lang.enums.RegexFlag;
import io.github.pangju666.commons.validation.validator.RegexValidator;
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
@Constraint(validatedBy = RegexValidator.class)
public @interface Regex {
	String regexp();

	RegexFlag[] flags() default {};

	boolean matchStart() default true;

	boolean matchEnd() default true;

	boolean notBlank() default false;

	boolean notEmpty() default false;

	String message() default "格式不正确";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
