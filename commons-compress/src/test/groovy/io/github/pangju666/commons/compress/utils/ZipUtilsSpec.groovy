package io.github.pangju666.commons.compress.utils

import io.github.pangju666.commons.compress.io.resource.ZipResource
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import spock.lang.Specification

import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.util.zip.Deflater

class ZipUtilsSpec extends Specification {

	def "压缩文件到ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(inputFile, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到ZIP文件指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(inputFile, outputFile, Deflater.DEFAULT_COMPRESSION)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到ZIP文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compress(inputFile, outputFile) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到ZIP文件指定压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compress(inputFile, outputFile, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到输出流成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZipUtils.compress(inputFile, out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到输出流指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZipUtils.compress(inputFile, out, Deflater.DEFAULT_COMPRESSION)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到输出流带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZipUtils.compress(inputFile, out) { ZipArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到输出流指定压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZipUtils.compress(inputFile, out, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到SeekableByteChannel成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ZipUtils.compress(inputFile, channel)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到SeekableByteChannel指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ZipUtils.compress(inputFile, channel, Deflater.DEFAULT_COMPRESSION)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到SeekableByteChannel带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ZipUtils.compress(inputFile, channel) { ZipArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "压缩文件到SeekableByteChannel指定压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ZipUtils.compress(inputFile, channel, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "批量压缩文件到ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress([inputFile1, inputFile2], outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到ZIP文件指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress([inputFile1, inputFile2], outputFile, Deflater.DEFAULT_COMPRESSION)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到ZIP文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compress([inputFile1, inputFile2], outputFile) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到ZIP文件指定压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compress([inputFile1, inputFile2], outputFile, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到输出流成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZipUtils.compress([inputFile1, inputFile2], out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到输出流指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZipUtils.compress([inputFile1, inputFile2], out, Deflater.DEFAULT_COMPRESSION)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到输出流带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZipUtils.compress([inputFile1, inputFile2], out) { ZipArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到输出流指定压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZipUtils.compress([inputFile1, inputFile2], out, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到SeekableByteChannel成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ZipUtils.compress([inputFile1, inputFile2], channel)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到SeekableByteChannel指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ZipUtils.compress([inputFile1, inputFile2], channel, Deflater.DEFAULT_COMPRESSION)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到SeekableByteChannel带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ZipUtils.compress([inputFile1, inputFile2], channel) { ZipArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量压缩文件到SeekableByteChannel指定压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		try (FileChannel channel = FileChannel.open(outputFile.toPath(),
			StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
			ZipUtils.compress([inputFile1, inputFile2], channel, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
				processedEntries.add(entry.name)
			}
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "压缩文件到ZIP文件使用ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(inputFile, outputFile, new ZipParameters())

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到ZIP文件使用ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress([inputFile1, inputFile2] as List<File>, outputFile, new ZipParameters())

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到加密ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(inputFile, outputFile, "123456")

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到加密ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress([inputFile1, inputFile2] as List<File>, outputFile, "123456")

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩文件到加密ZIP文件使用ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		ZipParameters parameters = new ZipParameters()
		parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD)

		when:
		ZipUtils.compress(inputFile, outputFile, "123456", parameters)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量压缩文件到加密ZIP文件使用ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		ZipParameters parameters = new ZipParameters()
		parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD)

		when:
		ZipUtils.compress([inputFile1, inputFile2] as List<File>, outputFile, "123456", parameters)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "从ZipResource解压到目录成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File zipFile = new File("src/test/resources/test.zip")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(new ZipResource(zipFile), outputDir)

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从ZipResource解压到目录忽略本地文件头成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File zipFile = new File("src/test/resources/test.zip")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(new ZipResource(zipFile), outputDir, true)

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从ZipFile解压到目录成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File zipFile = new File("src/test/resources/test.zip")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		try (ZipFile zip = ZipFile.builder().setFile(zipFile).get()) {
			ZipUtils.uncompress(zip, outputDir)
		}

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从带密码的ZipResource解压到目录成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File zipFile = new File("src/test/resources/test-password.zip")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(new ZipResource(zipFile), outputDir, "123456")

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "解压分卷ZIP文件到目录成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File zipFile = new File("src/test/resources/test-split.zip")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(new ZipResource(zipFile), outputDir)

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从分卷ZIP文件的ZipResource解压到目录成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File zipFile = new File("src/test/resources/test-split.zip")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(new ZipResource(zipFile), outputDir)

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "从带密码的分卷ZIP文件的ZipResource解压到目录成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File zipFile = new File("src/test/resources/password-split.zip")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(new ZipResource(zipFile), outputDir, "password-split")

		then:
		outputDir.exists()
		outputDir.list().length > 0
	}

	def "压缩null输入文件抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(null as File, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出文件抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		ZipUtils.compress(inputFile, null as File)

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出流抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		ZipUtils.compress(inputFile, null as OutputStream)

		then:
		thrown(NullPointerException)
	}

	def "压缩null SeekableByteChannel抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		ZipUtils.compress(inputFile, null as FileChannel)

		then:
		thrown(NullPointerException)
	}

	def "压缩null ZipParameters抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(inputFile, outputFile, null as ZipParameters)

		then:
		thrown(NullPointerException)
	}

	def "压缩空密码抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(inputFile, outputFile, "")

		then:
		thrown(IllegalArgumentException)
	}

	def "压缩null密码抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(inputFile, outputFile, null as String)

		then:
		thrown(NullPointerException)
	}

	def "批量压缩null文件集合抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compress(null as Collection<File>, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "解压null ZipResource抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(null as ZipResource, outputDir)

		then:
		thrown(NullPointerException)
	}

	def "解压null ZipFile抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(null as ZipFile, outputDir)

		then:
		thrown(NullPointerException)
	}

	def "解压null密码抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File zipFile = new File("src/test/resources/test-password.zip")
		File outputDir = new File(work, "output")
		outputDir.mkdirs()

		when:
		ZipUtils.uncompress(new ZipResource(zipFile), outputDir, null as String)

		then:
		thrown(NullPointerException)
	}

	def "压缩使用已存在的ZipArchiveOutputStream"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		try (FileOutputStream fos = new FileOutputStream(outputFile)
		     BufferedOutputStream bos = new BufferedOutputStream(fos)
		     ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(bos)) {
			ZipUtils.compress(inputFile, zaos)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	// ============ 分片压缩测试 ============

	def "分片压缩文件到ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到ZIP文件指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, Deflater.DEFAULT_COMPRESSION)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到ZIP文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compressSplit(inputFile, outputFile) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "分片压缩文件到ZIP文件指定压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compressSplit(inputFile, outputFile, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "分片压缩文件到ZIP文件指定分片大小成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, 1024 * 1024 as long)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到ZIP文件指定分片大小和压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, 1024 * 1024, Deflater.DEFAULT_COMPRESSION)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到ZIP文件指定分片大小和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compressSplit(inputFile, outputFile, 1024 * 1024 as long) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "分片压缩文件到ZIP文件指定分片大小、压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compressSplit(inputFile, outputFile, 1024 * 1024, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() > 0
	}

	def "批量分片压缩文件到ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2], outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量分片压缩文件到ZIP文件指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2], outputFile, Deflater.DEFAULT_COMPRESSION)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量分片压缩文件到ZIP文件带条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2], outputFile) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量分片压缩文件到ZIP文件指定压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2], outputFile, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量分片压缩文件到ZIP文件指定分片大小成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2], outputFile, 1024 * 1024 as long)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量分片压缩文件到ZIP文件指定分片大小和压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2], outputFile, 1024 * 1024, Deflater.DEFAULT_COMPRESSION)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量分片压缩文件到ZIP文件指定分片大小和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2], outputFile, 1024 * 1024 as long) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量分片压缩文件到ZIP文件指定分片大小、压缩级别和条目处理器成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		def processedEntries = []

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2], outputFile, 1024 * 1024, Deflater.DEFAULT_COMPRESSION) { ZipArchiveEntry entry ->
			processedEntries.add(entry.name)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
		processedEntries.size() == 2
	}

	def "批量分片压缩文件到ZIP文件使用ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2] as List<File>, outputFile, new ZipParameters())

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量分片压缩文件到ZIP文件指定分片大小和ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2] as List<File>, outputFile, 1024 * 1024 as long, new ZipParameters())

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量分片压缩文件到加密ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2] as List<File>, outputFile, "123456")

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量分片压缩文件到加密ZIP文件指定分片大小成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2] as List<File>, outputFile, "123456", 1024 * 1024 as long)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "批量分片压缩文件到加密ZIP文件指定分片大小和ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile1 = new File(work, "input1.txt")
		inputFile1.text = "test content 1"
		File inputFile2 = new File(work, "input2.txt")
		inputFile2.text = "test content 2"
		File outputFile = new File(work, "output.zip")
		ZipParameters parameters = new ZipParameters()
		parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD)

		when:
		ZipUtils.compressSplit([inputFile1, inputFile2] as List<File>, outputFile, "123456", 1024 * 1024 as long, parameters)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到ZIP文件使用ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, new ZipParameters())

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到ZIP文件指定分片大小和ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, 1024 * 1024, new ZipParameters())

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到加密ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, "123456")

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到加密ZIP文件指定分片大小成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, "123456", 1024 * 1024)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩文件到加密ZIP文件指定分片大小和ZipParameters成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")
		ZipParameters parameters = new ZipParameters()
		parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD)

		when:
		ZipUtils.compressSplit(inputFile, outputFile, "123456", 1024 * 1024, parameters)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩目录到ZIP文件成功"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputDir = new File(work, "input")
		inputDir.mkdirs()
		File inputFile = new File(inputDir, "file.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputDir, outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "分片压缩null输入文件抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(null as File, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "分片压缩null输出文件抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		ZipUtils.compressSplit(inputFile, null as File)

		then:
		thrown(NullPointerException)
	}

	def "批量分片压缩null文件集合抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(null as Collection<File>, outputFile)

		then:
		thrown(NullPointerException)
	}

	def "分片压缩null ZipParameters抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, null as ZipParameters)

		then:
		thrown(NullPointerException)
	}

	def "分片压缩空密码抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, "")

		then:
		thrown(IllegalArgumentException)
	}

	def "分片压缩null密码抛出异常"() {
		setup:
		File work = new File("target/test-work/zip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zip")

		when:
		ZipUtils.compressSplit(inputFile, outputFile, null as String)

		then:
		thrown(NullPointerException)
	}
}
