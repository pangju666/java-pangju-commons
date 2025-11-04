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
import io.github.pangju666.commons.validation.annotation.MimeType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 验证字符串是否为有效的MIME类型
 *
 * @author pangju666
 * @see MimeType
 * @since 1.0.0
 */
public class MimeTypeValidator implements ConstraintValidator<MimeType, CharSequence> {
	private static final Pattern PATTERN = Pattern.compile(RegExPool.MIME_TYPE);

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return PATTERN.matcher(value).matches();
	}
}