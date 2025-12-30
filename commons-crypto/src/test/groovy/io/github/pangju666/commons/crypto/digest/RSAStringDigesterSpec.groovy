package io.github.pangju666.commons.crypto.digest

import io.github.pangju666.commons.crypto.enums.RSASignatureAlgorithm
import io.github.pangju666.commons.crypto.key.RSAKeyPair
import io.github.pangju666.commons.crypto.lang.CryptoConstants
import io.github.pangju666.commons.crypto.utils.KeyPairUtils
import org.apache.commons.codec.binary.Base64
import org.jasypt.exceptions.AlreadyInitializedException
import org.jasypt.exceptions.EncryptionInitializationException
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

import java.security.KeyPair

@Title("RSAStringDigester 单元测试")
class RSAStringDigesterSpec extends Specification {

	def "默认算法构造与签名/验签"() {
		given:
		def digester = new RSAStringDigester()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.setKeyPair(pair)
		def msg = "hello"

		when:
		def sig = digester.digest(msg)

		then:
		sig != null && sig.length() > 0
		digester.matches(msg, sig)
	}

	def "指定算法构造为非空"() {
		expect:
		new RSAStringDigester(RSASignatureAlgorithm.SHA256_WITH_RSA) != null
	}

	def "指定算法构造传入null抛出异常"() {
		when:
		new RSAStringDigester(null)

		then:
		thrown(NullPointerException)
	}

	def "使用预配置的ByteDigester构造并签名/验签"() {
		given:
		def byteDigester = new RSAByteDigester()
		def digester = new RSAStringDigester(byteDigester)
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.setKeyPair(pair)

		expect:
		digester.matches("msg", digester.digest("msg"))
	}

	@Unroll
	def "digest 空输入返回空字符串: #label"() {
		given:
		def digester = new RSAStringDigester()

		expect:
		digester.digest(input) != null && digester.digest(input).length() == 0

		where:
		label  | input
		"null" | null
		"空"   | ""
	}

	def "digest 未设置私钥抛出初始化异常"() {
		given:
		def digester = new RSAStringDigester()

		when:
		digester.digest("test")

		then:
		thrown(EncryptionInitializationException)
	}

	def "matches 消息为空时仅当签名也为空返回true"() {
		given:
		def digester = new RSAStringDigester()

		expect:
		digester.matches(null, null)
		!digester.matches(null, "a")
		!digester.matches("", "a")
		digester.matches("", "")
	}

	def "matches 未设置公钥抛出初始化异常"() {
		given:
		def digester = new RSAStringDigester()

		when:
		digester.matches("msg", "c2ln")

		then:
		thrown(EncryptionInitializationException)
	}

	def "matches 验证真实签名成功与篡改失败"() {
		given:
		def digester = new RSAStringDigester()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.setKeyPair(pair)
		def msg = "message"
		def sig = digester.digest(msg)
		byte[] sigBytes = Base64.decodeBase64(sig)
		sigBytes[0] = (sigBytes[0] ^ 0x01) as byte
		def tampered = Base64.encodeBase64String(sigBytes)

		expect:
		digester.matches(msg, sig)
		!digester.matches("message2", sig)
		!digester.matches(msg, tampered)
	}

	def "initialize 后不可再设置密钥"() {
		given:
		def digester = new RSAStringDigester()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.initialize()

		when:
		digester.setKeyPair(pair)

		then:
		thrown(AlreadyInitializedException)
	}

	def "digest/匹配触发惰性初始化，之后再设置密钥抛异常"() {
		given:
		def digester = new RSAStringDigester()
		KeyPair kp = KeyPairUtils.generateKeyPair(CryptoConstants.RSA_ALGORITHM, 1024)
		def pair = RSAKeyPair.fromKeyPair(kp)
		digester.setKeyPair(pair)

		when:
		def sig = digester.digest("lazy-init")
		digester.setKeyPair(pair)

		then:
		sig.length() > 0
		thrown(AlreadyInitializedException)
	}
}
