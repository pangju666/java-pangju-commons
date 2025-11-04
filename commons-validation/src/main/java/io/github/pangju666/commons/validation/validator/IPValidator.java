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
import io.github.pangju666.commons.validation.annotation.IP;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 验证字符串是否为有效的IP地址
 *
 * @author pangju666
 * @see IP
 * @since 1.0.0
 */
public class IPValidator implements ConstraintValidator<IP, CharSequence> {
	private static final Pattern IPV4_PATTERN = RegExUtils.compile(RegExPool.IPV4, true, true);
	private static final Pattern IPV6_PATTERN = RegExUtils.compile(RegExPool.IPV6, true, true);

	private boolean isIpv6;

	@Override
	public void initialize(IP constraintAnnotation) {
		this.isIpv6 = constraintAnnotation.ipv6();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext constraintValidatorContext) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return isIpv6 ? IPV6_PATTERN.matcher(value).matches() : IPV4_PATTERN.matcher(value).matches();
	}
}