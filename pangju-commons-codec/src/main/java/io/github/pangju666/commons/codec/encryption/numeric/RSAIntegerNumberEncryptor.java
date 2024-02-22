package io.github.pangju666.commons.codec.encryption.numeric;

import io.github.pangju666.commons.codec.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.codec.utils.NumberUtils;
import org.jasypt.commons.CommonUtils;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.util.numeric.IntegerNumberEncryptor;

import java.math.BigInteger;
import java.util.Objects;

/**
 * RSA算法整数加密器（公钥加密，私钥解密）
 * <p>
 * 使用步骤：
 *    <ol>
 *        <li>创建一个实例（使用new）</li>
 *        <li>设置公钥（使用{@link #setPublicKey(byte[])}）<b>提示：</b>如果只需要解密可省略该操作</li>
 *        <li>设置私钥（使用{@link #setPrivateKey(byte[])}）<b>提示：</b>如果只需要加密可省略该操作</li>
 *        <li>初始化（使用{@link #initialize()}）<b>提示：</b>一旦加密器初始化，尝试更改密钥将导致抛出{@link AlreadyInitializedException}</li>
 *        <li>执行加密（使用{@link #encrypt(BigInteger)}）或解密（使用{@link #decrypt(BigInteger)}）操作</li>
 *    </ol>
 * </p>
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSAIntegerNumberEncryptor implements IntegerNumberEncryptor {
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
	public BigInteger encrypt(BigInteger number) {
		if (Objects.isNull(number)) {
			return null;
		}

		final byte[] messageBytes = number.toByteArray();
		final byte[] encryptedMessage = this.binaryEncryptor.encrypt(messageBytes);
		final byte[] encryptedMessageLengthBytes = NumberUtils.byteArrayFromInt(encryptedMessage.length);
		final byte[] encryptionResult = CommonUtils.appendArrays(encryptedMessage, encryptedMessageLengthBytes);
		return new BigInteger(encryptionResult);
	}

	@Override
	public BigInteger decrypt(BigInteger encryptedNumber) {
		if (Objects.isNull(encryptedNumber)) {
			return null;
		}

		byte[] encryptedMessageBytes = encryptedNumber.toByteArray();
		encryptedMessageBytes = NumberUtils.processBigIntegerEncryptedByteArray(encryptedMessageBytes, encryptedNumber.signum());
		byte[] message = binaryEncryptor.decrypt(encryptedMessageBytes);
		return new BigInteger(message);
	}
}
