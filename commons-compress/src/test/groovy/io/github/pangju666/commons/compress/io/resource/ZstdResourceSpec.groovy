package io.github.pangju666.commons.compress.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import spock.lang.Specification

class ZstdResourceSpec extends Specification {

	def "从IOResource构造ZstdResource成功"() {
		setup:
		IOResource resource = new IOResource(new File("src/test/resources/test.zst"))

		when:
		ZstdResource zstdResource = new ZstdResource(resource)

		then:
		zstdResource != null
	}

	def "从文件路径构造ZstdResource成功"() {
		when:
		ZstdResource zstdResource = new ZstdResource("src/test/resources/test.zst")

		then:
		zstdResource != null
	}

	def "从File构造ZstdResource成功"() {
		when:
		ZstdResource zstdResource = new ZstdResource(new File("src/test/resources/test.zst"))

		then:
		zstdResource != null
	}

	def "从字节数组构造ZstdResource成功"() {
		when:
		ZstdResource zstdResource = new ZstdResource(new File("src/test/resources/test.zst").bytes)

		then:
		zstdResource != null
	}

	def "从输入流构造ZstdResource成功"() {
		when:
		ZstdResource zstdResource = new ZstdResource(new FileInputStream(new File("src/test/resources/test.zst")))

		then:
		zstdResource != null
	}

	def "构造ZstdResource时非Zstd文件抛出异常"() {
		setup:
		File work = new File("target/test-work/zstd-resource")
		work.mkdirs()
		File txtFile = new File(work, "test.txt")
		txtFile.text = "test content"

		when:
		new ZstdResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "openZstdCompressorInputStream成功打开Zstd压缩输入流"() {
		setup:
		ZstdResource zstdResource = new ZstdResource(new File("src/test/resources/test.zst"))

		when:
		ZstdCompressorInputStream inputStream = zstdResource.openZstdCompressorInputStream()

		then:
		inputStream != null
		inputStream.read() > -1

		cleanup:
		inputStream?.close()
	}

	def "资源关闭后打开Zstd压缩输入流抛出异常"() {
		setup:
		ZstdResource zstdResource = new ZstdResource(new File("src/test/resources/test.zst"))
		zstdResource.close()

		when:
		zstdResource.openZstdCompressorInputStream()

		then:
		thrown(IllegalStateException)
	}
}
