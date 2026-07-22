package io.github.pangju666.commons.compress.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.archivers.zip.ZipFile
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class ZipResourceSpec extends Specification {

	def "从IOResource构造ZipResource成功"() {
		setup:
		IOResource resource = new IOResource(new File("src/test/resources/test.zip"))

		when:
		ZipResource zipResource = new ZipResource(resource)

		then:
		zipResource != null
	}

	def "从文件路径构造ZipResource成功"() {
		when:
		ZipResource zipResource = new ZipResource("src/test/resources/test.zip")

		then:
		zipResource != null
	}

	def "从File构造ZipResource成功"() {
		when:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"))

		then:
		zipResource != null
	}

	def "从字节数组构造ZipResource成功"() {
		when:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip").bytes)

		then:
		zipResource != null
	}

	def "从输入流构造ZipResource成功"() {
		when:
		ZipResource zipResource = new ZipResource(new FileInputStream(new File("src/test/resources/test.zip")))

		then:
		zipResource != null
	}

	def "构造ZipResource时非ZIP文件抛出异常"() {
		setup:
		File work = new File("target/test-work/zip-resource")
		work.mkdirs()
		File txtFile = new File(work, "test.txt")
		txtFile.text = "test content"

		when:
		new ZipResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从文件路径构造ZipResource并设置Unicode扩展字段成功"() {
		when:
		ZipResource zipResource = new ZipResource("src/test/resources/test.zip", false)

		then:
		zipResource != null
	}

	def "从File构造ZipResource并设置Unicode扩展字段成功"() {
		when:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"), false)

		then:
		zipResource != null
	}

	def "从文件路径构造ZipResource并指定编码成功"() {
		when:
		ZipResource zipResource = new ZipResource("src/test/resources/test.zip", StandardCharsets.UTF_8)

		then:
		zipResource != null
	}

	def "从File构造ZipResource并指定编码成功"() {
		when:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"), StandardCharsets.UTF_8)

		then:
		zipResource != null
	}

	def "从File构造ZipResource并指定编码和Unicode扩展字段成功"() {
		when:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"), StandardCharsets.UTF_8, false)

		then:
		zipResource != null
	}

	def "openZipArchiveInputStream成功打开ZIP归档输入流"() {
		setup:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"))

		when:
		ZipArchiveInputStream inputStream = zipResource.openZipArchiveInputStream()

		then:
		inputStream != null

		cleanup:
		inputStream?.close()
	}

	def "openZipArchiveInputStream使用指定参数成功"() {
		setup:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"))

		when:
		ZipArchiveInputStream inputStream = zipResource.openZipArchiveInputStream(true)

		then:
		inputStream != null

		cleanup:
		inputStream?.close()
	}

	def "openZipFile成功打开ZIP文件"() {
		setup:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"))

		when:
		ZipFile zipFileObj = zipResource.openZipFile()

		then:
		zipFileObj != null
		zipFileObj.getEntries().asIterator().size() > 0

		cleanup:
		zipFileObj?.close()
	}

	def "openZipFile使用指定参数成功"() {
		setup:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"))

		when:
		ZipFile zipFileObj = zipResource.openZipFile(false)

		then:
		zipFileObj != null
		zipFileObj.getEntries().asIterator().size() > 0

		cleanup:
		zipFileObj?.close()
	}

	def "资源关闭后打开ZIP归档输入流抛出异常"() {
		setup:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"))
		zipResource.close()

		when:
		zipResource.openZipArchiveInputStream()

		then:
		thrown(IllegalStateException)
	}

	def "资源关闭后打开ZIP文件抛出异常"() {
		setup:
		ZipResource zipResource = new ZipResource(new File("src/test/resources/test.zip"))
		zipResource.close()

		when:
		zipResource.openZipFile()

		then:
		thrown(IllegalStateException)
	}
}
