package io.github.pangju666.commons.codec.encryption.numeric;

import io.github.pangju666.commons.codec.encryption.binary.RSABinaryEncryptor;
import io.github.pangju666.commons.codec.key.RSAKey;
import io.github.pangju666.commons.codec.utils.NumberUtils;
import org.jasypt.commons.CommonUtils;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.numeric.DecimalNumberEncryptor;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * RSA算法浮点数加密器（公钥加密，私钥解密）
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSADecimalNumberEncryptor implements DecimalNumberEncryptor {
	private final RSABinaryEncryptor binaryEncryptor;

	public RSADecimalNumberEncryptor() {
		this.binaryEncryptor = new RSABinaryEncryptor();
	}

	public RSADecimalNumberEncryptor(RSAKey key) {
		this.binaryEncryptor = new RSABinaryEncryptor(key);
	}

	public void setKey(RSAKey key) {
		binaryEncryptor.setKey(key);
	}

	public void setAlgorithm(String algorithm) {
		binaryEncryptor.setAlgorithm(algorithm);
	}

	@Override
	public BigDecimal encrypt(final BigDecimal number) {
		if (Objects.isNull(number)) {
			return null;
		}
		try {
			final int scale = number.scale();
			final BigInteger unscaledMessage = number.unscaledValue();
			final byte[] messageBytes = unscaledMessage.toByteArray();
			final byte[] encryptedMessage = this.binaryEncryptor.encrypt(messageBytes);
			final byte[] encryptedMessageLengthBytes = NumberUtils.byteArrayFromInt(encryptedMessage.length);
			final byte[] encryptionResult = CommonUtils.appendArrays(encryptedMessage, encryptedMessageLengthBytes);
			return new BigDecimal(new BigInteger(encryptionResult), scale);
		} catch (EncryptionInitializationException | EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}
	}

	@Override
	public BigDecimal decrypt(final BigDecimal encryptedNumber) {
		if (Objects.isNull(encryptedNumber)) {
			return null;
		}
		try {
			int scale = encryptedNumber.scale();
			BigInteger unscaledEncryptedMessage = encryptedNumber.unscaledValue();
			byte[] encryptedMessageBytes = unscaledEncryptedMessage.toByteArray();
			encryptedMessageBytes = NumberUtils.processBigIntegerEncryptedByteArray(
				encryptedMessageBytes, encryptedNumber.signum());
			byte[] message = binaryEncryptor.decrypt(encryptedMessageBytes);
			return new BigDecimal(new BigInteger(message), scale);
		} catch (EncryptionInitializationException | EncryptionOperationNotPossibleException e) {
			throw e;
		} catch (Exception e) {
			throw new EncryptionOperationNotPossibleException();
		}
	}
}
