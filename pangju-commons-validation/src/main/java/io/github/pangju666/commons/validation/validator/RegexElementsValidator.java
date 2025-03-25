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
import io.github.pangju666.commons.validation.annotation.RegexElements;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.regex.Pattern;

public class RegexElementsValidator implements ConstraintValidator<RegexElements, Collection<String>> {
	private Pattern pattern;
	private boolean allMatch;
	private boolean notEmpty;

	@Override
	public void initialize(RegexElements constraintAnnotation) {
		int flags = RegExUtils.computeFlags(constraintAnnotation.flags());
		this.pattern = RegExUtils.compile(constraintAnnotation.regexp(), flags, constraintAnnotation.matchStart(), constraintAnnotation.matchEnd());
		this.notEmpty = constraintAnnotation.notEmpty();
		this.allMatch = constraintAnnotation.allMatch();
	}

	@Override
	public boolean isValid(Collection<String> values, ConstraintValidatorContext context) {
		return ConstraintValidatorUtils.validate(values, allMatch, notEmpty, pattern);
	}
}
