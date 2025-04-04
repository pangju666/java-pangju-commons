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

import org.apache.commons.lang3.Validate;

import java.nio.charset.Charset;
import java.util.*;

/**
 * 字符串工具类，继承并扩展{@link org.apache.commons.lang3.StringUtils}功能
 * <p>提供字符集转换、集合元素过滤等增强方法</p>
 *
 * @author pangju666
 * @see org.apache.commons.lang3.StringUtils
 * @since 1.0.0
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {
	protected StringUtils() {
	}

	/**
	 * 转换字符串字符集（使用系统默认源字符集）
	 *
	 * @param str           原始字符串
	 * @param targetCharset 目标字符集（不可为null）
	 * @return 转换后的新字符串，输入为空时返回原值
	 * @since 1.0.0
	 */
	public static String convertCharset(final String str, final Charset targetCharset) {
		Validate.notNull(targetCharset, "目标字符集不可为null");
		if (isBlank(str)) {
			return str;
		}
		return new String(str.getBytes(), targetCharset);
	}

	/**
	 * 转换字符串字符集（指定源字符集）
	 *
	 * @param str           原始字符串
	 * @param srcCharset    源字符集（不可为null）
	 * @param targetCharset 目标字符集（不可为null）
	 * @return 转换后的新字符串，输入为空或字符集相同时返回原值
	 * @since 1.0.0
	 */
	public static String convertCharset(final String str, final Charset srcCharset, final Charset targetCharset) {
		Validate.notNull(srcCharset, "原始字符集不可为null");
		Validate.notNull(targetCharset, "目标字符集不可为null");
		if (isBlank(str) || srcCharset.equals(targetCharset)) {
			return str;
		}
		return new String(str.getBytes(srcCharset), targetCharset);
	}

	/**
	 * 获取集合中非空字符串元素列表
	 *
	 * @param collection 原始字符串集合
	 * @return 过滤后的非空字符串列表（可能为空列表）
	 * @since 1.0.0
	 */
	public static List<String> getNotBlankElements(final Collection<String> collection) {
		if (Objects.isNull(collection) || collection.isEmpty()) {
			return Collections.emptyList();
		}
		return collection.stream()
			.filter(StringUtils::isNotBlank)
			.toList();
	}

	/**
	 * 获取集合中唯一且非空字符串元素列表
	 *
	 * @param collection 原始字符串集合
	 * @return 去重后的非空字符串列表（可能为空列表）
	 * @since 1.0.0
	 */
	public static List<String> getUniqueNotBlankElements(final Collection<String> collection) {
		if (Objects.isNull(collection) || collection.isEmpty()) {
			return Collections.emptyList();
		}
		return collection.stream()
			.filter(StringUtils::isNotBlank)
			.distinct()
			.toList();
	}

	/**
	 * 获取数组中非空字符串元素列表
	 *
	 * @param strings 原始字符串数组
	 * @return 过滤后的非空字符串列表（可能为空列表）
	 * @since 1.0.0
	 */
	public static List<String> getNotBlankElements(final String... strings) {
		if (Objects.isNull(strings) || strings.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(strings)
			.filter(StringUtils::isNotBlank)
			.toList();
	}

	/**
	 * 获取数组中唯一且非空字符串元素列表
	 *
	 * @param strings 原始字符串数组
	 * @return 去重后的非空字符串列表（可能为空列表）
	 * @since 1.0.0
	 */
	public static List<String> getUniqueNotBlankElements(final String... strings) {
		if (Objects.isNull(strings) || strings.length == 0) {
			return Collections.emptyList();
		}
		return Arrays.stream(strings)
			.filter(StringUtils::isNotBlank)
			.distinct()
			.toList();
	}
}
