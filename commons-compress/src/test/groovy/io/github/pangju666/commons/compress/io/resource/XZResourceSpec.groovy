package io.github.pangju666.commons.compress.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import spock.lang.Specification

class XZResourceSpec extends Specification {

	def "从IOResource构造XZResource成功"() {
		setup:
		IOResource resource = new IOResource(new File("src/test/resources/test.xz"))

		when:
		XZResource xzResource = new XZResource(resource)

		then:
		xzResource != null
	}

	def "从文件路径构造XZResource成功"() {
		when:
		XZResource xzResource = new XZResource("src/test/resources/test.xz")

		then:
		xzResource != null
	}

	def "从File构造XZResource成功"() {
		when:
		XZResource xzResource = new XZResource(new File("src/test/resources/test.xz"))

		then:
		xzResource != null
	}

	def "从字节数组构造XZResource成功"() {
		when:
		XZResource xzResource = new XZResource(new File("src/test/resources/test.xz").bytes)

		then:
		xzResource != null
	}

	def "从输入流构造XZResource成功"() {
		when:
		XZResource xzResource = new XZResource(new FileInputStream(new File("src/test/resources/test.xz")))

		then:
		xzResource != null
	}

	def "构造XZResource时非XZ文件抛出异常"() {
		setup:
		File work = new File("target/test-work/xz-resource")
		work.mkdirs()
		File txtFile = new File(work, "test.txt")
		txtFile.text = "test content"

		when:
		new XZResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "openXZCompressorInputStream成功打开XZ压缩输入流"() {
		setup:
		XZResource xzResource = new XZResource(new File("src/test/resources/test.xz"))

		when:
		XZCompressorInputStream inputStream = xzResource.openXZCompressorInputStream()

		then:
		inputStream != null
		inputStream.read() > -1

		cleanup:
		inputStream?.close()
	}

	def "资源关闭后打开XZ压缩输入流抛出异常"() {
		setup:
		XZResource xzResource = new XZResource(new File("src/test/resources/test.xz"))
		xzResource.close()

		when:
		xzResource.openXZCompressorInputStream()

		then:
		thrown(IllegalStateException)
	}
}
