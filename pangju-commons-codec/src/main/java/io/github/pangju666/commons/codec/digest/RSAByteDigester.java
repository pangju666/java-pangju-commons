package io.github.pangju666.commons.codec.digest;

import io.github.pangju666.commons.codec.key.RSAKey;
import io.github.pangju666.commons.codec.utils.RSAUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jasypt.digest.ByteDigester;
import org.jasypt.exceptions.AlreadyInitializedException;
import org.jasypt.exceptions.EncryptionInitializationException;
import org.jasypt.exceptions.EncryptionOperationNotPossibleException;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.Objects;

/**
 * RSA算法二进制消息签名器（私钥签名，公钥验证签名）
 * <br/>这个类是<i>线程安全的</i>
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSAByteDigester implements ByteDigester {
	private Signature privateSignature;
	private Signature publicSignature;
	private RSAKey key = new RSAKey();
	private boolean initialized = false;
	private String algorithm = RSAUtils.DEFAULT_SIGNATURE_ALGORITHM;

	public RSAByteDigester() {
	}

	public RSAByteDigester(RSAKey key) {
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
					this.publicSignature = Signature.getInstance(algorithm);
					this.publicSignature.initVerify(publicKey);
				}

				if (Objects.nonNull(key.getPrivateKey())) {
					PrivateKey privateKey = RSAUtils.getPrivateKey(key.getPrivateKey());
					this.privateSignature = Signature.getInstance(algorithm);
					this.privateSignature.initSign(privateKey);
				}
			} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
				throw new EncryptionInitializationException(e);
			}
			initialized = true;
		}
	}

	@Override
	public byte[] digest(byte[] message) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(key.getPrivateKey())) {
			throw new EncryptionInitializationException("未设置私钥");
		}
		if (!initialized) {
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
		if (Objects.isNull(key.getPublicKey())) {
			throw new EncryptionInitializationException("未设置公钥");
		}
		if (!initialized) {
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
