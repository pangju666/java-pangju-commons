package io.github.pangju666.commons.codec.utils;

import org.apache.commons.codec.binary.Base64;

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
public final class RsaUtils {
	/**
	 * 算法
	 */
	public static final String ALGORITHM = "RSA";
	/**
	 * 默认加解密算法
	 */
	public static final String DEFAULT_CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
	/**
	 * 默认签名算法
	 */
	public static final String DEFAULT_SIGNATURE_ALGORITHM = "SHA256withRSA";

	private static KeyPairGenerator DEFAULT_KEY_PAIR_GENERATOR;
	private static KeyFactory DEFAULT_KEY_FACTORY;

	private RsaUtils() {
	}

	/**
	 * 获取密钥工厂单例对象
	 *
	 * @throws NoSuchAlgorithmException 运行环境不支持RSA算法时
	 * @since 1.0.0
	 */
	public static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
		if (Objects.isNull(DEFAULT_KEY_FACTORY)) {
			synchronized (RsaUtils.class) {
				if (Objects.isNull(DEFAULT_KEY_FACTORY)) {
					DEFAULT_KEY_FACTORY = KeyFactory.getInstance(RsaUtils.ALGORITHM);
				}
			}
		}
		return DEFAULT_KEY_FACTORY;
	}

	/**
	 * 生成RSA公私钥对
	 *
	 * @throws NoSuchAlgorithmException 运行环境不支持RSA算法时
	 * @since 1.0.0
	 */
	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		if (Objects.isNull(DEFAULT_KEY_PAIR_GENERATOR)) {
			synchronized (RsaUtils.class) {
				if (Objects.isNull(DEFAULT_KEY_PAIR_GENERATOR)) {
					DEFAULT_KEY_PAIR_GENERATOR = KeyPairGenerator.getInstance(ALGORITHM);
				}
			}
		}
		return DEFAULT_KEY_PAIR_GENERATOR.generateKeyPair();
	}

	/**
	 * 将私钥字符串转换为私钥对象（{@link PrivateKey}）
	 *
	 * @param privateKey 私钥的字符串（BASE64编码）形式
	 * @throws InvalidKeySpecException  私钥字符串不符合{@link PKCS8EncodedKeySpec PKCS8}编码标准时
	 * @throws NoSuchAlgorithmException 运行环境不支持RSA算法时
	 * @since 1.0.0
	 */
	public static PrivateKey getPrivateKey(String privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		return getPrivateKey(Base64.decodeBase64(privateKey));
	}

	/**
	 * 将私钥字节数组转换为私钥对象（{@link PrivateKey}）
	 *
	 * @param privateKey 私钥的字节数组形式
	 * @throws InvalidKeySpecException  私钥字符串不符合{@link PKCS8EncodedKeySpec PKCS8}编码标准时
	 * @throws NoSuchAlgorithmException 运行环境不支持RSA算法时
	 * @since 1.0.0
	 */
	public static PrivateKey getPrivateKey(byte[] privateKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		KeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
		return getKeyFactory().generatePrivate(keySpec);
	}

	/**
	 * 将公钥字符串转换为公钥对象（{@link PublicKey}）
	 *
	 * @param publicKey 公钥的字符串（BASE64编码）形式
	 * @throws InvalidKeySpecException  私钥字符串不符合{@link X509EncodedKeySpec X509}编码标准时
	 * @throws NoSuchAlgorithmException 运行环境不支持RSA算法时
	 * @since 1.0.0
	 */
	public static PublicKey getPublicKey(String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		return getPublicKey(Base64.decodeBase64(publicKey));
	}

	/**
	 * 将私钥字节数组转换为公钥对象（{@link PublicKey}）
	 *
	 * @param publicKey 公钥的字节数组形式
	 * @throws InvalidKeySpecException  私钥字符串不符合{@link X509EncodedKeySpec X509}编码标准时
	 * @throws NoSuchAlgorithmException 运行环境不支持RSA算法时
	 * @since 1.0.0
	 */
	public static PublicKey getPublicKey(byte[] publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		KeySpec keySpec = new X509EncodedKeySpec(publicKey);
		return getKeyFactory().generatePublic(keySpec);
	}
}
