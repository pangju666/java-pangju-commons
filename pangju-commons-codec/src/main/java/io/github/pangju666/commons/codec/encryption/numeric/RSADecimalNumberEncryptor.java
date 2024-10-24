package io.github.pangju666.commons.codec.encryption.numeric;

import io.github.pangju666.commons.codec.encryption.binary.RSABinaryEncryptor;
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
 * <p>
 * 使用步骤：
 *    <ol>
 *        <li>创建一个实例（使用new）</li>
 *        <li>设置公钥（使用{@link #setPublicKey(byte[])}）<b>提示：</b>如果只需要解密可省略该操作</li>
 *        <li>设置私钥（使用{@link #setPrivateKey(byte[])}）<b>提示：</b>如果只需要加密可省略该操作</li>
 *        <li>执行加密（使用{@link #encrypt(BigDecimal)}）或解密（使用{@link #decrypt(BigDecimal)}）操作</li>
 *    </ol>
 * </p>
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

	public RSADecimalNumberEncryptor(RSABinaryEncryptor rsaBinaryEncryptor) {
		this.binaryEncryptor = rsaBinaryEncryptor;
	}

	public void setPublicKey(final byte[] publicKey) {
		binaryEncryptor.setPublicKey(publicKey);
	}

	public void setPrivateKey(final byte[] privateKey) {
		binaryEncryptor.setPrivateKey(privateKey);
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
