package io.github.pangju666.commons.validation.utils;

import io.github.pangju666.commons.lang.utils.RegExUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ConstraintValidatorUtils {
	protected ConstraintValidatorUtils() {
	}

	public static boolean validate(final String value, boolean notBlank, boolean notEmpty, final Pattern pattern) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (value.isBlank()) {
			return !notBlank;
		}
		if (value.isEmpty()) {
			return !notEmpty;
		}
		return RegExUtils.matches(pattern, value);
	}

	public static boolean validate(final String value, boolean notBlank, boolean notEmpty, final Predicate<String> predicate) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (value.isBlank()) {
			return !notBlank;
		}
		if (value.isEmpty()) {
			return !notEmpty;
		}
		return predicate.test(value);
	}

	public static boolean validate(final Collection<String> values, boolean allMatch, boolean notEmpty, final Pattern pattern) {
		if (CollectionUtils.isEmpty(values)) {
			return !notEmpty;
		}
		if (allMatch) {
			return values.stream().allMatch(value -> StringUtils.isNotBlank(value) && RegExUtils.matches(pattern, value));
		} else {
			return values.stream().anyMatch(value -> StringUtils.isNotBlank(value) && RegExUtils.matches(pattern, value));
		}
	}

	public static <T> boolean validate(final Collection<T> values, boolean allMatch, boolean notEmpty, final Predicate<T> predicate) {
		if (CollectionUtils.isEmpty(values)) {
			return !notEmpty;
		}
		if (allMatch) {
			return values.stream().allMatch(predicate);
		} else {
			return values.stream().anyMatch(predicate);
		}
	}
}
