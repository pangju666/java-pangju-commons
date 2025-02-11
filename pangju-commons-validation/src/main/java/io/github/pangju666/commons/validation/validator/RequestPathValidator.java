package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.RequestPath;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;
import java.util.regex.Pattern;

public class RequestPathValidator implements ConstraintValidator<RequestPath, String> {
	private static final Pattern PATTERN = RegExUtils.compile("^\\/[\\w/-]+$", true, true);

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if (Objects.isNull(value)) {
			return true;
		}
		return RegExUtils.matches(PATTERN, value);
	}
}
