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
import io.github.pangju666.commons.validation.annotation.Md5;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 验证字符串是否为有效的MD5哈希值
 *
 * @author pangju666
 * @see Md5
 * @since 1.0.0
 */
public class Md5Validator implements ConstraintValidator<Md5, CharSequence> {
	private static final Pattern PATTERN = RegExUtils.compile(RegExPool.MD5, true, true);

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return PATTERN.matcher(value).matches();
	}
}