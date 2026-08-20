package io.github.pangju666.commons.compress.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream
import spock.lang.Specification

class LZ4ResourceSpec extends Specification {

	def "从IOResource构造LZ4Resource成功"() {
		setup:
		IOResource resource = new IOResource(new File("src/test/resources/test.lz4"))

		when:
		LZ4Resource lZ4Resource = new LZ4Resource(resource)

		then:
		lZ4Resource != null
	}

	def "从文件路径构造LZ4Resource成功"() {
		when:
		LZ4Resource lZ4Resource = new LZ4Resource("src/test/resources/test.lz4")

		then:
		lZ4Resource != null
	}

	def "从File构造LZ4Resource成功"() {
		when:
		LZ4Resource lZ4Resource = new LZ4Resource(new File("src/test/resources/test.lz4"))

		then:
		lZ4Resource != null
	}

	def "从字节数组构造LZ4Resource成功"() {
		when:
		LZ4Resource lZ4Resource = new LZ4Resource(new File("src/test/resources/test.lz4").bytes)

		then:
		lZ4Resource != null
	}

	def "从输入流构造LZ4Resource成功"() {
		when:
		LZ4Resource lZ4Resource = new LZ4Resource(new FileInputStream(new File("src/test/resources/test.lz4")))

		then:
		lZ4Resource != null
	}

	def "构造LZ4Resource时非LZ4文件抛出异常"() {
		setup:
		File work = new File("target/test-work/lz4-resource")
		work.mkdirs()
		File txtFile = new File(work, "test.txt")
		txtFile.text = "test content"

		when:
		new LZ4Resource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "openGzipCompressorInputStream成功打开GZIP压缩输入流"() {
		setup:
		LZ4Resource lz4Resource = new LZ4Resource(new File("src/test/resources/test.lz4"))

		when:
		FramedLZ4CompressorInputStream inputStream = lz4Resource.openFramedLZ4CompressorInputStream()

		then:
		inputStream != null
		inputStream.read() > -1

		cleanup:
		inputStream?.close()
	}

	def "资源关闭后打开GZIP压缩输入流抛出异常"() {
		setup:
		LZ4Resource lz4Resource = new LZ4Resource(new File("src/test/resources/test.lz4"))
		lz4Resource.close()

		when:
		lz4Resource.openFramedLZ4CompressorInputStream()

		then:
		thrown(IllegalStateException)
	}
}
