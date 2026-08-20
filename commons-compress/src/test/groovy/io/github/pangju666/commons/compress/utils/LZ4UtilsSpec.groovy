package io.github.pangju666.commons.compress.utils

import io.github.pangju666.commons.compress.io.resource.LZ4Resource
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream
import spock.lang.Specification

class LZ4UtilsSpec extends Specification {

	def "压缩输入流到输出流成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
			 OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.compress(inputStream, out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到输出流指定参数成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")
		FramedLZ4CompressorOutputStream.Parameters parameters =
			new FramedLZ4CompressorOutputStream.Parameters(FramedLZ4CompressorOutputStream.BlockSize.K64)

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
			 OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.compress(inputStream, out, parameters)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到输出流成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.compress(new IOResource(inputFile), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到输出流指定参数成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")
		FramedLZ4CompressorOutputStream.Parameters parameters =
			new FramedLZ4CompressorOutputStream.Parameters(FramedLZ4CompressorOutputStream.BlockSize.K64)

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.compress(new IOResource(inputFile), out, parameters)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到文件成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			LZ4Utils.compress(inputStream, outputFile)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到文件指定参数成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")
		FramedLZ4CompressorOutputStream.Parameters parameters =
			new FramedLZ4CompressorOutputStream.Parameters(FramedLZ4CompressorOutputStream.BlockSize.K64)

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			LZ4Utils.compress(inputStream, outputFile, parameters)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到文件成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")

		when:
		LZ4Utils.compress(new IOResource(inputFile), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到文件指定参数成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")
		FramedLZ4CompressorOutputStream.Parameters parameters =
			new FramedLZ4CompressorOutputStream.Parameters(FramedLZ4CompressorOutputStream.BlockSize.K64)

		when:
		LZ4Utils.compress(new IOResource(inputFile), outputFile, parameters)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "从LZ4Resource解压到输出流成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File lz4File = new File("src/test/resources/test.lz4")
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.uncompress(new LZ4Resource(lz4File), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "从LZ4Resource解压到文件成功"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File lz4File = new File("src/test/resources/test.lz4")
		File outputFile = new File(work, "output.txt")

		when:
		LZ4Utils.uncompress(new LZ4Resource(lz4File), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩null输入流抛出异常"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File outputFile = new File(work, "output.lz4")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.compress(null as InputStream, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出流抛出异常"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			LZ4Utils.compress(inputStream, null as OutputStream)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null参数抛出异常"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
			 OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.compress(inputStream, out, null)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null IOResource抛出异常"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File outputFile = new File(work, "output.lz4")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.compress(null as IOResource, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出文件抛出异常"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			LZ4Utils.compress(inputStream, null as File)
		}

		then:
		thrown(NullPointerException)
	}

	def "解压null输入流抛出异常"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.uncompress(null as LZ4Resource, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "解压null LZ4Resource抛出异常"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			LZ4Utils.uncompress(null as LZ4Resource, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩使用已存在的FramedLZ4CompressorOutputStream"() {
		setup:
		File work = new File("target/test-work/lz4")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.lz4")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
			 FileOutputStream fos = new FileOutputStream(outputFile);
			 FramedLZ4CompressorOutputStream flos = new FramedLZ4CompressorOutputStream(fos)) {
			LZ4Utils.compress(inputStream, flos)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}
}
