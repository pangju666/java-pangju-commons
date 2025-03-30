package io.github.pangju666.commons.compress.utils

import org.apache.commons.compress.archivers.zip.ZipFile
import spock.lang.Specification
import spock.lang.TempDir

class ZipUtilsSpec extends Specification {
	@TempDir
	File tempDir

	def "测试解压ZIP文件到目录"() {
		given: "准备测试ZIP文件和目标目录"
		File zipFile = new File("src/test/resources/test.zip")
		File outputDir = new File("src/test/resources/output")

		when: "执行解压操作"
		ZipUtils.unCompress(zipFile, outputDir)

		then: "验证解压结果"
		outputDir.listFiles()?.size() == 2
		new File(outputDir, "test.png").exists()
		new File(outputDir, "subdir").isDirectory()
	}

	def "测试解压ZIP文件流到目录"() {
		given: "准备测试ZIP文件和目标目录"
		File zipFile = new File("src/test/resources/test.zip")
		File outputDir = new File("src/test/resources/output")

		when: "执行解压操作"
		ZipUtils.unCompress(new FileInputStream(zipFile), outputDir)

		then: "验证解压结果"
		outputDir.listFiles()?.size() == 2
		new File(outputDir, "test.png").exists()
		new File(outputDir, "subdir").isDirectory()
	}

	def "测试压缩单个文件到ZIP"() {
		given: "准备测试文件和输出路径"
		File inputFile = new File("src/test/resources/test.txt")
		File outputZip = new File(tempDir, "single.zip")

		when: "执行压缩操作"
		ZipUtils.compress(inputFile, outputZip)

		then: "验证压缩结果"
		outputZip.exists()
		verifyZipContents(outputZip, ["test.txt"])
	}

	def "测试压缩目录到ZIP"() {
		given: "准备测试目录和输出路径"
		File inputDir = new File("src/test/resources/subdir")
		File outputZip = new File("src/test/resources/output.zip")

		when: "执行压缩操作"
		ZipUtils.compress(inputDir, outputZip)

		then: "验证压缩结果"
		outputZip.exists()
		verifyZipContents(outputZip, [
			"subdir/",
			"subdir/test.txt",
		])
	}

	def "测试批量压缩文件集合"() {
		given: "准备文件集合和输出路径"
		Collection<File> files = [
			new File("src/test/resources/test.png"),
			new File("src/test/resources/test.txt")
		]
		File outputZip = new File("src/test/resources/output.zip")

		when: "执行批量压缩"
		ZipUtils.compress(files, outputZip)

		then: "验证压缩结果"
		outputZip.exists()
		verifyZipContents(outputZip, ["test.png", "test.txt"])
	}

	def "测试输入流解压"() {
		given: "准备输入流和输出目录"
		InputStream input = new FileInputStream("src/test/resources/output.zip")
		File outputDir = new File("src/test/resources/stream_output")

		when: "通过输入流解压"
		ZipUtils.unCompress(input, outputDir)

		then: "验证解压结果"
		new File(outputDir, "test.txt").exists()
	}

	def "测试无效ZIP文件解压抛出异常"() {
		given: "准备无效的ZIP文件"
		File invalidZip = new File("src/test/resources/invalid.zip")
		File outputDir = new File(tempDir, "invalid_output")

		when: "尝试解压无效文件"
		ZipUtils.unCompress(invalidZip, outputDir)

		then: "应抛出IOException"
		thrown(IOException)
	}

	private static void verifyZipContents(File zipFile, List<String> expectedEntries) {
		ZipFile zf = ZipFile.builder().setFile(zipFile).get()
		def actualEntries = zf.entries.collect { it.name }
		assert actualEntries.containsAll(expectedEntries)
		zf.close()
	}
}
