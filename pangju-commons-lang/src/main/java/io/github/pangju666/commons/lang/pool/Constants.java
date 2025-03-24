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

	// 网络相关常量
	/**
	 * http协议前缀
	 *
	 * @since 1.0.0
	 */
	public static final String HTTP_PREFIX = "http://";
	/**
	 * https协议前缀
	 *
	 * @since 1.0.0
	 */
	public static final String HTTPS_PREFIX = "https://";
	/**
	 * http 200 状态码
	 *
	 * @since 1.0.0
	 */
	public static final Integer HTTP_SUCCESS_STATUS = 200;
	/**
	 * http路径分隔符
	 *
	 * @since 1.0.0
	 */
	public static final String HTTP_PATH_SEPARATOR = "/";
	/**
	 * Token前缀
	 *
	 * @since 1.0.0
	 */
	public static final String TOKEN_PREFIX = "Bearer ";
	/**
	 * 本地Ipv4地址
	 *
	 * @since 1.0.0
	 */
	public static final String LOCALHOST_IPV4_ADDRESS = "127.0.0.1";
	/**
	 * 本地Ipv6地址
	 *
	 * @since 1.0.0
	 */
	public static final String LOCALHOST_IPV6_ADDRESS = "0:0:0:0:0:0:0:1";

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

	// 文件相关常量
	/**
	 * 任意文件类型
	 *
	 * @since 1.0.0
	 */
	public static final String ANY_MIME_TYPE = "*/*";

	// 反射相关常量
	/**
	 * set方法前缀
	 *
	 * @since 1.0.0
	 */
	public static final String SETTER_PREFIX = "set";
	/**
	 * get方法前缀
	 *
	 * @since 1.0.0
	 */
	public static final String GETTER_PREFIX = "get";
	/**
	 * cglib代理类前缀
	 *
	 * @since 1.0.0
	 */
	public static final String CGLIB_CLASS_SEPARATOR = "$$";
	/**
	 * cglib代理类方法前缀
	 *
	 * @since 1.0.0
	 */
	public static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

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