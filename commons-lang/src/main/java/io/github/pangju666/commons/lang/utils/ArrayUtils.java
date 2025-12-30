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
 * 数组操作工具类
 * <p>
 * 继承并扩展了{@link org.apache.commons.lang3.ArrayUtils}的功能，提供以下增强特性：
 * <ul>
 *   <li>数组分割功能：将数组按指定大小分割成子数组列表</li>
 *   <li>支持所有基本类型数组和泛型数组操作</li>
 *   <li>线程安全的方法实现</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @see org.apache.commons.lang3.ArrayUtils
 * @since 1.0.0
 */
public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils {
	protected ArrayUtils() {
	}

	/**
	 * 将数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的数组
	 * @param size 每个子数组的大小
	 * @return 分割后的子数组列表，不会返回null
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
			boolean[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new boolean[remainder] :
				new boolean[Math.min(size, array.length)];
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将byte数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的数组
	 * @param size 每个子数组的大小
	 * @return 分割后的子数组列表，不会返回null
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
			byte[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new byte[remainder] :
				new byte[Math.min(size, array.length)];
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将char数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的char数组
	 * @param size 每个子数组的大小
	 * @return 分割后的子数组列表，不会返回null
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
			char[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new char[remainder] :
				new char[Math.min(size, array.length)];
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将double数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的double数组
	 * @param size 每个子数组的大小
	 * @return 分割后的子数组列表，不会返回null
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
			double[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new double[remainder] :
				new double[Math.min(size, array.length)];
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将float数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的float数组
	 * @param size 每个子数组的大小
	 * @return 分割后的子数组列表，不会返回null
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
			float[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new float[remainder] :
				new float[Math.min(size, array.length)];
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将int数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的int数组
	 * @param size 每个子数组的大小
	 * @return 分割后的子数组列表，不会返回null
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
			int[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new int[remainder] :
				new int[Math.min(size, array.length)];
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将long数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的long数组
	 * @param size 每个子数组的大小
	 * @return 分割后的子数组列表，不会返回null
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
			long[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new long[remainder] :
				new long[Math.min(size, array.length)];
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将short数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的short数组
	 * @param size 每个子数组的大小
	 * @return 分割后的子数组列表，不会返回null
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
			short[] partition = arrayNum == arrayCount - 1 && remainder > 0 ? new short[remainder] :
				new short[Math.min(size, array.length)];
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}

	/**
	 * 将泛型数组分割成指定大小的子数组列表
	 * <p>
	 * 如果数组为空或size小于等于0，返回空列表。
	 * 最后一个子数组的大小可能小于指定size。
	 * </p>
	 *
	 * @param array 要分割的数组
	 * @param size 每个子数组的大小
	 * @param <T> 数组元素类型
	 * @return 分割后的子数组列表，不会返回null
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
			int length = endIndex - startIndex + 1;
			System.arraycopy(array, startIndex, partition, 0, Math.min(length, array.length));
			partitions.add(arrayNum, partition);

			startIndex = endIndex + 1;
			endIndex = Math.min(array.length, startIndex + size) - 1;
			++arrayNum;
		}
		return partitions;
	}
}
