package io.github.pangju666.commons.codec.encryption.binary;

import io.github.pangju666.commons.codec.key.RSAKey;
import io.github.pangju666.commons.codec.utils.RsaUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.binary.BinaryEncryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.ByteArrayOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Objects;

/**
 * RSA算法二进制加密器（公钥加密，私钥解密）
 *
 * <p>
 * 使用步骤：
 *    <ol>
 *        <li>创建一个实例（使用new）</li>
 *        <li>设置公钥（使用{@link #setPublicKey(byte[])}）<b>提示：</b>如果只需要解密可省略该操作</li>
 *        <li>设置私钥（使用{@link #setPrivateKey(byte[])}）<b>提示：</b>如果只需要加密可省略该操作</li>
 *        <li>初始化（使用{@link #initialize()}）<b>提示：</b>一旦加密器初始化，尝试更改密钥将导致抛出{@link AlreadyInitializedException}</li>
 *        <li>执行加密（使用{@link #encrypt(byte[])}）或解密（使用{@link #decrypt(byte[])}）操作</li>
 *    </ol>
 * </p>
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSABinaryEncryptor implements BinaryEncryptor {
	private Cipher decryptCipher;
	private Cipher encryptCipher;
	private int publicKeySize;
	private int privateKeySize;
	private final RSAKey rsaKey;

	public RSABinaryEncryptor() {
		this.rsaKey = new RSAKey();
	}

	public RSABinaryEncryptor(RSAKey rsaKey) {
		this.rsaKey = rsaKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.rsaKey.setPublicKey(publicKey);
	}

	public void setPrivateKey(byte[] privateKey) {
		this.rsaKey.setPrivateKey(privateKey);
	}

	public synchronized void initialize() {
		if (!rsaKey.isInitialized()) {
			try {
				if (Objects.nonNull(rsaKey.getPublicKey())) {
					PublicKey publicKey = RsaUtils.getPublicKey(rsaKey.getPublicKey());
					rsaKey.cleanPublicKey();

					RSAPublicKeySpec publicKeySpec = RsaUtils.getKeyFactory().getKeySpec(publicKey,
						RSAPublicKeySpec.class);
					this.publicKeySize = publicKeySpec.getModulus().bitLength() / 8 - 11;

					this.encryptCipher = Cipher.getInstance(RsaUtils.DEFAULT_CIPHER_ALGORITHM);
					this.encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
				}

				if (Objects.nonNull(rsaKey.getPrivateKey())) {
					PrivateKey privateKey = RsaUtils.getPrivateKey(rsaKey.getPrivateKey());
					rsaKey.cleanPrivateKey();

					RSAPrivateKeySpec privateKeySpec = RsaUtils.getKeyFactory().getKeySpec(privateKey,
						RSAPrivateKeySpec.class);
					this.privateKeySize = privateKeySpec.getModulus().bitLength() / 8;

					this.decryptCipher = Cipher.getInstance(RsaUtils.DEFAULT_CIPHER_ALGORITHM);
					this.decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
				}
			} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
					 InvalidKeySpecException e) {
				throw new EncryptionInitializationException(e);
			}
			rsaKey.initialize();
		}
	}

	public byte[] encrypt(final byte[] binary) {
		if (ArrayUtils.isEmpty(binary)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(rsaKey.getPublicKey())) {
			throw new EncryptionOperationNotPossibleException("未设置公钥");
		}
		if (!rsaKey.isInitialized()) {
			initialize();
		}
		try {
			return doFinal(this.encryptCipher, binary, this.publicKeySize);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	public byte[] decrypt(final byte[] encryptedBinary) {
		if (ArrayUtils.isEmpty(encryptedBinary)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(rsaKey.getPrivateKey())) {
			throw new EncryptionOperationNotPossibleException("未设置私钥");
		}
		if (!rsaKey.isInitialized()) {
			initialize();
		}
		try {
			return doFinal(this.decryptCipher, encryptedBinary, this.privateKeySize);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	private byte[] doFinal(final Cipher cipher, final byte[] input, final int size)
		throws IllegalBlockSizeException, BadPaddingException {
		if (input.length <= size) {
			return cipher.doFinal(input);
		}
		int inputLength = input.length;
		int offsetLength = 0;
		int i = 0;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		while (inputLength - offsetLength > 0) {
			byte[] bytes;
			if (inputLength - offsetLength > size) {
				bytes = cipher.doFinal(input, offsetLength, size);
			} else {
				bytes = cipher.doFinal(input, offsetLength, inputLength - offsetLength);
			}
			outputStream.writeBytes(bytes);
			++i;
			offsetLength = size * i;
		}
		return outputStream.toByteArray();
	}
}
