package io.github.pangju666.commons.codec.encryption.binary;

import io.github.pangju666.commons.codec.utils.RsaUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.commons.CommonUtils;
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
	private byte[] publicKeyBytes;
	private byte[] privateKeyBytes;
	private int publicKeySize;
	private int privateKeySize;
	private boolean initialized = false;

	private static void cleanKey(final byte[] key) {
		if (Objects.nonNull(key)) {
			synchronized (key) {
				final int keyLength = key.length;
				for (int i = 0; i < keyLength; i++) {
					key[i] = (byte) 0;
				}
			}
		}
	}

	public synchronized void setPublicKey(byte[] publicKey) {
		CommonUtils.validateNotNull(publicKey, "公钥不可为 null");
		CommonUtils.validateIsTrue(publicKey.length > 0, "公钥不可为空");
		if (this.initialized) {
			throw new AlreadyInitializedException();
		}
		if (Objects.nonNull(this.publicKeyBytes)) {
			cleanKey(this.publicKeyBytes);
		}
		this.publicKeyBytes = new byte[publicKey.length];
		System.arraycopy(publicKey, 0, this.publicKeyBytes, 0, publicKey.length);
	}

	public synchronized void setPrivateKey(byte[] privateKey) {
		CommonUtils.validateNotNull(privateKey, "私钥不可为 null");
		CommonUtils.validateIsTrue(privateKey.length > 0, "私钥不可为空");
		if (this.initialized) {
			throw new AlreadyInitializedException();
		}
		if (Objects.nonNull(this.privateKeyBytes)) {
			cleanKey(this.privateKeyBytes);
		}
		this.privateKeyBytes = new byte[privateKey.length];
		System.arraycopy(privateKey, 0, this.privateKeyBytes, 0, privateKey.length);
	}

	public synchronized void initialize() {
		if (!this.initialized) {
			try {
				if (Objects.nonNull(this.publicKeyBytes)) {
					PublicKey publicKey = RsaUtils.getPublicKey(this.publicKeyBytes);
					cleanKey(this.publicKeyBytes);

					RSAPublicKeySpec keySpec = RsaUtils.getKeyFactory().getKeySpec(publicKey, RSAPublicKeySpec.class);
					this.publicKeySize = keySpec.getModulus().bitLength() / 8 - 11;

					this.encryptCipher = Cipher.getInstance(RsaUtils.DEFAULT_CIPHER_ALGORITHM);
					this.encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
				}

				if (Objects.nonNull(privateKeyBytes)) {
					PrivateKey privateKey = RsaUtils.getPrivateKey(this.privateKeyBytes);
					cleanKey(privateKeyBytes);

					RSAPrivateKeySpec keySpec = RsaUtils.getKeyFactory().getKeySpec(privateKey, RSAPrivateKeySpec.class);
					this.privateKeySize = keySpec.getModulus().bitLength() / 8;

					this.decryptCipher = Cipher.getInstance(RsaUtils.DEFAULT_CIPHER_ALGORITHM);
					this.decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
				}
			} catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException |
					 InvalidKeySpecException e) {
				throw new EncryptionInitializationException(e);
			}

			this.initialized = true;
		}
	}

	public byte[] encrypt(final byte[] binary) {
		if (ArrayUtils.isEmpty(binary)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(this.publicKeyBytes)) {
			throw new EncryptionInitializationException("未设置公钥");
		}
		if (!this.initialized) {
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
		if (Objects.isNull(this.privateKeyBytes)) {
			throw new EncryptionInitializationException("未设置私钥");
		}
		if (!this.initialized) {
			initialize();
		}
		try {
			return doFinal(this.decryptCipher, encryptedBinary, this.privateKeySize);
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	private byte[] doFinal(final Cipher cipher, final byte[] input, int size)
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
