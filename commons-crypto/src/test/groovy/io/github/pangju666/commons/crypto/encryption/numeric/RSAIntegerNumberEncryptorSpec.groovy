package io.github.pangju666.commons.crypto.encryption.numeric


import io.github.pangju666.commons.crypto.key.RSAKey
import io.github.pangju666.commons.crypto.transformation.impl.RSAOEAPWithSHA256Transformation
import io.github.pangju666.commons.crypto.transformation.impl.RSAPKCS1PaddingTransformation
import org.jasypt.exceptions.EncryptionOperationNotPossibleException
import spock.lang.Specification

class RSAIntegerNumberEncryptorSpec extends Specification {
	RSAIntegerNumberEncryptor encryptor
	RSAKey keyPair

	def setup() {
		keyPair = RSAKey.random() // 假设RSAKey生成有效密钥对
		encryptor = new RSAIntegerNumberEncryptor()
		encryptor.setKey(keyPair)
	}

	def "测试默认构造函数初始化"() {
		when: "创建默认加密器"
		def defaultEncryptor = new RSAIntegerNumberEncryptor()

		then: "应使用OAEPWithSHA256方案"
		defaultEncryptor.binaryEncryptor.transformation instanceof RSAOEAPWithSHA256Transformation
	}

	def "测试加密解密基本流程"() {
		given: "测试数值集合"
		def testNumbers = [
			BigInteger.valueOf(123456789),
			BigInteger.valueOf(-987654321),
			BigInteger.ZERO,
			new BigInteger("999999999999999999999999999999")
		]

		expect: "加密后解密应得到原始值"
		testNumbers.each { number ->
			def encrypted = encryptor.encrypt(number)
			def decrypted = encryptor.decrypt(encrypted)
			decrypted == number
		}
	}

	def "测试边界值处理"() {
		given: "特殊边界值集合"
		def edgeCases = [
			BigInteger.ZERO,
			BigInteger.ONE,
			BigInteger.TEN,
			new BigInteger(Long.MAX_VALUE.toString()),
			new BigInteger(Long.MIN_VALUE.toString())
		]

		expect: "加密解密后保持值不变"
		edgeCases.each { number ->
			def encrypted = encryptor.encrypt(number)
			def decrypted = encryptor.decrypt(encrypted)
			decrypted == number
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
		given: "200位大整数"
		def hugeNumber = new BigInteger("9" * 200)

		when: "加密解密流程"
		def encrypted = encryptor.encrypt(hugeNumber)
		def decrypted = encryptor.decrypt(encrypted)

		then: "应保持精确相等"
		decrypted == hugeNumber
	}

	def "测试密钥不匹配场景"() {
		given: "使用不同密钥的加密器"
		def anotherEncryptor = new RSAIntegerNumberEncryptor()
		anotherEncryptor.setKey(RSAKey.random()) // 新密钥

		and: "原始加密数据"
		def original = new BigInteger("123456789")
		def encrypted = encryptor.encrypt(original)

		when: "用不同密钥解密"
		anotherEncryptor.decrypt(encrypted)

		then: "应抛出解密失败异常"
		thrown(EncryptionOperationNotPossibleException)
	}

	def "测试未初始化密钥场景"() {
		given: "未设置密钥的加密器"
		def uninitializedEncryptor = new RSAIntegerNumberEncryptor()
		uninitializedEncryptor.setKey(new RSAKey(null, null))

		when: "尝试加密操作"
		uninitializedEncryptor.encrypt(BigInteger.TEN)

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
		def number = new BigInteger("314159265358979323846")

		expect: "相同方案加解密成功"
		transformations.each { transformation ->
			def newEncryptor = new RSAIntegerNumberEncryptor()
			newEncryptor.setTransformation(transformation)
			def encrypted = newEncryptor.encrypt(number)
			newEncryptor.decrypt(encrypted) == number
		}

		and: "不同方案加密结果不兼容"
		def results = transformations.collect {
			def newEncryptor = new RSAIntegerNumberEncryptor()
			newEncryptor.setTransformation(it)
			newEncryptor.encrypt(number)
		}
		results.unique().size() == transformations.size()
	}

	def "测试符号保留能力"() {
		given: "正负数集合"
		def numbers = [
			new BigInteger("123456789"),
			new BigInteger("-987654321")
		]

		expect: "加密解密后符号不变"
		numbers.each { number ->
			def encrypted = encryptor.encrypt(number)
			def decrypted = encryptor.decrypt(encrypted)
			decrypted.signum() == number.signum()
		}
	}

	def "测试非法输入处理"() {
		given: "无效加密数据"
		def invalidEncrypted = new BigInteger("123456")

		when: "尝试解密无效数据"
		encryptor.decrypt(invalidEncrypted)

		then: "应抛出解密异常"
		thrown(EncryptionOperationNotPossibleException)
	}
}
