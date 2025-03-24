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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;

/**
 * 随机数组，因为Apache的{@link org.apache.commons.lang3.RandomUtils}没有这个功能，所以写了这个类
 *
 * @author pangju666
 * @see RandomList
 * @since 1.0.0
 */
public class RandomArray {
	protected static final RandomArray INSECURE = new RandomArray(RandomUtils.insecure());
	protected static final RandomArray SECURE = new RandomArray(RandomUtils.secure());
	protected static final RandomArray SECURE_STRONG = new RandomArray(RandomUtils.secureStrong());

	protected final RandomUtils randomUtils;

	protected RandomArray(RandomUtils randomUtils) {
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
	public static RandomArray insecure() {
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
	public static RandomArray secure() {
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
	public static RandomArray secureStrong() {
		return SECURE_STRONG;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param length 数组长度
	 * @return boolean数组
	 * @since 1.0.0
	 */
	public boolean[] randomBooleanArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");
		boolean[] booleans = new boolean[length];
		for (int i = 0; i < booleans.length; i++) {
			booleans[i] = randomUtils.randomBoolean();
		}
		return booleans;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param length 数组长度
	 * @return int数组
	 * @since 1.0.0
	 */
	public int[] randomIntArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		int[] values = new int[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomInt();
		}
		return values;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive 随机生成最大值
	 * @param length 数组长度
	 * @return int数组
	 * @since 1.0.0
	 */
	public int[] randomIntArray(int startInclusive, int endExclusive, int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		int[] values = new int[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomInt(startInclusive, endExclusive);
		}
		return values;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param length 数组长度
	 * @return long数组
	 * @since 1.0.0
	 */
	public long[] randomLongArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		long[] values = new long[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomLong();
		}
		return values;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive 随机生成最大值
	 * @param length 数组长度
	 * @return long数组
	 * @since 1.0.0
	 */
	public long[] randomLongArray(long startInclusive, long endExclusive, int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		long[] values = new long[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomLong(startInclusive, endExclusive);
		}
		return values;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param length 数组长度
	 * @return float数组
	 * @since 1.0.0
	 */
	public float[] randomFloatArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		float[] values = new float[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomFloat();
		}
		return values;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         数组长度
	 * @return float数组
	 * @since 1.0.0
	 */
	public float[] randomFloatArray(float startInclusive, float endExclusive, int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		float[] values = new float[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomFloat(startInclusive, endExclusive);
		}
		return values;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param length 数组长度
	 * @return double数组
	 * @since 1.0.0
	 */
	public double[] randomDoubleArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		double[] values = new double[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomDouble();
		}
		return values;
	}

	/**
	 * 生成指定长度的随机数组
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive 随机生成最大值
	 * @param length 数组长度
	 * @return double数组
	 * @since 1.0.0
	 */
	public double[] randomDoubleArray(double startInclusive, double endExclusive, int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		double[] values = new double[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomDouble(startInclusive, endExclusive);
		}
		return values;
	}

	/**
	 * 生成指定长度的不重复随机数组
	 *
	 * @param length 数组长度
	 * @return int数组
	 * @since 1.0.0
	 */
	public int[] randomUniqueIntArray(int length) {
		return randomSet(0, Integer.MAX_VALUE, length, randomUtils::randomInt)
			.stream()
			.mapToInt(Integer::intValue)
			.toArray();
	}

	/**
	 * 生成指定长度的不重复随机数组
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         数组长度
	 * @return int数组
	 * @since 1.0.0
	 */
	public int[] randomUniqueIntArray(int startInclusive, int endExclusive, int length) {
		return randomSet(startInclusive, endExclusive, length, randomUtils::randomInt)
			.stream()
			.mapToInt(Integer::intValue)
			.toArray();
	}

	/**
	 * 生成指定长度的不重复随机数组
	 *
	 * @param length 数组长度
	 * @return long数组
	 * @since 1.0.0
	 */
	public long[] randomUniqueLongArray(int length) {
		return randomSet(0L, Long.MAX_VALUE, length, randomUtils::randomLong)
			.stream()
			.mapToLong(Long::longValue)
			.toArray();
	}

	/**
	 * 生成指定长度的不重复随机数组
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         数组长度
	 * @return long数组
	 * @since 1.0.0
	 */
	public long[] randomUniqueLongArray(long startInclusive, long endExclusive, int length) {
		return randomSet(startInclusive, endExclusive, length, randomUtils::randomLong)
			.stream()
			.mapToLong(Long::longValue)
			.toArray();
	}

	/**
	 * 生成指定长度的不重复随机数组
	 *
	 * @param length 数组长度
	 * @return double数组
	 * @since 1.0.0
	 */
	public double[] randomUniqueDoubleArray(int length) {
		return randomSet(0d, Double.MAX_VALUE, length, randomUtils::randomDouble)
			.stream()
			.mapToDouble(Double::doubleValue)
			.toArray();
	}

	/**
	 * 生成指定长度的不重复随机数组
	 *
	 * @param startInclusive 随机生成最小值
	 * @param endExclusive   随机生成最大值
	 * @param length         数组长度
	 * @return double数组
	 * @since 1.0.0
	 */
	public double[] randomUniqueDoubleArray(double startInclusive, double endExclusive, int length) {
		return randomSet(startInclusive, endExclusive, length, randomUtils::randomDouble)
			.stream()
			.mapToDouble(Double::doubleValue)
			.toArray();
	}

	protected <T> Set<T> randomSet(T startInclusive, T endExclusive, int length, BiFunction<T, T, T> biFunction) {
		Validate.isTrue(length > 0, "length 不能为负数");
		Set<T> values = new HashSet<>(length);
		for (int i = 0; i < length; i++) {
			T value;
			do {
				value = biFunction.apply(startInclusive, endExclusive);
			} while (values.contains(value));
			values.add(value);
		}
		return values;
	}
}
