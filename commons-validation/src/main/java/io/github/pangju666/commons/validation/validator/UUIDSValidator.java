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

import io.github.pangju666.commons.validation.annotation.UUIDS;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * UUID集合校验器
 * <p>
 * 用于校验UUID格式字符串集合的有效性。
 *
 * <p>
 * 代码参考自{@link org.hibernate.validator.internal.constraintvalidators.hv.UUIDValidator}
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class UUIDSValidator implements ConstraintValidator<UUIDS, Collection<? extends CharSequence>> {
	private static final int[] GROUP_LENGTHS = {8, 4, 4, 4, 12};

	private boolean allowEmpty;
	private boolean allowNil;
	private int[] version;
	private int[] variant;
	private org.hibernate.validator.constraints.UUID.LetterCase letterCase;
	private boolean allMatch;

	private static int[] checkAndSortMultiOptionParameter(int[] values, String parameterName, int minimum, int maximum) {
		Validate.notNull(values, "The parameter" + parameterName + "must not be null.");
		Validate.isTrue(values.length > 0, "The parameter " + parameterName + " must not be empty.");

		for (int value : values) {
			Validate.isTrue(value >= minimum, "The parameter " + parameterName + " should be greater than or equal to %d.");
			Validate.isTrue(value <= maximum, "The parameter " + parameterName + " should be less than or equal to %d.");
		}
		Arrays.sort(values);
		return values;
	}

	private static int extractVersion(int version, int index, int value) {
		if (index == 14) {
			return value;
		}
		return version;
	}

	private static int extractVariant(int variant, int index, int value) {
		if (index == 19) {
			// 0xxx
			if (value >> 3 == 0) {
				return 0;
			}
			// 10xx
			if (value >> 2 == 2) {
				return 1;
			}
			// 110x
			if (value >> 1 == 6) {
				return 2;
			}
		}
		return variant;
	}

	@Override
	public void initialize(UUIDS constraintAnnotation) {
		this.allMatch = constraintAnnotation.allMatch();
		this.allowEmpty = constraintAnnotation.allowEmpty();
		this.allowNil = constraintAnnotation.allowNil();
		this.version = checkAndSortMultiOptionParameter(constraintAnnotation.version(), "version",
			1, 15);
		this.variant = checkAndSortMultiOptionParameter(constraintAnnotation.variant(), "variant",
			0, 2);
		this.letterCase = constraintAnnotation.letterCase();
		Validate.notNull(letterCase, "The parameter letterCase must not be null.");
	}

	@Override
	public boolean isValid(Collection<? extends CharSequence> values, ConstraintValidatorContext context) {
		if (Objects.isNull(values) || values.isEmpty()) {
			return true;
		}
		for (CharSequence value : values) {
			boolean result = validate(value);
			if (result && allMatch) {
				return false;
			} else if (!result && !allMatch) {
				return true;
			}
		}
		return true;
	}

	private boolean validate(CharSequence value) {
		if (StringUtils.isBlank(value)) {
			return allowEmpty;
		}

		int valueLength = value.length();
		if (valueLength != 36) {
			return false;
		}

		int groupIndex = 0;
		int groupLength = 0;
		int checksum = 0;
		int version = -1;
		int variant = -1;
		for (int charIndex = 0; charIndex < valueLength; charIndex++) {
			char ch = value.charAt(charIndex);
			if (ch == '-') {
				groupIndex++;
				groupLength = 0;
			} else {
				groupLength++;
				if (groupLength > GROUP_LENGTHS[groupIndex]) {
					return false;
				}

				int numericValue = Character.digit(ch, 16);
				if (numericValue == -1) {
					// not a hex digit
					return false;
				}
				if (numericValue > 9 && !hasCorrectLetterCase(ch)) {
					return false;
				}
				checksum += numericValue;
				version = extractVersion(version, charIndex, numericValue);
				variant = extractVariant(variant, charIndex, numericValue);
			}
		}

		if (checksum == 0) {
			return allowNil;
		} else {
			if (Arrays.binarySearch(this.version, version) < 0) {
				return false;
			}
			return Arrays.binarySearch(this.variant, variant) > -1;
		}
	}

	private boolean hasCorrectLetterCase(char ch) {
		if (letterCase == null) {
			return true;
		}
		if (letterCase == org.hibernate.validator.constraints.UUID.LetterCase.LOWER_CASE && !Character.isLowerCase(ch)) {
			return false;
		}
		return letterCase != org.hibernate.validator.constraints.UUID.LetterCase.UPPER_CASE || Character.isUpperCase(ch);
	}
}
