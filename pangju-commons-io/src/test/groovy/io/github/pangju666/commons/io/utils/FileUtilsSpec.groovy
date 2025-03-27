package io.github.pangju666.commons.io.utils

import org.apache.commons.io.FileExistsException
import org.apache.commons.lang3.RandomUtils
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path

class FileUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	// 通用测试文件准备
	private File createTestFile(String content = "test") {
		def file = tempDir.resolve("test.txt").toFile()
		Files.writeString(file.toPath(), content)
		return file
	}

	def "测试文件存在性检查"() {
		given: "准备存在和不存在文件"
		def existingFile = createTestFile()
		def nonExistingFile = new File(tempDir.toString(), "ghost.txt")

		expect: "验证存在性检查结果"
		FileUtils.exist(existingFile)
		!FileUtils.exist(nonExistingFile)
		!FileUtils.exist(null)
		FileUtils.notExist(nonExistingFile)
	}

	def "测试安全重命名文件"() {
		given: "创建源文件和目标文件"
		def srcFile = createTestFile()
		def newFile = new File(tempDir.toString(), "renamed.txt")

		when: "执行重命名"
		def result = FileUtils.rename(srcFile, "renamed.txt")

		then: "验证重命名结果"
		result.exists()
		!srcFile.exists()
		result.name == "renamed.txt"
	}

	def "测试重命名已存在文件应抛出异常"() {
		given: "创建同名文件"
		def srcFile = createTestFile()
		def existingFile = new File(tempDir.toString(), "existing.txt")
		existingFile.createNewFile()

		when: "重命名为已存在文件名"
		FileUtils.rename(srcFile, "existing.txt")

		then: "应抛出FileExistsException"
		thrown(FileExistsException)
	}

	def "测试AES/CBC文件加密解密"() {
		given: "准备测试文件和密码"
		def originalFile = createTestFile("secret data")
		def encryptedFile = new File(tempDir.toString(), "encrypted.dat")
		def decryptedFile = new File(tempDir.toString(), "decrypted.txt")
		def password = RandomUtils.secure().randomBytes(16)

		when: "执行加密解密流程"
		FileUtils.encryptFile(originalFile, encryptedFile, password)
		FileUtils.decryptFile(encryptedFile, decryptedFile, password)

		then: "验证解密后内容"
		decryptedFile.text == "secret data"
		encryptedFile.length() > originalFile.length()
	}

	def "测试使用错误密码解密应失败"() {
		given: "准备加密文件"
		def originalFile = createTestFile()
		def password = RandomUtils.secure().randomBytes(16)
		def encryptedFile = new File(tempDir.toString(), "encrypted.dat")
		FileUtils.encryptFile(originalFile, encryptedFile, password)

		when: "使用错误密码解密"
		FileUtils.decryptFile(encryptedFile, new File(tempDir.toString(), "output.txt"), "aaaaaaaaaaaaaaaa".getBytes())

		then: "应抛出解密异常"
		thrown(IOException)
	}

	def "测试强制删除文件"() {
		given: "创建测试文件和只读文件"
		def normalFile = createTestFile()
		def readOnlyFile = tempDir.resolve("readonly.txt").toFile().tap {
			it.createNewFile()
			it.setReadOnly()
		}

		when: "执行强制删除"
		FileUtils.forceDeleteIfExist(normalFile)
		FileUtils.forceDeleteIfExist(readOnlyFile)

		then: "验证文件已删除"
		!normalFile.exists()
		!readOnlyFile.exists()
	}

	def "测试内存映射文件流读取"() {
		given: "创建大测试文件"
		def bigFile = tempDir.resolve("big.data").toFile()
		def content = "A" * 1024 * 1024 // 1MB
		Files.writeString(bigFile.toPath(), content)

		when: "使用内存映射流读取"
		def inputStream = FileUtils.openMemoryMappedFileInputStream(bigFile)
		def result = new String(inputStream.readAllBytes())

		then: "验证读取内容正确"
		result == content
	}

	def "测试应用类型检测"() {
		given: "创建EXE文件头"
		def exeFile = tempDir.resolve("test.exe").toFile()
		Files.write(exeFile.toPath(), [0x4D, 0x5A] as byte[]) // MZ头

		expect: "验证应用类型检测"
		FileUtils.isApplicationType(exeFile)
	}

	@Unroll
	def "测试MIME类型精确匹配：#mimeType"() {
		given: "创建测试文件"
		def testFile = createTestFile()

		expect: "验证MIME类型匹配结果"
		FileUtils.isMimeType(testFile, mimeType) == expected

		where:
		mimeType                   | expected
		"text/plain"               | true
		"application/octet-stream" | false
	}

	def "测试批量MIME类型匹配（集合）"() {
		given: "创建文本文件"
		def textFile = createTestFile("Hello World")

		when: "匹配允许的类型集合"
		def result = FileUtils.isAnyMimeType(textFile, ["text/plain", "application/json"])

		then: "验证匹配结果"
		result
	}

	def "测试替换文件基名"() {
		given: "创建带扩展名的测试文件"
		def srcFile = tempDir.resolve("report.pdf").toFile()
		Files.writeString(srcFile.toPath(), "content")

		when: "替换基名"
		def newFile = FileUtils.replaceBaseName(srcFile, "年度报告")

		then: "验证新文件名"
		newFile.name == "年度报告.pdf"
		!srcFile.exists()
	}

	def "测试替换扩展名为空"() {
		given: "创建测试文件"
		def srcFile = createTestFile()

		when: "移除扩展名"
		def newFile = FileUtils.replaceExtension(srcFile, "")

		then: "验证新文件名"
		newFile.name == "test"
	}

	def "测试CTR模式加密解密"() {
		given: "准备测试文件和随机密码"
		def originalFile = createTestFile("CTR模式测试数据")
		def password = RandomUtils.secure().randomBytes(16)
		def iv = RandomUtils.secure().randomBytes(16)

		when: "执行CTR加密解密流程"
		FileUtils.encryptFileByCtr(originalFile, tempDir.resolve("encrypted.dat").toFile(), password, iv)
		FileUtils.decryptFileByCtr(tempDir.resolve("encrypted.dat").toFile(), tempDir.resolve("decrypted.txt").toFile(), password, iv)

		then: "验证解密结果"
		tempDir.resolve("decrypted.txt").toFile().text == "CTR模式测试数据"
	}

	def "测试缓冲通道输入流"() {
		given: "创建测试文件"
		def content = "BufferedChannel测试内容"
		def testFile = createTestFile(content)

		when: "使用缓冲通道流读取"
		def inputStream = FileUtils.openBufferedFileChannelInputStream(testFile)
		def result = new String(inputStream.readAllBytes())

		then: "验证读取内容正确"
		result == content
	}

	def "测试删除不存在文件不报错"() {
		when: "删除不存在的文件"
		FileUtils.deleteIfExist(new File(tempDir.toString(), "ghost.txt"))

		then: "无异常抛出"
		noExceptionThrown()
	}

	def "测试检查目录存在性"() {
		given: "创建测试目录"
		def dir = tempDir.resolve("testDir").toFile()
		dir.mkdir()

		when: "检查目录存在性（isFile=false）"
		FileUtils.checkExists(dir, "目录检查", false)

		then: "无异常抛出"
		noExceptionThrown()
	}

	def "测试检查文件类型错误抛出异常"() {
		given: "创建目录而不是文件"
		def dir = tempDir.resolve("testDir").toFile()
		dir.mkdir()

		when: "检查文件类型（isFile=true）"
		FileUtils.checkExists(dir, "类型检查", true)

		then: "抛出FileNotFoundException"
		thrown(FileNotFoundException)
	}
}
