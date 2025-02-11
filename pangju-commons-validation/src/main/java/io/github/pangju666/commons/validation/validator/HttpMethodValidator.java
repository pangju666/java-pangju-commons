package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.HttpMethod;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;
import java.util.Set;

public class HttpMethodValidator implements ConstraintValidator<HttpMethod, String> {
	private static final Set<String> HTTP_METHOD_NAMES = Set.of("GET", "POST", "PUT", "PATCH", "HEAD", "DELETE");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if (Objects.isNull(value)) {
			return true;
		}
		return HTTP_METHOD_NAMES.contains(value);
	}
}
