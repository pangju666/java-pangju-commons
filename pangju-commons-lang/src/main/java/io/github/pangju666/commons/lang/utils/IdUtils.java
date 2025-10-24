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
import org.bson.types.ObjectId;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ID生成工具类
 * <p>提供多种分布式ID生成方案，包括：UUID、MongoDB ObjectId、NanoId</p>
 * <p>创意来自ruoyi</p>
 *
 * @author pangju666
 * @see java.util.UUID
 * @see org.bson.types.ObjectId
 * @see io.github.pangju666.commons.lang.id.NanoId
 * @see io.github.pangju666.commons.lang.utils.UUIDUtils
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
	 * 生成高性能标准格式UUID
	 *
	 * @param random 用于生成密码学安全随机数的Random实例，提供UUID生成所需的熵源
	 * @return 由随机数生成的版本4 UUID对象，符合IETF变体规范
	 * @since 1.0.0
	 */
	public static String fastUUID(final Random random) {
		return UUIDUtils.fastUUID(random).toString();
	}

	/**
	 * 生成高性能简写格式UUID
	 *
	 * @param random 用于生成密码学安全随机数的Random实例，提供UUID生成所需的熵源
	 * @return 由随机数生成的版本4 UUID对象，符合IETF变体规范
	 * @since 1.0.0
	 */
	public static String simpleFastUUID(final Random random) {
		return UUIDUtils.fastUUID(random).toString().replace("-", "");
	}

	/**
	 * 生成高性能标准格式UUID
	 *
	 * @return 带连字符的UUID字符串（36位）
	 * @since 1.0.0
	 */
	public static String fastUUID() {
		return UUIDUtils.fastUUID(ThreadLocalRandom.current()).toString();
	}

	/**
	 * 生成高性能简写格式UUID
	 *
	 * @return 不带连字符的UUID字符串（32位）
	 * @since 1.0.0
	 */
	public static String simpleFastUUID() {
		return UUIDUtils.fastUUID(ThreadLocalRandom.current()).toString();
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
}
