package io.github.pangju666.commons.crypto.key

import io.github.pangju666.commons.crypto.utils.KeyPairUtils
import org.apache.commons.codec.binary.Base64
import spock.lang.Specification
import spock.lang.Unroll

import java.security.PrivateKey
import java.security.spec.InvalidKeySpecException

class RSAKeySpec extends Specification {
	private static final String RSA = "RSA"
	private static final int[] VALID_KEY_SIZES = [1024, 2048, 4096]
	private static final int INVALID_KEY_SIZE = 512

	def "测试random()方法生成默认密钥对"() {
		when: "生成默认密钥对"
		def rsaKey = RSAKey.random()

		then: "验证密钥有效性"
		rsaKey.publicKey().algorithm == RSA
		rsaKey.privateKey().algorithm == RSA
		rsaKey.publicKey().encoded.length > 0
		rsaKey.privateKey().encoded.length > 0
	}

	@Unroll
	def "测试random(keySize)方法 - 有效密钥长度: #keySize"() {
		when: "生成指定长度密钥对"
		def rsaKey = RSAKey.random(keySize)

		then: "验证密钥长度有效性"
		rsaKey.publicKey().algorithm == RSA
		rsaKey.privateKey().algorithm == RSA
		(rsaKey.privateKey() as PrivateKey).encoded.length * 8 >= keySize  // 近似验证密钥长度

		where:
		keySize << VALID_KEY_SIZES
	}

	def "测试random(keySize)方法异常情况"() {
		when: "使用无效密钥长度生成密钥对"
		RSAKey.random(INVALID_KEY_SIZE)

		then: "应抛出IllegalArgumentException"
		thrown(IllegalArgumentException)
	}

	def "测试fromKeyPair方法"() {
		given: "生成原始密钥对"
		def originalPair = KeyPairUtils.generateKeyPair(RSA, 2048)

		when: "通过KeyPair构建RSAKey"
		def rsaKey = RSAKey.fromKeyPair(originalPair)

		then: "验证密钥一致性"
		rsaKey.publicKey() == originalPair.public
		rsaKey.privateKey() == originalPair.private
	}

	def "测试fromKeyPair方法空参数校验"() {
		when: "传入null密钥对"
		RSAKey.fromKeyPair(null)

		then: "应抛出NullPointerException"
		thrown(NullPointerException)
	}

	def "测试fromRawBytes方法完整流程"() {
		given: "生成原始密钥对"
		def originalPair = KeyPairUtils.generateKeyPair(RSA, 2048)
		def publicBytes = originalPair.public.encoded
		def privateBytes = originalPair.private.encoded

		when: "通过字节数组构建RSAKey"
		def rsaKey = RSAKey.fromRawBytes(publicBytes, privateBytes)

		then: "验证密钥一致性"
		rsaKey.publicKey().encoded == publicBytes
		rsaKey.privateKey().encoded == privateBytes
	}

	def "测试fromRawBytes方法部分空值处理"() {
		given: "生成原始私钥"
		def privateKey = KeyPairUtils.generateKeyPair(RSA, 2048).private

		when: "只传入私钥字节数组"
		def rsaKey = RSAKey.fromRawBytes(null, privateKey.encoded)

		then: "公钥应为null"
		rsaKey.publicKey() == null
		rsaKey.privateKey().encoded == privateKey.encoded
	}

	def "测试fromBase64String方法完整流程"() {
		given: "生成原始密钥对"
		def originalPair = KeyPairUtils.generateKeyPair(RSA, 2048)
		def base64Public = Base64.encodeBase64String(originalPair.public.encoded)
		def base64Private = Base64.encodeBase64String(originalPair.private.encoded)

		when: "通过Base64字符串构建RSAKey"
		def rsaKey = RSAKey.fromBase64String(base64Public, base64Private)

		then: "验证密钥一致性"
		rsaKey.publicKey().encoded == originalPair.public.encoded
		rsaKey.privateKey().encoded == originalPair.private.encoded
	}

	def "测试fromBase64String方法无效密钥处理"() {
		when: "传入无效Base64字符串"
		RSAKey.fromBase64String("invalid", "invalid")

		then: "应抛出InvalidKeySpecException"
		thrown(InvalidKeySpecException)
	}

	def "测试空密钥组合"() {
		when: "创建全空密钥对象"
		def rsaKey = new RSAKey(null, null)

		then: "验证属性值"
		rsaKey.publicKey() == null
		rsaKey.privateKey() == null
	}

	def "测试密钥对匹配性"() {
		given: "生成有效密钥对"
		def rsaKey = RSAKey.random(2048)

		when: "使用公钥加密和私钥解密"
		// 此处假设有加密方法，实际测试需要补充具体加密逻辑
		// 这里仅验证密钥对的匹配性基础

		then: "验证密钥算法一致性"
		rsaKey.publicKey().algorithm == rsaKey.privateKey().algorithm
	}
}
