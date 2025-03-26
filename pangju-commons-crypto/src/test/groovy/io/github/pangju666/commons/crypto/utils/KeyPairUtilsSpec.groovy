package io.github.pangju666.commons.crypto.utils

import org.apache.commons.codec.binary.Base64
import spock.lang.Specification
import spock.lang.Unroll

import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

class KeyPairUtilsSpec extends Specification {
	// 测试算法常量
	private static final String RSA = "RSA"
	private static final int RSA_KEY_SIZE = 2048
	private static final String INVALID_ALGORITHM = "INVALID_ALG"

	def "测试getKeyFactory方法"() {
		when: "首次获取算法工厂"
		def firstFactory = KeyPairUtils.getKeyFactory(RSA)

		then: "工厂实例有效且被缓存"
		firstFactory != null
		KeyPairUtils.KEY_FACTORY_MAP.size() == 1

		when: "再次获取相同算法工厂"
		def cachedFactory = KeyPairUtils.getKeyFactory(RSA)

		then: "应返回缓存实例"
		cachedFactory.is(firstFactory)
	}

	@Unroll
	def "测试generateKeyPair方法 - 算法: #algorithm, 密钥长度: #keySize"() {
		when: "生成密钥对"
		def keyPair = KeyPairUtils.generateKeyPair(algorithm, keySize)

		then: "验证密钥对有效性"
		keyPair?.getPrivate()?.algorithm == algorithm
		keyPair?.getPublic()?.algorithm == algorithm

		where:
		algorithm | keySize
		RSA       | 1024
		RSA       | 2048
		"DSA"     | 1024
	}

	def "测试generateKeyPair方法异常情况"() {
		when: "使用无效算法生成密钥对"
		KeyPairUtils.generateKeyPair(INVALID_ALGORITHM)

		then: "应抛出NoSuchAlgorithmException"
		thrown(NoSuchAlgorithmException)
	}

	def "测试PKCS8私钥编解码流程"() {
		given: "生成RSA密钥对"
		def keyPair = KeyPairUtils.generateKeyPair(RSA, RSA_KEY_SIZE)

		when: "编码私钥为Base64"
		def privateKeyBytes = keyPair.getPrivate().encoded
		def base64Key = Base64.encodeBase64String(privateKeyBytes)

		then: "解析Base64应得到相同私钥"
		def parsedKey = KeyPairUtils.getPrivateKeyFromPKCS8Base64String(RSA, base64Key)
		parsedKey.algorithm == RSA
		parsedKey.encoded == privateKeyBytes
	}

	def "测试X509公钥编解码流程"() {
		given: "生成RSA密钥对"
		def keyPair = KeyPairUtils.generateKeyPair(RSA, RSA_KEY_SIZE)

		when: "编码公钥为Base64"
		def publicKeyBytes = keyPair.getPublic().encoded
		def base64Key = Base64.encodeBase64String(publicKeyBytes)

		then: "解析Base64应得到相同公钥"
		def parsedKey = KeyPairUtils.getPublicKeyFromX509Base64String(RSA, base64Key)
		parsedKey.algorithm == RSA
		parsedKey.encoded == publicKeyBytes
	}

	def "测试空密钥处理"() {
		when: "传入空值"
		def privateKey = KeyPairUtils.getPrivateKeyFromPKCS8Base64String(RSA, null)
		def publicKey = KeyPairUtils.getPublicKeyFromX509Base64String(RSA, "")

		then: "应返回null"
		privateKey == null
		publicKey == null
	}

	def "测试无效密钥规格"() {
		given: "构造无效密钥数据"
		def invalidKeyBytes = "invalid".bytes

		when: "尝试解析无效密钥"
		KeyPairUtils.getPrivateKeyFromPKCS8RawBytes(RSA, invalidKeyBytes)

		then: "应抛出InvalidKeySpecException"
		thrown(InvalidKeySpecException)
	}

	def "测试参数校验"() {
		when: "传入空算法参数"
		KeyPairUtils.getKeyFactory("")

		then: "应抛出IllegalArgumentException"
		thrown(IllegalArgumentException)
	}

	def "测试密钥工厂缓存清除"() {
		given: "获取并缓存一个工厂"
		KeyPairUtils.getKeyFactory(RSA)
		int initialSize = KeyPairUtils.KEY_FACTORY_MAP.size()

		when: "清除缓存"
		KeyPairUtils.KEY_FACTORY_MAP.clear()

		then: "缓存应被清空"
		KeyPairUtils.KEY_FACTORY_MAP.size() == initialSize - 1
	}
}
