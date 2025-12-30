package io.github.pangju666.commons.io.utils

import org.apache.commons.io.input.UnsynchronizedBufferedInputStream
import org.apache.commons.lang3.RandomUtils
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

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
		IOUtils.encrypt(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16, IV_16)
		IOUtils.decrypt(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16, IV_16)

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
		IOUtils.encrypt(new ByteArrayInputStream(TEST_TEXT.bytes), new ByteArrayOutputStream(), "invalid".getBytes(), IV_16)

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
		IOUtils.encryptByCtr(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16, IV_16)
		IOUtils.decryptByCtr(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16, IV_16)

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


	def "测试空数据流处理"() {
		given: "空输入流"
		def emptyData = new byte[0]

		when: "加密并解密空数据"
		def encrypted = new ByteArrayOutputStream().with {
			IOUtils.encrypt(new ByteArrayInputStream(emptyData), it, PASSWORD_16, IV_16)
			it.toByteArray()
		}
		def decrypted = new ByteArrayOutputStream().with {
			IOUtils.decrypt(new ByteArrayInputStream(encrypted), it, PASSWORD_16, IV_16)
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


	// 新增测试用例
	def "测试CTR非法密钥长度异常"() {
		when: "使用非法长度密钥"
		IOUtils.encryptByCtr(new ByteArrayInputStream(TEST_TEXT.bytes), new ByteArrayOutputStream(), "invalid".getBytes(), IV_16)

		then: "抛出参数异常"
		thrown(IllegalArgumentException)
	}

	def "测试通用接口空参数异常"() {
		when: "传入空参数"
		IOUtils.encrypt(null as InputStream, new ByteArrayOutputStream(), PASSWORD_16, IV_16)

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
		IOUtils.encrypt(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16, IV_16)
		long encryptTime = System.currentTimeMillis() - encryptStart

		long decryptStart = System.currentTimeMillis()
		IOUtils.decrypt(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16, IV_16)
		long decryptTime = System.currentTimeMillis() - decryptStart

		return [encryptTime, decryptTime]
	}

	@Unroll
	def "getBufferSize 返回预期大小: total=#total -> buffer=#expected"() {
		expect:
		IOUtils.getBufferSize(total) == expected

		where:
		total                   | expected
		0                       | 4 * 1024
		200 * 1024              | 4 * 1024
		300 * 1024              | 8 * 1024
		2 * 1024 * 1024         | 32 * 1024
		50 * 1024 * 1024        | 64 * 1024
		200 * 1024 * 1024       | 128 * 1024
		2L * 1024 * 1024 * 1024 | 256 * 1024
	}

	def "getBufferSize 负数抛异常"() {
		when:
		IOUtils.getBufferSize(-1)

		then:
		thrown(IllegalArgumentException)
	}

	def "unsynchronizedBuffer 默认与自定义缓冲区"() {
		given:
		def data = "buffer-test".bytes
		def inputStream = new ByteArrayInputStream(data)

		when:
		def bufDefault = IOUtils.unsynchronizedBuffer(inputStream)
		def readDefault = new byte[data.length]
		bufDefault.read(readDefault)

		then:
		readDefault == data

		when:
		def in2 = new ByteArrayInputStream(data)
		def bufCustom = IOUtils.unsynchronizedBuffer(in2, 8192)
		def readCustom = new byte[data.length]
		bufCustom.read(readCustom)

		then:
		readCustom == data
	}

	def "unsynchronizedBuffer 已是目标类型返回同实例"() {
		given:
		def inputStream = new ByteArrayInputStream("x".bytes)
		def wrapped = new UnsynchronizedBufferedInputStream.Builder()
			.setBufferSize(1024)
			.setInputStream(inputStream)
			.get()

		expect:
		IOUtils.unsynchronizedBuffer(wrapped, 2048).is(wrapped)
	}

	def "unsynchronizedBuffer 非法缓冲区抛异常"() {
		when:
		IOUtils.unsynchronizedBuffer(new ByteArrayInputStream("x".bytes), 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "toUnsynchronizedByteArrayInputStream 空与非空"() {
		expect:
		IOUtils.toUnsynchronizedByteArrayInputStream(null).readAllBytes().length == 0
		IOUtils.toUnsynchronizedByteArrayInputStream("abc".bytes).readAllBytes() == "abc".bytes
	}

	def "toUnsynchronizedByteArrayOutputStream 创建并写入"() {
		when:
		def out = IOUtils.toUnsynchronizedByteArrayOutputStream(1024)
		out.write("abc".bytes)

		then:
		out.toByteArray() == "abc".bytes
	}

	def "toUnsynchronizedByteArrayOutputStream 非法缓冲区抛异常"() {
		when:
		IOUtils.toUnsynchronizedByteArrayOutputStream(0)

		then:
		thrown(IllegalArgumentException)
	}

	def "toUnsynchronizedByteArrayOutputStream(InputStream) 读取正确"() {
		expect:
		IOUtils.toUnsynchronizedByteArrayOutputStream(new ByteArrayInputStream("xyz".bytes)).toByteArray() == "xyz".bytes
	}

	def "CBC 自定义缓冲区大小加解密"() {
		given:
		def iv = RandomUtils.secure().randomBytes(16)
		Path input = createTestFile("cbc_custom_buf.txt")
		Path encrypted = tempDir.resolve("cbc_custom_buf.enc")
		Path decrypted = tempDir.resolve("cbc_custom_buf.dec")

		when:
		IOUtils.encrypt(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16, iv, 8192)
		IOUtils.decrypt(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16, iv, 8192)

		then:
		Files.readAllBytes(decrypted) == TEST_TEXT.bytes
	}

	def "CTR 自定义缓冲区大小加解密"() {
		given:
		def iv = RandomUtils.secure().randomBytes(16)
		Path input = createTestFile("ctr_custom_buf.txt")
		Path encrypted = tempDir.resolve("ctr_custom_buf.enc")
		Path decrypted = tempDir.resolve("ctr_custom_buf.dec")

		when:
		IOUtils.encryptByCtr(Files.newInputStream(input), Files.newOutputStream(encrypted), PASSWORD_16, iv, 4096)
		IOUtils.decryptByCtr(Files.newInputStream(encrypted), Files.newOutputStream(decrypted), PASSWORD_16, iv, 4096)

		then:
		Files.readAllBytes(decrypted) == TEST_TEXT.bytes
	}

	def "encryptByCtr 空输入流抛异常"() {
		when:
		IOUtils.encryptByCtr(null, new ByteArrayOutputStream(), PASSWORD_16, IV_16)

		then:
		thrown(NullPointerException)
	}
}
