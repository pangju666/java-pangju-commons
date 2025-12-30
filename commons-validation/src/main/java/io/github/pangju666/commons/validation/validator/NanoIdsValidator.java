package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.NanoId;
import io.github.pangju666.commons.validation.annotation.NanoIds;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * 验证字符串集合中是否存在有效NanoId
 *
 * @author pangju666
 * @see NanoId
 * @see io.github.pangju666.commons.lang.id.NanoId
 * @since 1.0.0
 */
public class NanoIdsValidator implements ConstraintValidator<NanoIds, Collection<? extends CharSequence>> {
	private char[] alphabet;
	private int size;
	private boolean allMatch;

	@Override
	public void initialize(NanoIds constraintAnnotation) {
		this.alphabet = constraintAnnotation.alphabet();
		this.size = constraintAnnotation.size();
		this.allMatch = constraintAnnotation.allMatch();
	}

	@Override
	public boolean isValid(Collection<? extends CharSequence> values, ConstraintValidatorContext context) {
		if (Objects.isNull(values) || values.isEmpty()) {
			return true;
		}

		boolean anyValid = false;
		for (CharSequence value : values) {
			boolean isValid = Objects.nonNull(value) && !StringUtils.isBlank(value) && value.length() == size
				&& StringUtils.containsOnly(value, alphabet);
			if (!isValid && allMatch) {
				return false;
			}
			if (isValid) {
				anyValid = true;
			}
		}
		return allMatch || anyValid;
	}
}