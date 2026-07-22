package io.github.pangju666.commons.compress.utils

import io.github.pangju666.commons.compress.io.resource.ZstdResource
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream
import org.apache.commons.compress.compressors.zstandard.ZstdConstants
import spock.lang.Specification

class ZstdUtilsSpec extends Specification {

	def "压缩输入流到输出流成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
		     OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.compress(inputStream, out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到输出流指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
		     OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.compress(inputStream, out, ZstdConstants.ZSTD_CLEVEL_DEFAULT)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到输出流成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.compress(new IOResource(inputFile), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到输出流指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.compress(new IOResource(inputFile), out, ZstdConstants.ZSTD_CLEVEL_DEFAULT)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到文件成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			ZstdUtils.compress(inputStream, outputFile)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到文件指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			ZstdUtils.compress(inputStream, outputFile, ZstdConstants.ZSTD_CLEVEL_DEFAULT)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到文件成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		ZstdUtils.compress(new IOResource(inputFile), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到文件指定压缩级别成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		ZstdUtils.compress(new IOResource(inputFile), outputFile, ZstdConstants.ZSTD_CLEVEL_DEFAULT)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "从ZstdResource解压到输出流成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File zstFile = new File("src/test/resources/test.zst")
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.uncompress(new ZstdResource(zstFile), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "从ZstdResource解压到文件成功"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File zstFile = new File("src/test/resources/test.zst")
		File outputFile = new File(work, "output.txt")

		when:
		ZstdUtils.uncompress(new ZstdResource(zstFile), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩null输入流抛出异常"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File outputFile = new File(work, "output.zst")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.compress(null as InputStream, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出流抛出异常"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			ZstdUtils.compress(inputStream, null as OutputStream)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null IOResource抛出异常"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File outputFile = new File(work, "output.zst")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.compress(null as IOResource, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出文件抛出异常"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			ZstdUtils.compress(inputStream, null as File)
		}

		then:
		thrown(NullPointerException)
	}

	def "解压null输入流抛出异常"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.uncompress(null, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "解压null ZstdResource抛出异常"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			ZstdUtils.uncompress(null as ZstdResource, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩使用已存在的ZstdCompressorOutputStream"() {
		setup:
		File work = new File("target/test-work/zstd")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.zst")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
		     FileOutputStream fos = new FileOutputStream(outputFile);
		     ZstdCompressorOutputStream zos = ZstdCompressorOutputStream.builder()
				 .setOutputStream(fos)
				 .get()) {
			ZstdUtils.compress(inputStream, zos)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}
}
