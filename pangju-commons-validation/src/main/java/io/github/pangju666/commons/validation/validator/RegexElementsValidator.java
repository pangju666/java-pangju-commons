package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.RegexElements;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Collection;
import java.util.regex.Pattern;

public class RegexElementsValidator implements ConstraintValidator<RegexElements, Collection<String>> {
	private Pattern pattern;
	private boolean allMatch;
	private boolean notEmpty;

	@Override
	public void initialize(RegexElements constraintAnnotation) {
		int flags = RegExUtils.computeFlags(constraintAnnotation.flags());
		this.pattern = RegExUtils.compile(constraintAnnotation.regexp(), flags, constraintAnnotation.matchStart(), constraintAnnotation.matchEnd());
		this.notEmpty = constraintAnnotation.notEmpty();
		this.allMatch = constraintAnnotation.allMatch();
	}

	@Override
	public boolean isValid(Collection<String> values, ConstraintValidatorContext context) {
		return ConstraintValidatorUtils.validate(values, allMatch, notEmpty, pattern);
	}
}
