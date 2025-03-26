package io.github.pangju666.commons.crypto.encryption.numeric;

import io.github.pangju666.commons.crypto.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.crypto.key.RSAKey;
import org.jasypt.commons.CommonUtils;
import org.jasypt.util.numeric.IntegerNumberEncryptor;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * RSA算法整数加密器（公钥加密，私钥解密）
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSAIntegerNumberEncryptor implements IntegerNumberEncryptor {
	private final RSABinaryEncryptor binaryEncryptor;

	public RSAIntegerNumberEncryptor() throws NoSuchAlgorithmException {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	public RSAIntegerNumberEncryptor(RSAKey key) {
		this.binaryEncryptor = new RSABinaryEncryptor(key);
	}

	public void setKey(RSAKey key) {
		binaryEncryptor.setKey(key);
	}

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
