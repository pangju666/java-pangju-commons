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

import org.apache.commons.lang3.Validate;

import java.util.Random;
import java.util.UUID;

/**
 * <h1>UUID工具类</h1>
 * <p>
 * 高性能的 UUID 生成工具类，用于快速创建符合
 * <a href="https://datatracker.ietf.org/doc/html/rfc4122">RFC 4122</a>
 * 标准的随机版本（Version 4）UUID。
 * </p>
 *
 * <h2>算法说明</h2>
 * <p>
 * 本工具类生成的 UUID 基于随机数（Version 4），
 * 即通过 {@link java.util.Random} 生成 128 位随机比特位，
 * 并在特定位上设置标准的版本号与变体标志位。
 * </p>
 *
 * <ul>
 *   <li>版本号（version）：第 6 字节高 4 位，固定为 {@code 0100b}，即版本 4</li>
 *   <li>变体（variant）：第 8 字节高 2 位，固定为 {@code 10b}，表示 IETF 标准变体</li>
 * </ul>
 *
 * <h2>特点</h2>
 * <ul>
 *   <li>比 {@link java.util.UUID#randomUUID()} 更灵活，可指定自定义 {@link Random}</li>
 *   <li>线程安全（假设传入的 {@code Random} 为线程安全实现，如 {@code ThreadLocalRandom}）</li>
 *   <li>遵循 RFC 4122 标准</li>
 *   <li>无依赖、性能高、可嵌入任意项目</li>
 * </ul>
 *
 * <h2>示例</h2>
 * <pre>{@code
 * import java.util.UUID;
 * import java.util.concurrent.ThreadLocalRandom;
 *
 * public class Demo {
 *     public static void main(String[] args) {
 *         UUID uuid = UUIDUtils.fastUUID(ThreadLocalRandom.current());
 *         System.out.println(uuid);
 *     }
 * }
 * }</pre>
 * <p>
 * 输出示例：
 * <pre>
 * 550e8400-e29b-41d4-a716-446655440000
 * </pre>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class UUIDUtils {
	protected UUIDUtils() {
	}

	/**
	 * 使用指定的 {@link Random} 实例快速生成一个随机版本（Version 4）的 UUID。
	 * <p>
	 * 生成规则：
	 * <ul>
	 *   <li>生成 16 字节（128 位）的随机数作为基础</li>
	 *   <li>设置第 6 字节高 4 位为 {@code 0100b} 表示版本 4</li>
	 *   <li>设置第 8 字节高 2 位为 {@code 10b} 表示 IETF 变体</li>
	 * </ul>
	 *
	 * @param random 随机数生成器实例（建议使用 {@link java.util.concurrent.ThreadLocalRandom}）
	 * @return 符合 RFC 4122 标准的随机版本 UUID
	 * @see java.util.UUID
	 * @since 1.0.0
	 */
	public static UUID fastUUID(final Random random) {
		Validate.notNull(random, "random 不可为空");

		// 生成16字节的随机数据作为UUID基础
		byte[] randomBytes = new byte[16];
		random.nextBytes(randomBytes);

		/* 版本字段设置（第6字节高四位）：
		 * 1. 清除第6字节的高4位（0x0F掩码）
		 * 2. 设置版本号为4（0x40掩码） */
		randomBytes[6] &= 0x0f;  /* clear version        */
		randomBytes[6] |= 0x40;  /* set to version 4     */

		/* 变体字段设置（第8字节高两位）：
		 * 1. 清除第8字节的高2位（0x3F掩码）
		 * 2. 设置标准IETF变体（最高位为1，次高位为0） */
		randomBytes[8] &= 0x3f;  /* clear variant        */
		randomBytes[8] |= (byte) 0x80;  /* set to IETF variant  */

		// 将前8字节转换为高位long（MSB）
		long msb = 0;
		for (int i = 0; i < 8; i++)
			msb = (msb << 8) | (randomBytes[i] & 0xff);

		// 将后8字节转换为低位long（LSB）
		long lsb = 0;
		for (int i = 8; i < 16; i++)
			lsb = (lsb << 8) | (randomBytes[i] & 0xff);

		return new UUID(msb, lsb);
	}
}
