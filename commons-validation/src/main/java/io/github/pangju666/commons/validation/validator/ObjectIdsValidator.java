package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.ObjectIds;
import io.github.pangju666.commons.validation.annotation.Xss;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;

/**
 * 验证字符串集合中是否存在有效ObjectId
 *
 * @author pangju666
 * @see Xss
 * @see org.bson.types.ObjectId
 * @since 1.0.0
 */
public class ObjectIdsValidator implements ConstraintValidator<ObjectIds, Collection<? extends CharSequence>> {
	private boolean allMatch;

	@Override
	public void initialize(ObjectIds constraintAnnotation) {
		this.allMatch = constraintAnnotation.allMatch();
	}

	@Override
	public boolean isValid(Collection<? extends CharSequence> values, ConstraintValidatorContext context) {
		if (Objects.isNull(values) || values.isEmpty()) {
			return true;
		}

		boolean anyValid = false;
		for (CharSequence value : values) {
			boolean isValid = Objects.nonNull(value) && !StringUtils.isBlank(value) && org.bson.types.ObjectId.isValid(
				value.toString());
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