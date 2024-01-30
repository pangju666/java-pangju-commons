package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.Regex;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class RegexValidator implements ConstraintValidator<Regex, String> {
	private Pattern pattern;
	private boolean notBlank;
	private boolean notEmpty;

	@Override
	public void initialize(Regex constraintAnnotation) {
		int flags = RegExUtils.computeFlags(constraintAnnotation.flags());
		this.pattern = RegExUtils.compile(constraintAnnotation.regexp(), flags, constraintAnnotation.matchStart(), constraintAnnotation.matchEnd());
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return ConstraintValidatorUtils.validate(value, notBlank, notEmpty, pattern);
	}
}
