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

import io.github.pangju666.commons.validation.annotation.Enum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.EnumUtils;

import java.util.Objects;

public class EnumValidator implements ConstraintValidator<Enum, String> {
	private Class<? extends java.lang.Enum> enumClass;
	private boolean ignoreCase;

	@Override
	public void initialize(Enum constraintAnnotation) {
		this.enumClass = constraintAnnotation.enumClass();
		this.ignoreCase = constraintAnnotation.ignoreCase();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (value.isBlank()) {
			return false;
		}
		return ignoreCase ? EnumUtils.isValidEnumIgnoreCase(enumClass, value) : EnumUtils.isValidEnum(enumClass, value);
	}
}
