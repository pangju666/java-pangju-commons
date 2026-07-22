package io.github.pangju666.commons.compress.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarFile
import spock.lang.Specification

import java.nio.charset.StandardCharsets

class TarResourceSpec extends Specification {

	def "从IOResource构造TarResource成功"() {
		setup:
		IOResource resource = new IOResource(new File("src/test/resources/test.tar"))

		when:
		TarResource tarResource = new TarResource(resource)

		then:
		tarResource != null
	}

	def "从文件路径构造TarResource成功"() {
		when:
		TarResource tarResource = new TarResource("src/test/resources/test.tar")

		then:
		tarResource != null
	}

	def "从File构造TarResource成功"() {
		when:
		TarResource tarResource = new TarResource(new File("src/test/resources/test.tar"))

		then:
		tarResource != null
	}

	def "从字节数组构造TarResource成功"() {
		when:
		TarResource tarResource = new TarResource(new File("src/test/resources/test.tar").bytes)

		then:
		tarResource != null
	}

	def "从输入流构造TarResource成功"() {
		when:
		TarResource tarResource = new TarResource(new FileInputStream(new File("src/test/resources/test.tar")))

		then:
		tarResource != null
	}

	def "构造TarResource时非TAR文件抛出异常"() {
		setup:
		File work = new File("target/test-work/tar-resource")
		work.mkdirs()
		File txtFile = new File(work, "test.txt")
		txtFile.text = "test content"

		when:
		new TarResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从文件路径构造TarResource并指定编码成功"() {
		when:
		TarResource tarResource = new TarResource("src/test/resources/test.tar", StandardCharsets.UTF_8)

		then:
		tarResource != null
	}

	def "从File构造TarResource并指定编码成功"() {
		when:
		TarResource tarResource = new TarResource(new File("src/test/resources/test.tar"), StandardCharsets.UTF_8)

		then:
		tarResource != null
	}

	def "openTarArchiveInputStream成功打开TAR归档输入流"() {
		setup:
		TarResource tarResource = new TarResource(new File("src/test/resources/test.tar"))

		when:
		TarArchiveInputStream inputStream = tarResource.openTarArchiveInputStream()

		then:
		inputStream != null

		cleanup:
		inputStream?.close()
	}

	def "openTarArchiveInputStream使用指定编码成功"() {
		setup:
		TarResource tarResource = new TarResource(new File("src/test/resources/test.tar"), StandardCharsets.UTF_8)

		when:
		TarArchiveInputStream inputStream = tarResource.openTarArchiveInputStream()

		then:
		inputStream != null

		cleanup:
		inputStream?.close()
	}

	def "openTarFile成功打开TAR文件"() {
		setup:
		TarResource tarResource = new TarResource(new File("src/test/resources/test.tar"))

		when:
		TarFile tarFileObj = tarResource.openTarFile()

		then:
		tarFileObj != null
		tarFileObj.getEntries().size() > 0

		cleanup:
		tarFileObj?.close()
	}

	def "资源关闭后打开TAR归档输入流抛出异常"() {
		setup:
		TarResource tarResource = new TarResource(new File("src/test/resources/test.tar"))
		tarResource.close()

		when:
		tarResource.openTarArchiveInputStream()

		then:
		thrown(IllegalStateException)
	}

	def "资源关闭后打开TAR文件抛出异常"() {
		setup:
		TarResource tarResource = new TarResource(new File("src/test/resources/test.tar"))
		tarResource.close()

		when:
		tarResource.openTarFile()

		then:
		thrown(IllegalStateException)
	}
}
