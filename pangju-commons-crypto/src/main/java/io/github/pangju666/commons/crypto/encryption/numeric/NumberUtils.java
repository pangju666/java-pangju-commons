/*
 * =============================================================================
 *
 *   Copyright (c) 2007-2010, The JASYPT team (http://www.jasypt.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */

package io.github.pangju666.commons.crypto.encryption.numeric;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

/**
 * 加密模块中需要用到的数字工具类(内部使用)
 * <p>来源于：org.jasypt.encryption.pbe.NumberUtils</p>
 *
 * @since 1.0.0
 */
final class NumberUtils {
	private NumberUtils() {
	}

	static byte[] byteArrayFromInt(final int number) {
		final byte b0 = (byte) (0xff & number);
		final byte b1 = (byte) (0xff & (number >> 8));
		final byte b2 = (byte) (0xff & (number >> 16));
		final byte b3 = (byte) (0xff & (number >> 24));
		return new byte[]{b3, b2, b1, b0};
	}

	private static int intFromByteArray(final byte[] byteArray) {
		if (byteArray == null || byteArray.length == 0) {
			throw new IllegalArgumentException("无法将空数组转换为int");
		}
		int result = (0xff & byteArray[0]);
		for (int i = 1; i < byteArray.length; i++) {
			result = (result << 8) | (0xff & byteArray[i]);
		}
		return result;
	}

	static byte[] processBigIntegerEncryptedByteArray(final byte[] byteArray, final int signum) {
		if (byteArray.length > 4) {
			final int initialSize = byteArray.length;

			final byte[] encryptedMessageExpectedSizeBytes = new byte[4];
			System.arraycopy(byteArray, (initialSize - 4), encryptedMessageExpectedSizeBytes, 0, 4);

			final byte[] processedByteArray = new byte[initialSize - 4];
			System.arraycopy(byteArray, 0, processedByteArray, 0, (initialSize - 4));

			final int expectedSize = intFromByteArray(encryptedMessageExpectedSizeBytes);
			if (expectedSize < 0 || expectedSize > maxSafeSizeInBytes()) {
				throw new EncryptionOperationNotPossibleException();
			}

			if (processedByteArray.length != expectedSize) {
				final int sizeDifference = (expectedSize - processedByteArray.length);
				final byte[] paddedProcessedByteArray = new byte[expectedSize];
				for (int i = 0; i < sizeDifference; i++) {
					paddedProcessedByteArray[i] = (signum >= 0) ? (byte) 0x0 : (byte) -0x1;
				}
				System.arraycopy(processedByteArray, 0, paddedProcessedByteArray,
					sizeDifference, processedByteArray.length);
				return paddedProcessedByteArray;
			}
			return processedByteArray;
		}
		return byteArray.clone();
	}

	private static long maxSafeSizeInBytes() {
		final long max = Runtime.getRuntime().maxMemory();
		final long free = Runtime.getRuntime().freeMemory();
		final long total = Runtime.getRuntime().totalMemory();
		return ((free + (max - total)) / 2);
	}
}