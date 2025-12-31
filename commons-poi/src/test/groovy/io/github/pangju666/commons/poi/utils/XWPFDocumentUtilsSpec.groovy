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

package io.github.pangju666.commons.poi.utils

import org.apache.poi.xwpf.usermodel.XWPFDocument
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class XWPFDocumentUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	def "isDocx(File) 应该正确识别 DOCX 文件"() {
		given:
		def docxFile = new File("src/test/resources/test.docx")
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		expect:
		XWPFDocumentUtils.isDocx(docxFile)
		!XWPFDocumentUtils.isDocx(txtFile)
	}

	def "isDocx(byte[]) 应该正确识别 DOCX 内容"() {
		given:
		def docxBytes = new File("src/test/resources/test.docx").bytes
		def txtBytes = "Hello World".bytes
		def emptyBytes = new byte[0]

		expect:
		XWPFDocumentUtils.isDocx(docxBytes)
		!XWPFDocumentUtils.isDocx(txtBytes)
		!XWPFDocumentUtils.isDocx(emptyBytes)
	}

	def "isDocx(InputStream) 应该正确识别 DOCX 流"() {
		given:
		def docxFile = new File("src/test/resources/test.docx")
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		expect:
		docxFile.withInputStream { XWPFDocumentUtils.isDocx(it) }
		!txtFile.withInputStream { XWPFDocumentUtils.isDocx(it) }
		!XWPFDocumentUtils.isDocx((InputStream) null)
	}

	def "getDocument(File) 应该正确加载 DOCX 文档"() {
		given:
		def docxFile = new File("src/test/resources/test.docx")

		when:
		def document = XWPFDocumentUtils.getDocument(docxFile)

		then:
		document != null
		document instanceof XWPFDocument
	}

	def "getDocument(File) 处理非 DOCX 文件应抛出异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		when:
		XWPFDocumentUtils.getDocument(txtFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "getDocument(File) 处理 null 文件应抛出异常"() {
		when:
		XWPFDocumentUtils.getDocument((File) null)

		then:
		thrown(NullPointerException)
	}

	def "getDocument(File) 处理目录应抛出异常"() {
		given:
		def dir = tempDir.resolve("testDir").toFile()
		dir.mkdirs()

		when:
		XWPFDocumentUtils.getDocument(dir)

		then:
		thrown(IllegalArgumentException)
	}

	def "getDocument(byte[]) 应该正确加载 DOCX 文档"() {
		given:
		def docxBytes = new File("src/test/resources/test.docx").bytes

		when:
		def document = XWPFDocumentUtils.getDocument(docxBytes)

		then:
		document != null
		document instanceof XWPFDocument
	}

	def "getDocument(byte[]) 处理非 DOCX 字节应抛出异常"() {
		given:
		def txtBytes = "Hello World".bytes

		when:
		XWPFDocumentUtils.getDocument(txtBytes)

		then:
		thrown(IllegalArgumentException)
	}

	def "getDocument(byte[]) 处理空字节应抛出异常"() {
		when:
		XWPFDocumentUtils.getDocument((byte[]) null)

		then:
		thrown(IllegalArgumentException)

		when:
		XWPFDocumentUtils.getDocument(new byte[0])

		then:
		thrown(IllegalArgumentException)
	}
}
