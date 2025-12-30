package io.github.pangju666.commons.crypto.key

import io.github.pangju666.commons.crypto.lang.CryptoConstants
import io.github.pangju666.commons.crypto.utils.KeyPairUtils
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

@Title("RSAKeyPair 单元测试")
class RSAKeyPairSpec extends Specification {

	def "构造器至少需要提供公钥或私钥"() {
		when:
		new RSAKeyPair(null, null)

		then:
		thrown(IllegalArgumentException)
	}

	def "random 默认生成返回完整RSA密钥对"() {
		when:
		def pair = RSAKeyPair.random()

		then:
		pair.publicKey instanceof RSAPublicKey
		pair.privateKey instanceof RSAPrivateKey
	}

	@Unroll
	def "random 指定长度生成: keySize=#keySize"() {
		when:
		def pair = RSAKeyPair.random(keySize)

		then:
		pair.publicKey.modulus.bitLength() == keySize
		pair.privateKey.modulus.bitLength() == keySize

		where:
		keySize << [1024, 2048, 4096]
	}

	def "fromKeyPair 成功转换RSA KeyPair"() {
		given:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)

		when:
		def pair = RSAKeyPair.fromKeyPair(kp)

		then:
		pair.publicKey instanceof RSAPublicKey
		pair.privateKey instanceof RSAPrivateKey
	}

	def "fromKeyPair 非RSA密钥对抛出异常"() {
		given:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.DSA_ALGORITHM, 1024)

		when:
		RSAKeyPair.fromKeyPair(kp)

		then:
		thrown(IllegalArgumentException)
	}

	def "fromBytes 仅提供公钥字节可成功解析"() {
		given:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		byte[] pubBytes = ((PublicKey) kp.public).encoded

		when:
		def pair = RSAKeyPair.fromBytes(pubBytes, null)

		then:
		pair.publicKey instanceof RSAPublicKey
		pair.privateKey == null
	}

	def "fromBytes 仅提供私钥字节可成功解析"() {
		given:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		byte[] priBytes = ((PrivateKey) kp.private).encoded

		when:
		def pair = RSAKeyPair.fromBytes(null, priBytes)

		then:
		pair.publicKey == null
		pair.privateKey instanceof RSAPrivateKey
	}

	def "fromBytes 同时为空抛出异常"() {
		when:
		RSAKeyPair.fromBytes(null, null)

		then:
		thrown(IllegalArgumentException)
	}

	def "fromBase64String 仅提供公钥字符串可成功解析"() {
		given:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		String pubBase64 = Base64.encoder.encodeToString(((PublicKey) kp.public).encoded)

		when:
		def pair = RSAKeyPair.fromBase64String(pubBase64, null)

		then:
		pair.publicKey instanceof RSAPublicKey
		pair.privateKey == null
	}

	def "fromBase64String 仅提供私钥字符串可成功解析"() {
		given:
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		String priBase64 = Base64.encoder.encodeToString(((PrivateKey) kp.private).encoded)

		when:
		def pair = RSAKeyPair.fromBase64String(null, priBase64)

		then:
		pair.publicKey == null
		pair.privateKey instanceof RSAPrivateKey
	}

	def "fromBase64String 同时为空抛出异常"() {
		when:
		RSAKeyPair.fromBase64String(null, null)

		then:
		thrown(IllegalArgumentException)
	}
}
