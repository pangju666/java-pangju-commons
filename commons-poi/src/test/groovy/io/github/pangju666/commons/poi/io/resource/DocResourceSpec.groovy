/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.commons.poi.io.resource

import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.poi.hwpf.HWPFDocument
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class DocResourceSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_DOC_FILE = "src/test/resources/test.doc"

	def "使用文件路径创建 DocResource - 正常情况"() {
		given:
		def filePath = TEST_DOC_FILE

		when:
		def resource = new DocResource(filePath)

		then:
		resource != null
		resource instanceof DocResource

		cleanup:
		resource?.close()
	}

	def "使用文件路径创建 DocResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		when:
		new DocResource(txtFile.absolutePath)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用 File 对象创建 DocResource - 正常情况"() {
		given:
		def file = new File(TEST_DOC_FILE)

		when:
		def resource = new DocResource(file)

		then:
		resource != null
		resource instanceof DocResource

		cleanup:
		resource?.close()
	}

	def "使用 File 对象创建 DocResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		when:
		new DocResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用字节数组创建 DocResource - 正常情况"() {
		given:
		def bytes = new File(TEST_DOC_FILE).bytes

		when:
		def resource = new DocResource(bytes)

		then:
		resource != null
		resource instanceof DocResource

		cleanup:
		resource?.close()
	}

	def "使用字节数组创建 DocResource - 不支持的格式抛异常"() {
		given:
		def bytes = "Hello World".bytes

		when:
		new DocResource(bytes)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用输入流创建 DocResource - 正常情况"() {
		given:
		def file = new File(TEST_DOC_FILE)
		def inputStream = new FileInputStream(file)

		when:
		def resource = new DocResource(inputStream)

		then:
		resource != null
		resource instanceof DocResource

		cleanup:
		inputStream.close()
		resource?.close()
	}

	def "使用输入流创建 DocResource - 不支持的格式抛异常"() {
		given:
		def inputStream = new ByteArrayInputStream("Hello World".bytes)

		when:
		new DocResource(inputStream)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用 IOResource 创建 DocResource - 正常情况"() {
		given:
		def file = new File(TEST_DOC_FILE)
		def originalResource = new DocResource(file)

		when:
		def resource = new DocResource(originalResource)

		then:
		resource != null
		resource instanceof DocResource

		cleanup:
		originalResource?.close()
		resource?.close()
	}

	def "使用 IOResource 创建 DocResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"
		def originalResource = new IOResource(txtFile)

		when:
		new DocResource(originalResource)

		then:
		thrown(UnsupportedResourceException)

		cleanup:
		originalResource?.close()
	}

	def "getDocument - 正常情况"() {
		given:
		def resource = new DocResource(new File(TEST_DOC_FILE))

		when:
		def document = resource.getDocument()

		then:
		document != null
		document instanceof HWPFDocument

		cleanup:
		resource?.close()
	}

	def "getDocument - 懒加载，多次调用返回同一实例"() {
		given:
		def resource = new DocResource(new File(TEST_DOC_FILE))

		when:
		def document1 = resource.getDocument()
		def document2 = resource.getDocument()

		then:
		document1 != null
		document1 == document2

		cleanup:
		resource?.close()
	}

	def "getDocument - 关闭后调用抛异常"() {
		given:
		def resource = new DocResource(new File(TEST_DOC_FILE))
		resource.close()

		when:
		resource.getDocument()

		then:
		thrown(IOException)
	}

	def "getDocument - 使用输入流构建后获取文档"() {
		given:
		def file = new File(TEST_DOC_FILE)
		def inputStream = new FileInputStream(file)
		def resource = new DocResource(inputStream)

		when:
		def document = resource.getDocument()

		then:
		document != null
		document instanceof HWPFDocument

		cleanup:
		inputStream.close()
		resource?.close()
	}

	def "资源关闭后无法再次使用"() {
		given:
		def resource = new DocResource(new File(TEST_DOC_FILE))
		resource.close()

		when:
		resource.getDocument()

		then:
		thrown(IOException)
	}
}
