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

import io.github.pangju666.commons.validation.annotation.EnumName;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 验证字符串是否为有效的枚举值
 *
 * @author pangju666
 * @see EnumName
 * @since 1.0.0
 */
public class EnumNameValidator implements ConstraintValidator<EnumName, CharSequence> {
	private Class<? extends java.lang.Enum> enumClass;
	private boolean ignoreCase;

	@Override
	public void initialize(EnumName constraintAnnotation) {
		this.enumClass = constraintAnnotation.enumClass();
		this.ignoreCase = constraintAnnotation.ignoreCase();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return ignoreCase ? EnumUtils.isValidEnumIgnoreCase(enumClass, value.toString()) :
			EnumUtils.isValidEnum(enumClass, value.toString());
	}
}