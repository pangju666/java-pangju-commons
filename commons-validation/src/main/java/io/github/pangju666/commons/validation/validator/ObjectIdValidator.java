package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.ObjectId;
import io.github.pangju666.commons.validation.annotation.Xss;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * 验证字符串是否为有效的ObjectId
 *
 * @author pangju666
 * @see Xss
 * @see org.bson.types.ObjectId
 * @since 1.0.0
 */
public class ObjectIdValidator implements ConstraintValidator<ObjectId, CharSequence> {
	@Override
	public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (StringUtils.isBlank(value)) {
			return false;
		}
		return org.bson.types.ObjectId.isValid(value.toString());
	}
}