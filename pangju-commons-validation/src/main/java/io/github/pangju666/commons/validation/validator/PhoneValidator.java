package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.pool.RegExPool;
import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.Phone;
import io.github.pangju666.commons.validation.enums.PhoneType;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PhoneValidator implements ConstraintValidator<Phone, String> {
	private static final Pattern MOBILE_PHONE_STRONG_PATTERN = RegExUtils.compile(RegExPool.MOBILE_PHONE_STRONG, true, true);
	private static final Pattern MOBILE_PHONE_WEAK_PATTERN = RegExUtils.compile(RegExPool.MOBILE_PHONE_WEAK, true, true);
	private static final Pattern TEL_PHONE_PATTERN = RegExUtils.compile(RegExPool.TEL_PHONE, true, true);

	private PhoneType type;
	private boolean strongStrength;
	private boolean notBlank;
	private boolean notEmpty;

	@Override
	public void initialize(Phone constraintAnnotation) {
		this.type = constraintAnnotation.type();
		this.strongStrength = constraintAnnotation.strong();
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		return ConstraintValidatorUtils.validate(value, notBlank, notEmpty, val ->
			switch (type) {
				case TEL -> RegExUtils.matches(TEL_PHONE_PATTERN, val);
				case MOBILE -> {
					if (strongStrength) {
						yield RegExUtils.matches(MOBILE_PHONE_STRONG_PATTERN, val);
					}
					yield RegExUtils.matches(MOBILE_PHONE_WEAK_PATTERN, val);
				}
				default -> {
					if (RegExUtils.matches(TEL_PHONE_PATTERN, val)) {
						yield true;
					}
					if (strongStrength) {
						yield RegExUtils.matches(MOBILE_PHONE_STRONG_PATTERN, val);
					}
					yield RegExUtils.matches(MOBILE_PHONE_WEAK_PATTERN, val);
				}
			}
		);
	}
}
