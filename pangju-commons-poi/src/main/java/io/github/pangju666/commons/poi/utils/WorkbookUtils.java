package io.github.pangju666.commons.poi.utils;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.lang.pool.Constants;
import io.github.pangju666.commons.lang.utils.DateUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WorkbookUtils {
	protected static final String HSSF_MIME_TYPE = "application/vnd.ms-excel";
	protected static final String XSSF_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	protected WorkbookUtils() {
	}

	public static boolean isWorkbookFile(File file) {
		Validate.isTrue(FileUtils.exist(file), "文件不存在");
		return isWorkbookFile(file.getName());
	}

	public static boolean isWorkbookFile(String filePath) {
		return HSSF_MIME_TYPE.equals(FilenameUtils.getMimeType(filePath)) || XSSF_MIME_TYPE.equals(FilenameUtils.getMimeType(filePath));
	}

	public static Workbook getWorkBook(String filePath) throws IOException {
		return getWorkBook(new File(filePath));
	}

	public static Workbook getWorkBook(File file) throws IOException {
		if (FileUtils.notExist(file)) {
			return null;
		}
		try (InputStream inputStream = new FileInputStream(file)) {
			String mimeType = FilenameUtils.getMimeType(file.getName());
			return switch (mimeType) {
				case HSSF_MIME_TYPE -> new HSSFWorkbook(inputStream);
				case XSSF_MIME_TYPE -> new XSSFWorkbook(inputStream);
				default -> null;
			};
		}
	}

	public static List<Sheet> getSheets(Workbook workbook) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(workbook);
	}

	public static List<Row> getRows(Workbook workbook) {
		return stream(workbook)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.toList();
	}

	public static List<Row> getRows(Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(sheet);
	}

	public static List<Row> getRows(Sheet sheet, int startRowNum) {
		return getRows(sheet, startRowNum, sheet.getLastRowNum());
	}

	public static List<Row> getRows(Sheet sheet, int startRowNum, int endRowNum) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		int startNum = Math.max(sheet.getFirstRowNum(), startRowNum);
		int endNum = Math.max(sheet.getLastRowNum(), endRowNum);
		List<Row> rows = new ArrayList<>(endNum - startNum + 1);
		for (int i = startNum; i <= endNum; i++) {
			rows.add(sheet.getRow(i));
		}
		return rows;
	}

	public static List<Cell> getCells(Workbook workbook) {
		return stream(workbook)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.toList();
	}

	public static List<Cell> getCells(Workbook workbook, Row.MissingCellPolicy policy) {
		return stream(workbook)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.map(row -> getCells(row, policy))
			.flatMap(List::stream)
			.toList();
	}

	public static List<Cell> getCells(Sheet sheet, Row.MissingCellPolicy policy) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return stream(sheet)
			.map(row -> getCells(row, policy))
			.flatMap(List::stream)
			.toList();
	}

	public static List<Cell> getCells(Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return stream(sheet)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.toList();
	}

	public static List<Cell> getCells(Row row) {
		return IterableUtils.toList(row);
	}

	public static List<Cell> getCells(Row row, Row.MissingCellPolicy policy) {
		return getCells(row, row.getFirstCellNum(), row.getLastCellNum(), policy);
	}

	public static List<Cell> getCells(Row row, int startCellNum) {
		return getCells(row, startCellNum, row.getLastCellNum(), Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	public static List<Cell> getCells(Row row, int startCellNum, Row.MissingCellPolicy policy) {
		return getCells(row, startCellNum, row.getLastCellNum(), policy);
	}

	public static List<Cell> getCells(Row row, int startCellNum, int endCellNum) {
		return getCells(row, startCellNum, endCellNum, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	public static List<Cell> getCells(Row row, int startCellNum, int endCellNum, Row.MissingCellPolicy policy) {
		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		int startNum = Math.max(row.getFirstCellNum(), startCellNum);
		int endNum = Math.max(row.getLastCellNum(), endCellNum);
		List<Cell> cells = new ArrayList<>(endNum - startNum + 1);
		for (int i = startNum; i <= endNum; i++) {
			cells.add(row.getCell(i, policy));
		}
		return cells;
	}

	public static Stream<Sheet> stream(Workbook workbook) {
		return stream(workbook, false);
	}

	public static Stream<Sheet> stream(Workbook workbook, boolean parallel) {
		if (Objects.isNull(workbook)) {
			return Stream.empty();
		}
		return StreamSupport.stream(workbook.spliterator(), parallel);
	}

	public static Stream<Row> stream(Sheet sheet) {
		return stream(sheet, false);
	}

	public static Stream<Row> stream(Sheet sheet, boolean parallel) {
		if (Objects.isNull(sheet)) {
			return Stream.empty();
		}
		return StreamSupport.stream(sheet.spliterator(), parallel);
	}

	public static Stream<Cell> stream(Row row) {
		return stream(row, false);
	}

	public static Stream<Cell> stream(Row row, boolean parallel) {
		if (Objects.isNull(row)) {
			return Stream.empty();
		}
		return StreamSupport.stream(row.spliterator(), parallel);
	}

	public static String getStringCellValue(Cell cell) {
		return getStringCellValue(cell, StringUtils.EMPTY);
	}

	public static String getStringCellValue(Cell cell, String defaultValue) {
		if (Objects.isNull(cell)) {
			return defaultValue;
		}
		try {
			return switch (cell.getCellType()) {
				case NUMERIC -> String.valueOf(cell.getNumericCellValue());
				case STRING -> Objects.toString(cell.getStringCellValue(), defaultValue);
				case BOOLEAN -> BooleanUtils.toStringTrueFalse(cell.getBooleanCellValue());
				default -> defaultValue;
			};
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static Double getNumericCellValue(Cell cell) {
		return getNumericCellValue(cell, null);
	}

	public static Double getNumericCellValue(Cell cell, Double defaultValue) {
		if (Objects.isNull(cell)) {
			return defaultValue;
		}
		try {
			return switch (cell.getCellType()) {
				case NUMERIC -> cell.getNumericCellValue();
				case STRING -> NumberUtils.toDouble(cell.getStringCellValue(), defaultValue);
				default -> defaultValue;
			};
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static Boolean getBooleanCellValue(Cell cell) {
		return getBooleanCellValue(cell, null);
	}

	public static Boolean getBooleanCellValue(Cell cell, Boolean defaultValue) {
		if (Objects.isNull(cell)) {
			return defaultValue;
		}
		try {
			return switch (cell.getCellType()) {
				case NUMERIC -> cell.getNumericCellValue() > 0;
				case STRING -> BooleanUtils.toBoolean(cell.getStringCellValue());
				case BOOLEAN -> cell.getBooleanCellValue();
				default -> defaultValue;
			};
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static Date getDateCellValue(Cell cell) {
		return getDateCellValue(cell, Constants.DATE_FORMAT, Constants.DATETIME_FORMAT, Constants.TIME_FORMAT);
	}

	public static Date getDateCellValue(Cell cell, Date defaultValue) {
		return getDateCellValue(cell, defaultValue, Constants.DATE_FORMAT, Constants.DATETIME_FORMAT, Constants.TIME_FORMAT);
	}

	public static Date getDateCellValue(Cell cell, String... parsePatterns) {
		return getDateCellValue(cell, null, parsePatterns);
	}

	public static Date getDateCellValue(Cell cell, Date defaultValue, String... parsePatterns) {
		if (Objects.isNull(cell)) {
			return defaultValue;
		}
		try {
			return switch (cell.getCellType()) {
				case NUMERIC -> cell.getDateCellValue();
				case STRING -> DateUtils.parseDateOrDefault(cell.getStringCellValue(), defaultValue, parsePatterns);
				default -> defaultValue;
			};
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static int countRow(Sheet sheet) {
		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum == -1) {
			return 0;
		}
		return lastRowNum - sheet.getFirstRowNum() + 1;
	}

	public static int countCell(Row row) {
		short lastCellNum = row.getLastCellNum();
		if (lastCellNum == -1) {
			return 0;
		}
		return lastCellNum - row.getFirstCellNum() + 1;
	}

	public static void createTitleRow(Sheet sheet, String... titles) {
		createTitleRow(sheet, 0, null, titles);
	}

	public static void createTitleRow(Sheet sheet, CellStyle rowStyle, String... titles) {
		createTitleRow(sheet, 0, rowStyle, titles);
	}

	public static void createTitleRow(Sheet sheet, int rowNum, String... titles) {
		createTitleRow(sheet, rowNum, null, titles);
	}

	public static void createTitleRow(Sheet sheet, int rowNum, CellStyle rowStyle, String... titles) {
		Row row = sheet.createRow(rowNum);
		if (Objects.nonNull(rowStyle)) {
			row.setRowStyle(rowStyle);
		}
		for (int i = 0; i < titles.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(titles[i]);
		}
	}

	public static void createTitleRow(Row row, String... titles) {
		createTitleRow(row, null, titles);
	}

	public static void createTitleRow(Row row, CellStyle cellStyle, String... titles) {
		for (int i = 0; i < titles.length; i++) {
			Cell cell = row.createCell(i);
			if (Objects.nonNull(cellStyle)) {
				cell.setCellStyle(cellStyle);
			}
			cell.setCellValue(titles[i]);
		}
	}

	public static void setAdjustColWidth(Sheet sheet) {
		Row row = sheet.getRow(0);
		if (Objects.nonNull(row)) {
			setAdjustColWidth(sheet, row.getPhysicalNumberOfCells());
		}
	}

	public static void setAdjustColWidth(Sheet sheet, int columnCount) {
		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
			sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 17 / 10);
		}
	}
}
