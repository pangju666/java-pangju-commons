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
 * 随机数组生成工具类
 * <p>提供基本类型数组的随机生成能力，支持普通随机数组和元素唯一随机数组的生成</p>
 *
 * @author pangju666
 * @see org.apache.commons.lang3.RandomUtils
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
	 * 生成随机布尔数组
	 *
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机布尔值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public boolean[] randomBooleanArray(final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");
		boolean[] booleans = new boolean[length];
		for (int i = 0; i < booleans.length; i++) {
			booleans[i] = randomUtils.randomBoolean();
		}
		return booleans;
	}

	/**
	 * 生成全范围随机整数数组
	 *
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机整数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public int[] randomIntArray(final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		int[] values = new int[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomInt();
		}
		return values;
	}

	/**
	 * 生成指定范围内的唯一随机整数数组
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机整数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public int[] randomIntArray(final int startInclusive, final int endExclusive, final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		int[] values = new int[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomInt(startInclusive, endExclusive);
		}
		return values;
	}

	/**
	 * 生成全范围随机长整数数组
	 *
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机长整数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public long[] randomLongArray(final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		long[] values = new long[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomLong();
		}
		return values;
	}

	/**
	 * 生成指定范围内的唯一随机长整数数组
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机长整数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public long[] randomLongArray(final long startInclusive, final long endExclusive, final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		long[] values = new long[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomLong(startInclusive, endExclusive);
		}
		return values;
	}

	/**
	 * 生成全范围随机单精度浮点数数组
	 *
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机单精度浮点数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public float[] randomFloatArray(final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		float[] values = new float[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomFloat();
		}
		return values;
	}

	/**
	 * 生成指定范围内的唯一随机单精度浮点数数组
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机单精度浮点数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public float[] randomFloatArray(final float startInclusive, final float endExclusive, final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		float[] values = new float[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomFloat(startInclusive, endExclusive);
		}
		return values;
	}

	/**
	 * 生成全范围随机双精度浮点数数组
	 *
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机双精度浮点数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public double[] randomDoubleArray(final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		double[] values = new double[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomDouble();
		}
		return values;
	}

	/**
	 * 生成指定范围内的唯一随机双精度浮点数数组
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length 数组长度（必须大于0）
	 * @return 包含随机双精度浮点数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public double[] randomDoubleArray(final double startInclusive, final double endExclusive, final int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		double[] values = new double[length];
		for (int i = 0; i < values.length; i++) {
			values[i] = randomUtils.randomDouble(startInclusive, endExclusive);
		}
		return values;
	}

	/**
	 * 生成全范围唯一随机整数数组
	 *
	 * @param length 数组长度（必须大于0）
	 * @return 元素唯一的随机整数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public int[] randomUniqueIntArray(final int length) {
		return randomSet(0, Integer.MAX_VALUE, length, randomUtils::randomInt)
			.stream()
			.mapToInt(Integer::intValue)
			.toArray();
	}

	/**
	 * 生成指定范围内的唯一随机整数数组
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length 数组长度（必须大于0）
	 * @return 元素唯一的随机整数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public int[] randomUniqueIntArray(final int startInclusive, final int endExclusive, final int length) {
		return randomSet(startInclusive, endExclusive, length, randomUtils::randomInt)
			.stream()
			.mapToInt(Integer::intValue)
			.toArray();
	}

	/**
	 * 生成全范围唯一随机长整数数组
	 *
	 * @param length 数组长度（必须大于0）
	 * @return 元素唯一的随机长整数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public long[] randomUniqueLongArray(final int length) {
		return randomSet(0L, Long.MAX_VALUE, length, randomUtils::randomLong)
			.stream()
			.mapToLong(Long::longValue)
			.toArray();
	}

	/**
	 * 生成指定范围内的唯一随机长整数数组
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length 数组长度（必须大于0）
	 * @return 元素唯一的随机长整数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public long[] randomUniqueLongArray(final long startInclusive, final long endExclusive, final int length) {
		return randomSet(startInclusive, endExclusive, length, randomUtils::randomLong)
			.stream()
			.mapToLong(Long::longValue)
			.toArray();
	}

	/**
	 * 生成全范围唯一随机双精度浮点数数组
	 *
	 * @param length 数组长度（必须大于0）
	 * @return 元素唯一的随机双精度浮点数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @since 1.0.0
	 */
	public double[] randomUniqueDoubleArray(final int length) {
		return randomSet(0d, Double.MAX_VALUE, length, randomUtils::randomDouble)
			.stream()
			.mapToDouble(Double::doubleValue)
			.toArray();
	}

	/**
	 * 生成指定范围内的唯一随机双精度浮点数数组
	 *
	 * @param startInclusive 最小值（包含），必须是非负值
	 * @param endExclusive   最大值（不包含）
	 * @param length 数组长度（必须大于0）
	 * @return 元素唯一的随机双精度浮点数值的数组
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	public double[] randomUniqueDoubleArray(final double startInclusive, final double endExclusive, final int length) {
		return randomSet(startInclusive, endExclusive, length, randomUtils::randomDouble)
			.stream()
			.mapToDouble(Double::doubleValue)
			.toArray();
	}

	/**
	 * 基础随机集合生成方法
	 *
	 * @param startInclusive 最小值（包含）
	 * @param endExclusive   最大值（不包含）
	 * @param length         集合大小（必须大于0）
	 * @param biFunction     随机数生成函数
	 * @param <T>            数值类型
	 * @return 包含唯一随机值的集合
	 * @throws IllegalArgumentException 当length不合法时抛出
	 * @throws IllegalArgumentException 如果 {@code startInclusive > endExclusive} 或如果 {@code startInclusive} 是负数
	 * @since 1.0.0
	 */
	protected <T> Set<T> randomSet(final T startInclusive, final T endExclusive, final int length,
								   final BiFunction<T, T, T> biFunction) {
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
