package io.github.pangju666.commons.lang.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ArrayUtils extends org.apache.commons.lang3.ArrayUtils {
	protected ArrayUtils() {
	}

	public static List<boolean[]> partition(final boolean[] array, int size) {
		if (size <= 0) {
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

	public static List<byte[]> partition(final byte[] array, int size) {
		if (size <= 0) {
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

	public static List<char[]> partition(final char[] array, int size) {
		if (size <= 0) {
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

	public static List<double[]> partition(final double[] array, int size) {
		if (size <= 0) {
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

	public static List<float[]> partition(final float[] array, int size) {
		if (size <= 0) {
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

	public static List<int[]> partition(final int[] array, int size) {
		if (size <= 0) {
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

	public static List<long[]> partition(final long[] array, int size) {
		if (size <= 0) {
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

	public static List<short[]> partition(final short[] array, int size) {
		if (size <= 0) {
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

	@SuppressWarnings("unchecked")
	public static <T> List<T[]> partition(final T[] array, int size) {
		Class<?> type = array.getClass().getComponentType();
		if (size <= 0) {
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
