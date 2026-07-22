package io.github.pangju666.commons.compress.utils

import io.github.pangju666.commons.compress.io.resource.SevenZResource
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.sevenz.SevenZMethod
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile
import spock.lang.Specification

import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption

class SevenZUtilsSpec extends Specification {

	def "压缩文件到7z文件成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(inputFile, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到7z文件指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(inputFile, outputFile, SevenZMethod.COPY)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到7z文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		SevenZUtils.compress(inputFile, outputFile) { entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到加密7z文件成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(inputFile, outputFile, "password")

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到加密7z文件指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(inputFile, outputFile, "password", SevenZMethod.COPY)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到加密7z文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		SevenZUtils.compress(inputFile, outputFile, "password") { entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到SeekableByteChannel成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress(inputFile, channel)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到SeekableByteChannel指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress(inputFile, channel, SevenZMethod.COPY)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到SeekableByteChannel带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress(inputFile, channel) { entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到加密SeekableByteChannel成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress(inputFile, channel, "password")
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到加密SeekableByteChannel指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress(inputFile, channel, "password", SevenZMethod.COPY)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到加密SeekableByteChannel带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress(inputFile, channel, "password") { entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到SevenZOutputFile成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			SevenZUtils.compress(inputFile, sevenZOutputFile)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到SevenZOutputFile指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			SevenZUtils.compress(inputFile, sevenZOutputFile, SevenZMethod.COPY)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到SevenZOutputFile带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			SevenZUtils.compress(inputFile, sevenZOutputFile) { entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "批量压缩文件到7z文件成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress([inputFile1, inputFile2], outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到7z文件指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress([inputFile1, inputFile2], outputFile, SevenZMethod.COPY)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到7z文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		SevenZUtils.compress([inputFile1, inputFile2], outputFile) { entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到加密7z文件成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress([inputFile1, inputFile2], outputFile, "password")

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到加密7z文件指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress([inputFile1, inputFile2], outputFile, "password", SevenZMethod.COPY)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到加密7z文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		SevenZUtils.compress([inputFile1, inputFile2], outputFile, "password") { entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到SeekableByteChannel成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress([inputFile1, inputFile2], channel)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到SeekableByteChannel指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress([inputFile1, inputFile2], channel, SevenZMethod.COPY)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到SeekableByteChannel带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress([inputFile1, inputFile2], channel) { entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到加密SeekableByteChannel成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress([inputFile1, inputFile2], channel, "password")
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到加密SeekableByteChannel指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress([inputFile1, inputFile2], channel, "password", SevenZMethod.COPY)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到加密SeekableByteChannel带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			SevenZUtils.compress([inputFile1, inputFile2], channel, "password") { entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到SevenZOutputFile成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			SevenZUtils.compress([inputFile1, inputFile2], sevenZOutputFile)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到SevenZOutputFile指定压缩方法成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")

		when:
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			SevenZUtils.compress([inputFile1, inputFile2], sevenZOutputFile, SevenZMethod.COPY)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到SevenZOutputFile带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.7z")
		def processedEntries = []

		when:
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			SevenZUtils.compress([inputFile1, inputFile2], sevenZOutputFile) { entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "从SevenZFile解压到目录成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File sevenZFile = new File("src/test/resources/test.7z")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		try (SevenZFile sevenZ = SevenZFile.builder().setFile(sevenZFile).get()) {
			SevenZUtils.uncompress(sevenZ, outputDir)
		}

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从SevenZResource解压到目录成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File sevenZFile = new File("src/test/resources/test.7z")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		SevenZUtils.uncompress(new SevenZResource(sevenZFile), outputDir)

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从带密码的SevenZFile解压到目录成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File sevenZFile = new File("src/test/resources/test-password.7z")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		try (SevenZFile sevenZ = SevenZFile.builder().setFile(sevenZFile).setPassword("123456".toCharArray()).get()) {
			SevenZUtils.uncompress(sevenZ, outputDir)
		}

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从带密码的SevenZResource解压到目录成功"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File sevenZFile = new File("src/test/resources/test-password.7z")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		SevenZUtils.uncompress(new SevenZResource(sevenZFile, "123456"), outputDir)

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "压缩null输入文件抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(null as File, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出文件抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		SevenZUtils.compress(inputFile, null as File)

		then:
		thrown(NullPointerException)
	}

	def "压缩null压缩方法抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(inputFile, outputFile, null as SevenZMethod)

		then:
		thrown(NullPointerException)
	}

	def "压缩空密码抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(inputFile, outputFile, "")

		then:
		thrown(IllegalArgumentException)
	}

	def "压缩null密码抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(inputFile, outputFile, null as String)

		then:
		thrown(NullPointerException)
	}

	def "压缩null SeekableByteChannel抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		SevenZUtils.compress(inputFile, null as FileChannel)

		then:
		thrown(NullPointerException)
	}

	def "压缩null SevenZOutputFile抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		SevenZUtils.compress(inputFile, null as SevenZOutputFile)

		then:
		thrown(NullPointerException)
	}

	def "批量压缩null文件集合抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File outputFile = new File(work, "output.7z")

		when:
		SevenZUtils.compress(null as Collection<File>, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "解压null SevenZFile抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		SevenZUtils.uncompress(null as SevenZFile, outputDir)

		then:
		thrown(NullPointerException)
	}

	def "解压null SevenZResource抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		SevenZUtils.uncompress(null as SevenZResource, outputDir)

		then:
		thrown(NullPointerException)
	}

	def "解压null输出目录抛出异常"() {
		setup:
		File work = new File("target/test-work/7z")
		work.mkdirs()
		File sevenZFile = new File("src/test/resources/test.7z")

		when:
		try (SevenZFile sevenZ = SevenZFile.builder().setFile(sevenZFile).get()) {
			SevenZUtils.uncompress(sevenZ, null as File)
		}

		then:
		thrown(NullPointerException)
	}
}
