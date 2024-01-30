package io.github.pangju666.commons.lang.utils;

import java.util.function.Supplier;

public class ExceptionUtils extends org.apache.commons.lang3.exception.ExceptionUtils {
	protected ExceptionUtils() {
	}

	public static <T extends Throwable> void throwIfFalse(boolean expression, final Supplier<T> supplier) throws T {
		if (!expression) {
			throw supplier.get();
		}
	}

	public static <T extends Throwable> void throwIfTrue(boolean expression, final Supplier<T> supplier) throws T {
		if (expression) {
			throw supplier.get();
		}
	}
}
