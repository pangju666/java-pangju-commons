package io.github.pangju666.commons.lang.utils;

import io.github.pangju666.commons.lang.pool.ConstantPool;

public class RandomStringUtils extends org.apache.commons.lang3.RandomStringUtils {
	protected RandomStringUtils() {
	}

	public static String randomUppercaseAlphabetic(int length) {
		return random(length, ConstantPool.START_UPPERCASE_ALPHABETIC_CHARACTER, ConstantPool.END_UPPERCASE_ALPHABETIC_CHARACTER, false, false);
	}

	public static String randomLowerCaseAlphabetic(int length) {
		return random(length, ConstantPool.START_LOWERCASE_ALPHABETIC_CHARACTER, ConstantPool.END_LOWERCASE_ALPHABETIC_CHARACTER, false, false);
	}

	public static String randomChinese(int length) {
		return random(length, ConstantPool.START_CHINESE_CHARACTER, ConstantPool.END_CHINESE_CHARACTER, false, false);
	}
}
