package io.github.pangju666.commons.codec.encryption.text;

import io.github.pangju666.commons.codec.encryption.binary.RSABinaryEncryptor;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.TextEncryptor;

import java.nio.charset.StandardCharsets;

/**
 * RSA算法文本加密器（公钥加密，私钥解密）
 * <p>默认以BASE64格式返回结果</p>
 * <p>
 * 使用步骤：
 *    <ol>
 *        <li>创建一个实例（使用new）</li>
 *        <li>设置公钥（使用{@link #setPublicKey(byte[])}）<b>提示：</b>如果只需要解密可省略该操作</li>
 *        <li>设置私钥（使用{@link #setPrivateKey(byte[])}）<b>提示：</b>如果只需要加密可省略该操作</li>
 *        <li>初始化（使用{@link #initialize()}）<b>提示：</b>一旦加密器初始化，尝试更改密钥将导致抛出{@link AlreadyInitializedException}</li>
 *        <li>执行加密（使用{@link #encrypt(String)}）或解密（使用{@link #decrypt(String)}）操作</li>
 *    </ol>
 * </p>
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSATextEncryptor implements TextEncryptor {
	private final RSABinaryEncryptor binaryEncryptor = new RSABinaryEncryptor();

	public void setPublicKey(byte[] publicKey) {
		binaryEncryptor.setPublicKey(publicKey);
	}

	public void setPrivateKey(byte[] privateKey) {
		binaryEncryptor.setPrivateKey(privateKey);
	}

	public void initialize() {
		binaryEncryptor.initialize();
	}

	@Override
	public String encrypt(String message) {
		return encrypt(message, false);
	}

	@Override
	public String decrypt(String encryptedMessage) {
		return decrypt(encryptedMessage, false);
	}

	public String encrypt(String message, boolean outputHex) {
		if (StringUtils.isBlank(message)) {
			return StringUtils.EMPTY;
		}
		if (!outputHex) {
			return Base64.encodeBase64String(binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
		}
		return Hex.encodeHexString(binaryEncryptor.encrypt(message.getBytes(StandardCharsets.UTF_8)));
	}

	public String decrypt(String encryptedMessage, boolean inputHex) {
		if (StringUtils.isBlank(encryptedMessage)) {
			return StringUtils.EMPTY;
		}
		if (!inputHex) {
			return new String(binaryEncryptor.decrypt(Base64.decodeBase64(encryptedMessage)), StandardCharsets.UTF_8);
		}
		try {
			return new String(binaryEncryptor.decrypt(Hex.decodeHex(encryptedMessage)), StandardCharsets.UTF_8);
		} catch (DecoderException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}
}
