package io.github.pangju666.commons.lang.utils;

import org.apache.commons.lang3.StringUtils;

public class StringCaseUtils {
	protected StringCaseUtils() {
	}

	/**
	 * 全大写下划线
	 */
	public static String formatAsScreamingSnakeCase(final String str) {
		return formatAsSnakeCase(str).toUpperCase();
	}

	/**
	 * 全大写中横线
	 */
	public static String formatAsScreamingKebabCase(final String str) {
		return formatAsKebabCase(str).toUpperCase();
	}

	/**
	 * 中横线
	 */
	public static String formatAsKebabCase(final String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		String tmpStr = str.replace('_', '-');
		return delimiterLowerCase(tmpStr, '-');
	}

	/**
	 * 下划线
	 */
	public static String formatAsSnakeCase(final String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		String tmpStr = str.replace('-', '_');
		return delimiterLowerCase(tmpStr, '_');
	}

	/**
	 * 小驼峰
	 */
	public static String formatAsCamelCase(final String str) {
		return formatAsCamelCase(str, "-_");
	}

	/**
	 * 大驼峰
	 */
	public static String formatAsCamelCase(final String str, final String delimiter) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		StringBuilder builder = new StringBuilder(str.length());
		String[] splits = StringUtils.split(str, delimiter);
		for (int i = 0; i < splits.length; i++) {
			char[] chars = splits[i].toCharArray();
			if (i == 0) {
				if (chars[0] >= 'A' && chars[0] <= 'Z') {
					chars[0] += 32;
				}
			} else {
				if (chars[0] >= 'a' && chars[0] <= 'z') {
					chars[0] -= 32;
				}
			}
			builder.append(letterCase(chars));
		}
		return builder.toString();
	}

	public static String formatAsPascalCase(final String str) {
		return formatAsPascalCase(str, "-_");
	}

	public static String formatAsPascalCase(final String str, final String delimiter) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		StringBuilder builder = new StringBuilder(str.length());
		String[] splits = StringUtils.split(str, delimiter);
		for (String split : splits) {
			char[] chars = split.toCharArray();
			if (chars[0] >= 'a' && chars[0] <= 'z') {
				chars[0] -= 32;
			}
			builder.append(letterCase(chars));
		}
		return builder.toString();
	}

	protected static char[] letterCase(final char[] chars) {
		boolean uppercase = false;
		int uppercaseIndex = 0;
		for (int j = 1; j < chars.length; j++) {
			if (chars[j] >= 'A' && chars[j] <= 'Z') {
				if (!uppercase) {
					uppercase = true;
					uppercaseIndex = j;
				}
				if (j < chars.length - 1 && (chars[j + 1] >= 'a' && chars[j + 1] <= 'z')) {
					continue;
				}
				chars[j] += 32;
			} else {
				uppercase = false;
			}
		}
		if (uppercase) {
			chars[uppercaseIndex] -= 32;
		}
		return chars;
	}

	protected static String delimiterLowerCase(final String str, char delimiter) {
		StringBuilder builder = new StringBuilder(str.length());
		for (int i = 0; i < str.length(); ++i) {
			char ch = str.charAt(i);
			if (ch >= 'A' && ch <= 'Z') {
				ch += 32;
				if (i > 0 && !(str.charAt(i - 1) >= 'A' && str.charAt(i - 1) <= 'Z') && str.charAt(i - 1) != delimiter) {
					builder.append(delimiter);
				}
				builder.append(ch);
			} else {
				builder.append(ch);
			}
		}
		return builder.toString();
	}
}
