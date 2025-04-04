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
 * RSA OAEP-SHA256填充方案实现类
 * <p>
 * 提供符合PKCS#1 v2.2标准的RSA加密转换实现，使用SHA-256作为哈希算法和MGF1作为掩码生成函数。
 * 这是当前推荐的RSA加密填充方案，具有更高的安全性。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>算法名称</b>：RSA/ECB/OAEPWithSHA-256AndMGF1Padding</li>
 *   <li><b>加密分块</b>：模数字节数 - 66（OAEP填充开销）</li>
 *   <li><b>解密分块</b>：模数字节数（使用默认实现）</li>
 *   <li><b>安全等级</b>：提供强安全性，抵抗选择密文攻击</li>
 * </ul>
 *
 * <h3>技术规范</h3>
 * <ul>
 *   <li>符合PKCS#1 v2.2 (RFC 8017)标准</li>
 *   <li>使用SHA-256作为哈希算法</li>
 *   <li>使用MGF1作为掩码生成函数</li>
 *   <li>默认使用空标签参数</li>
 * </ul>
 *
 * <h3>安全建议</h3>
 * <ul>
 *   <li>推荐用于新系统和安全敏感场景</li>
 *   <li>密钥长度建议至少2048位</li>
 *   <li>相比PKCS#1 v1.5更安全但计算开销更大</li>
 * </ul>
 *
 * @author pangju666
 * @see RSATransformation
 * @since 1.0.0
 */
public class RSAOEAPWithSHA256Transformation implements RSATransformation {
	/**
	 * 获取标准算法转换名称
	 * <p>
	 * 返回符合JCE标准的OAEP填充方案名称，包含完整的哈希和MGF参数。
	 * </p>
	 *
	 * <h3>返回值说明</h3>
	 * <ul>
	 *   <li>固定返回"RSA/ECB/OAEPWithSHA-256AndMGF1Padding"</li>
	 *   <li>ECB模式仅为语法要求，RSA实际不使用分组模式</li>
	 *   <li>名称明确指定了SHA-256哈希和MGF1函数</li>
	 * </ul>
	 *
	 * @return 标准算法名称字符串，永不返回null
	 * @since 1.0.0
	 */
	@Override
	public String getName() {
		return "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	}

	/**
	 * 计算OAEP填充下的加密分块尺寸
	 * <p>
	 * 根据OAEPWithSHA-256AndMGF1Padding规范计算最大加密数据块大小。
	 * </p>
	 *
	 * <h3>计算公式</h3>
	 * <pre>blockSize = (modulusBitLength / 8) - 2 * hashLength - 2</pre>
	 * <p>对于SHA-256，hashLength=32，因此总填充开销为66字节</p>
	 *
	 * <h3>参数要求</h3>
	 * <ul>
	 *   <li>keySpec必须非null</li>
	 *   <li>模数必须已初始化</li>
	 *   <li>模数长度至少为2048位</li>
	 * </ul>
	 *
	 * @param keySpec 包含模数和公钥指数的RSA公钥规格
	 * @return 单次加密允许的最大数据字节数
	 * @throws NullPointerException 当keySpec为null时抛出
	 * @throws IllegalArgumentException 当模数长度不足时抛出
	 * @since 1.0.0
	 */
	@Override
	public int getEncryptBlockSize(RSAPublicKeySpec keySpec) {
		Validate.notNull(keySpec, "keySpec 不能为 null");
		return keySpec.getModulus().bitLength() / 8 - 66;
	}
}
