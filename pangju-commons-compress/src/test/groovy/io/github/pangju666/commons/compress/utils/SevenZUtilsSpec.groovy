package io.github.pangju666.commons.compress.utils

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import spock.lang.Specification
import spock.lang.TempDir

class SevenZUtilsSpec extends Specification {
	@TempDir
	File tempDir

	def "测试解压7z文件到目录"() {
		given: "准备测试7z文件和目标目录"
		File sevenZFile = new File("src/test/resources/test.7z")
		File outputDir = new File(tempDir, "output")

		when: "执行解压操作"
		SevenZUtils.unCompress(sevenZFile, outputDir)

		then: "验证解压结果"
		outputDir.listFiles()?.size() == 2
		new File(outputDir, "test.png").exists()
		new File(outputDir, "subdir").isDirectory()
		new File(outputDir, "subdir/test.txt").exists()
	}

	def "测试压缩单个文件到7z"() {
		given: "准备测试文件和输出路径"
		File inputFile = new File("src/test/resources/test.txt")
		File output7z = new File(tempDir, "single.7z")

		when: "执行压缩操作"
		SevenZUtils.compress(inputFile, output7z)

		then: "验证压缩结果"
		output7z.exists()
		verify7zContents(output7z, ["test.txt"])
	}

	def "测试批量压缩文件集合"() {
		given: "准备文件集合和输出路径"
		Collection<File> files = [
			new File("src/test/resources/test1.txt"),
			new File("src/test/resources/test.txt")
		]
		File output7z = new File(tempDir, "multi.7z")

		when: "执行批量压缩"
		SevenZUtils.compress(files, output7z)

		then: "验证压缩结果"
		output7z.exists()
		verify7zContents(output7z, ["test1.txt", "test.txt"])
	}

	def "测试解压无效7z文件抛出异常"() {
		given: "准备无效的7z文件"
		File invalid7z = new File("src/test/resources/invalid.7z")
		File outputDir = new File(tempDir, "invalid_output")

		when: "尝试解压无效文件"
		SevenZUtils.unCompress(invalid7z, outputDir)

		then: "应抛出IOException"
		thrown(IOException)
	}

	def "测试压缩不存在的文件抛出异常"() {
		given: "准备不存在的文件"
		File nonExisting = new File("nonexistent.txt")
		File output7z = new File(tempDir, "error.7z")

		when: "执行压缩操作"
		SevenZUtils.compress(nonExisting, output7z)

		then: "应抛出FileNotFoundException"
		thrown(FileNotFoundException)
	}

	private static void verify7zContents(File sevenZFile, List<String> expectedEntries) {
		SevenZFile z7 = new SevenZFile(sevenZFile)
		def actualEntries = []
		while (true) {
			SevenZArchiveEntry entry = z7.getNextEntry()
			if (entry == null) break
			actualEntries << entry.name
		}
		assert actualEntries.containsAll(expectedEntries)
		z7.close()
	}
}
