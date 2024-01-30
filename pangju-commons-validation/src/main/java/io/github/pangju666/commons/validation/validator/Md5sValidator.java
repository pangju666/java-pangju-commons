package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.pool.RegExPool;
import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.Md5s;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.regex.Pattern;

public class Md5sValidator implements ConstraintValidator<Md5s, Collection<String>> {
	private static final Pattern PATTERN = RegExUtils.compile(RegExPool.MD5, true, true);

	private boolean allMatch;
	private boolean notEmpty;

	@Override
	public void initialize(Md5s constraintAnnotation) {
		this.allMatch = constraintAnnotation.allMatch();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(Collection<String> value, ConstraintValidatorContext constraintValidatorContext) {
		return ConstraintValidatorUtils.validate(value, allMatch, notEmpty, PATTERN);
	}
}