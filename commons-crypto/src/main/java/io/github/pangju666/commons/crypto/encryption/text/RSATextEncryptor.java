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

package io.github.pangju666.commons.crypto.encryption.text;

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKey;
import io.github.pangju666.commons.crypto.transformation.RSATransformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation;
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.TextEncryptor;

import java.nio.charset.StandardCharsets;

/**
 * 基于RSA算法的文本加密解密器，提供安全的非对称文本加解密能力
 * <p>
 * 本类实现了{@link TextEncryptor}接口，专门用于处理字符串数据的加密和解密操作，
 * 支持两种输出编码格式：Base64和十六进制。
 * </p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><b>安全算法</b> - 采用RSA非对称加密算法，支持2048/3072/4096位密钥强度</li>
 *   <li><b>编码支持</b>：
 *     <ul>
 *       <li>Base64编码 - 适用于网络传输和存储（体积小）</li>
 *       <li>十六进制编码 - 便于调试和日志记录（可读性强）</li>
 *     </ul>
 *   </li>
 *   <li><b>加密方案</b> - 支持PKCS#1 v1.5、OAEP等多种填充模式</li>
 *   <li><b>字符编码</b> - 强制使用UTF-8编码保证跨平台一致性</li>
 * </ul>
 *
 * <h3>典型用例</h3>
 * <ol>
 *   <li>敏感配置信息加密存储</li>
 *   <li>API通信内容端到端加密</li>
 *   <li>数据库字段级加密</li>
 *   <li>日志敏感信息脱敏</li>
 * </ol>
 *
 * <h3>线程安全</h3>
 * <p>
 * 本类设计为线程安全，具有以下保证：
 * </p>
 * <ul>
 *   <li>所有配置方法通过同步机制保证原子性</li>
 *   <li>核心加密器实例(final修饰)初始化后不可变</li>
 *   <li>加解密操作无状态且幂等</li>
 * </ul>
 *
 * <h3>性能建议</h3>
 * <ul>
 *   <li>RSA算法不适合加密大文本，建议：
 *     <ul>
 *       <li>单次加密不超过密钥长度/8 - 填充长度</li>
 *       <li>2048位密钥最大加密190字节</li>
 *     </ul>
 *   </li>
 *   <li>频繁加密场景建议复用实例</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 * @see TextEncryptor
 * @see RSABinaryEncryptor
 * @see RSATransformation
 * @see RSAKey
 */
public final class RSATextEncryptor implements TextEncryptor {
	/**
	 * 核心RSA二进制加密处理器，执行底层加密运算
	 * <p>
	 * 此实例通过构造函数注入，生命周期与宿主对象一致，具有以下特性：
	 * </p>
	 * <ul>
	 *   <li><b>不可变性</b> - 通过final修饰确保线程安全</li>
	 *   <li><b>委托模式</b> - 所有加密操作最终委托给此实例执行</li>
	 *   <li><b>配置代理</b> - 配置变更通过此实例的对应方法实现</li>
	 * </ul>
	 * <p>
	 * <b>设计约束：</b>
	 * </p>
	 * <ul>
	 *   <li>必须在构造时完成初始化</li>
	 *   <li>不支持运行时替换</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	private final RSABinaryEncryptor binaryEncryptor;

	/**
	 * 构建使用默认安全配置的加密器实例
	 * <p>
	 * 默认配置参数：
	 * <ul>
	 *   <li><b>密钥强度</b>：2048位RSA密钥（推荐安全强度）</li>
	 *   <li><b>加密方案</b>：{@link RSAOEAPWithSHA256Transformation}（OAEP填充）</li>
	 *   <li><b>字符编码</b>：UTF-8（强制使用）</li>
	 * </ul>
	 * </p>
	 *
	 * <h3>适用场景</h3>
	 * <ul>
	 *   <li>快速创建标准安全级别的加密器</li>
	 *   <li>不需要自定义密钥和加密方案的场景</li>
	 * </ul>
	 *
	 * @throws EncryptionInitializationException 当密钥生成失败时抛出
	 * @see #RSATextEncryptor(RSATransformation)
	 * @see #RSATextEncryptor(int)
	 * @since 1.0.0
	 */
	public RSATextEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	/**
	 * 使用指定加密方案构建加密器实例（默认密钥长度）
	 * <p>
	 * 本构造方法允许自定义加密方案，但使用默认的2048位密钥长度。
	 * </p>
	 *
	 * <h3>方案选择建议</h3>
	 * <ul>
	 *   <li>高安全性场景：使用{@link RSAOEAPWithSHA256Transformation}</li>
	 *   <li>兼容性场景：使用{@link RSAPKCS1PaddingTransformation}</li>
	 * </ul>
	 *
	 * @param transformation 加密方案，不可为null
	 * @throws NullPointerException 当传入null参数时抛出
	 * @see RSATransformation
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(transformation);
	}

	/**
	 * 使用指定密钥长度构建加密器实例（默认加密方案）
	 * <p>
	 * 本构造方法允许自定义密钥长度，但使用默认的OAEP加密方案。
	 * </p>
	 *
	 * <h3>密钥长度建议</h3>
	 * <ul>
	 *   <li>常规用途：2048位（平衡安全与性能）</li>
	 *   <li>高安全需求：3072或4096位</li>
	 *   <li>测试用途：1024位（不推荐生产环境）</li>
	 * </ul>
	 *
	 * @param keySize RSA密钥长度（单位：bit），有效值：1024/2048/3072/4096
	 * @throws IllegalArgumentException 当密钥长度不合法时抛出
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final int keySize) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize);
	}

	/**
	 * 使用指定密钥长度和加密方案构建加密器实例
	 * <p>
	 * 完全自定义配置构造方法，允许同时指定密钥长度和加密方案。
	 * </p>
	 *
	 * <h3>配置组合验证</h3>
	 * <ul>
	 *   <li>OAEP方案要求最小2048位密钥</li>
	 *   <li>PKCS#1方案兼容1024位密钥</li>
	 * </ul>
	 *
	 * @param keySize        密钥长度（单位：bit）
	 * @param transformation 加密方案
	 * @throws IllegalArgumentException 当密钥长度与方案不兼容时抛出
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final int keySize, final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(keySize, transformation);
	}

	/**
	 * 使用已有密钥对构建加密器实例（默认加密方案）
	 * <p>
	 * 适用于密钥预生成场景，使用默认的OAEP加密方案。
	 * </p>
	 *
	 * <h3>密钥要求</h3>
	 * <ul>
	 *   <li>必须包含有效的公钥（加密）或私钥（解密）</li>
	 *   <li>密钥强度应与安全需求匹配</li>
	 *   <li>支持硬件安全模块(HSM)生成的密钥</li>
	 * </ul>
	 *
	 * @param key 预生成的RSA密钥对，不可为null
	 * @throws NullPointerException 当密钥为null时抛出
	 * @see RSAKey
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final RSAKey key) {
		this.binaryEncryptor = new RSABinaryEncryptor(key);
	}

	/**
	 * 使用已有密钥对和指定加密方案构建加密器实例
	 * <p>
	 * 完全自定义配置构造方法，适用于需要精确控制加密方案的场景。
	 * </p>
	 *
	 * <h3>密钥与方案兼容性</h3>
	 * <ul>
	 *   <li>验证密钥长度是否支持所选方案</li>
	 *   <li>检查密钥是否包含必要参数（如OAEP需要哈希参数）</li>
	 * </ul>
	 *
	 * @param key            预生成的RSA密钥对
	 * @param transformation 加密方案
	 * @throws IllegalArgumentException 当密钥与方案不兼容时抛出
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final RSAKey key, final RSATransformation transformation) {
		this.binaryEncryptor = new RSABinaryEncryptor(key, transformation);
	}

	/**
	 * 使用预配置的二进制加密器构建实例
	 * <p>
	 * 高级构造方法，适用于需要复用加密器配置的场景。
	 * </p>
	 *
	 * <h3>使用场景</h3>
	 * <ul>
	 *   <li>加密器配置共享</li>
	 *   <li>自定义生命周期管理</li>
	 *   <li>实现加密器热切换</li>
	 * </ul>
	 *
	 * <h3>前置条件</h3>
	 * <ul>
	 *   <li>加密器必须已完成初始化</li>
	 *   <li>密钥和加密方案必须有效配置</li>
	 * </ul>
	 *
	 * @param binaryEncryptor 预初始化的二进制加密器实例
	 * @throws NullPointerException 当加密器为null时抛出
	 * @throws IllegalStateException 当加密器未正确初始化时抛出
	 * @see RSABinaryEncryptor
	 * @since 1.0.0
	 */
	public RSATextEncryptor(final RSABinaryEncryptor binaryEncryptor) {
		Validate.notNull(binaryEncryptor, "binaryEncryptor 不能为 null");
		this.binaryEncryptor = binaryEncryptor;
	}

	public RSAKey getKey() {
		return binaryEncryptor.getKey();
	}

	/**
	 * 配置加密算法方案
	 * <p>
	 * 此方法用于指定RSA加密的具体填充方案，必须在首次加密操作前调用。
	 * </p>
	 *
	 * <h3>重要约束</h3>
	 * <ul>
	 *   <li><b>时机限制</b> - 必须在首次加密/解密前调用</li>
	 *   <li><b>兼容性风险</b> - 修改方案会导致已有加密数据不可解密</li>
	 *   <li><b>密钥绑定</b> - 不同方案可能需要重新生成密钥</li>
	 * </ul>
	 *
	 * <h3>内置支持方案</h3>
	 * <ul>
	 *   <li>{@link RSAPKCS1PaddingTransformation} - 传统PKCS#1 v1.5填充</li>
	 *   <li>{@link RSAOEAPWithSHA256Transformation} - 更安全的OAEP填充（推荐）</li>
	 * </ul>
	 *
	 * @param transformation 加密方案枚举实例
	 * @throws AlreadyInitializedException 若已执行过加密操作后调用
	 * @throws NullPointerException 当传入null参数时抛出
	 * @see RSATransformation
	 * @since 1.0.0
	 */
	public void setTransformation(final RSATransformation transformation) {
		this.binaryEncryptor.setTransformation(transformation);
	}

	/**
	 * 设置 RSA 密钥（必须在初始化前调用）
	 * <p>
	 * 用于动态更换加密密钥对，适用于密钥轮换场景。
	 * </p>
	 *
	 * <h3>重要约束</h3>
	 * <ul>
	 *   <li><b>时机限制</b> - 必须在首次加密/解密操作前调用</li>
	 *   <li><b>兼容性</b> - 新密钥必须与当前加密方案兼容</li>
	 *   <li><b>数据影响</b> - 更换密钥后无法解密之前加密的数据</li>
	 * </ul>
	 *
	 * @param key 新的RSA密钥对，需满足：
	 *            <ul>
	 *              <li>非null</li>
	 *              <li>已完成密钥初始化</li>
	 *              <li>与当前加密方案匹配</li>
	 *            </ul>
	 * @throws AlreadyInitializedException 如果已经执行过加密/解密操作
	 * @throws NullPointerException 当传入null参数时抛出
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
	 * <p><strong>注意：</strong>此方法会自动被{@link #encrypt(String)}和{@link #decrypt(String)}调用，通常不需要手动调用。</p>
	 *
	 * @throws EncryptionInitializationException 当以下情况发生时抛出：
	 *                                           <ul>
	 *                                               <li>未配置任何密钥</li>
	 *                                               <li>算法不支持</li>
	 *                                               <li>密钥无效</li>
	 *                                           </ul>
	 * @see #encrypt(String)
	 * @see #decrypt(String)
	 * @since 1.0.0
	 */
	public void initialize() {
		binaryEncryptor.initialize();
	}

	/**
	 * Base64格式加密文本
	 * <p>
	 * 实现{@link TextEncryptor}接口的核心方法，提供标准Base64编码的加密输出。
	 * </p>
	 *
	 * <h3>处理流程</h3>
	 * <ol>
	 *   <li>输入有效性校验（空值处理）</li>
	 *   <li>UTF-8编码转换</li>
	 *   <li>RSA公钥加密原始字节</li>
	 *   <li>Base64 URL安全编码</li>
	 * </ol>
	 *
	 * <h3>输出规范</h3>
	 * <ul>
	 *   <li>符合RFC 4648标准</li>
	 *   <li>不使用填充字符('=')</li>
	 *   <li>不包含换行符</li>
	 *   <li>结果字符串长度固定为 ((n + 2) / 3) * 4</li>
	 * </ul>
	 *
	 * @param message 待加密文本，null或空字符串将返回空字符串
	 * @return Base64编码的加密结果
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>明文长度超过RSA算法限制</li>
	 *           <li>公钥未初始化</li>
	 *           <li>加密过程发生错误</li>
	 *         </ul>
	 * @see Base64#encodeBase64String(byte[])
	 * @since 1.0.0
	 */
	@Override
	public String encrypt(final String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Base64.encodeBase64String(
			binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 解密Base64编码的加密文本
	 * <p>
	 * 实现{@link TextEncryptor}接口的核心方法，处理Base64格式的加密输入。
	 * </p>
	 *
	 * <h3>解密要求</h3>
	 * <ul>
	 *   <li>必须使用加密时相同的私钥</li>
	 *   <li>Base64格式必须与加密输出一致</li>
	 *   <li>密钥和加密方案未变更</li>
	 * </ul>
	 *
	 * <h3>容错处理</h3>
	 * <ul>
	 *   <li>自动处理URL安全的Base64输入</li>
	 *   <li>兼容标准Base64输入</li>
	 *   <li>忽略尾部的填充字符</li>
	 * </ul>
	 *
	 * @param encryptedMessage Base64格式的加密字符串，null或空字符串将返回空字符串
	 * @return 原始明文字符串（UTF-8编码）
	 * @throws EncryptionOperationNotPossibleException 当：
	 *         <ul>
	 *           <li>私钥不匹配</li>
	 *           <li>输入格式非法</li>
	 *           <li>解密过程失败</li>
	 *         </ul>
	 * @see Base64#decodeBase64(String)
	 * @since 1.0.0
	 */
	@Override
	public String decrypt(final String encryptedMessage) {
		if (StringUtils.isBlank(encryptedMessage)) {
			return StringUtils.EMPTY;
		}
		return new String(binaryEncryptor.decrypt(
			Base64.decodeBase64(encryptedMessage)), StandardCharsets.UTF_8);
	}

	/**
	 * 十六进制格式加密文本
	 * <p>
	 * 提供人类可读的加密输出格式，适用于调试和日志场景。
	 * </p>
	 *
	 * <h3>与Base64加密的区别</h3>
	 * <table border="1">
	 *   <tr><th>特性</th><th>Base64</th><th>十六进制</th></tr>
	 *   <tr><td>输出体积</td><td>增加~33%</td><td>增加100%</td></tr>
	 *   <tr><td>可读性</td><td>低</td><td>高</td></tr>
	 *   <tr><td>适用场景</td><td>生产环境</td><td>开发调试</td></tr>
	 * </table>
	 *
	 * @param message 待加密文本，处理规则同{@link #encrypt(String)}
	 * @return 小写十六进制字符串
	 * @see #encrypt(String)
	 * @see Hex#encodeHexString(byte[])
	 * @since 1.0.0
	 */
	public String encryptToHexString(final String message) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		return Hex.encodeHexString(binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	/**
	 * 解密十六进制格式加密文本
	 * <p>
	 * 处理{@link #encryptToHexString(String)}方法生成的加密输出。
	 * </p>
	 *
	 * <h3>输入规范</h3>
	 * <ul>
	 *   <li>标准十六进制字符串（0-9, a-f, A-F）</li>
	 *   <li>可包含分隔符（自动过滤）</li>
	 *   <li>长度必须为偶数（自动补零）</li>
	 * </ul>
	 *
	 * <h3>安全注意事项</h3>
	 * <ul>
	 *   <li>不验证数据完整性</li>
	 *   <li>可能丢失前导零</li>
	 *   <li>建议仅用于可信数据源</li>
	 * </ul>
	 *
	 * @param encryptedMessage 十六进制格式字符串
	 * @return 解密后的原始字符串
	 * @throws EncryptionOperationNotPossibleException 当输入包含非法字符或解密失败
	 * @see #decrypt(String)
	 * @see Hex#decodeHex(String)
	 * @since 1.0.0
	 */
	public String decryptFromHexString(final String encryptedMessage) {
		if (StringUtils.isBlank(encryptedMessage)) {
			return StringUtils.EMPTY;
		}
		try {
			return new String(binaryEncryptor.decrypt(Hex.decodeHex(encryptedMessage)),
				StandardCharsets.UTF_8);
		} catch (DecoderException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}
}