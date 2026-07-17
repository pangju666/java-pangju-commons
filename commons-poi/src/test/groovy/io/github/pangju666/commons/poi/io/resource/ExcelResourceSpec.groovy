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
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Specification
import spock.lang.TempDir

import java.nio.file.Path

class ExcelResourceSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_XLS_FILE = "src/test/resources/test.xls"
	static final String TEST_XLSX_FILE = "src/test/resources/test.xlsx"

	def "使用文件路径创建 ExcelResource - XLS 格式"() {
		given:
		def filePath = TEST_XLS_FILE

		when:
		def resource = new ExcelResource(filePath)

		then:
		resource != null
		resource instanceof ExcelResource
		resource.isXls()

		cleanup:
		resource?.close()
	}

	def "使用文件路径创建 ExcelResource - XLSX 格式"() {
		given:
		def filePath = TEST_XLSX_FILE

		when:
		def resource = new ExcelResource(filePath)

		then:
		resource != null
		resource instanceof ExcelResource
		resource.isXlsx()

		cleanup:
		resource?.close()
	}

	def "使用文件路径创建 ExcelResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		when:
		new ExcelResource(txtFile.absolutePath)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用 File 对象创建 ExcelResource - XLS 格式"() {
		given:
		def file = new File(TEST_XLS_FILE)

		when:
		def resource = new ExcelResource(file)

		then:
		resource != null
		resource instanceof ExcelResource
		resource.isXls()

		cleanup:
		resource?.close()
	}

	def "使用 File 对象创建 ExcelResource - XLSX 格式"() {
		given:
		def file = new File(TEST_XLSX_FILE)

		when:
		def resource = new ExcelResource(file)

		then:
		resource != null
		resource instanceof ExcelResource
		resource.isXlsx()

		cleanup:
		resource?.close()
	}

	def "使用 File 对象创建 ExcelResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"

		when:
		new ExcelResource(txtFile)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用字节数组创建 ExcelResource - XLS 格式"() {
		given:
		def bytes = new File(TEST_XLS_FILE).bytes

		when:
		def resource = new ExcelResource(bytes)

		then:
		resource != null
		resource instanceof ExcelResource
		resource.isXls()

		cleanup:
		resource?.close()
	}

	def "使用字节数组创建 ExcelResource - XLSX 格式"() {
		given:
		def bytes = new File(TEST_XLSX_FILE).bytes

		when:
		def resource = new ExcelResource(bytes)

		then:
		resource != null
		resource instanceof ExcelResource
		resource.isXlsx()

		cleanup:
		resource?.close()
	}

	def "使用字节数组创建 ExcelResource - 不支持的格式抛异常"() {
		given:
		def bytes = "Hello World".bytes

		when:
		new ExcelResource(bytes)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用输入流创建 ExcelResource - XLS 格式"() {
		given:
		def file = new File(TEST_XLS_FILE)
		def inputStream = new FileInputStream(file)

		when:
		def resource = new ExcelResource(inputStream)

		then:
		resource != null
		resource instanceof ExcelResource
		resource.isXls()

		cleanup:
		inputStream.close()
		resource?.close()
	}

	def "使用输入流创建 ExcelResource - XLSX 格式"() {
		given:
		def file = new File(TEST_XLSX_FILE)
		def inputStream = new FileInputStream(file)

		when:
		def resource = new ExcelResource(inputStream)

		then:
		resource != null
		resource instanceof ExcelResource
		resource.isXlsx()

		cleanup:
		inputStream.close()
		resource?.close()
	}

	def "使用输入流创建 ExcelResource - 不支持的格式抛异常"() {
		given:
		def inputStream = new ByteArrayInputStream("Hello World".bytes)

		when:
		new ExcelResource(inputStream)

		then:
		thrown(UnsupportedResourceException)
	}

	def "使用 IOResource 创建 ExcelResource - 正常情况"() {
		given:
		def file = new File(TEST_XLS_FILE)
		def originalResource = new ExcelResource(file)

		when:
		def resource = new ExcelResource(originalResource)

		then:
		resource != null
		resource instanceof ExcelResource

		cleanup:
		originalResource?.close()
		resource?.close()
	}

	def "使用 IOResource 创建 ExcelResource - 不支持的格式抛异常"() {
		given:
		def txtFile = tempDir.resolve("test.txt").toFile()
		txtFile.text = "Hello World"
		def originalResource = new IOResource(txtFile)

		when:
		new ExcelResource(originalResource)

		then:
		thrown(UnsupportedResourceException)

		cleanup:
		originalResource?.close()
	}

	def "getWorkbook - XLS 格式"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLS_FILE))

		when:
		def workbook = resource.getWorkbook()

		then:
		workbook != null
		workbook instanceof HSSFWorkbook

		cleanup:
		resource?.close()
	}

	def "getWorkbook - XLSX 格式"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLSX_FILE))

		when:
		def workbook = resource.getWorkbook()

		then:
		workbook != null
		workbook instanceof XSSFWorkbook

		cleanup:
		resource?.close()
	}

	def "getWorkbook - 懒加载，多次调用返回同一实例"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLS_FILE))

		when:
		def workbook1 = resource.getWorkbook()
		def workbook2 = resource.getWorkbook()

		then:
		workbook1 != null
		workbook1 == workbook2

		cleanup:
		resource?.close()
	}

	def "getWorkbook - 关闭后调用抛异常"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLS_FILE))
		resource.close()

		when:
		resource.getWorkbook()

		then:
		thrown(IOException)
	}

	def "getWorkbook - 使用输入流构建后获取工作簿 - XLS 格式"() {
		given:
		def file = new File(TEST_XLS_FILE)
		def inputStream = new FileInputStream(file)
		def resource = new ExcelResource(inputStream)

		when:
		def workbook = resource.getWorkbook()

		then:
		workbook != null
		workbook instanceof HSSFWorkbook

		cleanup:
		inputStream.close()
		resource?.close()
	}

	def "getWorkbook - 使用输入流构建后获取工作簿 - XLSX 格式"() {
		given:
		def file = new File(TEST_XLSX_FILE)
		def inputStream = new FileInputStream(file)
		def resource = new ExcelResource(inputStream)

		when:
		def workbook = resource.getWorkbook()

		then:
		workbook != null
		workbook instanceof XSSFWorkbook

		cleanup:
		inputStream.close()
		resource?.close()
	}

	def "isXls - XLS 格式返回 true"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLS_FILE))

		expect:
		resource.isXls()

		cleanup:
		resource?.close()
	}

	def "isXls - XLSX 格式返回 false"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLSX_FILE))

		expect:
		!resource.isXls()

		cleanup:
		resource?.close()
	}

	def "isXlsx - XLSX 格式返回 true"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLSX_FILE))

		expect:
		resource.isXlsx()

		cleanup:
		resource?.close()
	}

	def "isXlsx - XLS 格式返回 false"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLS_FILE))

		expect:
		!resource.isXlsx()

		cleanup:
		resource?.close()
	}

	def "资源关闭后无法再次使用"() {
		given:
		def resource = new ExcelResource(new File(TEST_XLS_FILE))
		resource.close()

		when:
		resource.getWorkbook()

		then:
		thrown(IOException)
	}
}
