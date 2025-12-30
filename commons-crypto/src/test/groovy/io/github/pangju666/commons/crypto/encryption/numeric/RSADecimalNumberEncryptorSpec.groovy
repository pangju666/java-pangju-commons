package io.github.pangju666.commons.crypto.encryption.numeric

import io.github.pangju666.commons.crypto.key.RSAKeyPair
import io.github.pangju666.commons.crypto.lang.CryptoConstants
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation
import io.github.pangju666.commons.crypto.utils.KeyPairUtils
import org.jasypt.exceptions.AlreadyInitializedException
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import spock.lang.Specification
import spock.lang.Title

import java.security.KeyPair
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@Title("RSADecimalNumberEncryptor 单元测试")
class RSADecimalNumberEncryptorSpec extends Specification {

	def "默认配置加密/解密成功(保留scale，含正负与不同scale)"() {
		given:
		def encryptor = new RSADecimalNumberEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def nums = [
			new BigDecimal("0"),
			new BigDecimal("1"),
			new BigDecimal("-1"),
			new BigDecimal("1234567890.123456"),
			new BigDecimal("-0.000001"),
			randomBigDecimal(1024, 20),
			randomBigDecimal(1024, 0),
			randomBigDecimal(2048, 50).negate()
		]

		expect:
		nums.every { encryptor.decrypt(encryptor.encrypt(it)) == it }
	}

	def "指定方案构造为非空"() {
		expect:
		new RSADecimalNumberEncryptor(new RSAOEAPWithSHA256Transformation()) != null
		new RSADecimalNumberEncryptor(new RSAPKCS1PaddingTransformation()) != null
	}

	def "指定方案构造传入null抛出异常"() {
		when:
		new RSADecimalNumberEncryptor(null)

		then:
		thrown(NullPointerException)
	}

	def "encrypt null 返回 null"() {
		expect:
		new RSADecimalNumberEncryptor().encrypt(null) == null
	}

	def "decrypt null 返回 null"() {
		expect:
		new RSADecimalNumberEncryptor().decrypt(null) == null
	}

	def "encrypt 未设置公钥抛出异常"() {
		given:
		def encryptor = new RSADecimalNumberEncryptor()

		when:
		encryptor.encrypt(new BigDecimal("1.23"))

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	def "decrypt 未设置私钥抛出异常"() {
		given:
		def encryptor = new RSADecimalNumberEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		encryptor.setPublicKey((RSAPublicKey) kp.public)

		when:
		encryptor.decrypt(new BigDecimal("1.23"))

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	def "initialize 后不可再设置密钥"() {
		given:
		def encryptor = new RSADecimalNumberEncryptor()
		encryptor.initialize()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)

		when:
		encryptor.setKeyPair(pair)

		then:
		thrown(AlreadyInitializedException)
	}

	def "加密触发惰性初始化，之后再设置密钥抛异常"() {
		given:
		def encryptor = new RSADecimalNumberEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)

		when:
		def v = encryptor.encrypt(new BigDecimal("12.34"))
		encryptor.setKeyPair(pair)

		then:
		v != null
		thrown(AlreadyInitializedException)
	}

	def "分段加密与解密保持一致（OAEP）"() {
		given:
		def encryptor = new RSADecimalNumberEncryptor(new RSAOEAPWithSHA256Transformation())
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def n = randomBigDecimal(5000, 30)

		expect:
		encryptor.decrypt(encryptor.encrypt(n)) == n
	}

	def "分段加密与解密保持一致（PKCS1）"() {
		given:
		def encryptor = new RSADecimalNumberEncryptor(new RSAPKCS1PaddingTransformation())
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def n = randomBigDecimal(5000, 30)

		expect:
		encryptor.decrypt(encryptor.encrypt(n)) == n
	}

	def "跨密钥解密失败抛异常"() {
		given:
		KeyPair kp1 = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		KeyPair kp2 = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair1 = RSAKeyPair.fromKeyPair(kp1)
		def pair2 = RSAKeyPair.fromKeyPair(kp2)

		def encryptor1 = new RSADecimalNumberEncryptor()
		encryptor1.setKeyPair(pair1)
		def encrypted = encryptor1.encrypt(new BigDecimal("2024.12"))

		def decryptor2 = new RSADecimalNumberEncryptor()
		decryptor2.setPrivateKey((RSAPrivateKey) kp2.private)

		when:
		decryptor2.decrypt(encrypted)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	private static BigDecimal randomBigDecimal(int unscaledBytes, int scale) {
		def b = new byte[unscaledBytes]
		new SecureRandom().nextBytes(b)
		def unscaled = new BigInteger(1, b)
		return new BigDecimal(unscaled, scale)
	}
}
