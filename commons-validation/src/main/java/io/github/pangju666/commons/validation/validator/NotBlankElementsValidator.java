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

import io.github.pangju666.commons.validation.annotation.NotBlankElements;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * 验证字符串集合中的元素是否为非空白字符串
 *
 * @author pangju666
 * @see NotBlankElements
 * @since 1.0.0
 */
public class NotBlankElementsValidator implements ConstraintValidator<NotBlankElements, Collection<? extends CharSequence>> {
	private boolean allMatch;

	@Override
	public void initialize(NotBlankElements constraintAnnotation) {
		this.allMatch = constraintAnnotation.allMatch();
	}

	@Override
	public boolean isValid(Collection<? extends CharSequence> values, ConstraintValidatorContext context) {
		if (Objects.isNull(values) || values.isEmpty()) {
			return true;
		}

		boolean anyValid = false;
		for (CharSequence value : values) {
			boolean isValid = Objects.nonNull(value) && !StringUtils.isBlank(value.toString());
			if (!isValid && allMatch) {
				return false;
			}
			if (isValid) {
				anyValid = true;
			}
		}
		return allMatch || anyValid;
	}
}