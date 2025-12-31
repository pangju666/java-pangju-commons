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

import io.github.pangju666.commons.io.utils.FileUtils
import org.apache.commons.lang3.tuple.Pair
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.stream.Collectors

class WorkbookUtilsSpec extends Specification {
	@Shared
	File xlsFile
	@Shared
	File xlsxFile
	@Shared
	File txtFile

	def setupSpec() {
		// 使用类加载器获取资源路径，适配不同工作目录
		URL xlsUrl = getClass().getResource("/test.xls")
		URL xlsxUrl = getClass().getResource("/test.xlsx")
		URL txtUrl = getClass().getResource("/test.txt")

		if (xlsUrl == null || xlsxUrl == null || txtUrl == null) {
			throw new IllegalStateException("Missing test resources in classpath")
		}

		xlsFile = new File(xlsUrl.toURI())
		xlsxFile = new File(xlsxUrl.toURI())
		txtFile = new File(txtUrl.toURI())
	}

	@Unroll
	def "isXls 格式检查: #desc"() {
		expect:
		WorkbookUtils.isXls(input) == expected

		where:
		desc       | input    || expected
		"XLS文件"  | xlsFile  || true
		"XLSX文件" | xlsxFile || false
		"文本文件" | txtFile  || false
	}

	@Unroll
	def "isXls 字节数组检查: #desc"() {
		expect:
		WorkbookUtils.isXls(input) == expected

		where:
		desc         | input          || expected
		"XLS字节"    | xlsFile.bytes  || true
		"XLSX字节"   | xlsxFile.bytes || false
		"空字节数组" | new byte[0]    || false
	}

	@Unroll
	def "isXlsx 格式检查: #desc"() {
		expect:
		WorkbookUtils.isXlsx(input) == expected

		where:
		desc       | input    || expected
		"XLS文件"  | xlsFile  || false
		"XLSX文件" | xlsxFile || true
		"文本文件" | txtFile  || false
	}

	@Unroll
	def "isXlsx 字节数组检查: #desc"() {
		expect:
		WorkbookUtils.isXlsx(input) == expected

		where:
		desc         | input          || expected
		"XLS字节"    | xlsFile.bytes  || false
		"XLSX字节"   | xlsxFile.bytes || true
		"空字节数组" | new byte[0]    || false
	}

	@Unroll
	def "isWorkbook 格式检查: #desc"() {
		expect:
		WorkbookUtils.isWorkbook(input) == expected

		where:
		desc       | input    || expected
		"XLS文件"  | xlsFile  || true
		"XLSX文件" | xlsxFile || true
		"文本文件" | txtFile  || false
	}

	def "getWorkbook 加载测试"() {
		expect:
		WorkbookUtils.getWorkbook(xlsFile).withCloseable {
			it instanceof HSSFWorkbook
		}
		WorkbookUtils.getWorkbook(xlsxFile).withCloseable {
			it instanceof XSSFWorkbook
		}
	}

	def "getSheets 获取工作表列表"() {
		given:
		Workbook wb = new XSSFWorkbook()
		wb.createSheet("Sheet1")
		wb.createSheet("Sheet2")

		when:
		def sheets = WorkbookUtils.getSheets(wb)

		then:
		sheets.size() == 2
		sheets[0].sheetName == "Sheet1"
		sheets[1].sheetName == "Sheet2"

		when:
		def nullSheets = WorkbookUtils.getSheets(null)

		then:
		nullSheets.isEmpty()

		cleanup:
		wb.close()
	}

	def "getPhysicalRows 获取物理行"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)
		sheet.createRow(2) // 跳过 Row 1

		when:
		def rows = WorkbookUtils.getPhysicalRows(sheet)

		then:
		rows.size() == 2
		rows[0].rowNum == 0
		rows[1].rowNum == 2

		when:
		def nullRows = WorkbookUtils.getPhysicalRows(null)

		then:
		nullRows.isEmpty()

		cleanup:
		wb.close()
	}

	def "getLogicalRows 获取逻辑行 (包含空行)"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)
		sheet.createRow(2)
		// 此时 LastRowNum 应为 2，逻辑行应包括 0, 1, 2

		when:
		def rows = WorkbookUtils.getLogicalRows(sheet)

		then:
		rows.size() == 3
		rows[0] != null
		rows[1] == null
		rows[2] != null

		cleanup:
		wb.close()
	}

	def "getLogicalRows 指定起始行"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)
		sheet.createRow(1)
		sheet.createRow(2)
		sheet.createRow(3)

		when:
		def rows = WorkbookUtils.getLogicalRows(sheet, 1)

		then:
		rows.size() == 3 // 1, 2, 3
		rows[0].rowNum == 1
		rows[2].rowNum == 3

		cleanup:
		wb.close()
	}

	def "getLogicalRows 指定范围"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		for (int i = 0; i < 5; i++) {
			sheet.createRow(i)
		}

		when:
		def rows = WorkbookUtils.getLogicalRows(sheet, 1, 3)

		then:
		rows.size() == 3 // 1, 2, 3
		rows[0].rowNum == 1
		rows[2].rowNum == 3

		when: "范围超出 LastRowNum"
		def rowsOverflow = WorkbookUtils.getLogicalRows(sheet, 3, 10)

		then:
		rowsOverflow.size() == 2 // 3, 4 (LastRowNum is 4)
		rowsOverflow[0].rowNum == 3
		rowsOverflow[1].rowNum == 4

		cleanup:
		wb.close()
	}

	def "getPhysicalCells 获取物理单元格"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		row.createCell(0)
		row.createCell(2) // 跳过 Cell 1

		when:
		def cells = WorkbookUtils.getPhysicalCells(row)

		then:
		cells.size() == 2
		cells[0].columnIndex == 0
		cells[1].columnIndex == 2

		when:
		def nullCells = WorkbookUtils.getPhysicalCells(null)

		then:
		nullCells.isEmpty()

		cleanup:
		wb.close()
	}

	def "getLogicalCells 获取逻辑单元格"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		row.createCell(0)
		row.createCell(2)
		// LastCellNum 是 3 (下一个空闲索引)，所以逻辑单元格是 0, 1, 2

		when:
		def cells = WorkbookUtils.getLogicalCells(row)

		then:
		cells.size() == 3
		cells[0] != null
		cells[1] == null
		cells[2] != null

		cleanup:
		wb.close()
	}

	def "getLogicalCells 指定起始列"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		row.createCell(0)
		row.createCell(1)
		row.createCell(2)
		row.createCell(3)

		when:
		def cells = WorkbookUtils.getLogicalCells(row, 1)

		then:
		cells.size() == 3 // 1, 2, 3
		cells[0].columnIndex == 1
		cells[2].columnIndex == 3

		cleanup:
		wb.close()
	}

	def "getLogicalCells 指定范围"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		for (int i = 0; i < 5; i++) {
			row.createCell(i)
		}

		when:
		def cells = WorkbookUtils.getLogicalCells(row, 1, 3)

		then:
		cells.size() == 3 // 1, 2, 3
		cells[0].columnIndex == 1
		cells[2].columnIndex == 3

		when: "范围超出 LastCellNum"
		def cellsOverflow = WorkbookUtils.getLogicalCells(row, 3, 10)

		then:
		cellsOverflow.size() == 2 // 3, 4 (LastCellNum is 5)
		cellsOverflow[0].columnIndex == 3
		cellsOverflow[1].columnIndex == 4

		cleanup:
		wb.close()
	}

	def "Stream流测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		row.createCell(0)

		expect:
		WorkbookUtils.sheetStream(wb).count() == 1
		WorkbookUtils.physicalRowStream(sheet).count() == 1
		WorkbookUtils.physicalCellStream(row).count() == 1
		WorkbookUtils.sheetStream(null).count() == 0

		cleanup:
		wb.close()
	}

	def "getMergedRegionCell 合并单元格测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(1)
		Cell cell = row.createCell(1)
		cell.setCellValue("Merged")
		sheet.addMergedRegion(new CellRangeAddress(1, 2, 1, 2)) // B2:C3

		expect:
		WorkbookUtils.getMergedRegionCell(sheet, 1, 1).stringCellValue == "Merged"
		WorkbookUtils.getMergedRegionCell(sheet, 1, 2).stringCellValue == "Merged"
		WorkbookUtils.getMergedRegionCell(sheet, 2, 1).stringCellValue == "Merged"
		WorkbookUtils.getMergedRegionCell(sheet, 2, 2).stringCellValue == "Merged"
		WorkbookUtils.getMergedRegionCell(sheet, 0, 0) == null

		cleanup:
		wb.close()
	}

	def "isEmptyCell 判空测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		expect:
		WorkbookUtils.isEmptyCell(null)
		WorkbookUtils.isEmptyCell(row.createCell(0, CellType.BLANK))
		WorkbookUtils.isEmptyCell(row.createCell(1, CellType.STRING)) // Empty string
		WorkbookUtils.isEmptyCell(row.createCell(2, CellType.ERROR))

		when:
		Cell cell = row.createCell(3)
		cell.setCellValue("Data")

		then:
		!WorkbookUtils.isEmptyCell(cell)

		cleanup:
		wb.close()
	}

	def "getStringCellValue 字符串获取测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		when:
		Cell numCell = row.createCell(0)
		numCell.setCellValue(123.45)
		Cell boolCell = row.createCell(1)
		boolCell.setCellValue(true)
		Cell strCell = row.createCell(2)
		strCell.setCellValue("Text")

		then:
		WorkbookUtils.getStringCellValue(numCell) == "123.45"
		WorkbookUtils.getStringCellValue(boolCell) == "true"
		WorkbookUtils.getStringCellValue(strCell) == "Text"
		WorkbookUtils.getStringCellValue(null, "Default") == "Default"

		cleanup:
		wb.close()
	}

	def "getRowSpan 行跨度测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)
		sheet.createRow(9)

		expect:
		WorkbookUtils.getRowSpan(sheet) == 10 // 0 to 9 is 10 rows
		WorkbookUtils.getRowSpan(wb.createSheet("Empty")) == 0

		cleanup:
		wb.close()
	}

	def "getColumnSpan 列跨度测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		row.createCell(0)
		row.createCell(4)

		expect:
		WorkbookUtils.getColumnSpan(row) == 5 // 0 to 4 is 5 cols
		WorkbookUtils.getColumnSpan(sheet.createRow(1)) == 0

		cleanup:
		wb.close()
	}

	def "createTitleRow 标题行测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()

		when:
		def map = WorkbookUtils.createTitleRow(sheet, "ID", "Name")

		then:
		map.size() == 2
		map["ID"] == 0
		map["Name"] == 1
		sheet.getRow(0).getCell(0).stringCellValue == "ID"
		sheet.getRow(0).getCell(1).stringCellValue == "Name"

		cleanup:
		wb.close()
	}

	def "addRow 添加行测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()

		when:
		WorkbookUtils.addRow(sheet, 1, "Test", true)
		Row row = sheet.getRow(0)

		then:
		row != null
		row.getCell(0).numericCellValue == 1.0
		row.getCell(1).stringCellValue == "Test"
		row.getCell(2).booleanCellValue

		cleanup:
		wb.close()
	}

	def "writeRowAt 插入行测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)

		when:
		WorkbookUtils.writeRowAt(sheet, 0, "Inserted")

		then:
		sheet.getRow(0).getCell(0).getStringCellValue() == "Inserted"

		cleanup:
		wb.close()
	}

	def "createCell URI/URL处理测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		URI uri = new URI("http://example.com")
		URL url = new URL("http://example.org")

		when:
		WorkbookUtils.createCell(row, 0, uri, null)
		WorkbookUtils.createCell(row, 1, url, null)

		then:
		row.getCell(0).hyperlink.address == "http://example.com"
		row.getCell(0).stringCellValue == "http://example.com"

		row.getCell(1).hyperlink.address == "http://example.org"
		row.getCell(1).stringCellValue == "http://example.org"

		cleanup:
		wb.close()
	}

	def "getNumericCellValue 数值获取测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		when:
		Cell numCell = row.createCell(0)
		numCell.setCellValue(123.45)
		Cell strNumCell = row.createCell(1)
		strNumCell.setCellValue("678.90")
		Cell invalidStrCell = row.createCell(2)
		invalidStrCell.setCellValue("NotANumber")

		then:
		WorkbookUtils.getNumericCellValue(numCell) == 123.45d
		WorkbookUtils.getNumericCellValue(strNumCell) == 678.90d
		WorkbookUtils.getNumericCellValue(invalidStrCell) == null
		WorkbookUtils.getNumericCellValue(null, 0d) == 0d

		cleanup:
		wb.close()
	}

	def "getBooleanCellValue 布尔值获取测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		when:
		Cell boolCell = row.createCell(0)
		boolCell.setCellValue(true)
		Cell numTrueCell = row.createCell(1)
		numTrueCell.setCellValue(1)
		Cell numFalseCell = row.createCell(2)
		numFalseCell.setCellValue(0)
		Cell strTrueCell = row.createCell(3)
		strTrueCell.setCellValue("true")
		Cell strFalseCell = row.createCell(4)
		strFalseCell.setCellValue("false")

		then:
		WorkbookUtils.getBooleanCellValue(boolCell)
		WorkbookUtils.getBooleanCellValue(numTrueCell)
		!WorkbookUtils.getBooleanCellValue(numFalseCell)
		WorkbookUtils.getBooleanCellValue(strTrueCell)
		!WorkbookUtils.getBooleanCellValue(strFalseCell)
		WorkbookUtils.getBooleanCellValue(null, false) == false

		cleanup:
		wb.close()
	}

	def "getDateCellValue 日期获取测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		Date now = new Date()

		when:
		Cell dateCell = row.createCell(0)
		dateCell.setCellValue(now)
		Cell strDateCell = row.createCell(1)
		strDateCell.setCellValue("2023/01/01")

		then:
		// Excel date precision might vary, just checking non-null and approximate correctness if needed
		// Here simply checking if it returns a date
		WorkbookUtils.getDateCellValue(dateCell) != null
		WorkbookUtils.getDateCellValue(strDateCell) != null
		WorkbookUtils.getDateCellValue(null) == null

		cleanup:
		wb.close()
	}

	def "setAdjustColWidth 列宽调整测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		row.createCell(0).setCellValue("Wide Content Here")
		row.createCell(1).setCellValue("Small")

		when:
		WorkbookUtils.setAdjustColWidth(sheet)

		then:
		// Just verify no exception and maybe width changed from default
		// Exact width calculation depends on font and OS, so hard to assert exact value
		sheet.getColumnWidth(0) > 2000

		when:
		WorkbookUtils.setAdjustColWidth(sheet, 1)

		then:
		noExceptionThrown()

		cleanup:
		wb.close()
	}

	def "addRow List集合测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()

		when:
		WorkbookUtils.addRow(sheet, [1, "List", true])
		Row row = sheet.getRow(0)

		then:
		row != null
		row.getCell(0).numericCellValue == 1.0
		row.getCell(1).stringCellValue == "List"
		row.getCell(2).booleanCellValue

		cleanup:
		wb.close()
	}

	def "getBooleanFormulaCellValue 公式布尔值测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		when:
		Cell trueFormula = row.createCell(0)
		trueFormula.setCellFormula("1=1")
		Cell falseFormula = row.createCell(1)
		falseFormula.setCellFormula("1=2")
		Cell notFormula = row.createCell(2)
		notFormula.setCellValue(true)

		then:
		// Formula evaluation might require calculation, but utility should handle it
		WorkbookUtils.getBooleanFormulaCellValue(trueFormula, wb) == true
		WorkbookUtils.getBooleanFormulaCellValue(falseFormula, wb) == false
		WorkbookUtils.getBooleanFormulaCellValue(notFormula, wb) == true // Fallback to normal cell value
		WorkbookUtils.getBooleanFormulaCellValue(null, wb) == null

		cleanup:
		wb.close()
	}

	def "getStringFormulaCellValue 公式字符串获取测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		when:
		Cell strFormula = row.createCell(0)
		strFormula.setCellFormula("\"Hello\" & \" World\"")
		Cell numFormula = row.createCell(1)
		numFormula.setCellFormula("100+23")
		Cell boolFormula = row.createCell(2)
		boolFormula.setCellFormula("1=1")
		Cell normalStr = row.createCell(3)
		normalStr.setCellValue("Normal")

		then:
		WorkbookUtils.getStringFormulaCellValue(strFormula, wb) == "Hello World"
		WorkbookUtils.getStringFormulaCellValue(numFormula, wb) == "123.0"
		WorkbookUtils.getStringFormulaCellValue(boolFormula, wb) == "true"
		WorkbookUtils.getStringFormulaCellValue(normalStr, wb) == "Normal"
		WorkbookUtils.getStringFormulaCellValue(null, wb) == ""
		WorkbookUtils.getStringFormulaCellValue(null, wb, "Default") == "Default"

		cleanup:
		wb.close()
	}

	def "getNumericFormulaCellValue 公式数值获取测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		when:
		Cell numFormula = row.createCell(0)
		numFormula.setCellFormula("10*5.5")
		Cell strFormula = row.createCell(1)
		strFormula.setCellFormula("\"Text\"")
		Cell boolFormula = row.createCell(2)
		boolFormula.setCellFormula("1=1")
		Cell normalNum = row.createCell(3)
		normalNum.setCellValue(99.9)

		then:
		WorkbookUtils.getNumericFormulaCellValue(numFormula, wb) == 55.0d
		WorkbookUtils.getNumericFormulaCellValue(strFormula, wb) == null
		WorkbookUtils.getNumericFormulaCellValue(strFormula, wb, 0d) == 0d
		WorkbookUtils.getNumericFormulaCellValue(boolFormula, wb) == 1.0d
		WorkbookUtils.getNumericFormulaCellValue(normalNum, wb) == 99.9d
		WorkbookUtils.getNumericFormulaCellValue(null, wb) == null

		cleanup:
		wb.close()
	}

	def "createCell 其他类型测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		Calendar calendar = Calendar.getInstance()
		RichTextString richText = wb.getCreationHelper().createRichTextString("Rich Text")

		when:
		WorkbookUtils.createCell(row, 0, calendar, null)
		WorkbookUtils.createCell(row, 1, richText, null)
		WorkbookUtils.createCell(row, 2, LocalDate.now(), null)
		WorkbookUtils.createCell(row, 3, LocalDateTime.now(), null)

		then:
		row.getCell(0).dateCellValue != null
		row.getCell(1).stringCellValue == "Rich Text"
		row.getCell(2).dateCellValue != null
		row.getCell(3).dateCellValue != null

		cleanup:
		wb.close()
	}

	def "createCell JSON转换测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		def map = [key: "value"]
		def list = [1, 2, 3]

		when:
		WorkbookUtils.createCell(row, 0, map, null)
		WorkbookUtils.createCell(row, 1, list, null)

		then:
		// Assuming JsonUtils.toString produces valid JSON string
		row.getCell(0).stringCellValue.contains("value")
		row.getCell(1).stringCellValue.contains("1")

		cleanup:
		wb.close()
	}

	def "getWorkbook 字节数组加载测试"() {
		expect:
		WorkbookUtils.getWorkbook(xlsFile.bytes).withCloseable {
			it instanceof HSSFWorkbook
		}
		WorkbookUtils.getWorkbook(xlsxFile.bytes).withCloseable {
			it instanceof XSSFWorkbook
		}
	}

	def "createRowIfAbsent 创建行测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()

		when:
		def row1 = WorkbookUtils.createRowIfAbsent(sheet, 0)
		row1.createCell(0).setCellValue("Exist")
		def row2 = WorkbookUtils.createRowIfAbsent(sheet, 0)

		then:
		row1.is(row2)
		row1.getCell(0).stringCellValue == "Exist"

		cleanup:
		wb.close()
	}

	def "createCellIfAbsent 创建单元格测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		when:
		def cell1 = WorkbookUtils.createCellIfAbsent(row, 0)
		cell1.setCellValue("Exist")
		def cell2 = WorkbookUtils.createCellIfAbsent(row, 0)

		then:
		cell1.is(cell2)
		cell1.stringCellValue == "Exist"

		cleanup:
		wb.close()
	}

	def "writeRowAt List集合测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)

		when:
		WorkbookUtils.writeRowAt(sheet, [1, "Inserted", true], 0)

		then:
		sheet.getRow(0).getCell(1).stringCellValue == "Inserted"

		cleanup:
		wb.close()
	}

	def "writeRowAt Pair集合测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)
		def pairs = [
			Pair.of("Inserted", 0)
		]

		when:
		WorkbookUtils.writeRowAt(sheet, pairs as Collection<Pair<?, Integer>>, 0)

		then:
		sheet.getRow(0).getCell(0).stringCellValue == "Inserted"

		cleanup:
		wb.close()
	}

	def "createTitleRow List标题测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		def titles = ["ID", "Name"]

		when:
		def map = WorkbookUtils.createTitleRow(sheet, titles)

		then:
		map.size() == 2
		map["ID"] == 0
		sheet.getRow(0).getCell(0).stringCellValue == "ID"

		cleanup:
		wb.close()
	}

	def "getDateCellValue 模式匹配测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		Cell cell = row.createCell(0)
		cell.setCellValue("2023-01-01")

		when:
		def date = WorkbookUtils.getDateCellValue(cell, "yyyy-MM-dd")

		then:
		date != null
		// Verify date components if possible, or just non-null

		cleanup:
		wb.close()
	}

	def "sheetStream 工作表流测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		wb.createSheet("Sheet1")
		wb.createSheet("Sheet2")

		when:
		def stream = WorkbookUtils.sheetStream(wb)
		def count = stream.count()

		then:
		count == 2
		WorkbookUtils.sheetStream(null).count() == 0

		cleanup:
		wb.close()
	}

	def "physicalRowStream 物理行流测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)
		sheet.createRow(2)

		when:
		def stream = WorkbookUtils.physicalRowStream(sheet)
		def list = stream.toList()

		then:
		list.size() == 2
		list[0].rowNum == 0
		list[1].rowNum == 2
		WorkbookUtils.physicalRowStream(null).count() == 0

		cleanup:
		wb.close()
	}

	def "physicalCellStream 物理单元格流测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		row.createCell(0)
		row.createCell(2)

		when:
		def stream = WorkbookUtils.physicalCellStream(row)
		def list = stream.collect(Collectors.toList())

		then:
		list.size() == 2
		list[0].columnIndex == 0
		list[1].columnIndex == 2
		WorkbookUtils.physicalCellStream(null).count() == 0

		cleanup:
		wb.close()
	}


	def "createTitleRow Varargs标题测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()

		when:
		def map = WorkbookUtils.createTitleRow(sheet, "ID", "Name", "Age")

		then:
		map.size() == 3
		map["ID"] == 0
		map["Age"] == 2
		sheet.getRow(0).getCell(0).stringCellValue == "ID"

		cleanup:
		wb.close()
	}

	def "addRow Object数组测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		CellStyle style = wb.createCellStyle()
		style.setAlignment(HorizontalAlignment.CENTER)

		when:
		WorkbookUtils.addRow(sheet, style, 1, "Text", true)
		Row row = sheet.getRow(0)

		then:
		row != null
		row.getCell(0).numericCellValue == 1.0
		row.getCell(0).cellStyle.alignment == HorizontalAlignment.CENTER
		row.getCell(1).stringCellValue == "Text"
		row.getCell(2).booleanCellValue

		cleanup:
		wb.close()
	}

	def "writeRowAt Object数组测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)
		CellStyle style = wb.createCellStyle()

		when:
		WorkbookUtils.writeRowAt(sheet, 0, style, "Inserted", 99)

		then:
		sheet.getRow(0).getCell(0).stringCellValue == "Inserted"
		sheet.getRow(0).getCell(1).numericCellValue == 99.0

		cleanup:
		wb.close()
	}

	def "getFormulaCellValue 公式单元格值测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)

		// String Formula
		Cell strFormula = row.createCell(0)
		strFormula.cellFormula = "\"Hello \" & \"World\""

		// Numeric Formula
		Cell numFormula = row.createCell(1)
		numFormula.cellFormula = "10 + 20"

		// Boolean Formula
		Cell boolFormula = row.createCell(2)
		boolFormula.cellFormula = "1 > 0"

		// Error/Empty/Plain cells for fallback testing
		Cell plainStr = row.createCell(3)
		plainStr.setCellValue("Plain")

		Cell nullCell = row.createCell(4) // Empty

		FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator()

		expect:
		// String Formula Tests
		WorkbookUtils.getStringFormulaCellValue(strFormula, wb) == "Hello World"
		WorkbookUtils.getStringFormulaCellValue(strFormula, evaluator) == "Hello World"
		WorkbookUtils.getStringFormulaCellValue(nullCell, wb) == ""
		WorkbookUtils.getStringFormulaCellValue(nullCell, wb, "Default") == "Default"
		WorkbookUtils.getStringFormulaCellValue(plainStr, wb) == "Plain"

		// Numeric Formula Tests
		WorkbookUtils.getNumericFormulaCellValue(numFormula, wb) == 30.0
		WorkbookUtils.getNumericFormulaCellValue(numFormula, evaluator) == 30.0
		WorkbookUtils.getNumericFormulaCellValue(nullCell, wb) == null
		WorkbookUtils.getNumericFormulaCellValue(nullCell, wb, 0.0) == 0.0
		WorkbookUtils.getNumericFormulaCellValue(plainStr, wb, 0.0) == 0.0 // Conversion fails, returns default

		// Boolean Formula Tests
		WorkbookUtils.getBooleanFormulaCellValue(boolFormula, wb) == true
		WorkbookUtils.getBooleanFormulaCellValue(boolFormula, evaluator) == true
		WorkbookUtils.getBooleanFormulaCellValue(nullCell, wb) == null
		WorkbookUtils.getBooleanFormulaCellValue(nullCell, wb, false) == false

		cleanup:
		wb.close()
	}

	def "getWorkbook 字节数组测试"() {
		given:
		File file = new File("src/test/resources/test.xlsx")
		byte[] bytes = FileUtils.readFileToByteArray(file)

		when:
		Workbook wb = WorkbookUtils.getWorkbook(bytes)

		then:
		wb != null
		WorkbookUtils.isXlsx(bytes)
		!WorkbookUtils.isXls(bytes)
		WorkbookUtils.isWorkbook(bytes)

		cleanup:
		wb?.close()
	}


	def "createCell 类型测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		CreationHelper helper = wb.getCreationHelper()

		when:
		// Date
		Date now = new Date()
		WorkbookUtils.createCell(row, 0, now, null)

		// Calendar
		Calendar cal = Calendar.getInstance()
		WorkbookUtils.createCell(row, 1, cal, null)

		// RichTextString
		RichTextString richText = helper.createRichTextString("Rich Text")
		WorkbookUtils.createCell(row, 2, richText, null)

		// JSON Fallback (Map)
		Map<String, Object> map = ["key": "value"]
		WorkbookUtils.createCell(row, 3, map, null)

		then:
		row.getCell(0).dateCellValue == now
		row.getCell(1).dateCellValue == cal.time
		row.getCell(2).richStringCellValue.string == "Rich Text"
		row.getCell(3).stringCellValue.contains("\"key\":\"value\"") // Simple check for JSON string

		cleanup:
		wb.close()
	}

	def "writeRowAt 集合测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()

		when:
		// List
		List<Object> listData = ["List", 123]
		WorkbookUtils.writeRowAt(sheet, listData, 0)

		// Pair Collection
		List<Pair<Object, Integer>> pairData = [
			Pair.of("Pair", 0),
			Pair.of(456, 2) // Skip index 1
		]
		WorkbookUtils.writeRowAt(sheet, pairData as Collection<Pair<?, Integer>>, 1)

		then:
		// Verify List Row (Row 0)
		sheet.getRow(0).getCell(0).stringCellValue == "List"
		sheet.getRow(0).getCell(1).numericCellValue == 123.0

		// Verify Pair Row (Row 1)
		sheet.getRow(1).getCell(0).stringCellValue == "Pair"
		sheet.getRow(1).getCell(1) == null // Skipped
		sheet.getRow(1).getCell(2).numericCellValue == 456.0

		cleanup:
		wb.close()
	}


	def "getDateCellValue 模式测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		Row row = sheet.createRow(0)
		Cell cell = row.createCell(0)
		cell.setCellValue("2023-12-31")

		when:
		Date date = WorkbookUtils.getDateCellValue(cell, "yyyy-MM-dd")
		Date defaultDate = WorkbookUtils.getDateCellValue(cell, new Date(0), "yyyy/MM/dd") // Should fail and return default

		then:
		date != null
		// Simple verification of year/month/day
		Calendar cal = Calendar.getInstance()
		cal.setTime(date)
		cal.get(Calendar.YEAR) == 2023
		cal.get(Calendar.MONTH) == Calendar.DECEMBER
		cal.get(Calendar.DAY_OF_MONTH) == 31

		defaultDate.time == 0

		cleanup:
		wb.close()
	}

	def "addRow Consumer测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()

		when:
		WorkbookUtils.addRow(sheet, { row ->
			row.createCell(0).setCellValue("Consumer")
		})

		then:
		sheet.getLastRowNum() == 0
		sheet.getRow(0).getCell(0).stringCellValue == "Consumer"

		cleanup:
		wb.close()
	}

	def "writeRowAt Consumer测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		sheet.createRow(0)

		when:
		WorkbookUtils.writeRowAt(sheet, 0, { row ->
			row.createCell(0).setCellValue("Inserted Consumer")
		})

		then:
		sheet.getRow(0).getCell(0).stringCellValue == "Inserted Consumer"

		cleanup:
		wb.close()
	}

	def "addRow 集合测试"() {
		given:
		Workbook wb = new XSSFWorkbook()
		Sheet sheet = wb.createSheet()
		CellStyle style = wb.createCellStyle()
		style.setAlignment(HorizontalAlignment.CENTER)

		when:
		// List
		List<Object> listData = ["List", 123]
		WorkbookUtils.addRow(sheet, listData)

		// Pair Collection with Style
		List<Pair<Object, Integer>> pairData = [
			Pair.of("Pair", 0),
			Pair.of(456, 2)
		]
		WorkbookUtils.addRow(sheet, pairData as Collection<Pair<?, Integer>>, style)

		then:
		// Verify List Row (Row 0)
		sheet.getRow(0).getCell(0).stringCellValue == "List"
		sheet.getRow(0).getCell(1).numericCellValue == 123.0

		// Verify Pair Row (Row 1)
		sheet.getRow(1).getCell(0).stringCellValue == "Pair"
		sheet.getRow(1).getCell(0).cellStyle.alignment == HorizontalAlignment.CENTER
		sheet.getRow(1).getCell(2).numericCellValue == 456.0

		cleanup:
		wb.close()
	}
}
