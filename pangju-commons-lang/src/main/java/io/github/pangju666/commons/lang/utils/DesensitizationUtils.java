package io.github.pangju666.commons.lang.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class DesensitizationUtils {
	protected static final String EMAIL_REGEX = "(\\w{3})\\w+@(\\w+)";
	protected static final String GENERAL_FORMAT = "$1***********$2";

	protected DesensitizationUtils() {
	}

	public static String desensitizeIdCard(final String idCard) {
		return hideRound(idCard, 0, 4);
	}

	public static String desensitizePhoneNumber(final String phoneNumber) {
		return hideRound(phoneNumber, 3, 4);
	}

	public static String desensitizeEmail(final String email) {
		return hide(email, EMAIL_REGEX, GENERAL_FORMAT);
	}

	/**
	 * 车辆发动机编号
	 */
	public static String desensitizeVehicleEngineNumber(final String email) {
		return hideRound(email, 1, 2);
	}

	/**
	 * 车架号
	 */
	public static String desensitizeVehicleFrameNumber(final String email) {
		return hideRound(email, 3, 3);
	}

	/**
	 * 车牌号
	 */
	public static String desensitizePlateNumber(final String email) {
		return hideRound(email, 2, 3);
	}

	public static String desensitizeNickName(final String email) {
		return hideRound(email, 1, 1);
	}

	public static String desensitizeBankCard(final String bankCard) {
		return hideRound(bankCard, 4, 4);
	}

	public static String desensitizeChineseName(final String name) {
		if (StringUtils.isBlank(name)) {
			return name;
		}
		int len = name.length();
		if (len < 3) {
			return hideLeft(name, 1);
		} else {
			return hideLeft(name, 2);
		}
	}

	public static String desensitizePassword(final String password) {
		if (StringUtils.isBlank(password)) {
			return StringUtils.EMPTY;
		}
		char[] chs = new char[password.length()];
		Arrays.fill(chs, '*');
		return StringUtils.valueOf(chs);
	}

	public static String desensitizeAddress(final String address) {
		if (StringUtils.isBlank(address)) {
			return address;
		}
		if (address.length() >= 12) {
			return hideRight(address, 6);
		}
		return hideRight(address, address.length() / 2 - 1);
	}

	public static String desensitizeTelPhone(final String telPhone) {
		if (StringUtils.isBlank(telPhone)) {
			return telPhone;
		}
		String[] temp = telPhone.split("-");
		temp[1] = hideLeft(temp[1], 4);
		return temp[0] + "-" + temp[1];
	}

	public static String hide(final String content, final String regex, final String format) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		if (StringUtils.isBlank(content)) {
			return content;
		}
		return content.replaceAll(regex, format);
	}

	/**
	 * 隐藏右侧字符串
	 *
	 * @param content      字符串
	 * @param prefixLength 左侧保留字符串长度
	 */
	public static String hideRight(final String content, int prefixLength) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		if (prefixLength < 0 || prefixLength >= content.length()) {
			return content;
		}
		String hideContent = StringUtils.left(content, prefixLength);
		return StringUtils.rightPad(hideContent, StringUtils.length(content), "*");
	}

	/**
	 * 隐藏左侧字符串
	 *
	 * @param content      字符串
	 * @param suffixLength 右侧保留字符串长度
	 */
	public static String hideLeft(final String content, int suffixLength) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		if (suffixLength < 0 || suffixLength >= content.length()) {
			return content;
		}
		String hideContent = StringUtils.right(content, suffixLength);
		return StringUtils.leftPad(hideContent, StringUtils.length(content), "*");
	}

	/**
	 * 保留两侧字符串
	 *
	 * @param content      字符串
	 * @param prefixLength 左边保留长度
	 * @param suffixLength 右边保留长度
	 */
	public static String hideRound(final String content, int prefixLength, int suffixLength) {
		if (StringUtils.isBlank(content)) {
			return content;
		}
		if (prefixLength < 0 || prefixLength >= content.length()) {
			return content;
		}
		if (suffixLength < 0 || suffixLength >= content.length()) {
			return content;
		}
		if (prefixLength > suffixLength) {
			prefixLength = suffixLength;
		}
		int length = StringUtils.length(content);
		String leftContent = StringUtils.left(content, prefixLength);
		String rightContent = StringUtils.right(content, suffixLength);
		rightContent = StringUtils.leftPad(rightContent, length, "*");
		rightContent = StringUtils.removeStart(rightContent, "***");
		return leftContent.concat(rightContent);
	}
}
