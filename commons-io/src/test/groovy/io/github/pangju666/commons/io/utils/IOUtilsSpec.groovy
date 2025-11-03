package io.github.pangju666.commons.io.utils


import org.apache.commons.lang3.RandomUtils
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import javax.crypto.spec.IvParameterSpec
import java.nio.file.Files
import java.nio.file.Path

class IOUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	// 测试常量
	static final String TEST_TEXT = "Hello, AES Encryption! 测试数据"
	static final byte[] PASSWORD_16 = "1234567890123456".getBytes()
	static final byte[] PASSWORD_24 = "123456789012345678901234".getBytes()
	static final byte[] PASSWORD_32 = "12345678901234567890123456789012".getBytes()
	static final byte[] IV_16 = "1234567890123456".bytes
	static final byte[] IV_INVALID = new byte[15]

	// 核心测试方法
	def "测试CBC默认IV加密解密流程"() {
		given: "创建测试文件"
		Path input = createTestFile("origin.txt")
		Path encrypted = tempDir.resolve("encrypted.cbc")
		Path decrypted = tempDir.resolve("decrypted.txt")

		when: "执行完整加解密流程"
		IOUtils.encrypt(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16)
		IOUtils.decrypt(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16)

		then: "验证解密结果"
		Files.readAllBytes(decrypted) == TEST_TEXT.bytes
	}

	@Unroll
	def "测试不同密钥长度加密(#password.length字节)"() {
		given: "准备测试文件"
		Path input = createTestFile("keylen_${password.length}.txt")
		def iv = RandomUtils.secure().randomBytes(16)

		when: "执行加密操作"
		IOUtils.encrypt(Files.newInputStream(input), Files.newOutputStream(tempDir.resolve("encrypted_${password.length}")), password, iv)

		then: "无异常抛出"
		noExceptionThrown()

		where:
		password << [PASSWORD_16, PASSWORD_24, PASSWORD_32]
	}

	def "测试非法密钥长度异常"() {
		when: "使用非法长度密钥"
		IOUtils.encrypt(new ByteArrayInputStream(TEST_TEXT.bytes), new ByteArrayOutputStream(), "invalid".getBytes())

		then: "抛出参数异常"
		thrown(IllegalArgumentException)
	}

	def "测试自定义IV的CBC加解密"() {
		given: "准备测试文件"
		Path input = createTestFile("custom_iv.txt")

		when: "使用自定义IV加解密"
		def encrypted = encryptWithCustomIV(input, IV_16)
		def decrypted = decryptWithCustomIV(encrypted, IV_16)

		then: "验证数据完整性"
		decrypted == TEST_TEXT.bytes
	}

	def "测试非法IV长度异常"() {
		when: "使用非法长度IV"
		encryptWithCustomIV(createTestFile("invalid_iv.txt"), IV_INVALID)

		then: "抛出参数异常"
		thrown(IllegalArgumentException)
	}

	def "测试CTR默认IV加解密流程"() {
		given: "创建测试文件"
		Path input = createTestFile("ctr_test.txt")
		Path encrypted = tempDir.resolve("ctr_encrypted")
		Path decrypted = tempDir.resolve("ctr_decrypted.txt")

		when: "执行CTR加解密"
		IOUtils.encryptByCtr(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16)
		IOUtils.decryptByCtr(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16)

		then: "验证结果正确"
		Files.readAllBytes(decrypted) == TEST_TEXT.bytes
	}

	def "测试CTR自定义IV加解密"() {
		given: "准备测试文件"
		Path input = createTestFile("ctr_custom_iv.txt")

		when: "使用自定义IV加解密"
		Path encrypted = tempDir.resolve("ctr_custom_encrypted")
		Path decrypted = tempDir.resolve("ctr_custom_decrypted.txt")
		IOUtils.encryptByCtr(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16, IV_16)
		IOUtils.decryptByCtr(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16, IV_16)

		then: "验证数据完整性"
		Files.readAllBytes(decrypted) == TEST_TEXT.bytes
	}

	def "测试通用加解密接口参数校验"() {
		given: "准备参数"
		def iv = new IvParameterSpec(IV_16)
		def transformation = "AES/CBC/PKCS5Padding"

		when: "使用不匹配参数解密"
		Path encrypted = encryptWithGenericParams(iv, transformation)
		IOUtils.decrypt(Files.newInputStream(encrypted),
			Files.newOutputStream(tempDir.resolve("wrong_decrypted.txt")),
			PASSWORD_16,
			new IvParameterSpec("a".padRight(15).bytes),
			transformation)

		then: "抛出解密异常"
		thrown(IOException)
	}

	def "测试空数据流处理"() {
		given: "空输入流"
		def emptyData = new byte[0]

		when: "加密并解密空数据"
		def encrypted = new ByteArrayOutputStream().with {
			IOUtils.encrypt(new ByteArrayInputStream(emptyData), it, PASSWORD_16)
			it.toByteArray()
		}
		def decrypted = new ByteArrayOutputStream().with {
			IOUtils.decrypt(new ByteArrayInputStream(encrypted), it, PASSWORD_16)
			it.toByteArray()
		}

		then: "结果为空"
		decrypted == emptyData
	}

	// 辅助方法
	private Path createTestFile(String filename) {
		Path path = tempDir.resolve(filename)
		Files.write(path, TEST_TEXT.bytes)
		return path
	}

	private static byte[] encryptWithCustomIV(Path input, byte[] iv) {
		new ByteArrayOutputStream().with {
			IOUtils.encrypt(Files.newInputStream(input), it, PASSWORD_16, iv)
			it.toByteArray()
		}
	}

	private static byte[] decryptWithCustomIV(byte[] encrypted, byte[] iv) {
		new ByteArrayOutputStream().with {
			IOUtils.decrypt(new ByteArrayInputStream(encrypted), it, PASSWORD_16, iv)
			it.toByteArray()
		}
	}

	private Path encryptWithGenericParams(IvParameterSpec iv, String transformation) {
		Path input = createTestFile("generic_test.txt")
		Path encrypted = tempDir.resolve("generic_encrypted")
		IOUtils.encrypt(Files.newInputStream(input), Files.newOutputStream(encrypted),
			PASSWORD_16, iv, transformation)
		return encrypted
	}

	// 新增测试用例
	def "测试CTR非法密钥长度异常"() {
		when: "使用非法长度密钥"
		IOUtils.encryptByCtr(new ByteArrayInputStream(TEST_TEXT.bytes), new ByteArrayOutputStream(), "invalid".getBytes())

		then: "抛出参数异常"
		thrown(IllegalArgumentException)
	}

	def "测试通用接口空参数异常"() {
		when: "传入空参数"
		IOUtils.encrypt(null, new ByteArrayOutputStream(), PASSWORD_16, new IvParameterSpec(IV_16), "AES/CBC/PKCS5Padding")

		then: "抛出空指针异常"
		thrown(NullPointerException)
	}

	def "测试大文件加解密性能"() {
		given: "生成5MB测试文件"
		Path bigFile = generateLargeFile(5 * 1024 * 1024) // 5MB

		when: "执行加解密并计时"
		def (encryptTime, decryptTime) = measurePerformance(bigFile)

		then: "验证文件完整性"
		Files.exists(tempDir.resolve("big_encrypted"))
		Files.exists(tempDir.resolve("big_decrypted.dat"))

		and: "输出性能数据"
		println "加密耗时: ${encryptTime}ms, 解密耗时: ${decryptTime}ms"
	}

	private Path generateLargeFile(int size) {
		Path path = tempDir.resolve("large_file.dat")
		byte[] data = new byte[size]
		new Random().nextBytes(data)
		Files.write(path, data)
		return path
	}

	private List<Long> measurePerformance(Path input) {
		Path encrypted = tempDir.resolve("big_encrypted")
		Path decrypted = tempDir.resolve("big_decrypted.dat")

		long encryptStart = System.currentTimeMillis()
		IOUtils.encrypt(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16)
		long encryptTime = System.currentTimeMillis() - encryptStart

		long decryptStart = System.currentTimeMillis()
		IOUtils.decrypt(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16)
		long decryptTime = System.currentTimeMillis() - decryptStart

		return [encryptTime, decryptTime]
	}
}
