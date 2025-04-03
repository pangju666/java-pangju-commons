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

package io.github.pangju666.commons.poi.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import io.github.pangju666.commons.lang.pool.Constants;
import io.github.pangju666.commons.lang.utils.DateUtils;
import io.github.pangju666.commons.lang.utils.JsonUtils;
import io.github.pangju666.commons.poi.lang.PoiConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class WorkbookUtils {
	protected WorkbookUtils() {
	}

	public static boolean isXls(final File file) throws IOException {
		return FileUtils.isMimeType(file, PoiConstants.XLS_MIME_TYPE);
	}

	public static boolean isXls(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType);
	}

	public static boolean isXls(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType);
	}

	public static boolean isXlsx(final File file) throws IOException {
		return FileUtils.isMimeType(file, PoiConstants.XLSX_MIME_TYPE);
	}

	public static boolean isXlsx(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	public static boolean isXlsx(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	public static boolean isWorkbook(final File file) throws IOException {
		return FileUtils.isAnyMimeType(file, PoiConstants.XLS_MIME_TYPE, PoiConstants.XLS_MIME_TYPE);
	}

	public static boolean isWorkbook(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType) || PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	public static boolean isWorkbook(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType) || PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	public static Workbook getWorkbook(final File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		String mimeType = IOConstants.getDefaultTika().detect(file);
		try (FileInputStream inputStream = FileUtils.openInputStream(file)) {
			return getWorkbook(inputStream, mimeType);
		}
	}

	public static Workbook getWorkbook(final File file, final String mimeType) throws IOException {
		Validate.notBlank(mimeType, "mimeType 不可为空");
		FileUtils.checkFile(file, "file 不可为 null");

		try (FileInputStream inputStream = FileUtils.openInputStream(file)) {
			return getWorkbook(inputStream, mimeType);
		}
	}

	public static Workbook getWorkbook(final InputStream inputStream, final String mimeType) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");

		if (!PoiConstants.XLSX_MIME_TYPE.equals(mimeType) && !PoiConstants.XLS_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是xlsx或xls文件");
		}
		return switch (mimeType) {
			case PoiConstants.XLS_MIME_TYPE -> new HSSFWorkbook(inputStream);
			case PoiConstants.XLSX_MIME_TYPE -> new XSSFWorkbook(inputStream);
			default -> null;
		};
	}

	public static Workbook getWorkbook(final byte[] bytes) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return getWorkbook(inputStream, mimeType);
	}

	public static Workbook getWorkbook(final byte[] bytes, final String mimeType) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");
		Validate.notBlank(mimeType, "mimeType 不可为空");

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return getWorkbook(inputStream, mimeType);
	}

	public static List<Sheet> getSheets(final Workbook workbook) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(workbook);
	}

	public static List<Row> getRows(final Workbook workbook) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		return stream(workbook)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.toList();
	}

	public static List<Row> getRows(final Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(sheet);
	}

	public static List<Row> getRows(final Sheet sheet, final int startRowNum) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return getRows(sheet, startRowNum, sheet.getLastRowNum());
	}

	public static List<Row> getRows(final Sheet sheet, final int startRowNum, final int endRowNum) {
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

	public static List<Cell> getCells(final Workbook workbook) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		return stream(workbook)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.toList();
	}

	public static List<Cell> getCells(final Workbook workbook, final Row.MissingCellPolicy policy) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		Validate.notNull(policy, "policy 不可为 null");

		return stream(workbook)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.map(row -> getCells(row, policy))
			.flatMap(List::stream)
			.toList();
	}

	public static List<Cell> getCells(final Sheet sheet, final Row.MissingCellPolicy policy) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		Validate.notNull(policy, "policy 不可为 null");

		return stream(sheet)
			.map(row -> getCells(row, policy))
			.flatMap(List::stream)
			.toList();
	}

	public static List<Cell> getCells(final Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return stream(sheet)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.toList();
	}

	public static List<Cell> getCells(final Row row) {
		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(row);
	}

	public static List<Cell> getCells(final Row row, final Row.MissingCellPolicy policy) {
		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		return getCells(row, row.getFirstCellNum(), row.getLastCellNum(), policy);
	}

	public static List<Cell> getCells(final Row row, final int startCellNum) {
		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		return getCells(row, startCellNum, row.getLastCellNum(), Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	public static List<Cell> getCells(final Row row, final int startCellNum, final Row.MissingCellPolicy policy) {
		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		return getCells(row, startCellNum, row.getLastCellNum(), policy);
	}

	public static List<Cell> getCells(final Row row, final int startCellNum, final int endCellNum) {
		return getCells(row, startCellNum, endCellNum, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	public static List<Cell> getCells(final Row row, final int startCellNum, final int endCellNum, final Row.MissingCellPolicy policy) {
		Validate.notNull(policy, "policy 不可为 null");
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

	public static Stream<Sheet> stream(final Workbook workbook) {
		return stream(workbook, false);
	}

	public static Stream<Sheet> stream(final Workbook workbook, final boolean parallel) {
		if (Objects.isNull(workbook)) {
			return Stream.empty();
		}
		return StreamSupport.stream(workbook.spliterator(), parallel);
	}

	public static Stream<Row> stream(final Sheet sheet) {
		return stream(sheet, false);
	}

	public static Stream<Row> stream(final Sheet sheet, final boolean parallel) {
		if (Objects.isNull(sheet)) {
			return Stream.empty();
		}
		return StreamSupport.stream(sheet.spliterator(), parallel);
	}

	public static Stream<Cell> stream(final Row row) {
		return stream(row, false);
	}

	public static Stream<Cell> stream(final Row row, final boolean parallel) {
		if (Objects.isNull(row)) {
			return Stream.empty();
		}
		return StreamSupport.stream(row.spliterator(), parallel);
	}

	public static Cell getMergedRegionCell(final Sheet sheet, final int row, final int column) {
		Validate.notNull(sheet, "sheet 不可为 null");

		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress ca = sheet.getMergedRegion(i);
			int firstColumn = ca.getFirstColumn();
			int lastColumn = ca.getLastColumn();
			int firstRow = ca.getFirstRow();
			int lastRow = ca.getLastRow();
			if (row >= firstRow && row <= lastRow) {
				if (column >= firstColumn && column <= lastColumn) {
					Row fRow = sheet.getRow(firstRow);
					return fRow.getCell(firstColumn);
				}
			}
		}
		return null;
	}

	public static boolean isEmptyCell(final Cell cell) {
		if (Objects.isNull(cell) || cell.getCellType() == CellType.BLANK || cell.getCellType() == CellType.ERROR) {
			return true;
		} else if (cell.getCellType() == CellType.STRING) {
			return StringUtils.isEmpty(cell.getStringCellValue());
		}
		return false;
	}

	public static String getStringCellValue(final Cell cell) {
		return getStringCellValue(cell, StringUtils.EMPTY);
	}

	public static String getStringCellValue(final Cell cell, final String defaultValue) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		return switch (cell.getCellType()) {
			case NUMERIC -> String.valueOf(cell.getNumericCellValue());
			case STRING -> Objects.toString(cell.getStringCellValue(), defaultValue);
			case BOOLEAN -> BooleanUtils.toStringTrueFalse(cell.getBooleanCellValue());
			default -> defaultValue;
		};
	}

	public static String getStringFormulaCellValue(final Cell cell, final Workbook workbook) {
		if (isEmptyCell(cell)) {
			return StringUtils.EMPTY;
		}
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getStringFormulaCellValue(cell, evaluator, null);
	}

	public static String getStringFormulaCellValue(final Cell cell, final Workbook workbook, final String defaultValue) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getStringFormulaCellValue(cell, evaluator, defaultValue);
	}

	public static String getStringFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator) {
		return getStringFormulaCellValue(cell, evaluator, null);
	}

	public static String getStringFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator, final String defaultValue) {
		Validate.notNull(evaluator, "evaluator 不可为 null");

		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		if (cell.getCellType() != CellType.FORMULA) {
			return getStringCellValue(cell, defaultValue);
		}
		CellValue cellValue = evaluator.evaluate(cell);
		if (Objects.isNull(cellValue)) {
			return defaultValue;
		}
		return switch (cellValue.getCellType()) {
			case NUMERIC -> String.valueOf(cellValue.getNumberValue());
			case STRING -> Objects.toString(cellValue.getStringValue(), defaultValue);
			case BOOLEAN -> BooleanUtils.toStringTrueFalse(cellValue.getBooleanValue());
			default -> defaultValue;
		};
	}

	public static Double getNumericCellValue(final Cell cell) {
		return getNumericCellValue(cell, null);
	}

	public static Double getNumericCellValue(final Cell cell, final Double defaultValue) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		try {
			return switch (cell.getCellType()) {
				case NUMERIC -> cell.getNumericCellValue();
				case STRING -> NumberUtils.toDouble(cell.getStringCellValue(), defaultValue);
				case BOOLEAN -> BooleanUtils.toIntegerObject(cell.getBooleanCellValue()).doubleValue();
				default -> defaultValue;
			};
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static Double getNumericFormulaCellValue(final Cell cell, final Workbook workbook) {
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getNumericFormulaCellValue(cell, evaluator, null);
	}

	public static Double getNumericFormulaCellValue(final Cell cell, final Workbook workbook, final Double defaultValue) {
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getNumericFormulaCellValue(cell, evaluator, defaultValue);
	}

	public static Double getNumericFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator) {
		return getNumericFormulaCellValue(cell, evaluator, null);
	}

	public static Double getNumericFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator, final Double defaultValue) {
		Validate.notNull(evaluator, "evaluator 不可为 null");

		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		if (cell.getCellType() != CellType.FORMULA) {
			return getNumericCellValue(cell, defaultValue);
		}
		CellValue cellValue = evaluator.evaluate(cell);
		if (Objects.isNull(cellValue)) {
			return defaultValue;
		}
		try {
			return switch (cellValue.getCellType()) {
				case NUMERIC -> cellValue.getNumberValue();
				case STRING -> NumberUtils.toDouble(cellValue.getStringValue(), defaultValue);
				case BOOLEAN -> BooleanUtils.toIntegerObject(cellValue.getBooleanValue()).doubleValue();
				default -> defaultValue;
			};
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public static Boolean getBooleanCellValue(final Cell cell) {
		return getBooleanCellValue(cell, null);
	}

	public static Boolean getBooleanCellValue(final Cell cell, final Boolean defaultValue) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		return switch (cell.getCellType()) {
			case NUMERIC -> cell.getNumericCellValue() > 0;
			case STRING -> BooleanUtils.toBoolean(cell.getStringCellValue());
			case BOOLEAN -> cell.getBooleanCellValue();
			default -> defaultValue;
		};
	}

	public static Boolean getBooleanFormulaCellValue(final Cell cell, final Workbook workbook) {
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getBooleanFormulaCellValue(cell, evaluator, null);
	}

	public static Boolean getBooleanFormulaCellValue(final Cell cell, final Workbook workbook, final Boolean defaultValue) {
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getBooleanFormulaCellValue(cell, evaluator, defaultValue);
	}

	public static Boolean getBooleanFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator) {
		return getBooleanFormulaCellValue(cell, evaluator, null);
	}

	public static Boolean getBooleanFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator, final Boolean defaultValue) {
		Validate.notNull(evaluator, "evaluator 不可为 null");

		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		if (cell.getCellType() != CellType.FORMULA) {
			return getBooleanCellValue(cell, defaultValue);
		}
		CellValue cellValue = evaluator.evaluate(cell);
		if (Objects.isNull(cellValue)) {
			return defaultValue;
		}
		return switch (cell.getCellType()) {
			case NUMERIC -> cell.getNumericCellValue() > 0;
			case STRING -> BooleanUtils.toBoolean(cell.getStringCellValue());
			case BOOLEAN -> cell.getBooleanCellValue();
			default -> defaultValue;
		};
	}

	public static Date getDateCellValue(final Cell cell) {
		return getDateCellValue(cell, Constants.DATE_FORMAT, Constants.DATETIME_FORMAT,
			Constants.TIME_FORMAT, "yyyy/MM/dd", "yyyy/M/d", "yyyy/M-d");
	}

	public static Date getDateCellValue(final Cell cell, final Date defaultValue) {
		return getDateCellValue(cell, defaultValue, Constants.DATE_FORMAT, Constants.DATETIME_FORMAT,
			Constants.TIME_FORMAT, "yyyy/MM/dd", "yyyy/M/d", "yyyy/M-d");
	}

	public static Date getDateCellValue(final Cell cell, final String... parsePatterns) {
		return getDateCellValue(cell, null, parsePatterns);
	}

	public static Date getDateCellValue(final Cell cell, final Date defaultValue, final String... parsePatterns) {
		if (isEmptyCell(cell)) {
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

	public static int countRow(final Sheet sheet) {
		Validate.notNull(sheet, "sheet 不可为 null");

		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum == -1) {
			return 0;
		}
		return lastRowNum - sheet.getFirstRowNum() + 1;
	}

	public static int countCell(final Row row) {
		Validate.notNull(row, "row 不可为 null");

		short lastCellNum = row.getLastCellNum();
		if (lastCellNum == -1) {
			return 0;
		}
		return lastCellNum - row.getFirstCellNum() + 1;
	}

	public static Row getRow(final Sheet sheet, final int rowNum) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum > 0, "rowNum 必须大于等于0");

		return ObjectUtils.defaultIfNull(sheet.getRow(rowNum), sheet.createRow(rowNum));
	}

	public static Cell getCell(final Row row, final int cellNum) {
		Validate.notNull(row, "row 不可为 null");
		Validate.isTrue(cellNum > 0, "cellNum 必须大于等于0");

		return ObjectUtils.defaultIfNull(row.getCell(cellNum), row.createCell(cellNum));
	}

	public static Map<String, Integer> createTitleRow(final Sheet sheet, final String... titles) {
		return createTitleRow(sheet, 0, null, titles);
	}

	public static Map<String, Integer> createTitleRow(final Sheet sheet, final CellStyle rowStyle, final String... titles) {
		return createTitleRow(sheet, 0, rowStyle, titles);
	}

	public static Map<String, Integer> createTitleRow(final Sheet sheet, final int rowNum, final String... titles) {
		return createTitleRow(sheet, rowNum, null, titles);
	}

	public static Map<String, Integer> createTitleRow(final Sheet sheet, final int rowNum, final CellStyle rowStyle,
													  final String... titles) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Map<String, Integer> titleIndexMap = new HashMap<>(titles.length);
		Row row = getRow(sheet, rowNum);
		if (Objects.nonNull(rowStyle)) {
			row.setRowStyle(rowStyle);
		}
		for (int i = 0; i < titles.length; i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(titles[i]);
			titleIndexMap.put(titles[i], i);
		}
		return titleIndexMap;
	}

	public static Map<String, Integer> createTitleRow(final Sheet sheet, final List<String> titles) {
		return createTitleRow(sheet, titles, 0, null);
	}

	public static Map<String, Integer> createTitleRow(final Sheet sheet, final List<String> titles, final CellStyle rowStyle) {
		return createTitleRow(sheet, titles, 0, rowStyle);
	}

	public static Map<String, Integer> createTitleRow(final Sheet sheet, final List<String> titles, final int rowNum) {
		return createTitleRow(sheet, titles, rowNum, null);
	}

	public static Map<String, Integer> createTitleRow(final Sheet sheet, final List<String> titles,
													  final int rowNum, final CellStyle rowStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Map<String, Integer> titleIndexMap = new HashMap<>(titles.size());
		Row row = getRow(sheet, rowNum);
		if (Objects.nonNull(rowStyle)) {
			row.setRowStyle(rowStyle);
		}
		for (int i = 0; i < titles.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(titles.get(i));
			titleIndexMap.put(titles.get(i), i);
		}
		return titleIndexMap;
	}

	public static void setAdjustColWidth(final Sheet sheet) {
		Validate.notNull(sheet, "sheet 不可为 null");

		Row row = sheet.getRow(0);
		if (Objects.nonNull(row)) {
			setAdjustColWidth(sheet, row.getPhysicalNumberOfCells());
		}
	}

	public static void setAdjustColWidth(final Sheet sheet, final int columnCount) {
		Validate.notNull(sheet, "sheet 不可为 null");

		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
			sheet.setColumnWidth(i, sheet.getColumnWidth(i) * 17 / 10);
		}
	}

	public static void addRow(final Sheet sheet, final Consumer<Row> consumer) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.notNull(consumer, "consumer 不可为 null");

		Row row = sheet.createRow(sheet.getLastRowNum() + 1);
		consumer.accept(row);
	}

	public static void insertRow(final Sheet sheet, final Consumer<Row> consumer, final int rowNum) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.notNull(consumer, "consumer 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Row row = sheet.createRow(rowNum);
		consumer.accept(row);
	}

	public static void addRow(final Sheet sheet, final Object... values) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, sheet.getLastRowNum() + 1, null, values);
	}

	public static void insertRow(final Sheet sheet, final int rowNum, final Object... values) {
		insertRow(sheet, rowNum, null, values);
	}

	public static void addRow(final Sheet sheet, final CellStyle cellStyle, final Object... values) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, sheet.getLastRowNum() + 1, cellStyle, values);
	}

	public static void insertRow(final Sheet sheet, final int rowNum, final CellStyle cellStyle, final Object... values) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Row row = getRow(sheet, rowNum);
		if (ArrayUtils.isEmpty(values)) {
			return;
		}
		for (int i = 0; i < values.length; i++) {
			createCell(row, i, values[i], cellStyle);
		}
	}

	public static void addRow(final Sheet sheet, final List<Object> values) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, values, sheet.getLastRowNum() + 1, null);
	}

	public static void insertRow(final Sheet sheet, final List<Object> values, final int rowNum) {
		insertRow(sheet, values, rowNum, null);
	}

	public static void addRow(final Sheet sheet, final List<Object> values, final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, values, sheet.getLastRowNum() + 1, cellStyle);
	}

	public static void insertRow(final Sheet sheet, final List<Object> values, final int rowNum, final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Row row = getRow(sheet, rowNum);
		if (CollectionUtils.isEmpty(values)) {
			return;
		}
		for (int i = 0; i < values.size(); i++) {
			createCell(row, i, values.get(i), cellStyle);
		}
	}

	public static void addRow(final Sheet sheet, final Collection<Pair<Object, Integer>> valueIndexPairs) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, valueIndexPairs, sheet.getLastRowNum() + 1, null);
	}

	public static void insertRow(final Sheet sheet, final Collection<Pair<Object, Integer>> valueIndexPairs,
								 final int rowNum) {
		insertRow(sheet, valueIndexPairs, rowNum, null);
	}

	public static void addRow(final Sheet sheet, final Collection<Pair<Object, Integer>> valueIndexPairs,
							  final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, valueIndexPairs, sheet.getLastRowNum() + 1, cellStyle);
	}

	public static void insertRow(final Sheet sheet, final Collection<Pair<Object, Integer>> valueIndexPairs,
								 final int rowNum, final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Row row = getRow(sheet, rowNum);
		if (CollectionUtils.isEmpty(valueIndexPairs)) {
			return;
		}

		for (Pair<Object, Integer> valueIndexPair : valueIndexPairs) {
			if (Objects.isNull(valueIndexPair) || Objects.isNull(valueIndexPair.getRight())) {
				continue;
			}
			createCell(row, valueIndexPair.getRight(), valueIndexPair.getLeft(), cellStyle);
		}
	}

	protected static void createCell(final Row row, final int i, final Object value, final CellStyle style) {
		Cell cell = row.createCell(i);
		if (Objects.isNull(value)) {
			cell.setBlank();
		} else if (value instanceof Number numberValue) {
			cell.setCellValue(numberValue.doubleValue());
		} else if (value instanceof Boolean booleanValue) {
			cell.setCellValue(booleanValue);
		} else if (value instanceof String str) {
			cell.setCellValue(str);
		} else if (value instanceof Date dateValue) {
			cell.setCellValue(dateValue);
		} else if (value instanceof LocalDate localDate) {
			cell.setCellValue(localDate);
		} else if (value instanceof LocalDateTime localDateTime) {
			cell.setCellValue(localDateTime);
		} else if (value instanceof Calendar calendar) {
			cell.setCellValue(calendar);
		} else if (value instanceof RichTextString richTextString) {
			cell.setCellValue(richTextString);
		} else if (value instanceof Hyperlink hyperlink) {
			cell.setHyperlink(hyperlink);
		} else if (value instanceof URI uri) {
			CreationHelper creationHelper = row.getSheet().getWorkbook().getCreationHelper();
			Hyperlink hyperlink;
			if (StringUtils.equals(uri.getScheme(), "file")) {
				hyperlink = creationHelper.createHyperlink(HyperlinkType.FILE);
				hyperlink.setAddress(uri.toString());
			} else {
				hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
				try {
					hyperlink.setAddress(uri.toURL().toString());
				} catch (MalformedURLException e) {
					cell.setBlank();
				}
			}
			hyperlink.setLabel(uri.toString());
			cell.setHyperlink(hyperlink);
		} else if (value instanceof URL url) {
			CreationHelper creationHelper = row.getSheet().getWorkbook().getCreationHelper();
			Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
			hyperlink.setLabel(url.toString());
			hyperlink.setAddress(url.toString());
			cell.setHyperlink(hyperlink);
		} else {
			cell.setCellValue(JsonUtils.toString(value));
		}
		if (Objects.nonNull(style)) {
			cell.setCellStyle(style);
		}
	}
}
