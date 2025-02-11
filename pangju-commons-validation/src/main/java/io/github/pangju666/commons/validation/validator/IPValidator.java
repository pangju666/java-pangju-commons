package io.github.pangju666.commons.validation.validator;

import io.github.pangju666.commons.lang.pool.RegExPool;
import io.github.pangju666.commons.lang.utils.RegExUtils;
import io.github.pangju666.commons.validation.annotation.IP;
import io.github.pangju666.commons.validation.utils.ConstraintValidatorUtils;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class IPValidator implements ConstraintValidator<IP, String> {
	private static final Pattern IPV4_PATTERN = RegExUtils.compile(RegExPool.IPV4, true, true);
	private static final Pattern IPV6_PATTERN = RegExUtils.compile(RegExPool.IPV6, true, true);

	private boolean notBlank;
	private boolean notEmpty;
	private boolean isIpv6;

	@Override
	public void initialize(IP constraintAnnotation) {
		this.notBlank = constraintAnnotation.notBlank();
		this.notEmpty = constraintAnnotation.notEmpty();
		this.isIpv6 = constraintAnnotation.ipv6();
	}

	@Override
	public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
		return isIpv6 ? ConstraintValidatorUtils.validate(value, notBlank, notEmpty, IPV6_PATTERN) :
			ConstraintValidatorUtils.validate(value, notBlank, notEmpty, IPV4_PATTERN);
	}
}
