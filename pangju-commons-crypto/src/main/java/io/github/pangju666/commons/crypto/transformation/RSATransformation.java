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

package io.github.pangju666.commons.crypto.transformation;

import org.apache.commons.lang3.Validate;

import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

/**
 * RSA 加密方案策略接口，定义加密算法模式/填充方式的名称、如何计算加密/解密时的分块大小
 * <p>
 * 实现类应提供具体的算法转换方案（如 RSA/ECB/PKCS1Padding），
 * 并计算对应方案下加密/解密时的分块处理尺寸。
 *
 * @author pangju666
 * @see java.security.spec.RSAPublicKeySpec
 * @see java.security.spec.RSAPrivateKeySpec
 * @since 1.0.0
 */
public interface RSATransformation {
	/**
	 * 获取完整的算法转换方案名称
	 * <p>
	 * 名称应符合 JCE 标准命名规范，格式为：算法/模式/填充（如："RSA/ECB/OAEPWithSHA-256AndMGF1Padding"）
	 *
	 * @return 算法转换方案的标准名称
	 * @since 1.0.0
	 */
	String getName();

	/**
	 * 计算指定公钥规格下的加密分块尺寸
	 * <p>
	 * 根据 RSA 公钥模数长度和填充方式计算加密时单次处理的最大字节数
	 *
	 * @param keySpec RSA 公钥规格参数
	 * @return 加密操作时每个数据块的最大允许字节数
	 * @since 1.0.0
	 */
	int getEncryptBlockSize(RSAPublicKeySpec keySpec);

	/**
	 * 计算指定私钥规格下的解密分块尺寸（默认实现）
	 * <p>
	 * 默认实现返回模数位长度/8（对应字节数），适用于大多数填充方案。
	 * 特殊填充方式可覆盖此方法实现。
	 *
	 * @param keySpec RSA 私钥规格参数
	 * @return 解密操作时每个数据块的最大允许字节数
	 * @since 1.0.0
	 */
	default int getDecryptBlockSize(RSAPrivateKeySpec keySpec) {
		Validate.notNull(keySpec, "keySpec 不能为 null");
		return keySpec.getModulus().bitLength() / 8;
	}
}
