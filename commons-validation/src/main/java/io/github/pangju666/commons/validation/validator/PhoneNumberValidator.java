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
import io.github.pangju666.commons.validation.annotation.PhoneNumber;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 验证字符串是否为有效的电话号码
 *
 * @author pangju666
 * @see PhoneNumber
 * @since 1.0.0
 */
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, CharSequence> {
	private static final Pattern MOBILE_PHONE_STRONG_PATTERN = RegExUtils.compile(RegExPool.MOBILE_PHONE_STRONG,
		true, true);
	private static final Pattern MOBILE_PHONE_WEAK_PATTERN = RegExUtils.compile(RegExPool.MOBILE_PHONE_WEAK,
		true, true);
	private static final Pattern TEL_PHONE_PATTERN = RegExUtils.compile(RegExPool.TEL_PHONE, true,
		true);

	private PhoneNumber.Type type;
	private boolean strongStrength;

	@Override
	public void initialize(PhoneNumber constraintAnnotation) {
		this.type = constraintAnnotation.type();
		this.strongStrength = constraintAnnotation.strong();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}
		switch (type) {
			case TEL:
				return TEL_PHONE_PATTERN.matcher(value).matches();
			case MOBILE:
				if (strongStrength) {
					return MOBILE_PHONE_STRONG_PATTERN.matcher(value).matches();
				}
				return MOBILE_PHONE_WEAK_PATTERN.matcher(value).matches();
			default:
				if (TEL_PHONE_PATTERN.matcher(value).matches()) {
					return true;
				}
				if (strongStrength) {
					return MOBILE_PHONE_STRONG_PATTERN.matcher(value).matches();
				}
				return MOBILE_PHONE_WEAK_PATTERN.matcher(value).matches();
		}
	}
}