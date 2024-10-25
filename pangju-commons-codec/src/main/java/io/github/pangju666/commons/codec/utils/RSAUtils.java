package io.github.pangju666.commons.codec.utils;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.commons.CommonUtils;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

/**
 * RSA工具类
 *
 * @author pangju
 * @since 1.0.0
 */
public final class RSAUtils {
	public static final String ALGORITHM = "RSA";
	public static final String DEFAULT_CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
	public static final String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";

	private static KeyPairGenerator DEFAULT_KEY_PAIR_GENERATOR;
	private static KeyFactory DEFAULT_KEY_FACTORY;

	private RSAUtils() {
	}

	public static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
		if (Objects.isNull(DEFAULT_KEY_FACTORY)) {
			synchronized (RSAUtils.class) {
				if (Objects.isNull(DEFAULT_KEY_FACTORY)) {
					DEFAULT_KEY_FACTORY = KeyFactory.getInstance(RSAUtils.ALGORITHM);
				}
			}
		}
		return DEFAULT_KEY_FACTORY;
	}

	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		if (Objects.isNull(DEFAULT_KEY_PAIR_GENERATOR)) {
			synchronized (RSAUtils.class) {
				if (Objects.isNull(DEFAULT_KEY_PAIR_GENERATOR)) {
					DEFAULT_KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance(ALGORITHM);
				}
			}
		}
		return DEFAULT_KEY_PAIR_GENERATOR.generateKeyPair();
	}

	public static KeyPair generateKeyPair(final int keySize) throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
		generator.initialize(keySize);
		return generator.generateKeyPair();
	}

	public static KeyPair generateKeyPair(final int keySize, final SecureRandom secureRandom) throws NoSuchAlgorithmException {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGORITHM);
		generator.initialize(keySize, secureRandom);
		return generator.generateKeyPair();
	}

	public static PrivateKey getPrivateKeyFromBase64(final String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (StringUtils.isBlank(privateKey)) {
			return null;
		}
		return getPrivateKey(Base64.decodeBase64(privateKey));
	}

	public static PrivateKey getPrivateKeyFromHex(final String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (StringUtils.isBlank(privateKey)) {
			return null;
		}
		return getPrivateKey(CommonUtils.fromHexadecimal(privateKey));
	}

	public static PrivateKey getPrivateKey(final byte[] privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (ArrayUtils.isEmpty(privateKey)) {
			return null;
		}
		KeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
		return getKeyFactory().generatePrivate(keySpec);
	}

	public static PublicKey getPublicKeyFromBase64(final String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (StringUtils.isBlank(publicKey)) {
			return null;
		}
		return getPublicKey(Base64.decodeBase64(publicKey));
	}

	public static PublicKey getPublicKeyFromHex(final String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (StringUtils.isBlank(publicKey)) {
			return null;
		}
		return getPublicKey(CommonUtils.fromHexadecimal(publicKey));
	}

	public static PublicKey getPublicKey(final byte[] publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		if (ArrayUtils.isEmpty(publicKey)) {
			return null;
		}
		KeySpec keySpec = new X509EncodedKeySpec(publicKey);
		return getKeyFactory().generatePublic(keySpec);
	}
}