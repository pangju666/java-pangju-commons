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
import io.github.pangju666.commons.validation.annotation.HexColor;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 验证字符串是否为有效的十六进制颜色代码
 *
 * @author pangju666
 * @see HexColor
 * @since 1.0.0
 */
public class HexColorValidator implements ConstraintValidator<HexColor, String> {
	private static final Pattern PATTERN = RegExUtils.compile(RegExPool.HEX_COLOR, true, true);

	private boolean notBlank;
	private boolean notEmpty;

	@Override
	public void initialize(HexColor constraintAnnotation) {
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return ConstraintValidatorUtils.validate(value, notBlank, notEmpty, PATTERN);
	}
}