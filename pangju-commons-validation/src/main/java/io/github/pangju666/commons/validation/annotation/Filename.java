package io.github.pangju666.commons.validation.annotation;

import io.github.pangju666.commons.validation.validator.FilenameValidator;
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
@Constraint(validatedBy = FilenameValidator.class)
public @interface Filename {
	String message() default "文件名禁止包含特殊字符";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	boolean extension() default true;
}
