package io.github.pangju666.commons.compress.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import spock.lang.Specification

class GzipResourceSpec extends Specification {

	def "从IOResource构造GzipResource成功"() {
		setup:
		IOResource resource = new IOResource(new File("src/test/resources/test.gz"))

		when:
		GzipResource gzipResource = new GzipResource(resource)

		then:
		gzipResource != null
	}

	def "从文件路径构造GzipResource成功"() {
		when:
		GzipResource gzipResource = new GzipResource("src/test/resources/test.gz")

		then:
		gzipResource != null
	}

	def "从File构造GzipResource成功"() {
		when:
		GzipResource gzipResource = new GzipResource(new File("src/test/resources/test.gz"))

		then:
		gzipResource != null
	}

	def "从字节数组构造GzipResource成功"() {
		when:
		GzipResource gzipResource = new GzipResource(new File("src/test/resources/test.gz").bytes)

		then:
		gzipResource != null
	}

	def "从输入流构造GzipResource成功"() {
		when:
		GzipResource gzipResource = new GzipResource(new FileInputStream(new File("src/test/resources/test.gz")))

		then:
		gzipResource != null
	}

	def "构造GzipResource时非GZIP文件抛出异常"() {
		setup:
		File work = new File("target/test-work/gzip-resource")
		work.mkdirs()
		File txtFile = new File(work, "test.txt")
		txtFile.text = "test content"

		when:
		new GzipResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "openGzipCompressorInputStream成功打开GZIP压缩输入流"() {
		setup:
		GzipResource gzipResource = new GzipResource(new File("src/test/resources/test.gz"))

		when:
		GzipCompressorInputStream inputStream = gzipResource.openGzipCompressorInputStream()

		then:
		inputStream != null
		inputStream.read() > -1

		cleanup:
		inputStream?.close()
	}

	def "资源关闭后打开GZIP压缩输入流抛出异常"() {
		setup:
		GzipResource gzipResource = new GzipResource(new File("src/test/resources/test.gz"))
		gzipResource.close()

		when:
		gzipResource.openGzipCompressorInputStream()

		then:
		thrown(IllegalStateException)
	}
}
