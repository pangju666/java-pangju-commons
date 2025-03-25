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

package io.github.pangju666.commons.validation.utils;

import io.github.pangju666.commons.lang.utils.RegExUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * 约束验证工具类
 * <p>提供通用的约束验证静态方法，用于校验字符串、集合等数据格式</p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ConstraintValidatorUtils {
	protected ConstraintValidatorUtils() {
	}

	/**
	 * 验证单个字符串值
	 *
	 * @param value    待验证的字符串值
	 * @param notBlank 是否要求非空白（当值为空白字符串时，若该参数为true则验证失败）
	 * @param notEmpty 是否要求非空（当值为空字符串时，若该参数为true则验证失败）
	 * @param pattern  正则表达式模式（用于格式校验）
	 * @return 验证结果：
	 * <ul>
	 *     <li>当值为null时始终返回true</li>
	 *     <li>当值为空/空白字符串时根据notEmpty/notBlank参数判断</li>
	 *     <li>其他情况返回正则匹配结果</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean validate(final String value, boolean notBlank, boolean notEmpty, final Pattern pattern) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (value.isBlank()) {
			return value.isEmpty() ? !notEmpty : !notBlank;
		}
		return RegExUtils.matches(pattern, value);
	}

	/**
	 * 验证单个字符串值（使用谓词判断）
	 *
	 * @param value     待验证的字符串值
	 * @param notBlank  是否要求非空白
	 * @param notEmpty  是否要求非空
	 * @param predicate 自定义验证谓词
	 * @return 验证结果（判断逻辑同pattern版本）
	 * @since 1.0.0
	 */
	public static boolean validate(final String value, boolean notBlank, boolean notEmpty, final Predicate<String> predicate) {
		if (Objects.isNull(value)) {
			return true;
		}
		if (value.isBlank()) {
			return value.isEmpty() ? !notEmpty : !notBlank;
		}
		return predicate.test(value);
	}

	/**
	 * 验证字符串集合
	 *
	 * @param values   待验证的字符串集合
	 * @param allMatch 是否要求所有元素都匹配
	 * @param notEmpty 是否要求集合非空
	 * @param pattern  正则表达式模式
	 * @return 验证结果：
	 * <ul>
	 *     <li>空集合时根据notEmpty参数判断</li>
	 *     <li>非空集合时根据allMatch判断全部/任一元素匹配</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static boolean validate(final Collection<String> values, boolean allMatch, boolean notEmpty, final Pattern pattern) {
		if (CollectionUtils.isEmpty(values)) {
			return !notEmpty;
		}
		if (allMatch) {
			return values.stream().allMatch(value -> StringUtils.isNotBlank(value) && RegExUtils.matches(pattern, value));
		} else {
			return values.stream().anyMatch(value -> StringUtils.isNotBlank(value) && RegExUtils.matches(pattern, value));
		}
	}

	/**
	 * 泛型集合验证
	 *
	 * @param values    待验证的集合
	 * @param allMatch  是否要求所有元素匹配
	 * @param notEmpty  是否要求集合非空
	 * @param predicate 元素验证谓词
	 * @param <T>       集合元素类型
	 * @return 验证结果：
	 * <ul>
	 *     <li>空集合时根据notEmpty参数判断</li>
	 *     <li>非空集合时根据allMatch判断全部/任一元素匹配</li>
	 * </ul>
	 * @since 1.0.0
	 */
	public static <T> boolean validate(final Collection<T> values, boolean allMatch, boolean notEmpty, final Predicate<T> predicate) {
		if (CollectionUtils.isEmpty(values)) {
			return !notEmpty;
		}
		if (allMatch) {
			return values.stream().allMatch(predicate);
		} else {
			return values.stream().anyMatch(predicate);
		}
	}
}
