package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.Filename;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Objects;
import java.util.regex.Pattern;

public class FilenameValidator implements ConstraintValidator<Filename, String> {
	private static final Pattern REGEX = RegExUtils.compile("[^\\\\<>:\"/|?*.]+(\\.[^\\\\<>:\"/|?*]+)?", true, true);
	private static final Pattern NO_EXTENSION_REGEX = RegExUtils.compile("[^\\\\<>:\"/|?*.]+", true, true);

	private boolean extension;

	@Override
	public void initialize(Filename constraintAnnotation) {
		this.extension = constraintAnnotation.extension();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (Objects.isNull(value)) {
			return true;
		}
		return extension ? RegExUtils.matches(REGEX, value) : RegExUtils.matches(NO_EXTENSION_REGEX, value);
	}
}
