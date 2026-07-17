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

import com.deepoove.poi.XWPFTemplate
import com.deepoove.poi.config.Configure
import io.github.pangju666.commons.io.exception.UnsupportedResourceException
import io.github.pangju666.commons.io.resource.IOResource
import org.apache.poi.xwpf.usermodel.XWPFDocument
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class DocxResourceSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_DOCX_FILE = "src/test/resources/test.docx"

	def "使用文件路径创建 DocxResource - 正常情况"() {
		given:
		def filePath = TEST_DOCX_FILE

		when:
		def resource = new DocxResource(filePath)

		then:
		resource != null
		resource instanceof DocxResource

		cleanup:
		resource?.close()
	}

	def "使用文件路径创建 DocxResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		when:
		new DocxResource(txtFile.absolutePath)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用 File 对象创建 DocxResource - 正常情况"() {
		given:
		def file = new File(TEST_DOCX_FILE)

		when:
		def resource = new DocxResource(file)

		then:
		resource != null
		resource instanceof DocxResource

		cleanup:
		resource?.close()
	}

	def "使用 File 对象创建 DocxResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		when:
		new DocxResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用字节数组创建 DocxResource - 正常情况"() {
		given:
		def bytes = new File(TEST_DOCX_FILE).bytes

		when:
		def resource = new DocxResource(bytes)

		then:
		resource != null
		resource instanceof DocxResource

		cleanup:
		resource?.close()
	}

	def "使用字节数组创建 DocxResource - 不支持的格式抛异常"() {
		given:
		def bytes = "Hello World".bytes

		when:
		new DocxResource(bytes)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用输入流创建 DocxResource - 正常情况"() {
		given:
		def file = new File(TEST_DOCX_FILE)
		def inputStream = new FileInputStream(file)

		when:
		def resource = new DocxResource(inputStream)

		then:
		resource != null
		resource instanceof DocxResource

		cleanup:
		inputStream.close()
		resource?.close()
	}

	def "使用输入流创建 DocxResource - 不支持的格式抛异常"() {
		given:
		def inputStream = new ByteArrayInputStream("Hello World".bytes)

		when:
		new DocxResource(inputStream)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用 IOResource 创建 DocxResource - 正常情况"() {
		given:
		def file = new File(TEST_DOCX_FILE)
		def originalResource = new DocxResource(file)

		when:
		def resource = new DocxResource(originalResource)

		then:
		resource != null
		resource instanceof DocxResource

		cleanup:
		originalResource?.close()
		resource?.close()
	}

	def "使用 IOResource 创建 DocxResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"
		def originalResource = new IOResource(txtFile)

		when:
		new DocxResource(originalResource)

		then:
		thrown(UnsupportedResourceException)

		cleanup:
		originalResource?.close()
	}

	def "getDocument - 正常情况"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))

		when:
		def document = resource.getDocument()

		then:
		document != null
		document instanceof XWPFDocument

		cleanup:
		resource?.close()
	}

	def "getDocument - 懒加载，多次调用返回同一实例"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))

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
		def resource = new DocxResource(new File(TEST_DOCX_FILE))
		resource.close()

		when:
		resource.getDocument()

		then:
		thrown(IllegalStateException)
	}

	def "compileTemplate - 正常情况"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))

		when:
		def template = resource.compileTemplate()

		then:
		template != null
		template instanceof XWPFTemplate

		cleanup:
		template?.close()
		resource?.close()
	}

	def "compileTemplate - 懒加载，多次调用返回同一实例"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))

		when:
		def template1 = resource.compileTemplate()
		def template2 = resource.compileTemplate()

		then:
		template1 != null
		template1 == template2

		cleanup:
		template1?.close()
		resource?.close()
	}

	def "compileTemplate - 使用文档对象编译"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))
		resource.getDocument()

		when:
		def template = resource.compileTemplate()

		then:
		template != null
		template instanceof XWPFTemplate

		cleanup:
		template?.close()
		resource?.close()
	}

	def "compileTemplate(Configure) - 正常情况"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))
		def configure = Configure.builder().build()

		when:
		def template = resource.compileTemplate(configure)

		then:
		template != null
		template instanceof XWPFTemplate

		cleanup:
		template?.close()
		resource?.close()
	}

	def "compileTemplate(Configure) - null 配置抛异常"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))

		when:
		resource.compileTemplate(null)

		then:
		thrown(NullPointerException)

		cleanup:
		resource?.close()
	}

	def "compileTemplate(Configure) - 懒加载，多次调用返回同一实例"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))
		def configure = Configure.builder().build()

		when:
		def template1 = resource.compileTemplate(configure)
		def template2 = resource.compileTemplate(configure)

		then:
		template1 != null
		template1 == template2

		cleanup:
		template1?.close()
		resource?.close()
	}

	def "compileTemplate(Configure) - 使用文档对象编译"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))
		def configure = Configure.builder().build()
		resource.getDocument()

		when:
		def template = resource.compileTemplate(configure)

		then:
		template != null
		template instanceof XWPFTemplate

		cleanup:
		template?.close()
		resource?.close()
	}

	def "compileTemplate - 关闭后调用抛异常"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))
		resource.close()

		when:
		resource.compileTemplate()

		then:
		thrown(IllegalStateException)
	}

	def "compileTemplate(Configure) - 关闭后调用抛异常"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))
		def configure = Configure.builder().build()
		resource.close()

		when:
		resource.compileTemplate(configure)

		then:
		thrown(IllegalStateException)
	}

	def "getDocument - 使用输入流构建后获取文档"() {
		given:
		def file = new File(TEST_DOCX_FILE)
		def inputStream = new FileInputStream(file)
		def resource = new DocxResource(inputStream)

		when:
		def document = resource.getDocument()

		then:
		document != null
		document instanceof XWPFDocument

		cleanup:
		inputStream.close()
		resource?.close()
	}

	def "资源关闭后无法再次使用"() {
		given:
		def resource = new DocxResource(new File(TEST_DOCX_FILE))
		resource.close()

		when:
		resource.getDocument()

		then:
		thrown(IllegalStateException)
	}
}
