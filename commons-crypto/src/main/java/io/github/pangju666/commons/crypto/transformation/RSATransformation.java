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
 * RSA加密转换策略接口，定义加密算法模式、填充方式及分块处理逻辑
 * <p>
 * 本接口为RSA加密算法提供标准化的转换方案定义，支持多种填充模式和分块策略，
 * 确保不同实现间的兼容性和一致性。
 * </p>
 *
 * <h3>核心职责</h3>
 * <ul>
 *   <li><b>算法定义</b> - 提供标准化的算法/模式/填充名称</li>
 *   <li><b>分块计算</b> - 根据密钥规格计算加密/解密分块尺寸</li>
 *   <li><b>扩展支持</b> - 允许自定义填充方案实现</li>
 * </ul>
 *
 * <h3>典型实现</h3>
 * <ol>
 *   <li>PKCS#1 v1.5填充方案</li>
 *   <li>OAEP填充方案（推荐）</li>
 * </ol>
 *
 * @author pangju666
 * @since 1.0.0
 * @see java.security.spec.RSAPublicKeySpec
 * @see java.security.spec.RSAPrivateKeySpec
 */
public interface RSATransformation {
	/**
	 * 获取完整的算法转换方案名称
	 * <p>
	 * 返回符合JCE标准的算法转换字符串，格式为"算法/模式/填充"。
	 * 该名称将直接用于Cipher.getInstance()方法初始化。
	 * </p>
	 *
	 * <h3>命名规范</h3>
	 * <ul>
	 *   <li><b>算法</b>：固定为"RSA"</li>
	 *   <li><b>模式</b>：通常为"ECB"（RSA无实际模式）</li>
	 *   <li><b>填充</b>：如"PKCS1Padding"、"OAEPWithSHA-256AndMGF1Padding"</li>
	 * </ul>
	 *
	 * @return 标准算法转换名称，格式示例：
	 *         <ul>
	 *           <li>"RSA/ECB/PKCS1Padding"</li>
	 *           <li>"RSA/ECB/OAEPWithSHA-256AndMGF1Padding"</li>
	 *         </ul>
	 * @since 1.0.0
	 */
	String getName();

	/**
	 * 计算公钥加密分块尺寸
	 * <p>
	 * 根据RSA公钥模数长度和填充方案计算单次加密操作的最大数据量。
	 * </p>
	 *
	 * <h3>计算规则</h3>
	 * <ul>
	 *   <li>PKCS#1 v1.5：模数字节数 - 11</li>
	 *   <li>OAEP：模数字节数 - 2*hLen - 2（hLen为哈希长度）</li>
	 *   <li>无填充：模数字节数</li>
	 * </ul>
	 *
	 * @param keySpec RSA公钥规格，包含模数和公钥指数
	 * @return 单次加密允许的最大字节数
	 * @throws NullPointerException 当keySpec为null时抛出
	 * @since 1.0.0
	 */
	int getEncryptBlockSize(RSAPublicKeySpec keySpec);

	/**
	 * 计算私钥解密分块尺寸（默认实现）
	 * <p>
	 * 默认返回模数对应的字节长度，适用于大多数填充方案。
	 * </p>
	 *
	 * <h3>实现说明</h3>
	 * <ul>
	 *   <li>解密块大小通常等于模数字节长度</li>
	 *   <li>特殊填充方案可覆盖此方法</li>
	 *   <li>自动验证参数有效性</li>
	 * </ul>
	 *
	 * @param keySpec RSA私钥规格，包含模数和私钥指数
	 * @return 单次解密处理的数据块字节数
	 * @throws NullPointerException 当keySpec为null时抛出
	 * @since 1.0.0
	 */
	default int getDecryptBlockSize(RSAPrivateKeySpec keySpec) {
		Validate.notNull(keySpec, "keySpec 不能为 null");
		return keySpec.getModulus().bitLength() / 8;
	}
}
