package io.github.pangju666.commons.crypto.enums;

/**
 * RSA签名算法名称
 * <p>遵循JCA标准的签名算法名称</p>
 *
 * @author pangju666
 * @see <a href="https://docs.oracle.com/en/java/javase/11/docs/specs/security/standard-names.html">JCA 签名算法标准名称</a>
 * @since 1.0.0
 */
public enum RsaSignatureAlgorithm {
	MD2_WITH_RSA("MD2withRSA"),
	MD5_WITH_RSA("MD5withRSA"),
	SHA1_WITH_RSA("SHA1withRSA"),
	SHA224_WITH_RSA("SHA224withRSA"),
	SHA256_WITH_RSA("SHA256withRSA"),
	SHA384_WITH_RSA("SHA384withRSA"),
	SHA512_WITH_RSA("SHA512withRSA"),
	SHA512_224_WITH_RSA("SHA512/224withRSA"),
	SHA512_256_WITH_RSA("SHA512/256withRSA");

	private final String algorithm;

	RsaSignatureAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getAlgorithm() {
		return algorithm;
	}
}
