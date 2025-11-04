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

import io.github.pangju666.commons.validation.annotation.BASE64;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 验证字符串是否为有效的Base64编码
 *
 * @author pangju666
 * @see BASE64
 * @since 1.0.0
 */
public class Base64Validator implements ConstraintValidator<BASE64, CharSequence> {
	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return Base64.isBase64(value.toString());
	}
}