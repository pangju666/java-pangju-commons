/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.commons.lang.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 字符串格式转换工具类，提供多种命名格式转换方法
 *
 * @author pangju666
 * @since 1.0.0
 */
public class StringFormatUtils {
	protected StringFormatUtils() {
	}

	/**
	 * 转换为全大写下划线格式（SCREAMING_SNAKE_CASE）
	 *
	 * @param str 原始字符串
	 * @return 转换后的全大写下划线格式字符串
	 * @since 1.0.0
	 */
	public static String formatAsScreamingSnakeCase(final String str) {
		return formatAsSnakeCase(str).toUpperCase();
	}

	/**
	 * 转换为全大写中横线格式（SCREAMING-KEBAB-CASE）
	 *
	 * @param str 原始字符串
	 * @return 转换后的全大写中横线格式字符串
	 * @since 1.0.0
	 */
	public static String formatAsScreamingKebabCase(final String str) {
		return formatAsKebabCase(str).toUpperCase();
	}

	/**
	 * 转换为中横线格式（kebab-case）
	 *
	 * @param str 原始字符串
	 * @return 转换后的中横线格式字符串
	 * @since 1.0.0
	 */
	public static String formatAsKebabCase(final String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		String tmpStr = str.replace('_', '-');
		return delimiterLowerCase(tmpStr, '-');
	}

	/**
	 * 转换为下划线格式（snake_case）
	 *
	 * @param str 原始字符串
	 * @return 转换后的下划线格式字符串
	 * @since 1.0.0
	 */
	public static String formatAsSnakeCase(final String str) {
		if (StringUtils.isBlank(str)) {
			return str;
		}
		String tmpStr = str.replace('-', '_');
		return delimiterLowerCase(tmpStr, '_');
	}

	/**
	 * 转换为小驼峰格式（camelCase），默认分隔符为"-"和"_"
	 *
	 * @param str 原始字符串
	 * @return 转换后的小驼峰格式字符串
	 * @since 1.0.0
	 */
	public static String formatAsCamelCase(final String str) {
		return formatAsCamelCase(str, "-_");
	}

	/**
	 * 转换为小驼峰格式（camelCase）支持自定义分隔符
	 *
	 * @param str       原始字符串
	 * @param delimiter 自定义分隔符字符串（多个字符组合）
	 * @return 转换后的小驼峰格式字符串
	 * @since 1.0.0
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

	/**
	 * 转换为大驼峰格式（PascalCase），默认分隔符为"-"和"_"
	 *
	 * @param str 原始字符串
	 * @return 转换后的大驼峰格式字符串
	 * @since 1.0.0
	 */
	public static String formatAsPascalCase(final String str) {
		return formatAsPascalCase(str, "-_");
	}

	/**
	 * 转换为大驼峰格式（PascalCase）支持自定义分隔符
	 *
	 * @param str       原始字符串
	 * @param delimiter 自定义分隔符字符串（多个字符组合）
	 * @return 转换后的大驼峰格式字符串
	 * @since 1.0.0
	 */
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

	/**
	 * 处理字符数组的大小写格式
	 *
	 * @param chars 原始字符数组
	 * @return 格式化后的字符数组
	 * @since 1.0.0
	 */
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

	/**
	 * 转换字符串为指定分隔符的小写格式
	 *
	 * @param str       原始字符串
	 * @param delimiter 分隔符字符
	 * @return 转换后的带分隔符小写字符串
	 * @since 1.0.0
	 */
	protected static String delimiterLowerCase(final String str, final char delimiter) {
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
