/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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
			return value.isEmpty() ? !notEmpty : !notBlank;
		}
		return RegExUtils.matches(pattern, value);
	}

	public static boolean validate(final String value, boolean notBlank, boolean notEmpty, final Predicate<String> predicate) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (value.isBlank()) {
			return value.isEmpty() ? !notEmpty : !notBlank;
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
