package io.github.pangju666.commons.lang.pool;

public class ConstantPool {
	// 日期/时间相关常量
	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String TIME_FORMAT = "HH:mm:ss";

	// 字符串相关常量
	public static final char START_CHINESE_CHARACTER = '\u4e00';
	public static final char END_CHINESE_CHARACTER = '\u9fa5';
	public static final char START_NUMBER_CHARACTER = '0';
	public static final char END_NUMBER_CHARACTER = '9';
	public static final char START_UPPERCASE_ALPHABETIC_CHARACTER = 'A';
	public static final char END_UPPERCASE_ALPHABETIC_CHARACTER = 'Z';
	public static final char START_LOWERCASE_ALPHABETIC_CHARACTER = 'a';
	public static final char END_LOWERCASE_ALPHABETIC_CHARACTER = 'z';

	// 网络相关常量
	public static final String HTTP_PREFIX = "http://";
	public static final String HTTPS_PREFIX = "https://";
	public static final Integer HTTP_SUCCESS_STATUS = 200;
	public static final String HTTP_PATH_SEPARATOR = "/";
	public static final String TOKEN_PREFIX = "Bearer ";
	public static final String LOCALHOST_IPV4_ADDRESS = "127.0.0.1";
	public static final String LOCALHOST_IPV6_ADDRESS = "0:0:0:0:0:0:0:1";

	// json 相关常量
	public static final String EMPTY_JSON_OBJECT_STR = "{}";
	public static final String EMPTY_JSON_ARRAY_STR = "[]";

	// 文件相关常量
	public static final String ANY_MIME_TYPE = "*/*";

	// 反射相关常量
	public static final String SETTER_PREFIX = "set";
	public static final String GETTER_PREFIX = "get";
	public static final String CGLIB_CLASS_SEPARATOR = "$$";
	public static final String CGLIB_RENAMED_METHOD_PREFIX = "CGLIB$";

	// XML相关常量
	/**
	 * 字符串常量：XML 不间断空格转义 {@code "&nbsp;" -> " "}
	 */
	public static final String XML_NBSP = "&nbsp;";
	/**
	 * 字符串常量：XML And 符转义 {@code "&amp;" -> "&"}
	 */
	public static final String XML_AMP = "&amp;";
	/**
	 * 字符串常量：XML 双引号转义 {@code "&quot;" -> "\""}
	 */
	public static final String XML_QUOTE = "&quot;";
	/**
	 * 字符串常量：XML 单引号转义 {@code "&apos" -> "'"}
	 */
	public static final String XML_APOS = "&apos;";
	/**
	 * 字符串常量：XML 小于号转义 {@code "&lt;" -> "<"}
	 */
	public static final String XML_LT = "&lt;";
	/**
	 * 字符串常量：XML 大于号转义 {@code "&gt;" -> ">"}
	 */
	public static final String XML_GT = "&gt;";

	protected ConstantPool() {
	}
}