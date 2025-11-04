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
import io.github.pangju666.commons.validation.annotation.Filename;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 验证字符串是否为有效的文件名
 *
 * @author pangju666
 * @see Filename
 * @since 1.0.0
 */
public class FilenameValidator implements ConstraintValidator<Filename, CharSequence> {
	private static final Pattern REGEX = RegExUtils.compile(RegExPool.FILENAME, true, true);
	private static final Pattern NO_EXTENSION_REGEX = RegExUtils.compile(RegExPool.FILENAME_WITHOUT_EXTENSION, true, true);

	private boolean extension;

	@Override
	public void initialize(Filename constraintAnnotation) {
		this.extension = constraintAnnotation.extension();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return extension ? REGEX.matcher(value).matches() : NO_EXTENSION_REGEX.matcher(value).matches();
	}
}