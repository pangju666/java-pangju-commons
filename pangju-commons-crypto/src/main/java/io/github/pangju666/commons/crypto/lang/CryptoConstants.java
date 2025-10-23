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

package io.github.pangju666.commons.crypto.lang;

import org.apache.commons.collections4.SetUtils;

import java.util.Set;

/**
 * 加密算法相关常量
 * <p>
 * 本类提供加密模块中使用的标准算法名称、默认配置参数以及合规性检查相关的常量定义。
 * 包含 RSA、Diffie-Hellman、DSA 等算法的标准配置参数。
 *
 * @author pangju666
 * @since 1.0.0
 */
public class CryptoConstants {
	/**
	 * RSA 算法标准名称（对应 Java Cryptography Architecture 标准名称）
	 *
	 * @since 1.0.0
	 */
	public static final String RSA_ALGORITHM = "RSA";
	/**
	 * Diffie-Hellman 密钥交换算法标准名称
	 *
	 * @since 1.0.0
	 */
	public static final String DIFFIE_HELLMAN_ALGORITHM = "DiffieHellman";
	/**
	 * DSA（数字签名算法）标准名称
	 *
	 * @since 1.0.0
	 */
	public static final String DSA_ALGORITHM = "DSA";

	/**
	 * RSA 默认密钥长度（单位：bit），当前值为 2048
	 * <p>
	 * 根据行业安全标准推荐的密钥长度设置
	 *
	 * @since 1.0.0
	 */
	public static final int RSA_DEFAULT_KEY_SIZE = 2048;

	/**
	 * 允许的 RSA 密钥长度集合（单位：bit）
	 * <p>
	 * 包含 1024（不推荐）、2048（推荐）、4096（高安全）三种选项
	 *
	 * @since 1.0.0
	 */
	public static final Set<Integer> RSA_KEY_SIZE_SET = SetUtils.unmodifiableSet(1024, 2048, 4096);
	/**
	 * 允许的 Diffie-Hellman 密钥长度集合（单位：bit）
	 *
	 * @since 1.0.0
	 */
	public static final Set<Integer> DIFFIE_HELLMAN_KEY_SIZE_SET = SetUtils.unmodifiableSet(1024, 2048, 4096);
	/**
	 * 允许的 DSA 密钥长度集合（单位：bit）
	 *
	 * @since 1.0.0
	 */
	public static final Set<Integer> DSA_KEY_SIZE_SET = SetUtils.unmodifiableSet(1024, 2048);

	protected CryptoConstants() {
	}
}
