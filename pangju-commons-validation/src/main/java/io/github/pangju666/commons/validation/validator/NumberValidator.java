package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.pool.RegExPool;
import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.Number;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;
import java.util.regex.Pattern;

public class NumberValidator implements ConstraintValidator<Number, String> {
	private static final Pattern PATTERN = RegExUtils.compile(RegExPool.NUMBER, true, true);
	private static final Pattern POSITIVE_PATTERN = RegExUtils.compile(RegExPool.POSITIVE_NUMBER, true, true);
	private static final Pattern FLOAT_PATTERN = RegExUtils.compile(RegExPool.FLOAT_NUMBER, true, true);
	private static final Pattern POSITIVE_FLOAT__PATTERN = RegExUtils.compile(RegExPool.POSITIVE_FLOAT_NUMBER, true, true);

	private boolean positive;
	private boolean decimal;

	@Override
	public void initialize(Number constraintAnnotation) {
		this.positive = constraintAnnotation.positive();
		this.decimal = constraintAnnotation.decimal();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (value.isEmpty() || value.isBlank()) {
			return false;
		}
		if (positive) {
			if (decimal) {
				return RegExUtils.matches(POSITIVE_FLOAT__PATTERN, value);
			}
			return RegExUtils.matches(POSITIVE_PATTERN, value);
		} else {
			if (decimal) {
				return RegExUtils.matches(FLOAT_PATTERN, value);
			}
			return RegExUtils.matches(PATTERN, value);

		}
	}
}
