package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.HttpMethodValidator;
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
@Constraint(validatedBy = HttpMethodValidator.class)
public @interface HttpMethod {
	String message() default "无效的请求方法名称";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
