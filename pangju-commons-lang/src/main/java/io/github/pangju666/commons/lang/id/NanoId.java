package io.github.pangju666.commons.lang.id;

import org.apache.commons.lang3.Validate;

import java.security.SecureRandom;
import java.util.Random;

/**
 * NanoId，一个小型、安全、对 URL友好的唯一字符串 ID 生成器，特点：
 *
 * <ul>
 *     <li>安全：它使用加密、强大的随机 API，并保证符号的正确分配</li>
 *     <li>体积小：只有 258 bytes 大小（压缩后）、无依赖</li>
 *     <li>紧凑：它使用比 UUID (A-Za-z0-9_~)更多的符号</li>
 * </ul>
 *
 * <p>
 * 此实现的逻辑基于JavaScript的NanoId实现，见：<a href="https://github.com/ai/nanoid">nanoid</a>
 *
 * <p>
 *     代码来源于：<a href="https://github.com/chinabugotech/hutool/blob/5.8.36/hutool-core/src/main/java/cn/hutool/core/lang/id/NanoId.java">cn.hutool.core.lang.id.NanoId 5.8.36</a>
 * </p>
 *
 * @since 1.0.0
 */
public final class NanoId {
	/**
	 * 默认随机数生成器，使用{@link SecureRandom}确保健壮性
	 */
	private static final SecureRandom DEFAULT_NUMBER_GENERATOR = new SecureRandom();

	/**
	 * 默认随机字符表，使用URL安全的Base64字符
	 */
	private static final char[] DEFAULT_ALPHABET =
		"_-0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

	/**
	 * 默认长度
	 */
	public static final int DEFAULT_SIZE = 21;

	/**
	 * 生成伪随机的NanoId字符串，长度为默认的{@link #DEFAULT_SIZE}，使用密码安全的伪随机生成器
	 *
	 * @return 伪随机的NanoId字符串
	 */
	public static String randomNanoId() {
		return randomNanoId(DEFAULT_SIZE);
	}

	/**
	 * 生成伪随机的NanoId字符串
	 *
	 * @param size ID长度
	 * @return 伪随机的NanoId字符串
	 */
	public static String randomNanoId(final int size) {
		return randomNanoId(DEFAULT_NUMBER_GENERATOR, DEFAULT_ALPHABET, size);
	}

	/**
	 * 生成伪随机的NanoId字符串
	 *
	 * @param random   随机数生成器
	 * @param alphabet 随机字符表
	 * @param size     ID长度
	 * @return 伪随机的NanoId字符串
	 */
	public static String randomNanoId(final Random random, final char[] alphabet, final int size) {
		Validate.notNull(random, "random 不可为空");
		Validate.isTrue(size > 0, "size 必须大于0");
		Validate.isTrue(alphabet.length >= 1 && alphabet.length <= 255, "alphabet必须包含 1 至 255 个符号。");

		final int mask = (2 << (int) Math.floor(Math.log(alphabet.length - 1) / Math.log(2))) - 1;
		final int step = (int) Math.ceil(1.6 * mask * size / alphabet.length);

		final StringBuilder idBuilder = new StringBuilder();

		while (true) {
			final byte[] bytes = new byte[step];
			random.nextBytes(bytes);
			for (int i = 0; i < step; i++) {
				final int alphabetIndex = bytes[i] & mask;
				if (alphabetIndex < alphabet.length) {
					idBuilder.append(alphabet[alphabetIndex]);
					if (idBuilder.length() == size) {
						return idBuilder.toString();
					}
				}
			}
		}
	}
}
