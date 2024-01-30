package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.validation.annotation.NotBlankElements;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

public class NotBlankElementsValidator implements ConstraintValidator<NotBlankElements, Collection<String>> {
	private boolean allMatch;
	private boolean notEmpty;

	@Override
	public void initialize(NotBlankElements constraintAnnotation) {
		this.allMatch = constraintAnnotation.allMatch();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(Collection<String> value, ConstraintValidatorContext context) {
		return ConstraintValidatorUtils.validate(value, allMatch, notEmpty, StringUtils::isNotBlank);
	}
}
