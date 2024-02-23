package io.github.pangju666.commons.codec.digest;

import io.github.pangju666.commons.codec.utils.RsaUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jasypt.commons.CommonUtils;
import org.jasypt.digest.ByteDigester;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * RSA算法二进制消息签名器（私钥签名，公钥验证签名）
 * <p>
 * 使用步骤：
 *    <ol>
 *        <li>创建一个实例（使用new）</li>
 *        <li>设置公钥（使用{@link #setPublicKey(byte[])}）<b>提示：</b>如果只需要签名可省略该操作</li>
 *        <li>设置私钥（使用{@link #setPrivateKey(byte[])}）<b>提示：</b>如果只需要验证签名可省略该操作</li>
 *        <li>初始化（使用{@link #initialize()}）<b>提示：</b>一旦加密器初始化，尝试更改密钥将导致抛出{@link AlreadyInitializedException}</li>
 *        <li>执行签名（使用{@link #digest(byte[])}）或验证签名（使用{@link #matches(byte[], byte[])}）操作</li>
 *    </ol>
 * </p>
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSAByteDigester implements ByteDigester {
	private byte[] publicKeyBytes;
	private byte[] privateKeyBytes;
	private Signature privateSignature;
	private Signature publicSignature;
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

					this.publicSignature = Signature.getInstance(RsaUtils.DEFAULT_SIGNATURE_ALGORITHM);
					this.publicSignature.initVerify(publicKey);
				}

				if (Objects.nonNull(privateKeyBytes)) {
					PrivateKey privateKey = RsaUtils.getPrivateKey(this.privateKeyBytes);
					cleanKey(privateKeyBytes);

					this.privateSignature = Signature.getInstance(RsaUtils.DEFAULT_SIGNATURE_ALGORITHM);
					this.privateSignature.initSign(privateKey);
				}
			} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
				throw new EncryptionInitializationException(e);
			}

			this.initialized = true;
		}
	}

	@Override
	public byte[] digest(byte[] message) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(this.privateKeyBytes)) {
			throw new EncryptionInitializationException("未设置私钥");
		}
		if (!this.initialized) {
			initialize();
		}
		try {
			privateSignature.update(message);
			return privateSignature.sign();
		} catch (SignatureException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}

	@Override
	public boolean matches(byte[] message, byte[] digest) {
		if (Objects.isNull(message)) {
			return Objects.isNull(digest);
		} else if (Objects.isNull(digest)) {
			return false;
		}
		if (Objects.isNull(this.publicKeyBytes)) {
			throw new EncryptionInitializationException("未设置公钥");
		}
		if (!this.initialized) {
			initialize();
		}
		try {
			publicSignature.update(message);
			return publicSignature.verify(digest);
		} catch (SignatureException e) {
			throw new EncryptionOperationNotPossibleException(e);
		}
	}
}
