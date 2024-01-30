package io.github.pangju666.commons.validation.validator;


import io.github.pangju666.commons.lang.pool.RegExPool;
import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.BankCard;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class BankCardValidator implements ConstraintValidator<BankCard, String> {
	private static final Pattern PATTERN = RegExUtils.compile(RegExPool.BANK_CARD, true, true);

	private boolean notBlank;
	private boolean notEmpty;

	@Override
	public void initialize(BankCard constraintAnnotation) {
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		return ConstraintValidatorUtils.validate(value, notBlank, notEmpty, PATTERN);
	}
}
