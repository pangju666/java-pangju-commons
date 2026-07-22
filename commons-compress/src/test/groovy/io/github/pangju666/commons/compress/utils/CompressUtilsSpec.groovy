package io.github.pangju666.commons.compress.utils


import io.github.pangju666.commons.compress.io.resource.CompressResource
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class CompressUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	def "解压GZ资源文件到输出流成功"() {
		setup:
		File gzFile = new File("src/test/resources/test.gz")
		File outputFile = tempDir.resolve("output.txt").toFile()

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			CompressUtils.uncompress(new CompressResource(gzFile), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "解压XZ资源文件到输出流成功"() {
		setup:
		File xzFile = new File("src/test/resources/test.xz")
		File outputFile = tempDir.resolve("output.txt").toFile()

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			CompressUtils.uncompress(new CompressResource(xzFile), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "解压ZST资源文件到输出流成功"() {
		setup:
		File zstFile = new File("src/test/resources/test.zst")
		File outputFile = tempDir.resolve("output.txt").toFile()

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			CompressUtils.uncompress(new CompressResource(zstFile), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "解压ZipResource到目录成功"() {
		setup:
		File zipFile = new File("src/test/resources/test.zip")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(zipFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压TarResource到目录成功"() {
		setup:
		File tarFile = new File("src/test/resources/test.tar")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(tarFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压SevenZResource到目录成功"() {
		setup:
		File sevenZFile = new File("src/test/resources/test.7z")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(sevenZFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压GzipResource到文件成功"() {
		setup:
		File gzFile = new File("src/test/resources/test.gz")
		File outputFile = tempDir.resolve("output.txt").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(gzFile), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "解压GzipResource(TAR格式)到目录成功"() {
		setup:
		File tarGzFile = new File("src/test/resources/test.tar.gz")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(tarGzFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压GzipResource(TGZ格式)到目录成功"() {
		setup:
		File tgzFile = new File("src/test/resources/test.tgz")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(tgzFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压XZResource到文件成功"() {
		setup:
		File xzFile = new File("src/test/resources/test.xz")
		File outputFile = tempDir.resolve("output.txt").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(xzFile), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "解压XZResource(TAR格式)到目录成功"() {
		setup:
		File tarXzFile = new File("src/test/resources/test.tar.xz")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(tarXzFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压XZResource(TXZ格式)到目录成功"() {
		setup:
		File txzFile = new File("src/test/resources/test.txz")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(txzFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压ZstdResource到文件成功"() {
		setup:
		File zstFile = new File("src/test/resources/test.zst")
		File outputFile = tempDir.resolve("output.txt").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(zstFile), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "解压ZstdResource(TAR格式)到目录成功"() {
		setup:
		File tarZstFile = new File("src/test/resources/test.tar.zst")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(tarZstFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压ZstdResource(TZST格式)到目录成功"() {
		setup:
		File tzstFile = new File("src/test/resources/test.tzst")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(tzstFile), outputDir)

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "使用密码解压ZipResource到目录成功"() {
		setup:
		File zipFile = new File("src/test/resources/test-password.zip")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(zipFile), outputDir, "123456")

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "使用密码解压SevenZResource到目录成功"() {
		setup:
		File sevenZFile = new File("src/test/resources/test-password.7z")
		File outputDir = tempDir.resolve("output").toFile()

		when:
		CompressUtils.uncompress(new CompressResource(sevenZFile), outputDir, "123456")

		then:
		outputDir.exists()
		outputDir.isDirectory()
		outputDir.listFiles().length > 0
	}

	def "解压CompressResource到输出流不支持归档格式抛出异常"() {
		setup:
		File zipFile = new File("src/test/resources/test.zip")

		when:
		try (OutputStream out = new FileOutputStream(tempDir.resolve("output.txt").toFile())) {
			CompressUtils.uncompress(new CompressResource(zipFile), out)
		}

		then:
		thrown(UnsupportedOperationException)
	}

	def "压缩文件outputFile为null抛出异常"() {
		setup:
		File inputFile = tempDir.resolve("input.txt").toFile()
		inputFile.text = "test content"

		when:
		CompressUtils.compress(inputFile, null)

		then:
		thrown(NullPointerException)
	}

	def "压缩文件集合到不支持的格式抛出异常"() {
		setup:
		File inputFile = tempDir.resolve("input.txt").toFile()
		inputFile.text = "test content"
		File outputFile = tempDir.resolve("output.gz").toFile()

		when:
		CompressUtils.compress([inputFile], outputFile)

		then:
		thrown(UnsupportedOperationException)
	}

	def "使用密码压缩到不支持的格式抛出异常"() {
		setup:
		File inputFile = tempDir.resolve("input.txt").toFile()
		inputFile.text = "test content"
		File outputFile = tempDir.resolve("output.tar").toFile()

		when:
		CompressUtils.compress(inputFile, outputFile, "password123")

		then:
		thrown(UnsupportedOperationException)
	}
}
