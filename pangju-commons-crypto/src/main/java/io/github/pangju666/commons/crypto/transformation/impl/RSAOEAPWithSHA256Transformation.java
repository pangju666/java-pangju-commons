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

package io.github.pangju666.commons.crypto.transformation.impl;

import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import org.apache.commons.lang3.Validate;

import java.security.spec.RSAPublicKeySpec;

/**
 * RSA 加密转换方案实现类，使用 OAEPWithSHA-256AndMGF1Padding 填充模式
 * <p>
 * 本实现类对应算法名称为 "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"，采用以下特性：
 * <ul>
 *   <li>加密分块大小计算公式：模数长度/8 - 66（单位：字节）</li>
 *   <li>解密分块大小使用默认实现：模数长度/8</li>
 *   <li>支持 PKCS#1 v2.2 规范中定义的 OAEP 填充方案</li>
 * </ul>
 *
 * @author pangju666
 * @see RSATransformation
 * @since 1.0.0
 */
public class RSAOEAPWithSHA256Transformation implements RSATransformation {
	/**
	 * 获取完整的算法转换方案名称
	 *
	 * @return 固定返回 "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
	 * @since 1.0.0
	 */
	@Override
	public String getName() {
		return "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	}

	/**
	 * 计算 OAEP 填充模式下的加密分块尺寸
	 * <p>
	 * 计算公式：模数位长度/8 - 66（对应 66 字节的填充开销）
	 *
	 * @param keySpec RSA 公钥规格参数（必须包含有效模数）
	 * @return 加密操作时每个数据块的最大允许字节数
	 * @throws NullPointerException 如果 keySpec 为 null
	 * @since 1.0.0
	 */
	@Override
	public int getEncryptBlockSize(RSAPublicKeySpec keySpec) {
		Validate.notNull(keySpec, "keySpec 不能为 null");
		return keySpec.getModulus().bitLength() / 8 - 66;
	}
}
