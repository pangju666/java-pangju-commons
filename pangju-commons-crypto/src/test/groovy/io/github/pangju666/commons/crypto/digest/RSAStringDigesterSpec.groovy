package io.github.pangju666.commons.crypto.digest

import io.github.pangju666.commons.crypto.key.RSAKey
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.binary.Hex
import org.springframework.security.crypto.encrypt.RsaAlgorithm
import spock.lang.Specification

class RSAStringDigesterSpec extends Specification {
	RSAStringDigester rsaStringDigester
	RSAKey rsaKey

	def setup() {
		rsaKey = RSAKey.random() // 假设RSAKey可以生成密钥对
		rsaStringDigester = new RSAStringDigester()
		rsaStringDigester.setKey(rsaKey)
		rsaStringDigester.setAlgorithm("SHA256withRSA")
	}

	def "测试默认构造函数"() {
		when:
		def digester = new RSAStringDigester()

		then:
		digester.byteDigester != null
	}

	def "测试自定义字节摘要处理器构造函数"() {
		given:
		def byteDigester = new RSAByteDigester()

		when:
		def digester = new RSAStringDigester(byteDigester)

		then:
		digester.byteDigester == byteDigester
	}

	def "测试设置RSA密钥对"() {
		given:
		def newKey = RSAKey.random()

		when:
		rsaStringDigester.setKey(newKey)

		then:
		rsaStringDigester.byteDigester.key == newKey
	}

	def "测试设置签名算法"() {
		given:
		def algorithm = "SHA512withRSA"

		when:
		rsaStringDigester.setAlgorithm(algorithm)

		then:
		rsaStringDigester.byteDigester.algorithm == algorithm
	}

	def "测试Base64签名生成与验证"() {
		given:
		def message = "Hello, RSA!"
		def signature = rsaStringDigester.digest(message)

		expect:
		signature.size() > 0
		Base64.isBase64(signature)
		rsaStringDigester.matches(message, signature)
	}

	def "测试空消息Base64签名"() {
		when:
		def signature = rsaStringDigester.digest("")

		then:
		signature == ""
	}

	def "测试Base64签名不匹配场景"() {
		given:
		def signature = rsaStringDigester.digest("Original Message")

		when:
		def result = rsaStringDigester.matches("Modified Message", signature)

		then:
		!result
	}

	def "测试Hex签名生成与验证"() {
		given:
		def message = "Hello, Hex!"
		def signature = rsaStringDigester.digestToHexString(message)

		expect:
		signature.size() > 0
		Hex.decodeHex(signature)
		rsaStringDigester.matchesFromHexString(message, signature)
	}

	def "测试空消息Hex签名"() {
		when:
		def signature = rsaStringDigester.digestToHexString("")

		then:
		signature == ""
	}

	def "测试Hex签名不匹配场景"() {
		given:
		def signature = rsaStringDigester.digestToHexString("Original Message")

		when:
		def result = rsaStringDigester.matchesFromHexString("Modified Message", signature)

		then:
		!result
	}

	def "测试无效Hex签名验证"() {
		given:
		def invalidHex = "zzz" // 非十六进制字符

		when:
		rsaStringDigester.matchesFromHexString("test", invalidHex)

		then:
		thrown(RuntimeException)
	}

	def "测试空签名验证逻辑"() {
		expect:
		!rsaStringDigester.matches("test", "")
		rsaStringDigester.matches("", "")
		!rsaStringDigester.matchesFromHexString("test", "")
		rsaStringDigester.matchesFromHexString("", "")
	}

	def "测试不同密钥的验证失败"() {
		given:
		RsaAlgorithm
		def newDigester = new RSAStringDigester()
		newDigester.setKey(RSAKey.random()) // 新密钥
		def signature = newDigester.digest("message")

		when:
		def result = rsaStringDigester.matches("message", signature)

		then:
		!result
	}
}
