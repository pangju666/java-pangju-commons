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
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Excel工作簿工具类
 * <p>
 * 提供对Excel工作簿(.xls和.xlsx格式)的各种操作支持，包括：
 * <ul>
 *   <li>工作簿格式验证</li>
 *   <li>工作簿内容读取</li>
 *   <li>单元格数据处理</li>
 *   <li>行列操作</li>
 *   <li>样式设置</li>
 * </ul>
 * 注意事项：
 * <ul>
 *   <li>同时支持HSSF(.xls)和XSSF(.xlsx)格式</li>
 *   <li>所有方法均为静态方法</li>
 *   <li>线程安全</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class WorkbookUtils {
	/**
	 * 单元格自适应宽度缩放倍数
	 *
	 * @since 1.0.0
	 */
	protected static final double CELL_ADJUST_WIDTH_SCALE = 17.0 / 10;

	protected WorkbookUtils() {
	}

	/**
	 * 检查文件是否为XLS格式(Excel 97-2003)
	 *
	 * @param file 待检查的文件
	 * @return true-是XLS格式，false-不是XLS格式或文件不存在
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isXls(final File file) throws IOException {
		return FileUtils.isMimeType(file, PoiConstants.XLS_MIME_TYPE);
	}

	/**
	 * 检查字节数组是否为XLS格式(Excel 97-2003)
	 *
	 * @param bytes 待检查的字节数组
	 * @return true-是XLS格式，false-不是XLS格式或字节数组为空
	 * @since 1.0.0
	 */
	public static boolean isXls(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 检查输入流是否为XLS格式(Excel 97-2003)
	 *
	 * @param inputStream 待检查的输入流
	 * @return true-是XLS格式，false-不是XLS格式或输入流为null
	 * @throws IOException 当流读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isXls(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 检查文件是否为XLSX格式(Excel 2007及以上)
	 *
	 * @param file 待检查的文件
	 * @return true-是XLSX格式，false-不是XLSX格式或文件不存在
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isXlsx(final File file) throws IOException {
		return FileUtils.isMimeType(file, PoiConstants.XLSX_MIME_TYPE);
	}

	/**
	 * 检查字节数组是否为XLSX格式(Excel 2007及以上)
	 *
	 * @param bytes 待检查的字节数组
	 * @return true-是XLSX格式，false-不是XLSX格式或字节数组为空
	 * @since 1.0.0
	 */
	public static boolean isXlsx(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 检查输入流是否为XLSX格式(Excel 2007及以上)
	 *
	 * @param inputStream 待检查的输入流
	 * @return true-是XLSX格式，false-不是XLSX格式或输入流为null
	 * @throws IOException 当流读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isXlsx(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 检查文件是否为Excel工作簿格式(XLS或XLSX)
	 *
	 * @param file 待检查的文件
	 * @return true-是Excel工作簿格式，false-不是Excel工作簿格式或文件不存在
	 * @throws IOException 当文件读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isWorkbook(final File file) throws IOException {
		return FileUtils.isAnyMimeType(file, PoiConstants.XLS_MIME_TYPE, PoiConstants.XLS_MIME_TYPE);
	}

	/**
	 * 检查字节数组是否为Excel工作簿格式(XLS或XLSX)
	 *
	 * @param bytes 待检查的字节数组
	 * @return true-是Excel工作簿格式，false-不是Excel工作簿格式或字节数组为空
	 * @since 1.0.0
	 */
	public static boolean isWorkbook(final byte[] bytes) {
		if (ArrayUtils.isEmpty(bytes)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType) || PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 检查输入流是否为Excel工作簿格式(XLS或XLSX)
	 *
	 * @param inputStream 待检查的输入流
	 * @return true-是Excel工作簿格式，false-不是Excel工作簿格式或输入流为null
	 * @throws IOException 当流读取失败时抛出
	 * @since 1.0.0
	 */
	public static boolean isWorkbook(final InputStream inputStream) throws IOException {
		if (Objects.isNull(inputStream)) {
			return false;
		}
		String mimeType = IOConstants.getDefaultTika().detect(inputStream);
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType) || PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 从文件加载Excel工作簿
	 *
	 * @param file Excel文件
	 * @return 加载的工作簿对象
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是Excel格式时抛出
	 * @since 1.0.0
	 */
	public static Workbook getWorkbook(final File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		String mimeType = IOConstants.getDefaultTika().detect(file);
		try (UnsynchronizedBufferedInputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(file)) {
			return getWorkbook(inputStream, mimeType);
		}
	}

	/**
	 * 从文件加载指定MIME类型的Excel工作簿
	 *
	 * @param file     Excel文件
	 * @param mimeType 指定的MIME类型
	 * @return 加载的工作簿对象
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是指定MIME类型时抛出
	 * @since 1.0.0
	 */
	public static Workbook getWorkbook(final File file, final String mimeType) throws IOException {
		Validate.notBlank(mimeType, "mimeType 不可为空");
		FileUtils.checkFile(file, "file 不可为 null");

		try (UnsynchronizedBufferedInputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(file)) {
			return getWorkbook(inputStream, mimeType);
		}
	}

	/**
	 * 从输入流加载指定MIME类型的Excel工作簿
	 *
	 * @param inputStream 输入流
	 * @param mimeType    指定的MIME类型
	 * @return 加载的工作簿对象
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当流内容不是指定MIME类型时抛出
	 * @since 1.0.0
	 */
	public static Workbook getWorkbook(final InputStream inputStream, final String mimeType) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notBlank(mimeType, "mimeType 不可为空");

		if (!PoiConstants.XLSX_MIME_TYPE.equals(mimeType) && !PoiConstants.XLS_MIME_TYPE.equals(mimeType)) {
			throw new IllegalArgumentException("不是xlsx或xls文件");
		}
		switch (mimeType) {
			case PoiConstants.XLS_MIME_TYPE:
				return new HSSFWorkbook(inputStream);
			case PoiConstants.XLSX_MIME_TYPE:
				return new XSSFWorkbook(inputStream);
			default:
				return null;
		}
	}

	/**
	 * 从字节数组加载Excel工作簿
	 *
	 * @param bytes Excel文件字节数组
	 * @return 加载的工作簿对象
	 * @throws IOException              当字节数组解析失败时抛出
	 * @throws IllegalArgumentException 当字节数组不是Excel格式时抛出
	 * @since 1.0.0
	 */
	public static Workbook getWorkbook(final byte[] bytes) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return getWorkbook(inputStream, mimeType);
	}

	/**
	 * 从字节数组加载指定MIME类型的Excel工作簿
	 *
	 * @param bytes    Excel文件字节数组
	 * @param mimeType 指定的MIME类型
	 * @return 加载的工作簿对象
	 * @throws IOException              当字节数组解析失败时抛出
	 * @throws IllegalArgumentException 当字节数组不是指定MIME类型时抛出
	 * @since 1.0.0
	 */
	public static Workbook getWorkbook(final byte[] bytes, final String mimeType) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");
		Validate.notBlank(mimeType, "mimeType 不可为空");

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		return getWorkbook(inputStream, mimeType);
	}

	/**
	 * 获取工作簿中的所有工作表
	 * <p>
	 * 返回工作簿中所有工作表的不可修改列表，如果工作簿为null则返回空列表
	 * </p>
	 *
	 * @param workbook Excel工作簿对象，可以为null
	 * @return 工作表列表，不会返回null。如果工作簿为null或没有工作表，返回空列表
	 * @since 1.0.0
	 */
	public static List<Sheet> getSheets(final Workbook workbook) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(workbook);
	}

	/**
	 * 获取工作簿中的所有行
	 * <p>
	 * 返回工作簿中所有工作表中所有行的不可修改列表，如果工作簿为null则返回空列表
	 * </p>
	 *
	 * @param workbook Excel工作簿对象，可以为null
	 * @return 行列表，不会返回null。如果工作簿为null或没有行，返回空列表
	 * @since 1.0.0
	 */
	public static List<Row> getRows(final Workbook workbook) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		return sheetStream(workbook)
			.flatMap(WorkbookUtils::rowStream)
			.collect(Collectors.toList());
	}

	/**
	 * 获取工作表中的所有行
	 * <p>
	 * 返回工作表中所有行的不可修改列表，如果工作表为null则返回空列表
	 * </p>
	 *
	 * @param sheet Excel工作表对象，可以为null
	 * @return 行列表，不会返回null。如果工作表为null或没有行，返回空列表
	 * @since 1.0.0
	 */
	public static List<Row> getRows(final Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(sheet);
	}

	/**
	 * 获取工作表中从指定行开始的所有行
	 * <p>
	 * 返回工作表中从startRowNum开始到最后一行的不可修改列表，如果工作表为null则返回空列表
	 * </p>
	 *
	 * @param sheet       Excel工作表对象，可以为null
	 * @param startRowNum 起始行号(从0开始)
	 * @return 行列表，不会返回null。如果工作表为null或没有行，返回空列表
	 * @since 1.0.0
	 */
	public static List<Row> getRows(final Sheet sheet, final int startRowNum) {
		if (Objects.isNull(sheet) || sheet.getLastRowNum() == -1) {
			return Collections.emptyList();
		}
		return getRows(sheet, startRowNum, sheet.getLastRowNum());
	}

	/**
	 * 获取工作表中指定范围内的行
	 * <p>
	 * 返回工作表中从startRowNum到endRowNum范围内的行列表，如果工作表为null则返回空列表。
	 * 会自动处理边界情况：
	 * <ul>
	 *   <li>如果startRowNum小于工作表的第一行索引，使用工作表的第一行索引</li>
	 *   <li>如果endRowNum大于工作表的最后一行索引，使用工作表的最后一行索引</li>
	 * </ul>
	 * </p>
	 *
	 * @param sheet       Excel工作表对象，可以为null
	 * @param startRowNum 起始行号(从0开始)，必须大于等于0
	 * @param endRowNum   结束行号(包含在内)，必须大于等于startRowNum
	 * @return 行列表，不会返回null。如果工作表为null或没有行，返回空列表
	 * @throws IllegalArgumentException 如果startRowNum小于0或startRowNum大于endRowNum
	 * @since 1.0.0
	 */
	public static List<Row> getRows(final Sheet sheet, final int startRowNum, final int endRowNum) {
		Validate.isTrue(startRowNum >= 0, "startRowNum 必须大于等于0");
		Validate.isTrue(startRowNum > endRowNum, "startRowNum 必须大于 endRowNum");

		if (Objects.isNull(sheet) || sheet.getLastRowNum() == -1) {
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

	/**
	 * 获取工作簿中所有单元格
	 * <p>
	 * 返回工作簿中所有工作表中所有行的单元格列表，使用默认的缺失单元格策略({@link Row.MissingCellPolicy#RETURN_BLANK_AS_NULL})。
	 * 如果工作簿为null则返回空列表。
	 * </p>
	 *
	 * @param workbook Excel工作簿对象，可以为null
	 * @return 单元格列表，不会返回null。如果工作簿为null或没有单元格，返回空列表
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Workbook workbook) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		return sheetStream(workbook)
			.flatMap(WorkbookUtils::rowStream)
			.map(row -> {
				if (row.getFirstCellNum() == -1) {
					return new ArrayList<Cell>();
				}
				return getCells(row, row.getFirstCellNum(), row.getLastCellNum(),
					Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
			})
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	/**
	 * 获取工作簿中所有单元格
	 * <p>
	 * 返回工作簿中所有工作表中所有行的单元格列表，使用指定的缺失单元格策略处理空单元格。
	 * 如果工作簿为null则返回空列表。
	 * </p>
	 *
	 * @param workbook Excel工作簿对象，可以为null
	 * @param policy   缺失单元格处理策略，不可为null
	 * @return 单元格列表，不会返回null。如果工作簿为null或没有单元格，返回空列表
	 * @throws IllegalArgumentException 如果policy为null
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Workbook workbook, final Row.MissingCellPolicy policy) {
		if (Objects.isNull(workbook)) {
			return Collections.emptyList();
		}
		Validate.notNull(policy, "policy 不可为 null");

		return sheetStream(workbook)
			.map(IterableUtils::toList)
			.flatMap(List::stream)
			.map(row -> {
				if (row.getLastCellNum() == -1) {
					return new ArrayList<Cell>();
				}
				return getCells(row, row.getFirstCellNum(), row.getLastCellNum(), policy);
			})
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	/**
	 * 获取工作表中所有单元格
	 * <p>
	 * 返回工作表中所有行的单元格列表，使用指定的缺失单元格策略处理空单元格。
	 * 如果工作表为null则返回空列表。
	 * </p>
	 *
	 * @param sheet  Excel工作表对象，可以为null
	 * @param policy 缺失单元格处理策略，不可为null
	 * @return 单元格列表，不会返回null。如果工作表为null或没有单元格，返回空列表
	 * @throws IllegalArgumentException 如果policy为null
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Sheet sheet, final Row.MissingCellPolicy policy) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		Validate.notNull(policy, "policy 不可为 null");

		return rowStream(sheet)
			.map(row -> {
				if (row.getLastCellNum() == -1) {
					return new ArrayList<Cell>();
				}
				return getCells(row, row.getFirstCellNum(), row.getLastCellNum(), policy);
			})
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	/**
	 * 获取工作表中所有单元格
	 * <p>
	 * 返回工作表中所有行的单元格列表，使用默认的缺失单元格策略({@link Row.MissingCellPolicy#RETURN_BLANK_AS_NULL})。
	 * 如果工作表为null则返回空列表。
	 * </p>
	 *
	 * @param sheet Excel工作表对象，可以为null
	 * @return 单元格列表，不会返回null。如果工作表为null或没有单元格，返回空列表
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return rowStream(sheet)
			.map(row -> {
				if (row.getLastCellNum() == -1) {
					return new ArrayList<Cell>();
				}
				return getCells(row, row.getFirstCellNum(), row.getLastCellNum(),
					Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
			})
			.flatMap(List::stream)
			.collect(Collectors.toList());
	}

	/**
	 * 获取行中的所有单元格
	 * <p>
	 * 返回行中所有单元格的不可修改列表，使用默认的缺失单元格策略({@link Row.MissingCellPolicy#RETURN_BLANK_AS_NULL})。
	 * 如果行为null则返回空列表。
	 * </p>
	 *
	 * @param row Excel行对象，可以为null
	 * @return 单元格列表，不会返回null。如果行为null或没有单元格，返回空列表
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Row row) {
		if (Objects.isNull(row) || row.getFirstCellNum() == -1) {
			return Collections.emptyList();
		}
		return getCells(row, row.getFirstCellNum(), row.getLastCellNum(),
			Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	/**
	 * 获取行中的所有单元格
	 * <p>
	 * 返回行中所有单元格的不可修改列表，使用指定的缺失单元格策略处理空单元格。
	 * 如果行为null则返回空列表。
	 * </p>
	 *
	 * @param row    Excel行对象，可以为null
	 * @param policy 缺失单元格处理策略，不可为null
	 * @return 单元格列表，不会返回null。如果行为null或没有单元格，返回空列表
	 * @throws IllegalArgumentException 如果policy为null
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Row row, final Row.MissingCellPolicy policy) {
		if (Objects.isNull(row) || row.getFirstCellNum() == -1) {
			return Collections.emptyList();
		}
		return getCells(row, row.getFirstCellNum(), row.getLastCellNum(), policy);
	}

	/**
	 * 获取行中从指定列开始的所有单元格
	 * <p>
	 * 返回行中从startCellNum开始到最后一列的单元格列表，使用默认的缺失单元格策略({@link Row.MissingCellPolicy#RETURN_NULL_AND_BLANK})。
	 * 如果行为null则返回空列表。
	 * </p>
	 *
	 * @param row          Excel行对象，可以为null
	 * @param startCellNum 起始列号(从0开始)
	 * @return 单元格列表，不会返回null。如果行为null或没有单元格，返回空列表
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Row row, final int startCellNum) {
		if (Objects.isNull(row) || row.getFirstCellNum() == -1) {
			return Collections.emptyList();
		}
		return getCells(row, startCellNum, row.getLastCellNum(),
			Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	/**
	 * 获取行中从指定列开始的所有单元格
	 * <p>
	 * 返回行中从startCellNum开始到最后一列的单元格列表，使用指定的缺失单元格策略处理空单元格。
	 * 如果行为null则返回空列表。
	 * </p>
	 *
	 * @param row          Excel行对象，可以为null
	 * @param startCellNum 起始列号(从0开始)
	 * @param policy       缺失单元格处理策略，不可为null
	 * @return 单元格列表，不会返回null。如果行为null或没有单元格，返回空列表
	 * @throws IllegalArgumentException 如果policy为null
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Row row, final int startCellNum, final Row.MissingCellPolicy policy) {
		if (Objects.isNull(row) || row.getFirstCellNum() == -1) {
			return Collections.emptyList();
		}
		return getCells(row, startCellNum, row.getLastCellNum(), policy);
	}

	/**
	 * 获取行中指定范围内的单元格
	 * <p>
	 * 返回行中从startCellNum到endCellNum范围内的单元格列表，使用默认的缺失单元格策略({@link Row.MissingCellPolicy#RETURN_NULL_AND_BLANK})。
	 * 如果行为null则返回空列表。
	 * </p>
	 *
	 * @param row          Excel行对象，可以为null
	 * @param startCellNum 起始列号(从0开始)
	 * @param endCellNum   结束列号(包含在内)
	 * @return 单元格列表，不会返回null。如果行为null或没有单元格，返回空列表
	 * @throws IllegalArgumentException 如果startCellNum大于endCellNum
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Row row, final int startCellNum, final int endCellNum) {
		return getCells(row, startCellNum, endCellNum, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	/**
	 * 获取行中指定范围内的单元格
	 * <p>
	 * 返回行中从startCellNum到endCellNum范围内的单元格列表，使用指定的缺失单元格策略处理空单元格。
	 * 会自动处理边界情况：
	 * <ul>
	 *   <li>如果startCellNum小于行的第一列索引，使用行的第一列索引</li>
	 *   <li>如果endCellNum大于行的最后一列索引，使用行的最后一列索引</li>
	 * </ul>
	 * </p>
	 *
	 * @param row          Excel行对象，可以为null
	 * @param startCellNum 起始列号(从0开始)，必须大于等于0
	 * @param endCellNum   结束列号(包含在内)，必须大于等于startCellNum
	 * @param policy       缺失单元格处理策略，不可为null
	 * @return 单元格列表，不会返回null。如果行为null或没有单元格，返回空列表
	 * @throws IllegalArgumentException 如果policy为null、startCellNum小于0或startCellNum大于endCellNum
	 * @since 1.0.0
	 */
	public static List<Cell> getCells(final Row row, final int startCellNum, final int endCellNum, final Row.MissingCellPolicy policy) {
		Validate.notNull(policy, "policy 不可为 null");
		Validate.isTrue(startCellNum >= 0, "startCellNum 必须大于等于0");
		Validate.isTrue(startCellNum > endCellNum, "startCellNum 必须大于 endCellNum");

		if (Objects.isNull(row) || row.getFirstCellNum() == -1) {
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

	/**
	 * 创建工作簿中所有工作表的顺序流
	 * <p>
	 * 返回工作簿中所有工作表的顺序流，如果工作簿为null则返回空流。
	 * </p>
	 *
	 * @param workbook Excel工作簿对象，可以为null
	 * @return 工作表流，不会返回null。如果工作簿为null，返回空流
	 * @since 1.0.0
	 */
	public static Stream<Sheet> sheetStream(final Workbook workbook) {
		return sheetStream(workbook, false);
	}

	/**
	 * 创建工作簿中所有工作表的流
	 * <p>
	 * 返回工作簿中所有工作表的流，可以指定是否并行处理，如果工作簿为null则返回空流。
	 * </p>
	 *
	 * @param workbook Excel工作簿对象，可以为null
	 * @param parallel 是否并行处理
	 * @return 工作表流，不会返回null。如果工作簿为null，返回空流
	 * @since 1.0.0
	 */
	public static Stream<Sheet> sheetStream(final Workbook workbook, final boolean parallel) {
		if (Objects.isNull(workbook)) {
			return Stream.empty();
		}
		return StreamSupport.stream(workbook.spliterator(), parallel);
	}

	/**
	 * 创建工作表中所有行的顺序流
	 * <p>
	 * 返回工作表中所有行的顺序流，如果工作表为null则返回空流。
	 * </p>
	 *
	 * @param sheet Excel工作表对象，可以为null
	 * @return 行流，不会返回null。如果工作表为null，返回空流
	 * @since 1.0.0
	 */
	public static Stream<Row> rowStream(final Sheet sheet) {
		return rowStream(sheet, false);
	}

	/**
	 * 创建工作表中所有行的流
	 * <p>
	 * 返回工作表中所有行的流，可以指定是否并行处理，如果工作表为null则返回空流。
	 * </p>
	 *
	 * @param sheet    Excel工作表对象，可以为null
	 * @param parallel 是否并行处理
	 * @return 行流，不会返回null。如果工作表为null，返回空流
	 * @since 1.0.0
	 */
	public static Stream<Row> rowStream(final Sheet sheet, final boolean parallel) {
		if (Objects.isNull(sheet)) {
			return Stream.empty();
		}
		return StreamSupport.stream(sheet.spliterator(), parallel);
	}

	/**
	 * 创建行中所有单元格的顺序流
	 * <p>
	 * 返回行中所有单元格的顺序流，如果行为null则返回空流。
	 * </p>
	 *
	 * @param row Excel行对象，可以为null
	 * @return 单元格流，不会返回null。如果行为null，返回空流
	 * @since 1.0.0
	 */
	public static Stream<Cell> cellStream(final Row row) {
		return cellStream(row, false);
	}

	/**
	 * 创建行中所有单元格的流
	 * <p>
	 * 返回行中所有单元格的流，可以指定是否并行处理，如果行为null则返回空流。
	 * </p>
	 *
	 * @param row      Excel行对象，可以为null
	 * @param parallel 是否并行处理
	 * @return 单元格流，不会返回null。如果行为null，返回空流
	 * @since 1.0.0
	 */
	public static Stream<Cell> cellStream(final Row row, final boolean parallel) {
		if (Objects.isNull(row)) {
			return Stream.empty();
		}
		return StreamSupport.stream(row.spliterator(), parallel);
	}

	/**
	 * 获取合并单元格区域中的单元格
	 * <p>
	 * 根据指定的行号和列号查找合并单元格区域，如果找到则返回合并区域左上角的单元格，
	 * 否则返回null。如果指定的单元格不在任何合并区域内，则返回null。
	 * </p>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param row    行号(从0开始)
	 * @param column 列号(从0开始)
	 * @return 合并区域左上角的单元格，如果未找到合并区域或单元格不在合并区域内则返回null
	 * @throws IllegalArgumentException 如果sheet为null或row小于0或column小于0
	 * @since 1.0.0
	 */
	public static Cell getMergedRegionCell(final Sheet sheet, final int row, final int column) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(row >= 0, "row 必须大于等于0");
		Validate.isTrue(column >= 0, "column 必须大于等于0");

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

	/**
	 * 判断单元格是否为空
	 * <p>
	 * 单元格为空的判断条件包括：
	 * 1. 单元格对象为null
	 * 2. 单元格类型为BLANK(空白)
	 * 3. 单元格类型为ERROR(错误)
	 * 4. 单元格类型为STRING但内容为空字符串
	 * </p>
	 *
	 * @param cell Excel单元格对象，可以为null
	 * @return 如果单元格为空返回true，否则返回false
	 * @since 1.0.0
	 */
	public static boolean isEmptyCell(final Cell cell) {
		if (Objects.isNull(cell) || cell.getCellType() == CellType.BLANK || cell.getCellType() == CellType.ERROR) {
			return true;
		} else if (cell.getCellType() == CellType.STRING) {
			return StringUtils.isEmpty(cell.getStringCellValue());
		}
		return false;
	}

	/**
	 * 获取单元格的字符串值
	 * <p>
	 * 返回单元格的字符串表示形式，如果单元格为空则返回空字符串({@link StringUtils#EMPTY})。
	 * </p>
	 * <p>
	 * 支持处理以下单元格类型：
	 * <ol>
	 * <li>NUMERIC: 数值类型转换为字符串</li>
	 * <li>STRING: 直接返回字符串值</li>
	 * <li>BOOLEAN: 布尔值转换为"true"/"false"</li>
	 * </ol>
	 * 其他类型返回空字符串
	 * </p>
	 *
	 * @param cell Excel单元格对象，可以为null
	 * @return 单元格的字符串值，不会返回null。如果单元格为空，返回空字符串
	 * @since 1.0.0
	 */
	public static String getStringCellValue(final Cell cell) {
		return getStringCellValue(cell, StringUtils.EMPTY);
	}

	/**
	 * 获取单元格的字符串值
	 * <p>
	 * 返回单元格的字符串表示形式，如果单元格为空则返回指定的默认值。
	 * </p>
	 * <p>
	 * 支持处理以下单元格类型：
	 * <ol>
	 * <li>NUMERIC: 数值类型转换为字符串</li>
	 * <li>STRING: 直接返回字符串值</li>
	 * <li>BOOLEAN: 布尔值转换为"true"/"false"</li>
	 * </ol>
	 * 其他类型返回默认值
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 单元格的字符串值，不会返回null。如果单元格为空，返回默认值
	 * @since 1.0.0
	 */
	public static String getStringCellValue(final Cell cell, final String defaultValue) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		switch (cell.getCellType()) {
			case NUMERIC:
				return String.valueOf(cell.getNumericCellValue());
			case STRING:
				return Objects.toString(cell.getStringCellValue(), defaultValue);
			case BOOLEAN:
				return BooleanUtils.toStringTrueFalse(cell.getBooleanCellValue());
			default:
				return defaultValue;
		}
	}

	/**
	 * 获取公式单元格的字符串值
	 * <p>
	 * 返回公式单元格的字符串表示形式，如果单元格为空则返回空字符串({@link StringUtils#EMPTY})。
	 * 如果单元格不是公式类型，则直接返回单元格的字符串值。
	 * </p>
	 *
	 * @param cell     Excel单元格对象，可以为null
	 * @param workbook Excel工作簿对象，不可为null
	 * @return 公式单元格的字符串值，不会返回null。如果单元格为空，返回空字符串
	 * @throws IllegalArgumentException 如果workbook为null
	 * @since 1.0.0
	 */
	public static String getStringFormulaCellValue(final Cell cell, final Workbook workbook) {
		if (isEmptyCell(cell)) {
			return StringUtils.EMPTY;
		}
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getStringFormulaCellValue(cell, evaluator, null);
	}

	/**
	 * 获取公式单元格的字符串值
	 * <p>
	 * 返回公式单元格的字符串表示形式，如果单元格为空则返回指定的默认值。
	 * 如果单元格不是公式类型，则直接返回单元格的字符串值。
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param workbook     Excel工作簿对象，不可为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 公式单元格的字符串值，不会返回null。如果单元格为空，返回默认值
	 * @throws IllegalArgumentException 如果workbook为null
	 * @since 1.0.0
	 */
	public static String getStringFormulaCellValue(final Cell cell, final Workbook workbook, final String defaultValue) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getStringFormulaCellValue(cell, evaluator, defaultValue);
	}

	/**
	 * 获取公式单元格的字符串值
	 * <p>
	 * 返回公式单元格的字符串表示形式，如果单元格为空则返回空字符串({@link StringUtils#EMPTY})。
	 * 如果单元格不是公式类型，则直接返回单元格的字符串值。
	 * </p>
	 *
	 * @param cell      Excel单元格对象，可以为null
	 * @param evaluator 公式计算器，不可为null
	 * @return 公式单元格的字符串值，不会返回null。如果单元格为空，返回空字符串
	 * @throws IllegalArgumentException 如果evaluator为null
	 * @since 1.0.0
	 */
	public static String getStringFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator) {
		return getStringFormulaCellValue(cell, evaluator, null);
	}

	/**
	 * 获取公式单元格的字符串值
	 * <p>
	 * 返回公式单元格的字符串表示形式，如果单元格为空则返回指定的默认值。
	 * 如果单元格不是公式类型，则直接返回单元格的字符串值。
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param evaluator    公式计算器，不可为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 公式单元格的字符串值，不会返回null。如果单元格为空，返回默认值
	 * @throws IllegalArgumentException 如果evaluator为null
	 * @since 1.0.0
	 */
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
		switch (cellValue.getCellType()) {
			case NUMERIC:
				return String.valueOf(cellValue.getNumberValue());
			case STRING:
				return Objects.toString(cellValue.getStringValue(), defaultValue);
			case BOOLEAN:
				return BooleanUtils.toStringTrueFalse(cellValue.getBooleanValue());
			default:
				return defaultValue;
		}
	}

	/**
	 * 获取单元格的数值型值
	 * <p>
	 * 返回单元格的数值表示形式，如果单元格为空则返回null。
	 * 支持处理以下单元格类型：
	 * <ol>
	 * <li>NUMERIC: 直接返回数值</li>
	 * <li>STRING: 尝试将字符串转换为数值</li>
	 * <li>BOOLEAN: 将布尔值转换为1.0(true)或0.0(false)</li>
	 * </ol>
	 * 其他类型返回null
	 * </p>
	 *
	 * @param cell Excel单元格对象，可以为null
	 * @return 单元格的数值型值，可能返回null。如果单元格为空，返回null
	 * @since 1.0.0
	 */
	public static Double getNumericCellValue(final Cell cell) {
		return getNumericCellValue(cell, null);
	}

	/**
	 * 获取单元格的数值型值
	 * <p>
	 * 返回单元格的数值表示形式，如果单元格为空则返回指定的默认值。
	 * 支持处理以下单元格类型：
	 * <ol>
	 * <li>NUMERIC: 直接返回数值</li>
	 * <li>STRING: 尝试将字符串转换为数值</li>
	 * <li>BOOLEAN: 将布尔值转换为1.0(true)或0.0(false)</li>
	 * </ol>
	 * 其他类型返回默认值
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 单元格的数值型值，不会返回null。如果单元格为空，返回默认值
	 * @since 1.0.0
	 */
	public static Double getNumericCellValue(final Cell cell, final Double defaultValue) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		try {
			switch (cell.getCellType()) {
				case NUMERIC:
					return cell.getNumericCellValue();
				case STRING:
					return NumberUtils.toDouble(cell.getStringCellValue(), defaultValue);
				case BOOLEAN:
					return BooleanUtils.toIntegerObject(cell.getBooleanCellValue()).doubleValue();
				default:
					return defaultValue;
			}
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 获取公式单元格的数值型值
	 * <p>
	 * 返回公式单元格的数值表示形式，如果单元格为空则返回null。
	 * 如果单元格不是公式类型，则直接返回单元格的数值型值。
	 * </p>
	 *
	 * @param cell     Excel单元格对象，可以为null
	 * @param workbook Excel工作簿对象，不可为null
	 * @return 公式单元格的数值型值，可能返回null。如果单元格为空，返回null
	 * @throws IllegalArgumentException 如果workbook为null
	 * @since 1.0.0
	 */
	public static Double getNumericFormulaCellValue(final Cell cell, final Workbook workbook) {
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getNumericFormulaCellValue(cell, evaluator, null);
	}

	/**
	 * 获取公式单元格的数值型值
	 * <p>
	 * 返回公式单元格的数值表示形式，如果单元格为空则返回指定的默认值。
	 * 如果单元格不是公式类型，则直接返回单元格的数值型值。
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param workbook     Excel工作簿对象，不可为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 公式单元格的数值型值，不会返回null。如果单元格为空，返回默认值
	 * @throws IllegalArgumentException 如果workbook为null
	 * @since 1.0.0
	 */
	public static Double getNumericFormulaCellValue(final Cell cell, final Workbook workbook, final Double defaultValue) {
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getNumericFormulaCellValue(cell, evaluator, defaultValue);
	}

	/**
	 * 获取公式单元格的数值型值
	 * <p>
	 * 返回公式单元格的数值表示形式，如果单元格为空则返回null。
	 * 如果单元格不是公式类型，则直接返回单元格的数值型值。
	 * </p>
	 *
	 * @param cell      Excel单元格对象，可以为null
	 * @param evaluator 公式计算器，不可为null
	 * @return 公式单元格的数值型值，可能返回null。如果单元格为空，返回null
	 * @throws IllegalArgumentException 如果evaluator为null
	 * @since 1.0.0
	 */
	public static Double getNumericFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator) {
		return getNumericFormulaCellValue(cell, evaluator, null);
	}

	/**
	 * 获取公式单元格的数值型值
	 * <p>
	 * 返回公式单元格的数值表示形式，如果单元格为空则返回指定的默认值。
	 * 如果单元格不是公式类型，则直接返回单元格的数值型值。
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param evaluator    公式计算器，不可为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 公式单元格的数值型值，不会返回null。如果单元格为空，返回默认值
	 * @throws IllegalArgumentException 如果evaluator为null
	 * @since 1.0.0
	 */
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
			switch (cellValue.getCellType()) {
				case NUMERIC:
					return cellValue.getNumberValue();
				case STRING:
					return NumberUtils.toDouble(cellValue.getStringValue(), defaultValue);
				case BOOLEAN:
					return BooleanUtils.toIntegerObject(cellValue.getBooleanValue()).doubleValue();
				default:
					return defaultValue;
			}
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 获取单元格的布尔型值
	 * <p>
	 * 返回单元格的布尔表示形式，如果单元格为空则返回null。
	 * 支持处理以下单元格类型：
	 * <ol>
	 * <li>NUMERIC: 数值大于0返回true，否则返回false</li>
	 * <li>STRING: 尝试将字符串转换为布尔值</li>
	 * <li>BOOLEAN: 直接返回布尔值</li>
	 * </ol>
	 * 其他类型返回null
	 * </p>
	 *
	 * @param cell Excel单元格对象，可以为null
	 * @return 单元格的布尔型值，可能返回null。如果单元格为空，返回null
	 * @since 1.0.0
	 */
	public static Boolean getBooleanCellValue(final Cell cell) {
		return getBooleanCellValue(cell, null);
	}

	/**
	 * 获取单元格的布尔型值
	 * <p>
	 * 返回单元格的布尔表示形式，如果单元格为空则返回指定的默认值。
	 * 支持处理以下单元格类型：
	 * <ol>
	 * <li>NUMERIC: 数值大于0返回true，否则返回false</li>
	 * <li>STRING: 尝试将字符串转换为布尔值</li>
	 * <li>BOOLEAN: 直接返回布尔值</li>
	 * </ol>
	 * 其他类型返回默认值
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 单元格的布尔型值，不会返回null。如果单元格为空，返回默认值
	 * @since 1.0.0
	 */
	public static Boolean getBooleanCellValue(final Cell cell, final Boolean defaultValue) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		switch (cell.getCellType()) {
			case NUMERIC:
				return cell.getNumericCellValue() > 0;
			case STRING:
				return BooleanUtils.toBoolean(cell.getStringCellValue());
			case BOOLEAN:
				return cell.getBooleanCellValue();
			default:
				return defaultValue;
		}
	}

	/**
	 * 获取公式单元格的布尔型值
	 * <p>
	 * 返回公式单元格的布尔表示形式，如果单元格为空则返回null。
	 * 如果单元格不是公式类型，则直接返回单元格的布尔型值。
	 * </p>
	 *
	 * @param cell     Excel单元格对象，可以为null
	 * @param workbook Excel工作簿对象，不可为null
	 * @return 公式单元格的布尔型值，可能返回null。如果单元格为空，返回null
	 * @throws IllegalArgumentException 如果workbook为null
	 * @since 1.0.0
	 */
	public static Boolean getBooleanFormulaCellValue(final Cell cell, final Workbook workbook) {
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getBooleanFormulaCellValue(cell, evaluator, null);
	}

	/**
	 * 获取公式单元格的布尔型值
	 * <p>
	 * 返回公式单元格的布尔表示形式，如果单元格为空则返回指定的默认值。
	 * 如果单元格不是公式类型，则直接返回单元格的布尔型值。
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param workbook     Excel工作簿对象，不可为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 公式单元格的布尔型值，不会返回null。如果单元格为空，返回默认值
	 * @throws IllegalArgumentException 如果workbook为null
	 * @since 1.0.0
	 */
	public static Boolean getBooleanFormulaCellValue(final Cell cell, final Workbook workbook, final Boolean defaultValue) {
		Validate.notNull(workbook, "workbook 不可为 null");

		FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		return getBooleanFormulaCellValue(cell, evaluator, defaultValue);
	}

	/**
	 * 获取公式单元格的布尔型值
	 * <p>
	 * 返回公式单元格的布尔表示形式，如果单元格为空则返回null。
	 * 如果单元格不是公式类型，则直接返回单元格的布尔型值。
	 * </p>
	 *
	 * @param cell      Excel单元格对象，可以为null
	 * @param evaluator 公式计算器，不可为null
	 * @return 公式单元格的布尔型值，可能返回null。如果单元格为空，返回null
	 * @throws IllegalArgumentException 如果evaluator为null
	 * @since 1.0.0
	 */
	public static Boolean getBooleanFormulaCellValue(final Cell cell, final FormulaEvaluator evaluator) {
		return getBooleanFormulaCellValue(cell, evaluator, null);
	}

	/**
	 * 获取公式单元格的布尔型值
	 * <p>
	 * 返回公式单元格的布尔表示形式，如果单元格为空则返回指定的默认值。
	 * 如果单元格不是公式类型，则直接返回单元格的布尔型值。
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param evaluator    公式计算器，不可为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 公式单元格的布尔型值，不会返回null。如果单元格为空，返回默认值
	 * @throws IllegalArgumentException 如果evaluator为null
	 * @since 1.0.0
	 */
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
		switch (cell.getCellType()) {
			case NUMERIC:
				return cell.getNumericCellValue() > 0;
			case STRING:
				return BooleanUtils.toBoolean(cell.getStringCellValue());
			case BOOLEAN:
				return cell.getBooleanCellValue();
			default:
				return defaultValue;
		}
	}

	/**
	 * 获取单元格的日期值
	 * <p>
	 * 返回单元格的日期表示形式，如果单元格为空则返回null。
	 * 使用默认的日期格式模式进行解析：
	 * <ol>
	 * <li>Constants.DATE_FORMAT</li>
	 * <li>Constants.DATETIME_FORMAT</li>
	 * <li>Constants.TIME_FORMAT</li>
	 * <li>yyyy/MM/dd</li>
	 * <li>yyyy/M/d</li>
	 * <li>yyyy/M-d</li>
	 * </ol>
	 * </p>
	 *
	 * @param cell Excel单元格对象，可以为null
	 * @return 单元格的日期值，可能返回null。如果单元格为空，返回null
	 * @since 1.0.0
	 */
	public static Date getDateCellValue(final Cell cell) {
		return getDateCellValue(cell, Constants.DATE_FORMAT, Constants.DATETIME_FORMAT,
			Constants.TIME_FORMAT, "yyyy/MM/dd", "yyyy/M/d", "yyyy/M-d");
	}

	/**
	 * 获取单元格的日期值
	 * <p>
	 * 返回单元格的日期表示形式，如果单元格为空则返回指定的默认值。
	 * 使用默认的日期格式模式进行解析：
	 * <ol>
	 * <li>Constants.DATE_FORMAT</li>
	 * <li>Constants.DATETIME_FORMAT</li>
	 * <li>Constants.TIME_FORMAT</li>
	 * <li>yyyy/MM/dd</li>
	 * <li>yyyy/M/d</li>
	 * <li>yyyy/M-d</li>
	 * </ol>
	 * </p>
	 *
	 * @param cell         Excel单元格对象，可以为null
	 * @param defaultValue 默认值，当单元格为空或转换失败时返回
	 * @return 单元格的日期值，不会返回null。如果单元格为空，返回默认值
	 * @since 1.0.0
	 */
	public static Date getDateCellValue(final Cell cell, final Date defaultValue) {
		return getDateCellValue(cell, defaultValue, Constants.DATE_FORMAT, Constants.DATETIME_FORMAT,
			Constants.TIME_FORMAT, "yyyy/MM/dd", "yyyy/M/d", "yyyy/M-d");
	}

	/**
	 * 获取单元格的日期值
	 * <p>
	 * 返回单元格的日期表示形式，如果单元格为空则返回null。
	 * 使用指定的日期格式模式进行解析。
	 * </p>
	 *
	 * @param cell          Excel单元格对象，可以为null
	 * @param parsePatterns 日期格式模式数组，用于解析字符串类型的单元格值
	 * @return 单元格的日期值，可能返回null。如果单元格为空，返回null
	 * @since 1.0.0
	 */
	public static Date getDateCellValue(final Cell cell, final String... parsePatterns) {
		return getDateCellValue(cell, null, parsePatterns);
	}

	/**
	 * 获取单元格的日期值
	 * <p>
	 * 返回单元格的日期表示形式，如果单元格为空则返回指定的默认值。
	 * 支持处理以下单元格类型：
	 * <ol>
	 * <li>NUMERIC: 直接返回日期值</li>
	 * <li>STRING: 尝试使用指定格式模式解析字符串为日期</li>
	 * </ol>
	 * 其他类型返回默认值
	 * </p>
	 *
	 * @param cell          Excel单元格对象，可以为null
	 * @param defaultValue  默认值，当单元格为空或转换失败时返回
	 * @param parsePatterns 日期格式模式数组，用于解析字符串类型的单元格值
	 * @return 单元格的日期值，不会返回null。如果单元格为空，返回默认值
	 * @since 1.0.0
	 */
	public static Date getDateCellValue(final Cell cell, final Date defaultValue, final String... parsePatterns) {
		if (isEmptyCell(cell)) {
			return defaultValue;
		}
		try {
			switch (cell.getCellType()) {
				case NUMERIC:
					return cell.getDateCellValue();
				case STRING:
					return DateUtils.parseDateOrDefault(cell.getStringCellValue(), defaultValue, parsePatterns);
				default:
					return defaultValue;
			}
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	/**
	 * 统计工作表中的行数
	 * <p>
	 * 返回工作表中实际使用的行数，计算方式为：(最后一行索引 - 第一行索引 + 1)。
	 * 如果工作表为空，返回0。
	 * </p>
	 *
	 * @param sheet Excel工作表对象，不可为null
	 * @return 工作表中的行数，如果工作表为空返回0
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static int countRow(final Sheet sheet) {
		Validate.notNull(sheet, "sheet 不可为 null");

		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum == -1) {
			return 0;
		}
		return lastRowNum - sheet.getFirstRowNum() + 1;
	}

	/**
	 * 统计行中的单元格数量
	 * <p>
	 * 返回行中实际使用的单元格数量，计算方式为：(最后一个单元格索引 - 第一个单元格索引 + 1)。
	 * 如果行为空，返回0。
	 * </p>
	 *
	 * @param row Excel行对象，不可为null
	 * @return 行中的单元格数量，如果行为空返回0
	 * @throws IllegalArgumentException 如果row为null
	 * @since 1.0.0
	 */
	public static int countCell(final Row row) {
		Validate.notNull(row, "row 不可为 null");

		short lastCellNum = row.getLastCellNum();
		if (lastCellNum == -1) {
			return 0;
		}
		return lastCellNum - row.getFirstCellNum() + 1;
	}

	/**
	 * 获取或创建工作表中的行
	 * <p>
	 * 如果指定行已存在则返回该行，否则创建新行并返回。
	 * </p>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param rowNum 行索引，必须大于等于0
	 * @return 指定行对象，不会返回null
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static Row getRow(final Sheet sheet, final int rowNum) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		return ObjectUtils.defaultIfNull(sheet.getRow(rowNum), sheet.createRow(rowNum));
	}

	/**
	 * 获取或创建行中的单元格
	 * <p>
	 * 如果指定单元格已存在则返回该单元格，否则创建新单元格并返回。
	 * </p>
	 *
	 * @param row     Excel行对象，不可为null
	 * @param cellNum 单元格索引，必须大于等于0
	 * @return 指定单元格对象，不会返回null
	 * @throws IllegalArgumentException 如果row为null或cellNum小于0
	 * @since 1.0.0
	 */
	public static Cell getCell(final Row row, final int cellNum) {
		Validate.notNull(row, "row 不可为 null");
		Validate.isTrue(cellNum >= 0, "cellNum 必须大于等于0");

		return ObjectUtils.defaultIfNull(row.getCell(cellNum), row.createCell(cellNum));
	}

	/**
	 * 创建工作表标题行
	 * <p>
	 * 在工作表的第一行(索引0)创建标题行，使用默认样式。
	 * </p>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param titles 标题文本数组
	 * @return 标题文本与列索引的映射关系
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static Map<String, Integer> createTitleRow(final Sheet sheet, final String... titles) {
		return createTitleRow(sheet, 0, null, titles);
	}

	/**
	 * 创建工作表标题行
	 * <p>
	 * 在工作表的第一行(索引0)创建标题行，使用指定样式。
	 * </p>
	 *
	 * @param sheet    Excel工作表对象，不可为null
	 * @param rowStyle 行样式，可以为null
	 * @param titles   标题文本数组
	 * @return 标题文本与列索引的映射关系
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static Map<String, Integer> createTitleRow(final Sheet sheet, final CellStyle rowStyle, final String... titles) {
		return createTitleRow(sheet, 0, rowStyle, titles);
	}

	/**
	 * 创建工作表标题行
	 * <p>
	 * 在指定行创建标题行，使用默认样式。
	 * </p>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param rowNum 行索引，必须大于等于0
	 * @param titles 标题文本数组
	 * @return 标题文本与列索引的映射关系
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static Map<String, Integer> createTitleRow(final Sheet sheet, final int rowNum, final String... titles) {
		return createTitleRow(sheet, rowNum, null, titles);
	}

	/**
	 * 创建工作表标题行
	 * <p>
	 * 在指定行创建标题行，使用指定样式。
	 * </p>
	 *
	 * @param sheet    Excel工作表对象，不可为null
	 * @param rowNum   行索引，必须大于等于0
	 * @param rowStyle 行样式，可以为null
	 * @param titles   标题文本数组
	 * @return 标题文本与列索引的映射关系
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
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
			Cell cell = getCell(row, i);
			cell.setCellValue(titles[i]);
			titleIndexMap.put(titles[i], i);
		}
		return titleIndexMap;
	}

	/**
	 * 创建工作表标题行
	 * <p>
	 * 在工作表的第一行(索引0)创建标题行，使用默认样式。
	 * </p>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param titles 标题文本列表
	 * @return 标题文本与列索引的映射关系
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static Map<String, Integer> createTitleRow(final Sheet sheet, final List<String> titles) {
		return createTitleRow(sheet, titles, 0, null);
	}

	/**
	 * 创建工作表标题行
	 * <p>
	 * 在工作表的第一行(索引0)创建标题行，使用指定样式。
	 * </p>
	 *
	 * @param sheet    Excel工作表对象，不可为null
	 * @param titles   标题文本列表
	 * @param rowStyle 行样式，可以为null
	 * @return 标题文本与列索引的映射关系
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static Map<String, Integer> createTitleRow(final Sheet sheet, final List<String> titles, final CellStyle rowStyle) {
		return createTitleRow(sheet, titles, 0, rowStyle);
	}

	/**
	 * 创建工作表标题行
	 * <p>
	 * 在指定行创建标题行，使用默认样式。
	 * </p>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param titles 标题文本列表
	 * @param rowNum 行索引，必须大于等于0
	 * @return 标题文本与列索引的映射关系
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static Map<String, Integer> createTitleRow(final Sheet sheet, final List<String> titles, final int rowNum) {
		return createTitleRow(sheet, titles, rowNum, null);
	}

	/**
	 * 创建工作表标题行
	 * <p>
	 * 在指定行创建标题行，使用指定样式。
	 * </p>
	 *
	 * @param sheet    Excel工作表对象，不可为null
	 * @param titles   标题文本列表
	 * @param rowNum   行索引，必须大于等于0
	 * @param rowStyle 行样式，可以为null
	 * @return 标题文本与列索引的映射关系
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
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
			Cell cell = getCell(row, i);
			cell.setCellValue(titles.get(i));
			titleIndexMap.put(titles.get(i), i);
		}
		return titleIndexMap;
	}

	/**
	 * 自动调整工作表中所有列的宽度
	 * <p>
	 * 根据第一行的内容自动调整所有列的宽度，并在自动调整的基础上增加70%的宽度作为缓冲。
	 * 如果工作表为空或第一行为空，则不执行任何操作。
	 * </p>
	 *
	 * @param sheet Excel工作表对象，不可为null
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void setAdjustColWidth(final Sheet sheet) {
		Validate.notNull(sheet, "sheet 不可为 null");

		Row row = sheet.getRow(0);
		if (Objects.nonNull(row)) {
			setAdjustColWidth(sheet, row.getPhysicalNumberOfCells());
		}
	}

	/**
	 * 自动调整工作表中指定数量的列的宽度
	 * <p>
	 * 自动调整指定数量列的宽度，并在自动调整的基础上增加70%的宽度作为缓冲。
	 * 调整的宽度计算公式为：当前宽度 × 1.7
	 * </p>
	 *
	 * @param sheet       Excel工作表对象，不可为null
	 * @param columnCount 要调整的列数量，必须大于等于0
	 * @throws IllegalArgumentException 如果sheet为null或columnCount小于0
	 * @since 1.0.0
	 */
	public static void setAdjustColWidth(final Sheet sheet, final int columnCount) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(columnCount >= 0, "columnCount 必须大于等于0");

		for (int i = 0; i < columnCount; i++) {
			sheet.autoSizeColumn(i);
			sheet.setColumnWidth(i, (int) (sheet.getColumnWidth(i) * CELL_ADJUST_WIDTH_SCALE));
		}
	}

	/**
	 * 在工作表末尾添加新行并应用消费者操作
	 * <p>
	 * 在工作表的最后一行之后创建新行，并将该行传递给消费者进行处理。
	 * </p>
	 *
	 * @param sheet    Excel工作表对象，不可为null
	 * @param consumer 行消费者，用于处理新创建的行，不可为null
	 * @throws IllegalArgumentException 如果sheet或consumer为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final Consumer<Row> consumer) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.notNull(consumer, "consumer 不可为 null");

		Row row = sheet.createRow(sheet.getLastRowNum() + 1);
		consumer.accept(row);
	}

	/**
	 * 在指定位置插入新行并应用消费者操作
	 * <p>
	 * 在指定行索引位置创建新行，并将该行传递给消费者进行处理。
	 * 如果该位置已有行，则会被新行替换。
	 * </p>
	 *
	 * @param sheet    Excel工作表对象，不可为null
	 * @param consumer 行消费者，用于处理新创建的行，不可为null
	 * @param rowNum   行索引，必须大于等于0
	 * @throws IllegalArgumentException 如果sheet或consumer为null，或rowNum小于0
	 * @since 1.0.0
	 */
	public static void insertRow(final Sheet sheet, final Consumer<Row> consumer, final int rowNum) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.notNull(consumer, "consumer 不可为 null");

		Row row = getRow(sheet, rowNum);
		consumer.accept(row);
	}

	/**
	 * 在工作表末尾添加新行并填充数据
	 * <p>
	 * 在工作表的最后一行之后创建新行，并使用提供的值填充单元格。
	 * 使用默认样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param values 单元格值数组，可以为空
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final Object... values) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, sheet.getLastRowNum() + 1, null, values);
	}

	/**
	 * 在指定位置插入新行并填充数据
	 * <p>
	 * 在指定行索引位置创建新行，并使用提供的值填充单元格。
	 * 使用默认样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param rowNum 行索引，必须大于等于0
	 * @param values 单元格值数组，可以为空
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static void insertRow(final Sheet sheet, final int rowNum, final Object... values) {
		insertRow(sheet, rowNum, null, values);
	}

	/**
	 * 在工作表末尾添加新行并填充数据
	 * <p>
	 * 在工作表的最后一行之后创建新行，并使用提供的值和指定样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet     Excel工作表对象，不可为null
	 * @param cellStyle 单元格样式，可以为null
	 * @param values    单元格值数组，可以为空
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final CellStyle cellStyle, final Object... values) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, sheet.getLastRowNum() + 1, cellStyle, values);
	}

	/**
	 * 在指定位置插入新行并填充数据
	 * <p>
	 * 在指定行索引位置创建新行，并使用提供的值和指定样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet     Excel工作表对象，不可为null
	 * @param rowNum    行索引，必须大于等于0
	 * @param cellStyle 单元格样式，可以为null
	 * @param values    单元格值数组，可以为空
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
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

	/**
	 * 在工作表末尾添加新行并填充数据
	 * <p>
	 * 在工作表的最后一行之后创建新行，并使用提供的列表值填充单元格。
	 * 使用默认样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param values 单元格值列表，可以为空
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final List<Object> values) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, values, sheet.getLastRowNum() + 1, null);
	}

	/**
	 * 在指定位置插入新行并填充数据
	 * <p>
	 * 在指定行索引位置创建新行，并使用提供的列表值填充单元格。
	 * 使用默认样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param values 单元格值列表，可以为空
	 * @param rowNum 行索引，必须大于等于0
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static void insertRow(final Sheet sheet, final List<Object> values, final int rowNum) {
		insertRow(sheet, values, rowNum, null);
	}

	/**
	 * 在工作表末尾添加新行并填充数据
	 * <p>
	 * 在工作表的最后一行之后创建新行，并使用提供的列表值和指定样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet     Excel工作表对象，不可为null
	 * @param values    单元格值列表，可以为空
	 * @param cellStyle 单元格样式，可以为null
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final List<Object> values, final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, values, sheet.getLastRowNum() + 1, cellStyle);
	}

	/**
	 * 在指定位置插入新行并填充数据
	 * <p>
	 * 在指定行索引位置创建新行，并使用提供的列表值和指定样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet     Excel工作表对象，不可为null
	 * @param values    单元格值列表，可以为空
	 * @param rowNum    行索引，必须大于等于0
	 * @param cellStyle 单元格样式，可以为null
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
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

	/**
	 * 在工作表末尾添加新行并填充数据
	 * <p>
	 * 在工作表的最后一行之后创建新行，并使用提供的值-索引对集合填充单元格。
	 * 使用默认样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet           Excel工作表对象，不可为null
	 * @param valueIndexPairs 单元格值-索引对集合，可以为空
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final Collection<Pair<Object, Integer>> valueIndexPairs) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, valueIndexPairs, sheet.getLastRowNum() + 1, null);
	}

	/**
	 * 在指定位置插入新行并填充数据
	 * <p>
	 * 在指定行索引位置创建新行，并使用提供的值-索引对集合填充单元格。
	 * 使用默认样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet           Excel工作表对象，不可为null
	 * @param valueIndexPairs 单元格值-索引对集合，可以为空
	 * @param rowNum          行索引，必须大于等于0
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static void insertRow(final Sheet sheet, final Collection<Pair<Object, Integer>> valueIndexPairs,
								 final int rowNum) {
		insertRow(sheet, valueIndexPairs, rowNum, null);
	}

	/**
	 * 在工作表末尾添加新行并填充数据
	 * <p>
	 * 在工作表的最后一行之后创建新行，并使用提供的值-索引对集合和指定样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet           Excel工作表对象，不可为null
	 * @param valueIndexPairs 单元格值-索引对集合，可以为空
	 * @param cellStyle       单元格样式，可以为null
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final Collection<Pair<Object, Integer>> valueIndexPairs,
							  final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");

		insertRow(sheet, valueIndexPairs, sheet.getLastRowNum() + 1, cellStyle);
	}

	/**
	 * 在指定位置插入新行并填充数据
	 * <p>
	 * 在指定行索引位置创建新行，并使用提供的值-索引对集合和指定样式填充单元格。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 * </p>
	 *
	 * @param sheet           Excel工作表对象，不可为null
	 * @param valueIndexPairs 单元格值-索引对集合，可以为空
	 * @param rowNum          行索引，必须大于等于0
	 * @param cellStyle       单元格样式，可以为null
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
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

	/**
	 * 创建单元格并设置值和样式
	 * <p>
	 * 在指定行的指定列位置创建单元格，并根据值的类型设置单元格内容。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>Number: 设置为数值</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 如果提供了单元格样式，将应用到新创建的单元格。
	 * </p>
	 *
	 * @param row   行对象，不可为null
	 * @param i     列索引，必须大于等于0
	 * @param value 单元格值，可以为null
	 * @param style 单元格样式，可以为null
	 * @throws IllegalArgumentException 如果row为null或i小于0
	 * @since 1.0.0
	 */
	public static void createCell(final Row row, final int i, final Object value, final CellStyle style) {
		Validate.notNull(row, "row 不可为 null");

		Cell cell = getCell(row, i);
		if (Objects.isNull(value)) {
			cell.setBlank();
		} else if (value instanceof Number) {
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value instanceof String) {
			cell.setCellValue((String) value);
		} else if (value instanceof Date) {
			cell.setCellValue((Date) value);
		} else if (value instanceof LocalDate) {
			cell.setCellValue((LocalDate) value);
		} else if (value instanceof LocalDateTime) {
			cell.setCellValue((LocalDateTime) value);
		} else if (value instanceof Calendar) {
			cell.setCellValue((Calendar) value);
		} else if (value instanceof RichTextString) {
			cell.setCellValue((RichTextString) value);
		} else if (value instanceof Hyperlink) {
			cell.setHyperlink((Hyperlink) value);
		} else if (value instanceof URI) {
			URI uri = (URI) value;
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
		} else if (value instanceof URL) {
			URL url = (URL) value;
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
