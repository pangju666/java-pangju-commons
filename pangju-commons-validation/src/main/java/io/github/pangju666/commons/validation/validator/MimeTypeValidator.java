package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.MimeType;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class MimeTypeValidator implements ConstraintValidator<MimeType, String> {
	private static final Pattern PATTERN = Pattern.compile("^.+/.+$");

	private boolean notBlank;
	private boolean notEmpty;

	@Override
	public void initialize(MimeType constraintAnnotation) {
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		return ConstraintValidatorUtils.validate(value, notBlank, notEmpty, PATTERN);
	}
}
