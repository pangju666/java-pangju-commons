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

@Title("RSAIntegerNumberEncryptor 单元测试")
class RSAIntegerNumberEncryptorSpec extends Specification {

	def "默认配置加密/解密成功(含正负数)"() {
		given:
		def encryptor = new RSAIntegerNumberEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def nums = [
			BigInteger.ZERO,
			BigInteger.ONE,
			BigInteger.valueOf(-1),
			new BigInteger(1, randomBytes(512)),
			new BigInteger(-1, randomBytes(512))
		]

		expect:
		nums.every { encryptor.decrypt(encryptor.encrypt(it)) == it }
	}

	def "指定方案构造为非空"() {
		expect:
		new RSAIntegerNumberEncryptor(new RSAOEAPWithSHA256Transformation()) != null
		new RSAIntegerNumberEncryptor(new RSAPKCS1PaddingTransformation()) != null
	}

	def "指定方案构造传入null抛出异常"() {
		when:
		new RSAIntegerNumberEncryptor(null)

		then:
		thrown(NullPointerException)
	}

	def "encrypt null 返回 null"() {
		expect:
		new RSAIntegerNumberEncryptor().encrypt(null) == null
	}

	def "decrypt null 返回 null"() {
		expect:
		new RSAIntegerNumberEncryptor().decrypt(null) == null
	}

	def "encrypt 未设置公钥抛出异常"() {
		given:
		def encryptor = new RSAIntegerNumberEncryptor()

		when:
		encryptor.encrypt(BigInteger.ONE)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	def "decrypt 未设置私钥抛出异常"() {
		given:
		def encryptor = new RSAIntegerNumberEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		encryptor.setPublicKey((RSAPublicKey) kp.public)

		when:
		encryptor.decrypt(BigInteger.ONE)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	def "initialize 后不可再设置密钥"() {
		given:
		def encryptor = new RSAIntegerNumberEncryptor()
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
		def encryptor = new RSAIntegerNumberEncryptor()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)

		when:
		def v = encryptor.encrypt(BigInteger.valueOf(123))
		encryptor.setKeyPair(pair)

		then:
		v != null
		thrown(AlreadyInitializedException)
	}

	def "分段加密与解密保持一致（OAEP）"() {
		given:
		def encryptor = new RSAIntegerNumberEncryptor(new RSAOEAPWithSHA256Transformation())
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def n = new BigInteger(1, randomBytes(5000))

		expect:
		encryptor.decrypt(encryptor.encrypt(n)) == n
	}

	def "分段加密与解密保持一致（PKCS1）"() {
		given:
		def encryptor = new RSAIntegerNumberEncryptor(new RSAPKCS1PaddingTransformation())
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		encryptor.setKeyPair(pair)
		def n = new BigInteger(1, randomBytes(5000))

		expect:
		encryptor.decrypt(encryptor.encrypt(n)) == n
	}

	def "跨密钥解密失败抛异常"() {
		given:
		KeyPair kp1 = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		KeyPair kp2 = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair1 = RSAKeyPair.fromKeyPair(kp1)
		def pair2 = RSAKeyPair.fromKeyPair(kp2)

		def encryptor1 = new RSAIntegerNumberEncryptor()
		encryptor1.setKeyPair(pair1)
		def encrypted = encryptor1.encrypt(BigInteger.valueOf(2024))

		def decryptor2 = new RSAIntegerNumberEncryptor()
		decryptor2.setPrivateKey((RSAPrivateKey) kp2.private)

		when:
		decryptor2.decrypt(encrypted)

		then:
		thrown(EncryptionOperationNotPossibleException)
	}

	private static byte[] randomBytes(int len) {
		def b = new byte[len]
		new SecureRandom().nextBytes(b)
		return b
	}
}
