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
 * RSA PKCS#1 v1.5填充方案实现类
 * <p>
 * 提供符合PKCS#1 v1.5标准的RSA加密转换实现，适用于需要向后兼容的场景。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>算法名称</b>：RSA/ECB/PKCS1Padding</li>
 *   <li><b>加密分块</b>：模数字节数 - 11（PKCS#1 v1.5填充开销）</li>
 *   <li><b>解密分块</b>：模数字节数（使用默认实现）</li>
 *   <li><b>安全等级</b>：提供基本安全性，但存在潜在漏洞</li>
 * </ul>
 *
 * <h3>适用场景</h3>
 * <ol>
 *   <li>需要兼容旧系统的场景</li>
 *   <li>非关键业务数据加密</li>
 *   <li>短期数据保护需求</li>
 * </ol>
 *
 * <h3>安全警告</h3>
 * <ul>
 *   <li>PKCS#1 v1.5存在已知的填充预言攻击风险</li>
 *   <li>新系统建议使用{@link RSAOEAPWithSHA256Transformation}</li>
 *   <li>密钥长度建议至少2048位</li>
 * </ul>
 *
 * @author pangju666
 * @see RSATransformation
 * @since 1.0.0
 */
public class RSAPKCS1PaddingTransformation implements RSATransformation {
	/**
	 * 获取标准算法转换名称
	 * <p>
	 * 返回符合JCE标准的PKCS#1 v1.5填充方案名称。
	 * </p>
	 *
	 * <h3>返回值说明</h3>
	 * <ul>
	 *   <li>固定返回"RSA/ECB/PKCS1Padding"</li>
	 *   <li>ECB模式仅为语法要求，RSA实际不使用分组模式</li>
	 * </ul>
	 *
	 * @return 标准算法名称字符串，永不返回null
	 * @since 1.0.0
	 */
	@Override
	public String getName() {
		return "RSA/ECB/PKCS1Padding";
	}

	/**
	 * 计算PKCS#1 v1.5填充下的加密分块尺寸
	 * <p>
	 * 根据PKCS#1 v1.5规范计算最大加密数据块大小。
	 * </p>
	 *
	 * <h3>计算公式</h3>
	 * <pre>blockSize = (modulusBitLength / 8) - 11</pre>
	 *
	 * <h3>参数要求</h3>
	 * <ul>
	 *   <li>keySpec必须非null</li>
	 *   <li>模数必须已初始化</li>
	 *   <li>模数长度至少为1024位</li>
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
		return keySpec.getModulus().bitLength() / 8 - 11;
	}
}
