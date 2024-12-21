package io.github.pangju666.commons.lang.random;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.Validate;

/**
 * 参考自 Apache Commons Lang3 RandomUtils
 *
 * @see RandomUtils
 */
public class RandomArray {
	protected static final RandomArray INSECURE = new RandomArray(RandomUtils.insecure());
	protected static final RandomArray SECURE = new RandomArray(RandomUtils.secure());
	protected static final RandomArray SECURE_STRONG = new RandomArray(RandomUtils.secureStrong());

	protected final RandomUtils randomUtils;

	protected RandomArray(RandomUtils randomUtils) {
		this.randomUtils = randomUtils;
	}

	public static RandomArray insecure() {
		return INSECURE;
	}

	public static RandomArray secure() {
		return SECURE;
	}

	public static RandomArray secureStrong() {
		return SECURE_STRONG;
	}

	public boolean[] randomBooleanArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");
		boolean[] booleans = new boolean[length];
		for (int i = 0; i < booleans.length; i++) {
			booleans[i] = randomUtils.randomBoolean();
		}
		return booleans;
	}

	public int[] randomIntArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		int[] numbers = new int[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = randomUtils.randomInt();
		}
		return numbers;
	}

	public int[] randomIntArray(int startInclusive, int endExclusive, int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		int[] numbers = new int[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = randomUtils.randomInt(startInclusive, endExclusive);
		}
		return numbers;
	}

	public long[] randomLongArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		long[] numbers = new long[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = randomUtils.randomLong();
		}
		return numbers;
	}

	public long[] randomLongArray(long startInclusive, long endExclusive, int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		long[] numbers = new long[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = randomUtils.randomLong(startInclusive, endExclusive);
		}
		return numbers;
	}

	public float[] randomFloatArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		float[] numbers = new float[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = randomUtils.randomFloat();
		}
		return numbers;
	}

	public float[] nextFloatArray(float startInclusive, float endExclusive, int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		float[] numbers = new float[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = randomUtils.randomFloat(startInclusive, endExclusive);
		}
		return numbers;
	}

	public double[] randomDoubleArray(int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		double[] numbers = new double[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = randomUtils.randomDouble();
		}
		return numbers;
	}

	public double[] randomDoubleArray(double startInclusive, double endExclusive, int length) {
		Validate.isTrue(length > 0, "length 不能为负数");

		double[] numbers = new double[length];
		for (int i = 0; i < numbers.length; i++) {
			numbers[i] = randomUtils.randomDouble(startInclusive, endExclusive);
		}
		return numbers;
	}
}
