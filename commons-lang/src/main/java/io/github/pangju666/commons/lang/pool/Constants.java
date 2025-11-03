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

package io.github.pangju666.commons.lang.pool;

/**
 * 常用的一些常量
 *
 * @author pangju666
 * @since 1.0.0
 */
public class Constants {
	// 日期/时间相关常量
	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH:mm:ss";

	// 字符串相关常量
	/**
	 * 起始中文字符（网上搜的，不一定准确）
	 *
	 * @since 1.0.0
	 */
	public static final char START_CHINESE_CHARACTER = '\u4e00';
	/**
	 * 最终中文字符（网上搜的，不一定准确）
	 *
	 * @since 1.0.0
	 */
	public static final char END_CHINESE_CHARACTER = '\u9fa5';
	/**
	 * 起始数字字符
	 *
	 * @since 1.0.0
	 */
	public static final char START_NUMBER_CHARACTER = '0';
	/**
	 * 最终数字字符
	 *
	 * @since 1.0.0
	 */
	public static final char END_NUMBER_CHARACTER = '9';
	/**
	 * 起始大写字母字符
	 *
	 * @since 1.0.0
	 */
	public static final char START_UPPERCASE_ALPHABETIC_CHARACTER = 'A';
	/**
	 * 最终大写字母字符
	 *
	 * @since 1.0.0
	 */
	public static final char END_UPPERCASE_ALPHABETIC_CHARACTER = 'Z';
	/**
	 * 起始小写字母字符
	 *
	 * @since 1.0.0
	 */
	public static final char START_LOWERCASE_ALPHABETIC_CHARACTER = 'a';
	/**
	 * 最终小写字母字符
	 *
	 * @since 1.0.0
	 */
	public static final char END_LOWERCASE_ALPHABETIC_CHARACTER = 'z';
	/**
	 * 下划线字符
	 *
	 * @since 1.0.0
	 */
	public static final char UNDERLINE_CHARACTER = '_';
	/**
	 * 下划线
	 *
	 * @since 1.0.0
	 */
	public static final String UNDERLINE = "_";

	// json 相关常量
	/**
	 * 空json对象
	 *
	 * @since 1.0.0
	 */
	public static final String EMPTY_JSON_OBJECT_STR = "{}";
	/**
	 * 空json数组
	 *
	 * @since 1.0.0
	 */
	public static final String EMPTY_JSON_ARRAY_STR = "[]";

	// XML相关常量
	/**
	 * XML 不间断空格转义 {@code "&nbsp;" -> " "}
	 *
	 * @since 1.0.0
	 */
	public static final String XML_NBSP = "&nbsp;";
	/**
	 * XML And 符转义 {@code "&amp;" -> "&"}
	 *
	 * @since 1.0.0
	 */
	public static final String XML_AMP = "&amp;";
	/**
	 * XML 双引号转义 {@code "&quot;" -> "\""}
	 *
	 * @since 1.0.0
	 */
	public static final String XML_QUOTE = "&quot;";
	/**
	 * XML 单引号转义 {@code "&apos" -> "'"}
	 *
	 * @since 1.0.0
	 */
	public static final String XML_APOS = "&apos;";
	/**
	 * XML 小于号转义 {@code "&lt;" -> "<"}
	 *
	 * @since 1.0.0
	 */
	public static final String XML_LT = "&lt;";
	/**
	 * XML 大于号转义 {@code "&gt;" -> ">"}
	 *
	 * @since 1.0.0
	 */
	public static final String XML_GT = "&gt;";

	protected Constants() {
	}
}