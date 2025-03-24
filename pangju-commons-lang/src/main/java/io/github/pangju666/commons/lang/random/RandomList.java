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
 * 随机集合，因为Apache的{@link RandomUtils}没有这个功能，所以写了这个类
 *
 * @author pangju666
 * @see RandomArray
 * @since 1.0.0
 */
public class RandomList {
	protected static final RandomList INSECURE = new RandomList(RandomUtils.insecure());
	protected static final RandomList SECURE = new RandomList(RandomUtils.secure());
	protected static final RandomList SECURE_STRONG = new RandomList(RandomUtils.secureStrong());

	protected final RandomUtils randomUtils;

	protected RandomList(RandomUtils randomUtils) {
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
	 * 生成指定长度的随机List
	 *
	 * @param length List长度
	 * @return Boolean List
	 * @since 1.0.0
	 */
	public List<Boolean> randomBooleanList(int length) {
		return randomList(null, null, length, false,
			(l, r) -> randomUtils.randomBoolean());
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param length List长度
	 * @return Integer List
	 * @since 1.0.0
	 */
	public List<Integer> randomIntegerList(int length) {
		return randomList(0, Integer.MAX_VALUE, length, false, randomUtils::randomInt);
	}

	/**
	 * 生成指定长度的无重复值随机List
	 *
	 * @param length List长度
	 * @return Integer List
	 * @since 1.0.0
	 */
	public List<Integer> randomUniqueIntegerList(int length) {
		return randomList(0, Integer.MAX_VALUE, length, true, randomUtils::randomInt);
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @return Integer List
	 * @since 1.0.0
	 */
	public List<Integer> randomIntegerList(int startInclusive, int endExclusive, int length) {
		return randomList(startInclusive, endExclusive, length, false, randomUtils::randomInt);
	}

	/**
	 * 生成指定长度的无重复值随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @return Integer List
	 * @since 1.0.0
	 */
	public List<Integer> randomUniqueIntegerList(int startInclusive, int endExclusive, int length) {
		return randomList(startInclusive, endExclusive, length, true, randomUtils::randomInt);
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param length List长度
	 * @return Float List
	 * @since 1.0.0
	 */
	public List<Float> randomFloatList(int length) {
		return randomList(0f, Float.MAX_VALUE, length, false, randomUtils::randomFloat);
	}

	/**
	 * 生成指定长度的无重复值随机List
	 *
	 * @param length List长度
	 * @return Float List
	 * @since 1.0.0
	 */
	public List<Float> randomUniqueFloatList(int length) {
		return randomList(0f, Float.MAX_VALUE, length, true, randomUtils::randomFloat);
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @return Float List
	 * @since 1.0.0
	 */
	public List<Float> randomFloatList(float startInclusive, float endExclusive, int length) {
		return randomList(startInclusive, endExclusive, length, false, randomUtils::randomFloat);
	}

	/**
	 * 生成指定长度的无重复值随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @return Float List
	 * @since 1.0.0
	 */
	public List<Float> randomUniqueFloatList(float startInclusive, float endExclusive, int length) {
		return randomList(startInclusive, endExclusive, length, true, randomUtils::randomFloat);
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param length List长度
	 * @return Long List
	 * @since 1.0.0
	 */
	public List<Long> randomLongList(int length) {
		return randomList(0L, Long.MAX_VALUE, length, false, randomUtils::randomLong);
	}

	/**
	 * 生成指定长度的无重复值随机List
	 *
	 * @param length List长度
	 * @return Long List
	 * @since 1.0.0
	 */
	public List<Long> randomUniqueLongList(int length) {
		return randomList(0L, Long.MAX_VALUE, length, true, randomUtils::randomLong);
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @return Long List
	 * @since 1.0.0
	 */
	public List<Long> randomLongList(long startInclusive, long endExclusive, int length) {
		return randomList(startInclusive, endExclusive, length, false, randomUtils::randomLong);
	}

	/**
	 * 生成指定长度的无重复值随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @return Long List
	 * @since 1.0.0
	 */
	public List<Long> randomUniqueLongList(long startInclusive, long endExclusive, int length) {
		return randomList(startInclusive, endExclusive, length, true, randomUtils::randomLong);
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @return Double List
	 * @since 1.0.0
	 */
	public List<Double> randomDoubleList(double startInclusive, double endExclusive, int length) {
		return randomList(startInclusive, endExclusive, length, false, randomUtils::randomDouble);
	}

	/**
	 * 生成指定长度的无重复值随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @return Double List
	 * @since 1.0.0
	 */
	public List<Double> randomUniqueDoubleList(double startInclusive, double endExclusive, int length) {
		return randomList(startInclusive, endExclusive, length, true, randomUtils::randomDouble);
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param length List长度
	 * @return Double List
	 * @since 1.0.0
	 */
	public List<Double> randomDoubleList(int length) {
		return randomList(0d, Double.MAX_VALUE, length, false, randomUtils::randomDouble);
	}

	/**
	 * 生成指定长度的无重复值随机List
	 *
	 * @param length List长度
	 * @return Double List
	 * @since 1.0.0
	 */
	public List<Double> randomUniqueDoubleList(int length) {
		return randomList(0d, Double.MAX_VALUE, length, true, randomUtils::randomDouble);
	}

	/**
	 * 生成指定长度的随机List
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         List长度
	 * @param unique         是否允许重复值
	 * @param biFunction     随机值生成函数
	 * @param <T>            值类型
	 * @return 指定类型的List
	 * @since 1.0.0
	 */
	public <T> List<T> randomList(T startInclusive, T endExclusive, int length, boolean unique, BiFunction<T, T, T> biFunction) {
		Validate.isTrue(length > 0, "length 不能为负数");
		List<T> values = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			T value;
			if (unique) {
				value = biFunction.apply(startInclusive, endExclusive);
			} else {
				do {
					value = biFunction.apply(startInclusive, endExclusive);
				} while (values.contains(value));
			}
			values.add(value);
		}
		return values;
	}
}
