package io.github.pangju666.commons.codec.digest;

import io.github.pangju666.commons.codec.key.RSAKey;
import io.github.pangju666.commons.codec.utils.RsaUtils;
import org.apache.commons.lang3.ArrayUtils;
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
	private Signature privateSignature;
	private Signature publicSignature;
	private final RSAKey rsaKey;

	public RSAByteDigester() {
		this.rsaKey = new RSAKey();
	}

	public RSAByteDigester(RSAKey rsaKey) {
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

					this.publicSignature = Signature.getInstance(RsaUtils.DEFAULT_SIGNATURE_ALGORITHM);
					this.publicSignature.initVerify(publicKey);
				}

				if (Objects.nonNull(rsaKey.getPrivateKey())) {
					PrivateKey privateKey = RsaUtils.getPrivateKey(rsaKey.getPrivateKey());
					rsaKey.cleanPrivateKey();

					this.privateSignature = Signature.getInstance(RsaUtils.DEFAULT_SIGNATURE_ALGORITHM);
					this.privateSignature.initSign(privateKey);
				}
			} catch (NoSuchAlgorithmException | InvalidKeyException | InvalidKeySpecException e) {
				throw new EncryptionInitializationException(e);
			}
			rsaKey.initialize();
		}
	}

	@Override
	public byte[] digest(byte[] message) {
		if (ArrayUtils.isEmpty(message)) {
			return ArrayUtils.EMPTY_BYTE_ARRAY;
		}
		if (Objects.isNull(rsaKey.getPrivateKey())) {
			throw new EncryptionInitializationException("未设置私钥");
		}
		if (!rsaKey.isInitialized()) {
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
		if (Objects.isNull(rsaKey.getPublicKey())) {
			throw new EncryptionInitializationException("未设置公钥");
		}
		if (!rsaKey.isInitialized()) {
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
