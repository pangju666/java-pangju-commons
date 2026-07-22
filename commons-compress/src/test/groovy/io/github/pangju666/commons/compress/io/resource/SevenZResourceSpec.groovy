package io.github.pangju666.commons.compress.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import spock.lang.Specification

class SevenZResourceSpec extends Specification {

	def "从IOResource构造SevenZResource成功"() {
		setup:
		IOResource resource = new IOResource(new File("src/test/resources/test.7z"))

		when:
		SevenZResource sevenZResource = new SevenZResource(resource)

		then:
		sevenZResource != null
	}

	def "从文件路径构造SevenZResource成功"() {
		when:
		SevenZResource sevenZResource = new SevenZResource("src/test/resources/test.7z")

		then:
		sevenZResource != null
	}

	def "从File构造SevenZResource成功"() {
		when:
		SevenZResource sevenZResource = new SevenZResource(new File("src/test/resources/test.7z"))

		then:
		sevenZResource != null
	}

	def "从字节数组构造SevenZResource成功"() {
		when:
		SevenZResource sevenZResource = new SevenZResource(new File("src/test/resources/test.7z").bytes)

		then:
		sevenZResource != null
	}

	def "从输入流构造SevenZResource成功"() {
		when:
		SevenZResource sevenZResource = new SevenZResource(new FileInputStream(new File("src/test/resources/test.7z")))

		then:
		sevenZResource != null
	}

	def "构造SevenZResource时非7Z文件抛出异常"() {
		setup:
		File work = new File("target/test-work/sevenz-resource")
		work.mkdirs()
		File txtFile = new File(work, "test.txt")
		txtFile.text = "test content"

		when:
		new SevenZResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "从文件路径构造加密SevenZResource成功"() {
		when:
		SevenZResource sevenZResource = new SevenZResource("src/test/resources/test-password.7z", "password")

		then:
		sevenZResource != null
	}

	def "从File构造加密SevenZResource成功"() {
		when:
		SevenZResource sevenZResource = new SevenZResource(new File("src/test/resources/test-password.7z"), "password")

		then:
		sevenZResource != null
	}

	def "构造加密SevenZResource时密码为空抛出异常"() {
		when:
		new SevenZResource(new File("src/test/resources/test-password.7z"), "")

		then:
		thrown(IllegalArgumentException)
	}

	def "openSevenZFile成功打开7Z文件"() {
		setup:
		SevenZResource sevenZResource = new SevenZResource(new File("src/test/resources/test.7z"))

		when:
		SevenZFile sevenZFileObj = sevenZResource.openSevenZFile()

		then:
		sevenZFileObj != null
		sevenZFileObj.getEntries().size() > 0

		cleanup:
		sevenZFileObj?.close()
	}

	def "openSevenZFile使用密码成功"() {
		setup:
		SevenZResource sevenZResource = new SevenZResource(new File("src/test/resources/test-password.7z"), "123456")

		when:
		SevenZFile sevenZFileObj = sevenZResource.openSevenZFile()

		then:
		sevenZFileObj != null

		cleanup:
		sevenZFileObj?.close()
	}

	def "资源关闭后打开7Z文件抛出异常"() {
		setup:
		SevenZResource sevenZResource = new SevenZResource(new File("src/test/resources/test.7z"))
		sevenZResource.close()

		when:
		sevenZResource.openSevenZFile()

		then:
		thrown(IllegalStateException)
	}
}
