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

import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.Regex;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

/**
 * 验证字符串是否匹配指定的正则表达式
 *
 * @author pangju666
 * @see Regex
 * @since 1.0.0
 */
public class RegexValidator implements ConstraintValidator<Regex, String> {
	private Pattern pattern;
	private boolean notBlank;
	private boolean notEmpty;

	@Override
	public void initialize(Regex constraintAnnotation) {
		int flags = RegExUtils.computeFlags(constraintAnnotation.flags());
		this.pattern = RegExUtils.compile(constraintAnnotation.regexp(), flags, constraintAnnotation.matchStart(), constraintAnnotation.matchEnd());
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return ConstraintValidatorUtils.validate(value, notBlank, notEmpty, pattern);
	}
}