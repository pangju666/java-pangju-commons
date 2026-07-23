package io.github.pangju666.commons.compress.utils

import io.github.pangju666.commons.compress.io.resource.TarResource
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.tar.TarFile
import spock.lang.Specification

class TarUtilsSpec extends Specification {

	def "压缩文件到TAR文件成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.tar")

		when:
		TarUtils.archive(inputFile, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到TAR文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.tar")
		def processedEntries = []

		when:
		TarUtils.archive(inputFile, outputFile) { TarArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩目录到TAR文件成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputDir = new File(work, "inputDir")
		inputDir.mkdirs()
		File inputFile = new File(inputDir, "file.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.tar")

		when:
		TarUtils.archive(inputDir, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到输出流成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.tar")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			TarUtils.archive(inputFile, out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到输出流带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.tar")
		def processedEntries = []

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			TarUtils.archive(inputFile, out) { TarArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "批量压缩文件到TAR文件成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.tar")

		when:
		TarUtils.archive([inputFile1, inputFile2], outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到TAR文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.tar")
		def processedEntries = []

		when:
		TarUtils.archive([inputFile1, inputFile2], outputFile) { TarArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到输出流成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.tar")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			TarUtils.archive([inputFile1, inputFile2], out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到输出流带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.tar")
		def processedEntries = []

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			TarUtils.archive([inputFile1, inputFile2], out) { TarArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "从TarFile解压到目录成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File tarFile = new File("src/test/resources/test.tar")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		try (TarFile tar = new TarFile(tarFile)) {
			TarUtils.extract(tar, outputDir)
		}

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从TarResource解压到目录成功"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File tarFile = new File("src/test/resources/test.tar")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		TarUtils.extract(new TarResource(tarFile), outputDir)

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "压缩null输入文件抛出异常"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File outputFile = new File(work, "output.tar")

		when:
		TarUtils.archive(null as File, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出文件抛出异常"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		TarUtils.archive(inputFile, null as File)

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出流抛出异常"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		TarUtils.archive(inputFile, null as OutputStream)

		then:
		thrown(NullPointerException)
	}

	def "批量压缩null文件集合抛出异常"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File outputFile = new File(work, "output.tar")

		when:
		TarUtils.archive(null as Collection<File>, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "解压null TarFile抛出异常"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		TarUtils.extract(null as TarFile, outputDir)

		then:
		thrown(NullPointerException)
	}

	def "解压null TarResource抛出异常"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		TarUtils.extract(null as TarResource, outputDir)

		then:
		thrown(NullPointerException)
	}

	def "压缩使用已存在的TarArchiveOutputStream"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.tar")

		when:
		try (FileOutputStream fos = new FileOutputStream(outputFile);
		     BufferedOutputStream bos = new BufferedOutputStream(fos);
		     TarArchiveOutputStream taos = new TarArchiveOutputStream(bos)) {
			TarUtils.archive(inputFile, taos)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩使用已存在的TarArchiveOutputStream"() {
		setup:
		File work = new File("target/test-work/tar")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.tar")

		when:
		try (FileOutputStream fos = new FileOutputStream(outputFile);
		     BufferedOutputStream bos = new BufferedOutputStream(fos);
		     TarArchiveOutputStream taos = new TarArchiveOutputStream(bos)) {
			TarUtils.archive([inputFile1, inputFile2], taos)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}
}
