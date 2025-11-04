package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.NanoId;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 验证字符串是否为有效的NanoId
 *
 * @author pangju666
 * @see NanoId
 * @see io.github.pangju666.commons.lang.id.NanoId
 * @since 1.0.0
 */
public class NanoIdValidator implements ConstraintValidator<NanoId, CharSequence> {
	private char[] alphabet;
	private int size;

	@Override
	public void initialize(NanoId constraintAnnotation) {
		this.alphabet = constraintAnnotation.alphabet();
		this.size = constraintAnnotation.size();
	}

	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value) || value.length() != size) {
			return false;
		}
		return StringUtils.containsOnly(value, alphabet);
	}
}