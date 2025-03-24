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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 数组操作工具类，继承并扩展了{@link org.apache.commons.lang3.ArrayUtils}的功能
 * <p>新增数组分块方法，支持基本类型数组及对象数组的分块操作，功能类似{@link org.apache.commons.collections4.ListUtils#partition}的数组实现</p>
 *
 * @author pangju666
 * @see org.apache.commons.lang3.ArrayUtils
 * @since 1.0.0
 */
public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils {
	protected ArrayUtils() {
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @since 1.0.0
	 */
	public static List<boolean[]> partition(final boolean[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<boolean[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			boolean[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new boolean[remainder] : new boolean[size];
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @since 1.0.0
	 */
	public static List<byte[]> partition(final byte[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<byte[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			byte[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new byte[remainder] : new byte[size];
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @since 1.0.0
	 */
	public static List<char[]> partition(final char[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<char[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			char[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new char[remainder] : new char[size];
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @since 1.0.0
	 */
	public static List<double[]> partition(final double[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<double[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			double[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new double[remainder] : new double[size];
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @since 1.0.0
	 */
	public static List<float[]> partition(final float[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<float[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			float[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new float[remainder] : new float[size];
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @since 1.0.0
	 */
	public static List<int[]> partition(final int[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<int[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			int[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new int[remainder] : new int[size];
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @since 1.0.0
	 */
	public static List<long[]> partition(final long[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<long[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			long[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new long[remainder] : new long[size];
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @since 1.0.0
	 */
	public static List<short[]> partition(final short[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<short[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			short[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new short[remainder] : new short[size];
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将指定数组按给定大小分块（类似 {@link org.apache.commons.collections4.ListUtils#partition ListUtils.partition}）
	 *
	 * @param array 待分块的数组（null或空数组时返回空列表）
	 * @param size  每块的大小（小于等于0时返回空列表）
	 * @param <T>   数组元素的类型
	 * @return 包含分块数组的不可变列表（当array无效或size不合法时返回{@link Collections#emptyList()}）
	 * @see Array#newInstance
	 * @since 1.0.0
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T[]> partition(final T[] array, final int size) {
		if (size <= 0 || ArrayUtils.isEmpty(array)) {
			return Collections.emptyList();
		}

		Class<?> type = array.getClass().getComponentType();
		if (Objects.isNull(type)) {
			return Collections.emptyList();
		}

		int remainder = array.length % size;
		int arrayCount = array.length / size + (remainder > 0 ? 1 : 0);
		List<T[]> partitions = new ArrayList<>(arrayCount);
		int arrayNum = 0;
		int startIndex = 0;
		int endIndex = size - 1;
		while (arrayNum < arrayCount) {
			T[] partition = (T[]) (arrayNum == arrayCount - 1 && remainder > 0 ?
				Array.newInstance(type, remainder) : Array.newInstance(type, size));
			System.arraycopy(array, startIndex, partition, 0, endIndex - startIndex + 1);
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}
}
