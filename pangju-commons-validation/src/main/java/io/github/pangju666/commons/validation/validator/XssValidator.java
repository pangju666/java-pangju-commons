package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.pool.RegExPool;
import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.Xss;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

/**
 * 验证字符串是否为HTML字符串
 *
 * @author pangju666
 * @see Xss
 * @since 1.0.0
 */
public class XssValidator implements ConstraintValidator<Xss, String> {
	private static final Pattern PATTERN = RegExUtils.compile(RegExPool.HTML_PATTERN, true, true);

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (StringUtils.isBlank(value)) {
			return true;
		}
		return !PATTERN.matcher(value).matches();
	}
}