package io.github.pangju666.commons.crypto.digest

import io.github.pangju666.commons.crypto.key.RSAKey
import org.jasypt.exceptions.AlreadyInitializedException
import org.jasypt.exceptions.EncryptionInitializationException
import spock.lang.Specification
import spock.lang.Unroll

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class RSAByteDigesterSpec extends Specification {
	// 基本功能测试
	def "测试基础签名验证流程"() {
		given: "初始化签名器"
		def digester = new RSAByteDigester()
		def message = "测试数据".bytes

		when: "生成签名并验证"
		def signature = digester.digest(message)
		def isValid = digester.matches(message, signature)

		then: "验证通过"
		isValid
	}

	@Unroll
	def "测试不同密钥长度(#keySize)的签名验证"() {
		given: "使用指定密钥长度的签名器"
		def digester = new RSAByteDigester(keySize)
		def message = new byte[1024]
		new Random().nextBytes(message)

		when: "完整签名验证流程"
		def signature = digester.digest(message)
		def isValid = digester.matches(message, signature)

		then: "验证结果正确"
		isValid

		where:
		keySize << [1024, 2048, 4096]
	}

	// 异常情况测试
	def "测试无私钥时签名抛出异常"() {
		given: "创建只有公钥的签名器"
		def key = RSAKey.random(2048)
		key = new RSAKey(key.publicKey(), null)
		def digester = new RSAByteDigester(key)

		when: "尝试生成签名"
		digester.digest("test".bytes)

		then: "抛出初始化异常"
		thrown(EncryptionInitializationException)
	}

	def "测试无公钥时验证抛出异常"() {
		given: "创建只有私钥的签名器"
		def key = RSAKey.random(2048)
		key = new RSAKey(null, key.privateKey())
		def digester = new RSAByteDigester(key)
		def message = "test".bytes

		when: "尝试验证签名"
		digester.matches(message, new byte[256])

		then: "抛出初始化异常"
		thrown(EncryptionInitializationException)
	}

	// 算法兼容性测试
	@Unroll
	def "测试算法兼容性 - #algorithm"() {
		given: "使用不同算法的签名器"
		def digester = new RSAByteDigester(algorithm)
		def message = new byte[1024]
		new Random().nextBytes(message)

		when: "完整签名验证流程"
		def signature = digester.digest(message)
		def isValid = digester.matches(message, signature)

		then: "验证通过"
		isValid

		where:
		algorithm << [
			"SHA256withRSA",
			"SHA384withRSA",
			"SHA512withRSA"
		]
	}

	// 边界条件测试
	def "测试空消息处理"() {
		given: "初始化签名器"
		def digester = new RSAByteDigester()

		when: "处理空消息"
		def signature = digester.digest(new byte[0])
		def isValid = digester.matches(new byte[0], signature)

		then: "正确处理空数据"
		isValid
	}

	def "测试签名篡改检测"() {
		given: "初始化签名器"
		def digester = new RSAByteDigester()
		def message = "重要数据".bytes

		when: "篡改签名后验证"
		def originalSignature = digester.digest(message)
		def tamperedSignature = Arrays.copyOf(originalSignature, originalSignature.length)
		tamperedSignature[0] = (byte) (tamperedSignature[0] ^ 0xFF)
		def isValid = digester.matches(message, tamperedSignature)

		then: "验证失败"
		!isValid
	}

	// 配置变更测试
	def "测试初始化后修改密钥抛出异常"() {
		given: "已初始化的签名器"
		def digester = new RSAByteDigester()
		digester.initialize()

		when: "尝试修改密钥"
		digester.setKey(RSAKey.random(2048))

		then: "抛出已初始化异常"
		thrown(AlreadyInitializedException)
	}

	def "测试初始化后修改算法抛出异常"() {
		given: "已初始化的签名器"
		def digester = new RSAByteDigester()
		digester.initialize()

		when: "尝试修改算法"
		digester.setAlgorithm("SHA384withRSAandMGF1")

		then: "抛出已初始化异常"
		thrown(AlreadyInitializedException)
	}

	// 多线程测试
	def "测试多线程并发签名"() {
		given: "初始化签名器和线程池"
		def digester = new RSAByteDigester()
		def executor = Executors.newFixedThreadPool(10)
		def messages = (1..100).collect { "Message-$it".bytes }

		when: "并发执行签名操作"
		def futures = messages.collect { message ->
			executor.submit { digester.digest(message) }
		}
		executor.shutdown()
		executor.awaitTermination(10, TimeUnit.SECONDS)
		def signatures = futures*.get()

		then: "所有签名有效"
		messages.eachWithIndex { msg, i ->
			assert digester.matches(msg, signatures[i])
		}

		cleanup:
		executor?.shutdownNow()
	}

	// 特殊场景测试
	def "测试大文件签名(5MB)"() {
		given: "准备5MB数据"
		def digester = new RSAByteDigester()
		def largeData = new byte[5 * 1024 * 1024]
		new Random().nextBytes(largeData)

		when: "签名并验证"
		def signature = digester.digest(largeData)
		def isValid = digester.matches(largeData, signature)

		then: "验证通过"
		isValid
	}

	def "测试重复初始化"() {
		given: "未初始化的签名器"
		def digester = new RSAByteDigester()

		when: "多次调用初始化"
		(1..5).each { digester.initialize() }

		then: "不会抛出异常"
		noExceptionThrown()
	}

	// 算法异常测试
	def "测试无效算法初始化"() {
		when: "使用无效算法创建签名器"
		new RSAByteDigester("InvalidAlgorithm").initialize()

		then: "抛出初始化异常"
		thrown(EncryptionInitializationException)
	}
}
