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

import io.github.pangju666.commons.lang.enums.RegexFlag;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 正则表达式工具类，继承并扩展了{@link org.apache.commons.lang3.RegExUtils}的功能
 * <p>提供正则表达式编译、匹配检测、模式查找等增强功能</p>
 *
 * @author pangju666
 * @see org.apache.commons.lang3.RegExUtils
 * @see io.github.pangju666.commons.lang.pool.RegExPool
 * @see java.util.regex.Pattern
 * @since 1.0.0
 */
public class RegExUtils extends org.apache.commons.lang3.RegExUtils {
	protected RegExUtils() {
	}

	/**
	 * 计算正则表达式标志位的组合值
	 *
	 * @param regexFlags 正则表达式标志枚举数组
	 * @return 组合后的标志位整数值
	 * @since 1.0.0
	 */
	public static int computeFlags(final RegexFlag... regexFlags) {
		int flags = 0;
		for (RegexFlag regexFlag : regexFlags) {
			if (flags == 0) {
				flags = regexFlag.getValue();
			} else {
				flags |= regexFlag.getValue();
			}
		}
		return flags;
	}

	/**
	 * 带标志位重新编译现有模式对象
	 *
	 * @param pattern 要复制的模式对象
	 * @param flags   正则表达式标志位组合值
	 * @return 新编译的Pattern对象
	 * @since 1.0.0
	 */
	public static Pattern compile(final Pattern pattern, final int flags) {
		Validate.notNull(pattern, "pattern 不可为null");
		return Pattern.compile(pattern.pattern(), flags);
	}

	/**
	 * 编译正则表达式并配置起止匹配（无标志位）
	 *
	 * @param regex      正则表达式字符串
	 * @param matchStart 是否强制起始匹配(自动添加^前缀)
	 * @param matchEnd   是否强制结束匹配(自动添加$后缀)
	 * @return 编译后的Pattern对象
	 * @since 1.0.0
	 */
	public static Pattern compile(final String regex, final boolean matchStart, final boolean matchEnd) {
		return compile(regex, 0, matchStart, matchEnd);
	}

	/**
	 * 完整参数编译正则表达式
	 *
	 * @param regex      正则表达式字符串
	 * @param flags      正则表达式标志位组合值
	 * @param matchStart 是否强制起始匹配(自动添加^前缀)
	 * @param matchEnd   是否强制结束匹配(自动添加$后缀)
	 * @return 编译后的Pattern对象
	 * @since 1.0.0
	 */
	public static Pattern compile(final String regex, final int flags, final boolean matchStart, final boolean matchEnd) {
		Validate.notBlank(regex, "regex 不可为空");
		return Pattern.compile((matchStart && !regex.startsWith("^") ? "^" : "") + regex +
			(matchEnd && !regex.endsWith("$") ? "$" : ""), flags);
	}

	/**
	 * 检查字符串是否完全匹配正则表达式
	 *
	 * @param regex 正则表达式字符串
	 * @param str 要匹配的目标字符串
	 * @return 完全匹配时返回true
	 * @since 1.0.0
	 */
	public static boolean matches(final String regex, final String str) {
		Validate.notBlank(regex, "regex 不可为空");
		if (StringUtils.isBlank(str)) {
			return false;
		}
		return Pattern.compile(regex).matcher(str).matches();
	}

	/**
	 * 检查字符串是否完全匹配模式对象
	 *
	 * @param pattern 编译后的Pattern对象
	 * @param str 要匹配的目标字符串
	 * @return 完全匹配时返回true
	 * @since 1.0.0
	 */
	public static boolean matches(final Pattern pattern, final String str) {
		Validate.notNull(pattern, "pattern 不可为null");
		if (StringUtils.isBlank(str)) {
			return false;
		}
		return pattern.matcher(str).matches();
	}

	/**
	 * 查找字符串中所有匹配正则表达式的子串
	 *
	 * @param regex 正则表达式字符串
	 * @param str 要查找的目标字符串
	 * @return 包含所有匹配子串的列表（可能为空列表）
	 * @since 1.0.0
	 */
	public static List<String> find(final String regex, final String str) {
		Validate.notBlank(regex, "regex 不可为空");
		return find(Pattern.compile(regex), str);
	}

	/**
	 * 查找字符串中所有匹配模式对象的子串
	 *
	 * @param pattern 编译后的Pattern对象
	 * @param str 要查找的目标字符串
	 * @return 包含所有匹配子串的列表（可能为空列表）
	 * @since 1.0.0
	 */
	public static List<String> find(final Pattern pattern, final String str) {
		Validate.notNull(pattern, "pattern 不可为null");
		if (StringUtils.isBlank(str)) {
			return Collections.emptyList();
		}
		List<String> result = new ArrayList<>();
		Matcher matcher = pattern.matcher(str);
		while (matcher.find()) {
			result.add(matcher.group());
		}
		return result;
	}
}
