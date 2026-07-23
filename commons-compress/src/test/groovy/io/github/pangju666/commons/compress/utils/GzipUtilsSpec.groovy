package io.github.pangju666.commons.compress.utils

import io.github.pangju666.commons.compress.io.resource.GzipResource
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipParameters
import spock.lang.Specification

class GzipUtilsSpec extends Specification {

	def "压缩输入流到输出流成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
		     OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.compress(inputStream, out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到输出流指定参数成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")
		GzipParameters parameters = new GzipParameters()

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
		     OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.compress(inputStream, out, parameters)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到输出流成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.compress(new IOResource(inputFile), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到输出流指定参数成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")
		GzipParameters parameters = new GzipParameters()

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.compress(new IOResource(inputFile), out, parameters)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到文件成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			GzipUtils.compress(inputStream, outputFile)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩输入流到文件指定参数成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")
		GzipParameters parameters = new GzipParameters()

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			GzipUtils.compress(inputStream, outputFile, parameters)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到文件成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")

		when:
		GzipUtils.compress(new IOResource(inputFile), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩IOResource到文件指定参数成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")
		GzipParameters parameters = new GzipParameters()

		when:
		GzipUtils.compress(new IOResource(inputFile), outputFile, parameters)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "从GzipResource解压到输出流成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File gzFile = new File("src/test/resources/test.gz")
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.uncompress(new GzipResource(gzFile), out)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "从GzipResource解压到文件成功"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File gzFile = new File("src/test/resources/test.gz")
		File outputFile = new File(work, "output.txt")

		when:
		GzipUtils.uncompress(new GzipResource(gzFile), outputFile)

		then:
		outputFile.exists()
		outputFile.length() > 0
	}

	def "压缩null输入流抛出异常"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File outputFile = new File(work, "output.gz")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.compress(null as InputStream, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出流抛出异常"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			GzipUtils.compress(inputStream, null as OutputStream)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null参数抛出异常"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
		     OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.compress(inputStream, out, null)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null IOResource抛出异常"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File outputFile = new File(work, "output.gz")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.compress(null as IOResource, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩null输出文件抛出异常"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"

		when:
		try (InputStream inputStream = new FileInputStream(inputFile)) {
			GzipUtils.compress(inputStream, null as File)
		}

		then:
		thrown(NullPointerException)
	}

	def "解压null输入流抛出异常"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.uncompress(null as GzipResource, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "解压null GzipResource抛出异常"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File outputFile = new File(work, "output.txt")

		when:
		try (OutputStream out = new FileOutputStream(outputFile)) {
			GzipUtils.uncompress(null as GzipResource, out)
		}

		then:
		thrown(NullPointerException)
	}

	def "压缩使用已存在的GzipCompressorOutputStream"() {
		setup:
		File work = new File("target/test-work/gzip")
		work.mkdirs()
		File inputFile = new File(work, "input.txt")
		inputFile.text = "test content"
		File outputFile = new File(work, "output.gz")

		when:
		try (InputStream inputStream = new FileInputStream(inputFile);
		     FileOutputStream fos = new FileOutputStream(outputFile);
		     GzipCompressorOutputStream gzos = new GzipCompressorOutputStream(fos)) {
			GzipUtils.compress(inputStream, gzos)
		}

		then:
		outputFile.exists()
		outputFile.length() > 0
	}
}
