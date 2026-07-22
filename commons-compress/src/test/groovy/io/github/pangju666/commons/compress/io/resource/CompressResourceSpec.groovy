package io.github.pangju666.commons.compress.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.archivers.ArchiveEntry
import spock.lang.Specification

class CompressResourceSpec extends Specification {
	static def archiveFiles = [
		"test.gz",
		"test.tar.gz",
		"test.tar.xz",
		"test.tar.zst",
		"test.tgz",
		"test.tzst",
		"test.xz",
		"test.zst",
	]

	static def compressFiles = [
		"test.zip",
		"test.tar",
		"test-password.zip"
	]

	static def testFiles = archiveFiles + compressFiles + [
		"test.7z",
		"test-password.7z",
	]

	def "从IOResource构造CompressResource成功"() {
		setup:
		IOResource resource = new IOResource(new File("src/test/resources/" + testFile))

		when:
		CompressResource compressResource = new CompressResource(resource)

		then:
		noExceptionThrown()

		where:
		testFile << testFiles
	}

	def "从文件路径构造CompressResource成功"() {
		when:
		CompressResource compressResource = new CompressResource("src/test/resources/" + testFile)

		then:
		noExceptionThrown()

		where:
		testFile << testFiles
	}

	def "从File构造CompressResource成功"() {
		when:
		CompressResource compressResource = new CompressResource("src/test/resources/" + testFile)

		then:
		noExceptionThrown()

		where:
		testFile << testFiles
	}

	def "从字节数组构造CompressResource成功"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/" + testFile).bytes)

		then:
		noExceptionThrown()

		where:
		testFile << testFiles
	}

	def "从输入流构造CompressResource成功"() {
		when:
		CompressResource compressResource = new CompressResource(new FileInputStream(new File("src/test/resources/" + testFile)))

		then:
		noExceptionThrown()

		where:
		testFile << testFiles
	}

	def "构造CompressResource时非压缩文件抛出异常"() {
		setup:
		File work = new File("target/test-work/compress-resource")
		work.mkdirs()
		File txtFile = new File(work, "test.txt")
		txtFile.text = "test content"

		when:
		new CompressResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "isGzip方法正确识别GZIP格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.gz"))

		then:
		compressResource.isGzip()
	}

	def "isZip方法正确识别ZIP格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.zip"))

		then:
		compressResource.isZip()
	}

	def "isTar方法正确识别TAR格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.tar"))

		then:
		compressResource.isTar()
	}

	def "isXz方法正确识别XZ格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.xz"))

		then:
		compressResource.isXz()
	}

	def "is7z方法正确识别7Z格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.7z"))

		then:
		compressResource.is7z()
	}

	def "isZstd方法正确识别ZSTD格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.zst"))

		then:
		compressResource.isZstd()
	}

	def "isGzip方法正确识别TGZ格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.tgz"))

		then:
		compressResource.isGzip()
	}

	def "isGzip方法正确识别TAR.GZ格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.tar.gz"))

		then:
		compressResource.isGzip()
	}

	def "isXz方法正确识别TXZ格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.txz"))

		then:
		compressResource.isXz()
	}

	def "isXz方法正确识别TAR.XZ格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.tar.xz"))

		then:
		compressResource.isXz()
	}

	def "isZstd方法正确识别TZST格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.tzst"))

		then:
		compressResource.isZstd()
	}

	def "isZstd方法正确识别TAR.ZST格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.tar.zst"))

		then:
		compressResource.isZstd()
	}

	def "is7z方法正确识别加密7Z格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test-password.7z"))

		then:
		compressResource.is7z()
	}

	def "isZip方法正确识别加密ZIP格式"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test-password.zip"))

		then:
		compressResource.isZip()
	}

	def "openCompressorInputStream成功打开GZIP压缩输入流"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/" + testFile))
		try (def inputStream = compressResource.openCompressorInputStream()) {
			inputStream.read()
		}

		then:
		noExceptionThrown()

		where:
		testFile << [
			"test.gz",
			"test.tgz",
			"test.tar.gz",
		]
	}

	def "openCompressorInputStream成功打开XZ压缩输入流"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/" + testFile))
		try (def inputStream = compressResource.openCompressorInputStream()) {
			inputStream.read()
		}

		then:
		noExceptionThrown()

		where:
		testFile << [
			"test.xz",
			"test.txz",
			"test.tar.xz",
		]
	}

	def "openCompressorInputStream成功打开ZSTD压缩输入流"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/" + testFile))
		try (def inputStream = compressResource.openCompressorInputStream()) {
			inputStream.read()
		}

		then:
		noExceptionThrown()

		where:
		testFile << [
			"test.zst",
			"test.tzst",
			"test.tar.zst",
		]
	}

	def "openCompressorInputStream不支持的格式抛出异常"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/" + testFile))
		compressResource.openCompressorInputStream()

		then:
		thrown(UnsupportedResourceException)

		where:
		testFile << [
			"test.zip",
			"test.tar",
			"test.7z",
			"test-password.7z",
			"test-password.zip",
		]
	}

	def "openArchiveInputStream成功打开TAR归档输入流"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.tar"))
		try (def inputStream = compressResource.openArchiveInputStream()) {
			ArchiveEntry archiveEntry = inputStream.getNextEntry()
			inputStream.read()
		}

		then:
		noExceptionThrown()
	}

	def "openArchiveInputStream成功打开ZIP归档输入流"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/" + testFile))
		try (def inputStream = compressResource.openArchiveInputStream()) {
			ArchiveEntry archiveEntry = inputStream.getNextEntry()
			inputStream.read()
		}

		then:
		noExceptionThrown()

		where:
		testFile << [
			"test.zip",
			"test-password.zip",
		]
	}

	def "openArchiveInputStream不支持的格式抛出异常"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/" + testFile))
		compressResource.openArchiveInputStream()

		then:
		thrown(UnsupportedResourceException)

		where:
		testFile << [
			"test.gz",
			"test.xz",
			"test.zst",
			"test.7z",
			"test-password.7z",
			"test.tgz",
			"test.txz",
			"test.tzst",
			"test.tar.gz",
			"test.tar.xz",
			"test.tar.zst",
		]
	}

	def "资源关闭后openCompressorInputStream抛出异常"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.gz"))
		compressResource.close()
		compressResource.openCompressorInputStream()

		then:
		thrown(IllegalStateException)
	}

	def "资源关闭后openArchiveInputStream抛出异常"() {
		when:
		CompressResource compressResource = new CompressResource(new File("src/test/resources/test.tar"))
		compressResource.close()
		compressResource.openArchiveInputStream()

		then:
		thrown(IllegalStateException)
	}
}
