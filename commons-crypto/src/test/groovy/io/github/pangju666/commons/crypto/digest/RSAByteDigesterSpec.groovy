package io.github.pangju666.commons.crypto.digest

import io.github.pangju666.commons.crypto.enums.RSASignatureAlgorithm
import io.github.pangju666.commons.crypto.key.RSAKeyPair
import io.github.pangju666.commons.crypto.lang.CryptoConstants
import io.github.pangju666.commons.crypto.utils.KeyPairUtils
import org.jasypt.exceptions.AlreadyInitializedException
import org.jasypt.exceptions.EncryptionInitializationException
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.security.KeyPair

@Title("RSAByteDigester 单元测试")
class RSAByteDigesterSpec extends Specification {

	def "默认算法构造与签名/验签"() {
		given:
		def digester = new RSAByteDigester()
		def kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.setKeyPair(pair)
		byte[] message = "hello".bytes

		when:
		byte[] sig = digester.digest(message)

		then:
		sig != null && sig.length > 0
		digester.matches(message, sig)
	}

	def "指定算法构造为非空"() {
		expect:
		new RSAByteDigester(RSASignatureAlgorithm.SHA256_WITH_RSA) != null
	}

	def "指定算法构造传入null抛出异常"() {
		when:
		new RSAByteDigester(null)

		then:
		thrown(NullPointerException)
	}

	@Unroll
	def "digest 空输入返回空数组: #label"() {
		given:
		def digester = new RSAByteDigester()

		expect:
		digester.digest(input) != null && digester.digest(input).length == 0

		where:
		label  | input
		"null" | null
		"空"   | new byte[0]
	}

	def "digest 未设置私钥抛出初始化异常"() {
		given:
		def digester = new RSAByteDigester()

		when:
		digester.digest("test".bytes)

		then:
		thrown(EncryptionInitializationException)
	}

	def "matches 消息为空时仅当签名也为空返回true"() {
		given:
		def digester = new RSAByteDigester()

		expect:
		digester.matches(null, null)
		!digester.matches(null, "a".bytes)
		!digester.matches(new byte[0], "a".bytes)
		digester.matches(new byte[0], new byte[0])
	}

	def "matches 未设置公钥抛出初始化异常"() {
		given:
		def digester = new RSAByteDigester()

		when:
		digester.matches("msg".bytes, "sig".bytes)

		then:
		thrown(EncryptionInitializationException)
	}

	def "matches 验证真实签名成功与篡改失败"() {
		given:
		def digester = new RSAByteDigester()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.setKeyPair(pair)
		byte[] msg = "message".bytes
		byte[] sig = digester.digest(msg)

		expect:
		digester.matches(msg, sig)
		!digester.matches("message2".bytes, sig)
		!digester.matches(msg, sig.collect { it ^ 0x01 as byte } as byte[])
	}

	def "initialize 后不可再设置密钥"() {
		given:
		def digester = new RSAByteDigester()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.initialize()

		when:
		digester.setKeyPair(pair)

		then:
		thrown(AlreadyInitializedException)
	}

	def "digest/匹配调用触发惰性初始化，之后再设置密钥抛异常"() {
		given:
		def digester = new RSAByteDigester()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.setKeyPair(pair)
		byte[] msg = "lazy-init".bytes

		when:
		byte[] sig = digester.digest(msg)
		digester.setKeyPair(pair)

		then:
		sig.length > 0
		thrown(AlreadyInitializedException)
	}
}
