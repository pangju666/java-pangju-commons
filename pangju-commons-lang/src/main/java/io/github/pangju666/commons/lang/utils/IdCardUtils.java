package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.ConstantPool;
import io.github.pangju666.commons.lang.pool.RegExPool;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

public class IdCardUtils {
	protected static final Pattern PATTERN = RegExUtils.compile(RegExPool.ID_CARD, true, true);
	// 18位身份证中最后一位校验码
	protected static final char[] VERIFY_CODE = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
	// 18位身份证中，前17位各个数字的生成校验码时的权重值
	protected static final int[] VERIFY_CODE_WEIGHT = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
	protected static final int MIN_YEAR = 1920;

	protected IdCardUtils() {
	}

	public static boolean validate(final String idCardNumber) {
		if (StringUtils.isBlank(idCardNumber)) {
			return false;
		}
		if (!RegExUtils.matches(PATTERN, idCardNumber)) {
			return false;
		}

		int count = 0;
		for (int i = 0; i < 17; i++) {
			int index = idCardNumber.charAt(i) - ConstantPool.START_NUMBER_CHARACTER;
			count += index * VERIFY_CODE_WEIGHT[i];
		}
		char verifyCode = VERIFY_CODE[count % 11];
		if (Character.toUpperCase(idCardNumber.charAt(17)) != verifyCode) {
			return false;
		}

		try {
			int year = Integer.parseInt(idCardNumber.substring(6, 10));
			int month = Integer.parseInt(idCardNumber.substring(10, 12));
			int day = Integer.parseInt(idCardNumber.substring(12, 14));
			if (year < MIN_YEAR) {
				return false;
			}
			Date nowDate = new Date();
			Date birthDate = DateUtils.toDate(LocalDate.of(year, month, day));
			return DateUtils.truncatedCompareTo(birthDate, nowDate, Calendar.DATE) <= 0;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static String parseSex(final String idCardNumber) {
		if (StringUtils.isBlank(idCardNumber)) {
			return null;
		}
		if (idCardNumber.length() == 15) {
			int flag = idCardNumber.charAt(14) - '0';
			return flag % 2 == 0 ? "女" : "男";
		} else if (idCardNumber.length() == 18) {
			int flag = idCardNumber.charAt(16) - '0';
			return flag % 2 == 0 ? "女" : "男";
		} else {
			return null;
		}
	}

	public static Date parseBirthDate(final String idCardNumber) {
		if (StringUtils.isBlank(idCardNumber)) {
			return null;
		}
		if (idCardNumber.length() == 15) {
			int year = NumberUtils.toInt("19" + idCardNumber.substring(6, 8));
			int month = NumberUtils.toInt(idCardNumber.substring(8, 10));
			int date = NumberUtils.toInt(idCardNumber.substring(10, 12));
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month - 1, date);
			return calendar.getTime();
		} else if (idCardNumber.length() == 18) {
			int year = NumberUtils.toInt(idCardNumber.substring(6, 10));
			int month = NumberUtils.toInt(idCardNumber.substring(10, 12));
			int date = NumberUtils.toInt(idCardNumber.substring(12, 14));
			Calendar calendar = Calendar.getInstance();
			calendar.set(year, month - 1, date);
			return calendar.getTime();
		} else {
			return null;
		}
	}
}
