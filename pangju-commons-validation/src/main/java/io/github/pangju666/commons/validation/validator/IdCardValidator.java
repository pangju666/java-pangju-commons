package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.utils.IdCardUtils;
import io.github.pangju666.commons.validation.annotation.IdCard;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IdCardValidator implements ConstraintValidator<IdCard, String> {
	private boolean notBlank;
	private boolean notEmpty;

	@Override
	public void initialize(IdCard constraintAnnotation) {
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		return ConstraintValidatorUtils.validate(value, notBlank, notEmpty, IdCardUtils::validate);
	}
}
