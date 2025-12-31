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

import org.apache.poi.hwpf.HWPFDocument
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class HWPFDocumentUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	def "isDoc(File) 应该正确识别 DOC 文件"() {
		given:
		def docFile = new File("src/test/resources/test.doc")
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		expect:
		HWPFDocumentUtils.isDoc(docFile)
		!HWPFDocumentUtils.isDoc(txtFile)
	}

	def "isDoc(byte[]) 应该正确识别 DOC 内容"() {
		given:
		def docBytes = new File("src/test/resources/test.doc").bytes
		def txtBytes = "Hello World".bytes
		def emptyBytes = new byte[0]

		expect:
		HWPFDocumentUtils.isDoc(docBytes)
		!HWPFDocumentUtils.isDoc(txtBytes)
		!HWPFDocumentUtils.isDoc(emptyBytes)
	}

	def "isDoc(InputStream) 应该正确识别 DOC 流"() {
		given:
		def docFile = new File("src/test/resources/test.doc")
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		expect:
		docFile.withInputStream { HWPFDocumentUtils.isDoc(it) }
		!txtFile.withInputStream { HWPFDocumentUtils.isDoc(it) }
		!HWPFDocumentUtils.isDoc((InputStream) null)
	}

	def "getDocument(File) 应该正确加载 DOC 文档"() {
		given:
		def docFile = new File("src/test/resources/test.doc")

		when:
		def document = HWPFDocumentUtils.getDocument(docFile)

		then:
		document != null
		document instanceof HWPFDocument
	}

	def "getDocument(File) 处理非 DOC 文件应抛出异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		when:
		HWPFDocumentUtils.getDocument(txtFile)

		then:
		thrown(IllegalArgumentException)
	}

	def "getDocument(byte[]) 应该正确加载 DOC 文档"() {
		given:
		def docBytes = new File("src/test/resources/test.doc").bytes

		when:
		def document = HWPFDocumentUtils.getDocument(docBytes)

		then:
		document != null
		document instanceof HWPFDocument
	}

	def "getDocument(byte[]) 处理非 DOC 字节应抛出异常"() {
		given:
		def txtBytes = "Hello World".bytes

		when:
		HWPFDocumentUtils.getDocument(txtBytes)

		then:
		thrown(IllegalArgumentException)
	}

	def "getDocument(byte[]) 处理空字节应抛出异常"() {
		when:
		HWPFDocumentUtils.getDocument((byte[]) null)

		then:
		thrown(IllegalArgumentException)

		when:
		HWPFDocumentUtils.getDocument(new byte[0])

		then:
		thrown(IllegalArgumentException)
	}

	def "getDocument(File) 处理 null 文件应抛出异常"() {
		when:
		HWPFDocumentUtils.getDocument((File) null)

		then:
		thrown(NullPointerException) // FileUtils.checkFile throws NullPointerException or IllegalArgumentException
	}

	def "getDocument(File) 处理目录应抛出异常"() {
		given:
		def dir = tempDir.resolve("testDir").toFile()
		dir.mkdirs()

		when:
		HWPFDocumentUtils.getDocument(dir)

		then:
		thrown(IllegalArgumentException)
	}
}
