package io.github.pangju666.commons.lang.utils;

import org.apache.commons.lang3.Validate;

import java.util.Arrays;
import java.util.Random;

public class RandomUtils {
	protected static final Random RANDOM = new Random();

	protected RandomUtils() {
	}

	public static boolean[] nextBooleanArray(int length) {
		return nextBooleanArray(length, RANDOM);
	}

	public static boolean[] nextBooleanArray(int length, final Random random) {
		Validate.isTrue(length > 0, "length 不能为负数");

		boolean[] booleans = new boolean[length];
		for (int i = 0; i < booleans.length; i++) {
			booleans[i] = random.nextBoolean();
		}
		return booleans;
	}

	public static int[] nextIntArray(int length) {
		return nextIntArray(length, RANDOM);
	}

	public static int[] nextIntArray(int length, final Random random) {
		return nextIntArray(0, Integer.MAX_VALUE, length, random);
	}

	public static int[] nextIntArray(int startInclusive, int endExclusive, int length) {
		return nextIntArray(startInclusive, endExclusive, length, RANDOM);
	}

	public static int[] nextIntArray(int startInclusive, int endExclusive, int length, final Random random) {
		Validate.isTrue(endExclusive >= startInclusive, "startInclusive 必须小于或等于 endExclusive");
		Validate.isTrue(startInclusive >= 0, "startInclusive 和 endExclusive 必须都是非负数");
		Validate.isTrue(length > 0, "length 不能为负数");

		int[] numbers = new int[length];
		if (startInclusive == endExclusive) {
			Arrays.fill(numbers, startInclusive);
		} else {
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = startInclusive + random.nextInt(endExclusive - startInclusive);
			}
		}
		return numbers;
	}

	public static long[] nextLongArray(int length) {
		return nextLongArray(length, RANDOM);
	}

	public static long[] nextLongArray(int length, final Random random) {
		Validate.isTrue(length > 0, "length 不能为负数");

		long[] numbers = new long[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = nextLong(Long.MAX_VALUE, random);
		}
		return numbers;
	}

	public static long[] nextLongArray(long startInclusive, long endExclusive, int length) {
		return nextLongArray(startInclusive, endExclusive, length, RANDOM);
	}

	public static long[] nextLongArray(long startInclusive, long endExclusive, int length, final Random random) {
		Validate.isTrue(endExclusive >= startInclusive, "startInclusive 必须小于或等于 endExclusive");
		Validate.isTrue(startInclusive >= 0, "startInclusive 和 endExclusive 必须都是非负数");
		Validate.isTrue(length > 0, "length 不能为负数");

		long[] numbers = new long[length];
		if (startInclusive == endExclusive) {
			Arrays.fill(numbers, startInclusive);
		} else {
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = startInclusive + nextLong(endExclusive - startInclusive, random);
			}
		}
		return numbers;
	}

	public static float[] nextFloatArray(int length) {
		return nextFloatArray(length, RANDOM);
	}

	public static float[] nextFloatArray(int length, final Random random) {
		return nextFloatArray(0, Float.MAX_VALUE, length, random);
	}

	public static float[] nextFloatArray(float startInclusive, float endExclusive, int length) {
		return nextFloatArray(startInclusive, endExclusive, length, RANDOM);
	}

	public static float[] nextFloatArray(float startInclusive, float endExclusive, int length, final Random random) {
		Validate.isTrue(endExclusive >= startInclusive, "startInclusive 必须小于或等于 endExclusive");
		Validate.isTrue(startInclusive >= 0, "startInclusive 和 endExclusive 必须都是非负数");
		Validate.isTrue(length > 0, "length 不能为负数");

		float[] numbers = new float[length];
		if (startInclusive == endExclusive) {
			Arrays.fill(numbers, startInclusive);
		} else {
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = startInclusive + ((endExclusive - startInclusive) * random.nextFloat());
			}
		}
		return numbers;
	}

	public static double[] nextDoubleArray(int length) {
		return nextDoubleArray(length, RANDOM);
	}

	public static double[] nextDoubleArray(int length, final Random random) {
		return nextDoubleArray(0, Double.MAX_VALUE, length, random);
	}

	public static double[] nextDoubleArray(double startInclusive, double endExclusive, int length) {
		return nextDoubleArray(startInclusive, endExclusive, length, RANDOM);
	}

	public static double[] nextDoubleArray(double startInclusive, double endExclusive, int length, final Random random) {
		Validate.isTrue(endExclusive >= startInclusive, "startInclusive 必须小于或等于 endExclusive");
		Validate.isTrue(startInclusive >= 0, "startInclusive 和 endExclusive 必须都是非负数");
		Validate.isTrue(length > 0, "length 不能为负数");

		double[] numbers = new double[length];
		if (startInclusive == endExclusive) {
			Arrays.fill(numbers, startInclusive);
		} else {
			for (int i = 0; i < numbers.length; i++) {
				numbers[i] = startInclusive + ((endExclusive - startInclusive) * random.nextDouble());
			}
		}
		return numbers;
	}

	public static boolean nextBoolean() {
		return nextBoolean(RANDOM);
	}

	public static boolean nextBoolean(final Random random) {
		return random.nextBoolean();
	}

	public static byte[] nextBytes(int length) {
		return nextBytes(length, RANDOM);
	}

	public static byte[] nextBytes(int length, final Random random) {
		Validate.isTrue(length >= 0, "length 不能为负数");

		final byte[] result = new byte[length];
		random.nextBytes(result);
		return result;
	}

	public static int nextInt() {
		return nextInt(RANDOM);
	}

	public static int nextInt(final Random random) {
		return nextInt(0, Integer.MAX_VALUE, random);
	}

	public static int nextInt(int startInclusive, int endExclusive) {
		return nextInt(startInclusive, endExclusive, RANDOM);
	}

	public static int nextInt(int startInclusive, int endExclusive, final Random random) {
		Validate.isTrue(endExclusive >= startInclusive, "startInclusive 必须小于或等于 endExclusive");
		Validate.isTrue(startInclusive >= 0, "startInclusive 和 endExclusive 必须都是非负数");

		if (startInclusive == endExclusive) {
			return startInclusive;
		}
		return startInclusive + random.nextInt(endExclusive - startInclusive);
	}

	public static long nextLong() {
		return nextLong(RANDOM);
	}

	public static long nextLong(final Random random) {
		return nextLong(Long.MAX_VALUE, random);
	}

	public static long nextLong(long startInclusive, long endExclusive) {
		return nextLong(startInclusive, endExclusive, RANDOM);
	}

	public static long nextLong(long startInclusive, long endExclusive, final Random random) {
		Validate.isTrue(endExclusive >= startInclusive, "startInclusive 必须小于或等于 endExclusive");
		Validate.isTrue(startInclusive >= 0, "startInclusive 和 endExclusive 必须都是非负数");

		if (startInclusive == endExclusive) {
			return startInclusive;
		}
		return startInclusive + nextLong(endExclusive - startInclusive, random);
	}

	private static long nextLong(long n, final Random random) {
		// Extracted from o.a.c.rng.core.BaseProvider.nextLong(long)
		long bits;
		long val;
		do {
			bits = random.nextLong() >>> 1;
			val = bits % n;
		} while (bits - val + (n - 1) < 0);

		return val;
	}

	public static double nextDouble() {
		return nextDouble(0, Double.MAX_VALUE);
	}

	public static double nextDouble(double startInclusive, double endExclusive) {
		return nextDouble(startInclusive, endExclusive, RANDOM);
	}

	public static double nextDouble(double startInclusive, double endExclusive, final Random random) {
		Validate.isTrue(endExclusive >= startInclusive, "startInclusive 必须小于或等于 endExclusive");
		Validate.isTrue(startInclusive >= 0, "startInclusive 和 endExclusive 必须都是非负数");

		if (startInclusive == endExclusive) {
			return startInclusive;
		}
		return startInclusive + ((endExclusive - startInclusive) * random.nextDouble());
	}

	public static float nextFloat() {
		return nextFloat(0, Float.MAX_VALUE);
	}

	public static float nextFloat(float startInclusive, float endExclusive) {
		return nextFloat(startInclusive, endExclusive, RANDOM);
	}

	public static float nextFloat(float startInclusive, float endExclusive, final Random random) {
		Validate.isTrue(endExclusive >= startInclusive, "startInclusive 必须小于或等于 endExclusive");
		Validate.isTrue(startInclusive >= 0, "startInclusive 和 endExclusive 必须都是非负数");

		if (startInclusive == endExclusive) {
			return startInclusive;
		}
		return startInclusive + ((endExclusive - startInclusive) * random.nextFloat());
	}
}
