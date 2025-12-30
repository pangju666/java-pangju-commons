package io.github.pangju666.commons.crypto.utils

import io.github.pangju666.commons.crypto.lang.CryptoConstants
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.security.KeyFactory
import java.security.KeyPair
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@Title("KeyPairUtils 单元测试")
class KeyPairUtilsSpec extends Specification {

	def "getKeyFactory 缓存返回同实例"() {
		when:
		KeyFactory f1 = KeyPairUtils.getKeyFactory(CryptoConstants.RSA_ALGORITHM)
		KeyFactory f2 = KeyPairUtils.getKeyFactory(CryptoConstants.RSA_ALGORITHM)

		then:
		f1.is(f2)
	}

	def "generateKeyPair 默认RSA长度为配置默认值"() {
		when:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM)

		then:
		((RSAPublicKey) kp.public).modulus.bitLength() == CryptoConstants.RSA_DEFAULT_KEY_SIZE
		((RSAPrivateKey) kp.private).modulus.bitLength() == CryptoConstants.RSA_DEFAULT_KEY_SIZE
	}

	@Unroll
	def "generateKeyPair 指定长度: #keySize"() {
		when:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, keySize)

		then:
		((RSAPublicKey) kp.public).modulus.bitLength() == keySize
		((RSAPrivateKey) kp.private).modulus.bitLength() == keySize

		where:
		keySize << [1024, 2048, 4096]
	}

	def "generateKeyPair 指定长度与随机源"() {
		given:
		def random = new SecureRandom(new byte[16])

		when:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024, random)

		then:
		((RSAPublicKey) kp.public).modulus.bitLength() == 1024
		((RSAPrivateKey) kp.private).modulus.bitLength() == 1024
	}

	def "getPrivateKeyFromPKCS8Base64String 空返回null"() {
		expect:
		KeyPairUtils.getPrivateKeyFromPKCS8Base64String(CryptoConstants.RSA_ALGORITHM, null) == null
		KeyPairUtils.getPrivateKeyFromPKCS8Base64String(CryptoConstants.RSA_ALGORITHM, "") == null
	}

	def "getPrivateKeyFromPKCS8Base64String 支持PEM与纯Base64"() {
		given:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		String base64 = Base64.encoder.encodeToString(kp.private.encoded)
		String pem = "-----BEGIN PRIVATE KEY-----\n${base64}\n-----END PRIVATE KEY-----"

		expect:
		KeyPairUtils.getPrivateKeyFromPKCS8Base64String(CryptoConstants.RSA_ALGORITHM, base64) instanceof RSAPrivateKey
		KeyPairUtils.getPrivateKeyFromPKCS8Base64String(CryptoConstants.RSA_ALGORITHM, pem) instanceof RSAPrivateKey
	}

	def "getPrivateKeyFromPKCS8EncodedKey 空字节抛异常"() {
		when:
		KeyPairUtils.getPrivateKeyFromPKCS8EncodedKey(CryptoConstants.RSA_ALGORITHM, new byte[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "getPublicKeyFromX509Base64String 空返回null"() {
		expect:
		KeyPairUtils.getPublicKeyFromX509Base64String(CryptoConstants.RSA_ALGORITHM, null) == null
		KeyPairUtils.getPublicKeyFromX509Base64String(CryptoConstants.RSA_ALGORITHM, "") == null
	}

	def "getPublicKeyFromX509Base64String 支持PEM与纯Base64"() {
		given:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		String base64 = Base64.encoder.encodeToString(kp.public.encoded)
		String pem = "-----BEGIN PUBLIC KEY-----\n${base64}\n-----END PUBLIC KEY-----"

		expect:
		KeyPairUtils.getPublicKeyFromX509Base64String(CryptoConstants.RSA_ALGORITHM, base64) instanceof RSAPublicKey
		KeyPairUtils.getPublicKeyFromX509Base64String(CryptoConstants.RSA_ALGORITHM, pem) instanceof RSAPublicKey
	}

	def "getPublicKeyFromX509EncodedKey 空字节抛异常"() {
		when:
		KeyPairUtils.getPublicKeyFromX509EncodedKey(CryptoConstants.RSA_ALGORITHM, new byte[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "getKeyFactory 未知算法抛异常"() {
		when:
		KeyPairUtils.getKeyFactory("UNKNOWN")

		then:
		thrown(NoSuchAlgorithmException)
	}

	def "generateKeyPair 未知算法抛异常"() {
		when:
		KeyPairUtils.generateKeyPair("UNKNOWN")

		then:
		thrown(NoSuchAlgorithmException)
	}

	def "generateKeyPair 空算法抛异常"() {
		when:
		KeyPairUtils.generateKeyPair("")

		then:
		thrown(IllegalArgumentException)
	}
}
