package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.Enum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.EnumUtils;

import java.util.Objects;

public class EnumValidator implements ConstraintValidator<Enum, String> {
	private Class<? extends java.lang.Enum> enumClass;
	private boolean ignoreCase;

	@Override
	public void initialize(Enum constraintAnnotation) {
		this.enumClass = constraintAnnotation.enumClass();
		this.ignoreCase = constraintAnnotation.ignoreCase();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (value.isBlank()) {
			return false;
		}
		return ignoreCase ? EnumUtils.isValidEnumIgnoreCase(enumClass, value) : EnumUtils.isValidEnum(enumClass, value);
	}
}
