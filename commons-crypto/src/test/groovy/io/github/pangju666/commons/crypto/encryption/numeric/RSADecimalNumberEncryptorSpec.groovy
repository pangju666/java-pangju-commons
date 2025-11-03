package io.github.pangju666.commons.crypto.encryption.numeric


import io.github.pangju666.commons.crypto.key.RSAKey
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import spock.lang.Specification

import java.math.MathContext

class RSADecimalNumberEncryptorSpec extends Specification {
	RSADecimalNumberEncryptor encryptor
	RSAKey keyPair

	def setup() {
		keyPair = RSAKey.random() // 假设RSAKey生成有效密钥对
		encryptor = new RSADecimalNumberEncryptor()
		encryptor.setKey(keyPair)
	}

	def "测试默认构造函数初始化"() {
		when: "创建默认加密器"
		def defaultEncryptor = new RSADecimalNumberEncryptor()

		then: "应使用OAEPWithSHA256方案"
		defaultEncryptor.binaryEncryptor.transformation instanceof RSAOEAPWithSHA256Transformation
	}

	def "测试自定义加密方案"() {
		given: "准备PKCS1填充方案"
		def transformation = new RSAPKCS1PaddingTransformation()

		when: "设置加密方案"
		encryptor.setTransformation(transformation)

		then: "底层加密器应使用新方案"
		encryptor.binaryEncryptor.transformation == transformation
	}

	def "测试基本加密解密流程"() {
		given: "测试不同精度的数值"
		def testNumbers = [
			new BigDecimal("123456789.123456789"),
			new BigDecimal("-987654.321"),
			new BigDecimal("0.000000001"),
			new BigDecimal("99999999999999999999.9999999999")
		]

		when: "加密后解密"
		def results = testNumbers.collect { number ->
			def encrypted = encryptor.encrypt(number)
			encryptor.decrypt(encrypted)
		}

		then: "解密结果应与原始值完全一致"
		results.eachWithIndex { result, i ->
			assert result.compareTo(testNumbers[i]) == 0
		}
	}

	def "测试边界值处理"() {
		given: "特殊边界值集合"
		def edgeCases = [
			BigDecimal.ZERO,
			BigDecimal.ONE,
			BigDecimal.TEN,
			new BigDecimal("0.0"),
			new BigDecimal("-0.0"),
			new BigDecimal(Double.MAX_VALUE),
			new BigDecimal(Double.MIN_NORMAL)
		]

		expect: "加密解密后保持精度和值不变"
		edgeCases.each { number ->
			def encrypted = encryptor.encrypt(number)
			def decrypted = encryptor.decrypt(encrypted)
			decrypted == number
		}
	}

	def "测试不同标度处理"() {
		given: "相同数值不同标度"
		def numbers = [
			new BigDecimal("123.45"),
			new BigDecimal("123.450"),
			new BigDecimal("12345E-2"),
			new BigDecimal("1.2345E+2")
		]

		when: "加密解密处理"
		def results = numbers.collect { number ->
			encryptor.decrypt(encryptor.encrypt(number))
		}

		then: "标度应保持原始值"
		results.eachWithIndex { result, i ->
			assert result.scale() == numbers[i].scale()
			assert result == numbers[i]
		}
	}

	def "测试空值处理"() {
		when: "加密null值"
		def encryptedNull = encryptor.encrypt(null)

		then: "应返回null"
		encryptedNull == null

		when: "解密null值"
		def decryptedNull = encryptor.decrypt(null)

		then: "应返回null"
		decryptedNull == null
	}

	def "测试超大数值处理"() {
		given: "超过Long.MAX_VALUE的数值"
		def hugeNumber = new BigDecimal("9" * 200 + ".9999999999")

		when: "加密解密流程"
		def encrypted = encryptor.encrypt(hugeNumber)
		def decrypted = encryptor.decrypt(encrypted)

		then: "应保持精确相等"
		decrypted == hugeNumber
	}

	def "测试密钥不匹配场景"() {
		given: "使用不同密钥的加密器"
		def anotherEncryptor = new RSADecimalNumberEncryptor()
		anotherEncryptor.setKey(RSAKey.random()) // 新密钥

		and: "原始加密数据"
		def original = new BigDecimal("123.456")
		def encrypted = encryptor.encrypt(original)

		when: "用不同密钥解密"
		anotherEncryptor.decrypt(encrypted)

		then: "应抛出解密失败异常"
		thrown(EncryptionOperationNotPossibleException)
	}

	def "测试未初始化密钥场景"() {
		given: "未设置密钥的加密器"
		def uninitializedEncryptor = new RSADecimalNumberEncryptor()
		uninitializedEncryptor.setKey(new RSAKey(null, null))

		when: "尝试加密操作"
		uninitializedEncryptor.encrypt(new BigDecimal("123.45"))

		then: "应抛出初始化异常"
		thrown(EncryptionOperationNotPossibleException)
	}

	def "测试加密方案兼容性"() {
		given: "不同加密方案配置"
		def transformations = [
			new RSAPKCS1PaddingTransformation(),
			new RSAOEAPWithSHA256Transformation()
		]

		and: "测试数据"
		def number = new BigDecimal("3.14159265358979323846")

		expect: "相同方案加解密成功"
		transformations.each { transformation ->
			def newEncryptor = new RSADecimalNumberEncryptor()
			newEncryptor.setTransformation(transformation)
			def encrypted = newEncryptor.encrypt(number)
			newEncryptor.decrypt(encrypted) == number
		}

		and: "不同方案加密结果不兼容"
		def results = transformations.collect {
			def newEncryptor = new RSADecimalNumberEncryptor()
			newEncryptor.setTransformation(it)
			newEncryptor.encrypt(number)
		}
		results.unique().size() == transformations.size()
	}

	def "测试精度保持能力"() {
		given: "高精度数值"
		def pi = new BigDecimal("3.14159265358979323846264338327950288419716939937510", new MathContext(50))

		when: "加密解密流程"
		def encrypted = encryptor.encrypt(pi)
		def decrypted = encryptor.decrypt(encrypted)

		then: "应保持50位精度"
		decrypted.precision() == 50
		decrypted == pi
	}

	def "测试非法输入处理"() {
		when: "使用无效的加密数据"
		def invalidEncrypted = new BigDecimal("123456")
		encryptor.decrypt(invalidEncrypted)

		then: "应抛出解密异常"
		thrown(EncryptionOperationNotPossibleException)
	}
}