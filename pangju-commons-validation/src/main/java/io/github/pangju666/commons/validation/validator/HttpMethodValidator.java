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

import io.github.pangju666.commons.validation.annotation.HttpMethod;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;
import java.util.Set;

/**
 * 验证字符串是否为有效的HTTP方法
 *
 * @author pangju666
 * @see HttpMethod
 * @since 1.0.0
 */
public class HttpMethodValidator implements ConstraintValidator<HttpMethod, String> {
	private static final Set<String> HTTP_METHODS = Set.of("GET", "POST", "PUT", "PATCH", "HEAD", "DELETE", "OPTIONS");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		if (Objects.isNull(value)) {
			return true;
		}
		return HTTP_METHODS.contains(value);
	}
}