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

package io.github.pangju666.commons.crypto.encryption.numeric;

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import org.apache.commons.lang3.Validate;
import org.jasypt.commons.CommonUtils;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.numeric.IntegerNumberEncryptor;

import java.math.BigInteger;
import java.util.Objects;

/**
 * 基于RSA算法的大整数加密解密器，提供精确的数值加密能力
 * <p>
 * 本类实现了{@link IntegerNumberEncryptor}接口，专门用于处理BigInteger类型的加密和解密，
 * 保留原始数值的数学特性，适用于需要精确数值运算的安全场景。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>精确加密</b> - 保持BigInteger的数值精度和符号位</li>
 *   <li><b>安全算法</b> - 采用RSA非对称加密算法，支持2048/3072/4096位密钥</li>
 *   <li><b>编码规范</b> - 使用二进制补码格式处理数值，确保跨平台一致性</li>
 *   <li><b>长度标识</b> - 自动添加4字节长度头，支持变长数据解密</li>
 * </ul>
 *
 * <h3>典型应用场景</h3>
 * <ol>
 *   <li>金融交易金额加密</li>
 *   <li>密码学协议中的大数运算</li>
 *   <li>数据库ID字段加密</li>
 *   <li>需要精确恢复的数值加密</li>
 * </ol>
 *
 * <h3>技术实现细节</h3>
 * <ul>
 *   <li>加密流程：BigInteger → 补码字节数组 → RSA加密 → 添加长度头 → 新BigInteger</li>
 *   <li>解密流程：加密BigInteger → 提取数据 → RSA解密 → 重构原始BigInteger</li>
 *   <li>最大加密长度：密钥长度/8 - 填充长度 - 4字节头</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see IntegerNumberEncryptor
 * @see RSABinaryEncryptor
 * @see RSATransformation
 * @see RSAKey
 */
public final class RSAIntegerNumberEncryptor implements IntegerNumberEncryptor {
	/**
	 * 核心RSA二进制加密处理器，执行底层加密运算
	 * <p>
	 * 此实例通过构造函数注入，具有以下设计特性：
	 * </p>
	 * <ul>
	 *   <li><b>不可变性</b> - final修饰确保线程安全</li>
	 *   <li><b>委托模式</b> - 所有加密操作最终委托给此实例</li>
	 *   <li><b>配置代理</b> - 配置变更通过此实例的对应方法实现</li>
	 *   <li><b>生命周期</b> - 与宿主对象绑定，不可单独存在</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	private final RSABinaryEncryptor binaryEncryptor;

	/**
	 * 构建使用默认安全配置的加密器
	 * <p>
	 * 默认采用行业推荐的安全配置：
	 * </p>
	 * <ul>
	 *   <li><b>密钥强度</b>：2048位RSA密钥（平衡安全与性能）</li>
	 *   <li><b>加密方案</b>：RSA/ECB/OAEPWithSHA-256AndMGF1Padding</li>
	 *   <li><b>填充方式</b>：最优非对称加密填充(OAEP)</li>
	 * </ul>
	 *
	 * @throws EncryptionInitializationException 当密钥生成失败时抛出
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	/**
	 * 构造方法（使用默认密钥长度和指定加密方案）
	 *
	 * @param transformation 加密方案，支持：
	 *                       <ul>
	 *                         <li>PKCS1v1.5 - 兼容性好但安全性较低</li>
	 *                         <li>OAEP - 安全性高（推荐）</li>
	 *                       </ul>
	 * @throws NullPointerException 当传入null参数时抛出
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(transformation);
	}

	/**
	 * 构造方法（使用指定密钥长度和默认加密方案）
	 * <p>
	 * 允许自定义密钥长度但使用默认的OAEP加密方案。
	 * </p>
	 *
	 * <h3>密钥长度建议</h3>
	 * <ul>
	 *   <li><b>生产环境</b>：2048位（平衡安全与性能）</li>
	 *   <li><b>高安全需求</b>：3072或4096位</li>
	 *   <li><b>测试用途</b>：1024位（不推荐生产环境）</li>
	 * </ul>
	 *
	 * @param keySize RSA密钥长度（单位：bit），有效值：
	 *                <ul>
	 *                  <li>1024（仅测试）</li>
	 *                  <li>2048（推荐）</li>
	 *                  <li>3072/4096（高安全）</li>
	 *                </ul>
	 * @throws IllegalArgumentException 当密钥长度不在上述范围内时抛出
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final int keySize) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize);
	}

	/**
	 * 构造方法（使用指定密钥长度和加密方案）
	 * <p>
	 * 完全自定义配置构造方法，需确保密钥长度与加密方案兼容。
	 * </p>
	 *
	 * <h3>兼容性要求</h3>
	 * <ul>
	 *   <li>OAEP方案要求最小2048位密钥</li>
	 *   <li>PKCS#1方案兼容1024位密钥</li>
	 *   <li>3072/4096位密钥推荐使用OAEP方案</li>
	 * </ul>
	 *
	 * @param keySize 密钥长度（单位：bit）
	 * @param transformation 加密方案
	 * @throws IllegalArgumentException 当密钥长度与方案不兼容时抛出
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final int keySize, final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize, transformation);
	}

	/**
	 * 构造方法（使用已有密钥和默认加密方案）
	 * <p>
	 * 适用于密钥预生成场景，使用默认OAEP加密方案。
	 * </p>
	 *
	 * <h3>密钥要求</h3>
	 * <ul>
	 *   <li>必须包含有效的公钥（加密）或私钥（解密）</li>
	 *   <li>密钥强度应与安全需求匹配</li>
	 *   <li>支持硬件安全模块(HSM)生成的密钥</li>
	 * </ul>
	 *
	 * @param key 预生成的RSA密钥对
	 * @throws NullPointerException 当密钥为null时抛出
	 * @see RSAKey
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final RSAKey key) {
		this.binaryEncryptor = new RSABinaryEncryptor(key);
	}

	/**
	 * 构造方法（使用已有密钥和指定加密方案）
	 * <p>
	 * 完全自定义配置构造方法，需确保密钥与加密方案兼容。
	 * </p>
	 *
	 * <h3>验证规则</h3>
	 * <ul>
	 *   <li>检查密钥长度是否支持所选方案</li>
	 *   <li>验证密钥是否包含必要参数</li>
	 *   <li>确保密钥未过期或被撤销</li>
	 * </ul>
	 *
	 * @param key 预生成的RSA密钥对
	 * @param transformation 加密方案
	 * @throws IllegalArgumentException 当密钥与方案不兼容时抛出
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final RSAKey key, final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(key, transformation);
	}

	/**
	 * 使用预配置的加密器构建实例
	 * <p>
	 * 高级构造方法，适用于需要精细控制加密器生命周期的场景。
	 * </p>
	 *
	 * <h3>典型使用场景</h3>
	 * <ul>
	 *   <li><b>加密器池</b>：复用预配置的加密器实例</li>
	 *   <li><b>热更新</b>：实现加密方案的无缝切换</li>
	 *   <li><b>性能优化</b>：预初始化加密器减少首次加密延迟</li>
	 * </ul>
	 *
	 * <h3>技术要求</h3>
	 * <ul>
	 *   <li>加密器必须已完成密钥配置</li>
	 *   <li>加密方案必须与密钥兼容</li>
	 *   <li>加密器应处于未初始化或已初始化状态</li>
	 * </ul>
	 *
	 * @param binaryEncryptor 预配置的RSABinaryEncryptor实例，要求：
	 *                        <ul>
	 *                          <li>非null</li>
	 *                          <li>已完成必要配置</li>
	 *                          <li>未被其他线程独占使用</li>
	 *                        </ul>
	 * @throws NullPointerException 当参数为null时抛出
	 * @throws IllegalStateException 当加密器配置不完整时抛出
	 * @see RSABinaryEncryptor
	 * @since 1.0.0
	 */
	public RSAIntegerNumberEncryptor(final RSABinaryEncryptor binaryEncryptor) {
		Validate.notNull(binaryEncryptor, "binaryEncryptor 不能为 null");
		this.binaryEncryptor = binaryEncryptor;
	}

	public RSAKey getKey() {
		return binaryEncryptor.getKey();
	}

	/**
	 * 配置RSA加密方案
	 * <p>
	 * 修改加密/填充方案，影响后续所有加密操作。
	 * </p>
	 *
	 * <h3>方案选择指南</h3>
	 * <table border="1">
	 *   <tr><th>方案</th><th>安全性</th><th>兼容性</th><th>推荐场景</th></tr>
	 *   <tr><td>PKCS1v1.5</td><td>中</td><td>高</td><td>传统系统兼容</td></tr>
	 *   <tr><td>OAEP</td><td>高</td><td>中</td><td>新系统(推荐)</td></tr>
	 * </table>
	 *
	 * @param transformation 加密方案枚举，必须：
	 *                       <ul>
	 *                         <li>非null</li>
	 *                         <li>与当前密钥长度兼容</li>
	 *                       </ul>
	 * @throws AlreadyInitializedException 如果已执行过加密操作
	 * @throws IllegalArgumentException 当方案与密钥不兼容时抛出
	 * @see RSATransformation
	 * @since 1.0.0
	 */
	public void setTransformation(final RSATransformation transformation) {
		this.binaryEncryptor.setTransformation(transformation);
	}

	/**
	 * 更新RSA密钥对
	 * <p>
	 * 动态更换加密/解密使用的密钥对。
	 * </p>
	 *
	 * <h3>密钥更换流程</h3>
	 * <ol>
	 *   <li>验证新密钥有效性</li>
	 *   <li>检查与当前加密方案的兼容性</li>
	 *   <li>原子性更新密钥引用</li>
	 *   <li>重置内部状态</li>
	 * </ol>
	 *
	 * @param key 新的RSA密钥对，必须：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>包含有效的公钥或私钥</li>
	 *              <li>与当前加密方案兼容</li>
	 *            </ul>
	 * @throws AlreadyInitializedException 如果已初始化后调用
	 * @throws EncryptionInitializationException 当密钥无效时抛出
	 * @see RSAKey
	 * @since 1.0.0
	 */
	public void setKey(final RSAKey key) {
		this.binaryEncryptor.setKey(key);
	}

	/**
	 * 初始化加密组件
	 * <p>根据当前配置初始化加密/解密处理器，此方法会自动检测可用密钥：</p>
	 * <ul>
	 *   <li>存在公钥：初始化加密功能</li>
	 *   <li>存在私钥：初始化解密功能</li>
	 * </ul>
	 * <p><strong>注意：</strong>此方法会自动被{@link #encrypt(BigInteger)}和{@link #decrypt(BigInteger)}调用，通常不需要手动调用。</p>
	 *
	 * @throws EncryptionInitializationException 当以下情况发生时抛出：
	 *                                           <ul>
	 *                                               <li>未配置任何密钥</li>
	 *                                               <li>算法不支持</li>
	 *                                               <li>密钥无效</li>
	 *                                           </ul>
	 * @see #encrypt(BigInteger)
	 * @see #decrypt(BigInteger)
	 * @since 1.0.0
	 */
	public void initialize() {
		binaryEncryptor.initialize();
	}

	/**
	 * 加密BigInteger整数
	 * <p>
	 * 实现{@link IntegerNumberEncryptor}接口的核心方法，提供精确的数值加密。
	 * </p>
	 *
	 * <h3>加密数据格式</h3>
	 * <pre>
	 * +----------------+----------------+
	 * | 4字节长度头(MSB) | RSA加密数据体 |
	 * +----------------+----------------+
	 * </pre>
	 *
	 * @param number 待加密整数，处理规则：
	 *               <ul>
	 *                 <li>null → 返回null</li>
	 *                 <li>0 → 加密后的非零值</li>
	 *                 <li>负数 → 保留符号位</li>
	 *               </ul>
	 * @return 加密后的BigInteger，具有以下特征：
	 *         <ul>
	 *           <li>数值部分为加密数据</li>
	 *           <li>符号位与原始数据无关</li>
	 *           <li>长度 = 加密数据长度 + 4字节</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>数值超过最大加密长度</li>
	 *           <li>公钥未初始化</li>
	 *           <li>加密失败</li>
	 *         </ul>
	 * @since 1.0.0
	 */
	@Override
	public BigInteger encrypt(final BigInteger number) {
		if (Objects.isNull(number)) {
			return null;
		}
		final byte[] messageBytes = number.toByteArray();
		final byte[] encryptedMessage = this.binaryEncryptor.encrypt(messageBytes);
		final byte[] encryptedMessageLengthBytes = NumberUtils.byteArrayFromInt(encryptedMessage.length);
		final byte[] encryptionResult = CommonUtils.appendArrays(encryptedMessage, encryptedMessageLengthBytes);
		return new BigInteger(encryptionResult);
	}

	/**
	 * 解密BigInteger密文
	 * <p>
	 * 精确还原加密前的原始数值，保证数值一致性。
	 * </p>
	 *
	 * <h3>解密验证</h3>
	 * <ul>
	 *   <li>校验长度头与实际数据长度是否匹配</li>
	 *   <li>验证私钥是否与加密公钥配对</li>
	 *   <li>检查填充方案是否一致</li>
	 * </ul>
	 *
	 * @param encryptedNumber 加密后的BigInteger，必须：
	 *                        <ul>
	 *                          <li>由本类{@link #encrypt(BigInteger)}方法生成</li>
	 *                          <li>未被篡改</li>
	 *                        </ul>
	 * @return 解密后的原始数值，保证：
	 *         <ul>
	 *           <li>数值与加密前完全一致</li>
	 *           <li>符号位正确还原</li>
	 *           <li>null输入返回null</li>
	 *         </ul>
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>输入格式非法</li>
	 *           <li>私钥不匹配</li>
	 *           <li>解密失败</li>
	 *         </ul>
	 * @since 1.0.0
	 */
	@Override
	public BigInteger decrypt(final BigInteger encryptedNumber) {
		if (Objects.isNull(encryptedNumber)) {
			return null;
		}
		byte[] encryptedMessageBytes = encryptedNumber.toByteArray();
		encryptedMessageBytes = NumberUtils.processBigIntegerEncryptedByteArray(
			encryptedMessageBytes, encryptedNumber.signum());
		byte[] message = binaryEncryptor.decrypt(encryptedMessageBytes);
		return new BigInteger(message);
	}
}
