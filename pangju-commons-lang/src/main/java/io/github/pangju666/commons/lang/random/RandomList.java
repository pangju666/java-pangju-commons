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

package io.github.pangju666.commons.lang.random;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;

import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

/**
 * 随机列表生成工具类
 * <p>提供基本类型列表的随机生成能力，支持普通列表和元素唯一列表的生成</p>
 *
 * @author pangju666
 * @see org.apache.commons.lang3.RandomUtils
 * @since 1.0.0
 */
public class RandomList {
	protected static final RandomList INSECURE = new RandomList(RandomUtils.insecure());
	protected static final RandomList SECURE = new RandomList(RandomUtils.secure());
	protected static final RandomList SECURE_STRONG = new RandomList(RandomUtils.secureStrong());

	protected final RandomUtils randomUtils;

	protected RandomList(final RandomUtils randomUtils) {
		this.randomUtils = randomUtils;
	}

	/**
	 * 基于 {@link ThreadLocalRandom#current()} 获取单例实例；<b>这在密码学上并不安全</b>；请使用 {@link #secure()} 中指定的算法/提供者。
	 * 使用{@link #secure()}来使用{@code securerandom.strongAlgorithms}中指定的算法/提供者。
	 * {@code securerandom.strongAlgorithms} {@link Security} 属性中指定的算法/提供者。
	 * <p>
	 * 按需调用 {@link ThreadLocalRandom#current()} 方法。
	 * </p>
	 * <p>
	 * 返回基于 {@link ThreadLocalRandom#current()} 的单例实例。
	 *
	 * @see ThreadLocalRandom#current()
	 * @see #secure()
	 * @since 1.0.0
	 */
	public static RandomList insecure() {
		return INSECURE;
	}

	/**
	 * 获取基于 {@link SecureRandom#SecureRandom()} 的单例实例，该实例使用 {@code securerandom.strongAlgorithms} {@link Security} 属性中指定的算法/提供者。
	 * 在 {@code securerandom.strongAlgorithms} {@link Security} 属性中指定。
	 * <p>
	 * 按需调用 {@link SecureRandom#SecureRandom()} 方法。
	 * </p>
	 * <p>
	 * 返回基于 {@link SecureRandom#SecureRandom()} 的单例实例。
	 *
	 * @see SecureRandom#SecureRandom()
	 * @since 1.0.0
	 */
	public static RandomList secure() {
		return SECURE;
	}

	/**
	 * 获取基于 {@link SecureRandom#getInstanceStrong()} 的单例实例，该实例使用 {@code Securerandom.strongAlgorithms} {@link Security} 属性中指定的算法/提供者。
	 * 在 {@code securerandom.strongAlgorithms} {@link Security} 属性中指定。
	 * <p>
	 * 按需调用 {@link SecureRandom#getInstanceStrong()} 方法。
	 * </p>
	 * <p>
	 * 返回基于 {@link SecureRandom#getInstanceStrong()} 的单例实例。
	 *
	 * @see SecureRandom#getInstanceStrong()
	 * @since 1.0.0
	 */
	public static RandomList secureStrong() {
		return SECURE_STRONG;
	}

	/**
	 * 生成随机布尔列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 包含随机布尔值的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Boolean> randomBooleanList(final int length) {
		return randomList(null, null, length, false,
			(l, r) -> randomUtils.randomBoolean());
	}

	/**
	 * 生成全范围随机整数列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 包含随机整数的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Integer> randomIntegerList(final int length) {
		return randomList(0, Integer.MAX_VALUE, length, false, randomUtils::randomInt);
	}

	/**
	 * 生成全范围唯一随机整数列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 元素唯一的随机整数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Integer> randomUniqueIntegerList(final int length) {
		return randomList(0, Integer.MAX_VALUE, length, true, randomUtils::randomInt);
	}

	/**
	 * 生成指定范围随机整数列表
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @return 包含随机整数的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public List<Integer> randomIntegerList(final int startInclusive, final int endExclusive, final int length) {
		return randomList(startInclusive, endExclusive, length, false, randomUtils::randomInt);
	}

	/**
	 * 生成指定范围唯一随机整数列表
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @return 元素唯一的随机整数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public List<Integer> randomUniqueIntegerList(final int startInclusive, final int endExclusive, final int length) {
		return randomList(startInclusive, endExclusive, length, true, randomUtils::randomInt);
	}

	/**
	 * 生成全范围随机单精度浮点数列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 包含随机单精度浮点数的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Float> randomFloatList(final int length) {
		return randomList(0f, Float.MAX_VALUE, length, false, randomUtils::randomFloat);
	}

	/**
	 * 生成全范围唯一随机单精度浮点数列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 元素唯一的随机单精度浮点数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Float> randomUniqueFloatList(final int length) {
		return randomList(0f, Float.MAX_VALUE, length, true, randomUtils::randomFloat);
	}

	/**
	 * 生成指定范围随机单精度浮点数列表
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @return 包含随机单精度浮点数的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public List<Float> randomFloatList(final float startInclusive, final float endExclusive, final int length) {
		return randomList(startInclusive, endExclusive, length, false, randomUtils::randomFloat);
	}

	/**
	 * 生成指定范围唯一随机单精度浮点数列表
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @return 元素唯一的随机单精度浮点数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public List<Float> randomUniqueFloatList(final float startInclusive, final float endExclusive, final int length) {
		return randomList(startInclusive, endExclusive, length, true, randomUtils::randomFloat);
	}

	/**
	 * 生成全范围随机长整数列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 包含随机长整数的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Long> randomLongList(final int length) {
		return randomList(0L, Long.MAX_VALUE, length, false, randomUtils::randomLong);
	}

	/**
	 * 生成全范围唯一随机长整数列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 元素唯一的随机单长整数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Long> randomUniqueLongList(final int length) {
		return randomList(0L, Long.MAX_VALUE, length, true, randomUtils::randomLong);
	}

	/**
	 * 生成指定范围随机长整数列表
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @return 包含随机长整数的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public List<Long> randomLongList(final long startInclusive, final long endExclusive, final int length) {
		return randomList(startInclusive, endExclusive, length, false, randomUtils::randomLong);
	}

	/**
	 * 生成指定范围唯一随机长整数列表
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @return 元素唯一的随机长整数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public List<Long> randomUniqueLongList(final long startInclusive, final long endExclusive, final int length) {
		return randomList(startInclusive, endExclusive, length, true, randomUtils::randomLong);
	}

	/**
	 * 生成指定范围唯一随机双精度浮点数列表
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @return 元素唯一的随机双精度浮点数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public List<Double> randomDoubleList(final double startInclusive, final double endExclusive, final int length) {
		return randomList(startInclusive, endExclusive, length, false, randomUtils::randomDouble);
	}

	/**
	 * 生成指定范围唯一随机双精度浮点数列表
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @return 元素唯一的随机双精度浮点数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public List<Double> randomUniqueDoubleList(final double startInclusive, final double endExclusive, final int length) {
		return randomList(startInclusive, endExclusive, length, true, randomUtils::randomDouble);
	}

	/**
	 * 生成全范围随机双精度浮点数列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 包含随机双精度浮点数的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Double> randomDoubleList(final int length) {
		return randomList(0d, Double.MAX_VALUE, length, false, randomUtils::randomDouble);
	}

	/**
	 * 生成全范围唯一随机双精度浮点数列表
	 *
	 * @param length 列表长度（必须大于0）
	 * @return 元素唯一的随机双精度浮点数列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public List<Double> randomUniqueDoubleList(final int length) {
		return randomList(0d, Double.MAX_VALUE, length, true, randomUtils::randomDouble);
	}

	/**
	 * 基础随机列表生成方法
	 *
	 * @param startInclusive 最小值（包含）
	 * @param endExclusive   最大值（不包含）
	 * @param length         列表长度（必须大于0）
	 * @param unique         是否要求元素唯一
	 * @param biFunction     随机数生成函数
	 * @param <T>            数值类型
	 * @return 包含随机值的列表
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	protected <T> List<T> randomList(final T startInclusive, final T endExclusive, final int length, final boolean unique,
									 final BiFunction<T, T, T> biFunction) {
		Validate.isTrue(length > 0, "length 不能为负数");
		List<T> values = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			T value;
			if (unique) {
				do {
					value = biFunction.apply(startInclusive, endExclusive);
				} while (values.contains(value));
			} else {
				value = biFunction.apply(startInclusive, endExclusive);
			}
			values.add(value);
		}
		return values;
	}
}