package io.github.pangju666.commons.codec.encryption.binary;

import io.github.pangju666.commons.codec.key.RSAKey;
import io.github.pangju666.commons.codec.utils.RSAUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
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
	private RSAKey key = new RSAKey();
	private boolean initialized = false;
	private String algorithm = RSAUtils.DEFAULT_CIPHER_ALGORITHM;

	public RSABinaryEncryptor() {
	}

	public RSABinaryEncryptor(RSAKey key) {
		this.key = key;
	}

	public synchronized void setKey(RSAKey key) {
		Validate.notNull(algorithm, "密钥不可为空");
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.key = key;
	}

	public synchronized void setAlgorithm(String algorithm) {
		Validate.notBlank(algorithm, "算法不可为空");
		if (initialized) {
			throw new AlreadyInitializedException();
		}
		this.algorithm = algorithm;
	}

	public synchronized void initialize() {
		if (!initialized) {
			try {
				if (Objects.nonNull(key.getPublicKey())) {
					PublicKey publicKey = RSAUtils.getPublicKey(key.getPublicKey());
					RSAPublicKeySpec publicKeySpec = RSAUtils.getKeyFactory().getKeySpec(publicKey,
						RSAPublicKeySpec.class);
					this.publicKeySize = publicKeySpec.getModulus().bitLength() / 8 - 11;
					this.encryptCipher = Cipher.getInstance(algorithm);
					this.encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
				}

				if (Objects.nonNull(key.getPrivateKey())) {
					PrivateKey privateKey = RSAUtils.getPrivateKey(key.getPrivateKey());
					RSAPrivateKeySpec privateKeySpec = RSAUtils.getKeyFactory().getKeySpec(privateKey,
						RSAPrivateKeySpec.class);
					this.privateKeySize = privateKeySpec.getModulus().bitLength() / 8;
					this.decryptCipher = Cipher.getInstance(algorithm);
					this.decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
				}
			} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
					 InvalidKeySpecException e) {
				throw new EncryptionInitializationException(e);
			}
			initialized = true;
		}
	}

	public byte[] encrypt(final byte[] binary) {
		if (ArrayUtils.isEmpty(binary)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(key.getPublicKey())) {
			throw new EncryptionOperationNotPossibleException("未设置公钥");
		}
		if (!initialized) {
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
		if (Objects.isNull(key.getPrivateKey())) {
			throw new EncryptionOperationNotPossibleException("未设置私钥");
		}
		if (!initialized) {
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