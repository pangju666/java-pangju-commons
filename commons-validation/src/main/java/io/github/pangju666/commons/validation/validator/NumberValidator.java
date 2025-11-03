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
package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.pool.RegExPool;
import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.Number;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 验证字符串是否为有效的数字
 *
 * @author pangju666
 * @see Number
 * @since 1.0.0
 */
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
		if (value.isBlank()) {
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