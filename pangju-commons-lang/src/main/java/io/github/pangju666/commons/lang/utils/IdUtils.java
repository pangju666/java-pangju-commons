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

import io.github.pangju666.commons.lang.id.NanoId;
import io.github.pangju666.commons.lang.id.SnowflakeIdWorker;
import org.bson.types.ObjectId;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID生成工具类
 * <p>提供多种分布式ID生成方案，包括：UUID、MongoDB ObjectId、NanoId、雪花算法ID等</p>
 * <p>创意来自ruoyi</p>
 *
 * @author pangju666
 * @see java.util.UUID
 * @see org.bson.types.ObjectId
 * @see io.github.pangju666.commons.lang.id.NanoId
 * @see io.github.pangju666.commons.lang.id.SnowflakeIdWorker
 * @since 1.0.0
 */
public class IdUtils {
	protected IdUtils() {
	}

	/**
	 * 生成标准格式随机UUID
	 *
	 * @return 带连字符的UUID字符串（36位）
	 * @since 1.0.0
	 */
	public static String randomUUID() {
		return UUID.randomUUID().toString();
	}

	/**
	 * 生成简写格式随机UUID
	 *
	 * @return 不带连字符的UUID字符串（32位）
	 * @since 1.0.0
	 */
	public static String simpleRandomUUID() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 生成符合RFC 4122标准的版本4 UUID
	 *
	 * @param random 用于生成密码学安全随机数的Random实例，提供UUID生成所需的熵源
	 * @return 由随机数生成的版本4 UUID对象，符合IETF变体规范
	 * @since 1.0.0
	 */
	public static UUID fastUUID(final Random random) {
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

	/**
	 * 生成高性能标准格式UUID
	 *
	 * @return 带连字符的UUID字符串（36位）
	 * @since 1.0.0
	 */
	public static String fastUUID() {
		return fastUUID(ThreadLocalRandom.current()).toString();
	}

	/**
	 * 生成高性能简写格式UUID
	 *
	 * @return 不带连字符的UUID字符串（32位）
	 * @since 1.0.0
	 */
	public static String simpleFastUUID() {
		return fastUUID(ThreadLocalRandom.current()).toString().replace("-", "");
	}

	/**
	 * 转换UUID对象为标准字符串
	 *
	 * @param uuid UUID对象
	 * @return 带连字符的标准格式字符串
	 * @since 1.0.0
	 */
	public static String uuid(final UUID uuid) {
		return uuid.toString();
	}

	/**
	 * 转换UUID对象为简写字符串
	 *
	 * @param uuid UUID对象
	 * @return 无连字符的紧凑格式字符串
	 * @since 1.0.0
	 */
	public static String simpleUUID(final UUID uuid) {
		return uuid.toString().replace("-", "");
	}

	/**
	 * 生成MongoDB风格ObjectId
	 *
	 * @return 24位十六进制字符串
	 * @since 1.0.0
	 */
	public static String objectId() {
		return ObjectId.get().toHexString();
	}

	/**
	 * 生成默认长度NanoId
	 *
	 * @return 21字符的URL安全随机字符串
	 * @since 1.0.0
	 */
	public static String nanoId() {
		return NanoId.randomNanoId();
	}

	/**
	 * 生成指定长度NanoId
	 *
	 * @param size ID长度（建议21-128之间）
	 * @return URL安全随机字符串
	 * @since 1.0.0
	 */
	public static String nanoId(final int size) {
		return NanoId.randomNanoId(size);
	}

	/**
	 * 通过雪花算法生成分布式ID
	 *
	 * @param worker 预配置的雪花算法工作器
	 * @return 64位长整型ID
	 * @throws NullPointerException 当worker为null时抛出
	 * @since 1.0.0
	 */
	public static Long snowflakeId(final SnowflakeIdWorker worker) {
		return worker.nextId();
	}
}
