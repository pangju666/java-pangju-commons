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
import io.github.pangju666.commons.validation.enums.PhoneNumberType;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
	private static final Pattern MOBILE_PHONE_STRONG_PATTERN = RegExUtils.compile(RegExPool.MOBILE_PHONE_STRONG, true, true);
	private static final Pattern MOBILE_PHONE_WEAK_PATTERN = RegExUtils.compile(RegExPool.MOBILE_PHONE_WEAK, true, true);
	private static final Pattern TEL_PHONE_PATTERN = RegExUtils.compile(RegExPool.TEL_PHONE, true, true);

	private PhoneNumberType type;
	private boolean strongStrength;
	private boolean notBlank;
	private boolean notEmpty;

	@Override
	public void initialize(PhoneNumber constraintAnnotation) {
		this.type = constraintAnnotation.type();
		this.strongStrength = constraintAnnotation.strong();
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return ConstraintValidatorUtils.validate(value, notBlank, notEmpty, val ->
			switch (type) {
				case TEL -> RegExUtils.matches(TEL_PHONE_PATTERN, val);
				case MOBILE -> {
					if (strongStrength) {
						yield RegExUtils.matches(MOBILE_PHONE_STRONG_PATTERN, val);
					}
					yield RegExUtils.matches(MOBILE_PHONE_WEAK_PATTERN, val);
				}
				default -> {
					if (RegExUtils.matches(TEL_PHONE_PATTERN, val)) {
						yield true;
					}
					if (strongStrength) {
						yield RegExUtils.matches(MOBILE_PHONE_STRONG_PATTERN, val);
					}
					yield RegExUtils.matches(MOBILE_PHONE_WEAK_PATTERN, val);
				}
			}
		);
	}
}
