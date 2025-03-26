package io.github.pangju666.commons.crypto.encryption.binary

import io.github.pangju666.commons.crypto.key.RSAKey
import io.github.pangju666.commons.crypto.lang.CryptoConstants
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation
import io.github.pangju666.commons.crypto.utils.KeyPairUtils
import org.jasypt.exceptions.AlreadyInitializedException
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import spock.lang.Specification
import spock.lang.Unroll

import java.security.spec.RSAPrivateKeySpec
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RSABinaryEncryptorSpec extends Specification {
	// 测试数据配置
	def "测试构造方法和基本功能"() {
		given: "使用默认参数的加密器"
		def encryptor = new RSABinaryEncryptor()

		expect: "默认参数验证"
		encryptor.key != null
		encryptor.transformation instanceof RSAOEAPWithSHA256Transformation
	}

	@Unroll
	def "测试不同密钥长度构造方法 - 密钥长度: #keySize"() {
		when: "使用指定密钥长度创建加密器"
		def encryptor = new RSABinaryEncryptor(keySize)

		then: "验证密钥长度"
		KeyPairUtils.getKeyFactory(CryptoConstants.RSA_ALGORITHM)
			.getKeySpec(encryptor.key.privateKey(), RSAPrivateKeySpec.class).modulus.bitLength() == keySize

		where:
		keySize << [1024, 2048, 4096]
	}

	def "测试设置加密方案在初始化后抛出异常"() {
		given: "已初始化的加密器"
		def encryptor = new RSABinaryEncryptor()
		encryptor.initialize()

		when: "尝试修改加密方案"
		encryptor.setTransformation(new RSAPKCS1PaddingTransformation())

		then: "应抛出AlreadyInitializedException"
		thrown(AlreadyInitializedException)
	}

	def "测试加密解密完整流程"() {
		given: "初始化加密器"
		def encryptor = new RSABinaryEncryptor(2048, new RSAPKCS1PaddingTransformation())
		def originalData = "Hello, RSA加密测试!".bytes

		when: "加密然后解密"
		def encrypted = encryptor.encrypt(originalData)
		def decrypted = encryptor.decrypt(encrypted)

		then: "验证解密结果"
		decrypted == originalData
	}

	@Unroll
	def "测试不同数据长度加密解密 - 数据长度: #length"() {
		given: "准备测试数据"
		def encryptor = new RSABinaryEncryptor()
		def data = new byte[length]
		new Random().nextBytes(data)

		when: "执行加密解密"
		def encrypted = encryptor.encrypt(data)
		def decrypted = encryptor.decrypt(encrypted)

		then: "验证结果"
		decrypted == data

		where:
		length << [0, 1, 245, 500] // 245是2048位密钥PKCS1填充的单块最大长度
	}

	def "测试未设置公钥时加密抛出异常"() {
		given: "创建只有私钥的加密器"
		def key = RSAKey.random(2048)
		def encryptor = new RSABinaryEncryptor(key)
		encryptor.setKey(new RSAKey(null, key.privateKey()))

		when: "尝试加密"
		encryptor.encrypt("test".bytes)

		then: "应抛出异常"
		thrown(EncryptionOperationNotPossibleException)
	}

	def "测试延迟初始化功能"() {
		given: "未初始化的加密器"
		def encryptor = new RSABinaryEncryptor()

		when: "首次加密操作"
		def result = encryptor.encrypt("test".bytes)

		then: "自动初始化并完成加密"
		result != null
		encryptor.initialized
	}

	@Unroll
	def "测试不同填充方案兼容性 - 方案: #transformation.class.simpleName"() {
		given: "使用不同填充方案"
		def encryptor = new RSABinaryEncryptor(transformation)
		def data = new byte[dataSize]
		new Random().nextBytes(data)

		when: "加密解密流程"
		def encrypted = encryptor.encrypt(data)
		def decrypted = encryptor.decrypt(encrypted)

		then: "验证结果一致性"
		decrypted == data

		where:
		transformation                        | dataSize
		new RSAPKCS1PaddingTransformation()   | 200
		new RSAOEAPWithSHA256Transformation() | 180
	}

	def "测试多线程安全初始化"() {
		given: "未初始化的加密器"
		def encryptor = new RSABinaryEncryptor()
		def threadCount = 10
		def executor = Executors.newFixedThreadPool(threadCount)

		when: "多线程同时触发初始化"
		def futures = (1..threadCount).collect {
			executor.submit({ encryptor.encrypt("test".bytes) } as Callable<byte[]>)
		}
		executor.shutdown()
		executor.awaitTermination(10, TimeUnit.SECONDS)
		def results = futures*.get()

		then: "所有线程都应成功完成"
		results.every { it != null && it.length > 0 }

		cleanup:
		executor?.shutdownNow()
	}

	def "测试空数据处理"() {
		given: "初始化加密器"
		def encryptor = new RSABinaryEncryptor()

		when: "加密空数据"
		def encrypted = encryptor.encrypt(new byte[0])

		then: "返回空数组"
		encrypted.size() == 0
	}

	def "测试超大文件加密(10MB)"() {
		given: "准备10MB数据"
		def encryptor = new RSABinaryEncryptor()
		def largeData = new byte[10 * 1024 * 1024]
		new Random().nextBytes(largeData)

		when: "执行加密解密"
		def encrypted = encryptor.encrypt(largeData)
		def decrypted = encryptor.decrypt(encrypted)

		then: "验证数据完整性"
		decrypted == largeData
	}
}