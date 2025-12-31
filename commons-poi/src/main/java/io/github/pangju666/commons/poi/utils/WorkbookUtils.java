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
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Excel工作簿工具类
 * <p>
 * 提供对Excel工作簿(.xls和.xlsx格式)的各种操作支持，包括：
 * <ul>
 *   <li>工作簿格式验证</li>
 *   <li>工作簿内容读取</li>
 *   <li>单元格数据处理(含URL/URI超链接、LocalDate等复杂类型)</li>
 *   <li>行列操作（读取、创建、写入）</li>
 *   <li>样式设置</li>
 *   <li>工作表/行/单元格流操作</li>
 *   <li>合并区域处理</li>
 * </ul>
 * <p>
 * 特性：
 * <ul>
 *   <li>同时支持HSSF(.xls)和XSSF(.xlsx)格式</li>
 *   <li>所有方法均为静态方法</li>
 *   <li>线程安全</li>
 *   <li>调用者需显式关闭返回的Workbook实例以释放资源</li>
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
		return FileUtils.isAnyMimeType(file, PoiConstants.XLS_MIME_TYPE, PoiConstants.XLSX_MIME_TYPE);
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
	 * <p>
	 * 根据文件扩展名自动识别工作簿格式（XLS或XLSX），并加载相应的工作簿对象。
	 * 调用者负责关闭返回的工作簿对象以释放资源。
	 * </p>
	 *
	 * @param file Excel文件
	 * @return 加载的工作簿对象
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当文件不是Excel格式（XLS或XLSX）时抛出
	 * @since 1.0.0
	 */
	public static Workbook getWorkbook(final File file) throws IOException {
		String mimeType = FileUtils.getMimeType(file);
		if (!Strings.CS.equalsAny(mimeType, PoiConstants.XLS_MIME_TYPE, PoiConstants.XLSX_MIME_TYPE)) {
			throw new IllegalArgumentException("不是 xlsx 或 xls文件");
		}

		try (UnsynchronizedBufferedInputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(file)) {
			switch (mimeType) {
				case PoiConstants.XLS_MIME_TYPE:
					return new HSSFWorkbook(inputStream);
				case PoiConstants.XLSX_MIME_TYPE:
					return new XSSFWorkbook(inputStream);
				default:
					return null;
			}
		}
	}

	/**
	 * 从字节数组加载Excel工作簿
	 * <p>
	 * 根据字节内容自动识别工作簿格式（XLS或XLSX），并加载相应的工作簿对象。
	 * 调用者负责关闭返回的工作簿对象以释放资源。
	 * </p>
	 *
	 * @param bytes Excel文件字节数组
	 * @return 加载的工作簿对象
	 * @throws IOException              当字节数组解析失败时抛出
	 * @throws IllegalArgumentException 当字节数组不是Excel格式（XLS或XLSX）时抛出
	 * @since 1.0.0
	 */
	public static Workbook getWorkbook(final byte[] bytes) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		if (!Strings.CS.equalsAny(mimeType, PoiConstants.XLS_MIME_TYPE, PoiConstants.XLSX_MIME_TYPE)) {
			throw new IllegalArgumentException("不是 xlsx 或 xls文件字节数组");
		}

		InputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
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
	 * 获取工作表中的所有物理行
	 * <p>
	 * 返回工作表中所有实际存在的行（物理行）列表。
	 * </p>
	 * <p>
	 * 该方法基于 {@link Sheet#iterator()} 实现，会自动跳过未创建或为空的行（即不包含 {@code null} 元素）。
	 * 如果工作表为 {@code null}，则返回空列表。
	 * </p>
	 *
	 * @param sheet Excel工作表对象，可以为 {@code null}
	 * @return 行列表，不会返回 {@code null}，且列表中不包含 {@code null} 元素
	 * @see Sheet#iterator()
	 * @since 1.0.0
	 */
	public static List<Row> getPhysicalRows(final Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(sheet);
	}

	/**
	 * 获取工作表中的所有逻辑行
	 * <p>
	 * 返回工作表中从第一行（{@code firstRowNum}）到最后一行（{@code lastRowNum}）的所有逻辑行。
	 * 列表中的元素可能为 {@code null}（如果对应的逻辑行未创建）。
	 * </p>
	 * <p>
	 * 如果工作表为 {@code null} 或没有任何行，则返回空列表。
	 * </p>
	 *
	 * @param sheet Excel工作表对象，可以为 {@code null}
	 * @return 逻辑行列表，不会返回 {@code null}
	 * @since 1.0.0
	 */
	public static List<Row> getLogicalRows(final Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}

		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum == -1) {
			return Collections.emptyList();
		}

		return getRows(sheet, sheet.getFirstRowNum(), lastRowNum);
	}

	/**
	 * 获取工作表中从指定行开始的所有逻辑行
	 * <p>
	 * 返回工作表中从 {@code startRowNum} 开始到最后一行（{@code lastRowNum}）的逻辑行列表。
	 * 列表中的元素可能为 {@code null}（如果对应的逻辑行未创建）。
	 * </p>
	 * <p>
	 * 如果工作表为 {@code null}、没有任何行或 {@code startRowNum} 超过最后一行索引，则返回空列表。
	 * </p>
	 *
	 * @param sheet       Excel工作表对象，可以为 {@code null}
	 * @param startRowNum 起始行号(从0开始)，必须大于等于0
	 * @return 逻辑行列表，不会返回 {@code null}
	 * @throws IllegalArgumentException 如果 {@code startRowNum} 小于0
	 * @since 1.0.0
	 */
	public static List<Row> getLogicalRows(final Sheet sheet, final int startRowNum) {
		Validate.isTrue(startRowNum >= 0, "startRowNum 必须大于等于0");

		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum == -1 || startRowNum > lastRowNum) {
			return Collections.emptyList();
		}
		return getRows(sheet, startRowNum, lastRowNum);
	}

	/**
	 * 获取工作表中指定范围内的逻辑行
	 * <p>
	 * 返回工作表中从 {@code startRowNum} 到 {@code endRowNum} 范围内的逻辑行列表。
	 * 列表中的元素可能为 {@code null}（如果对应的逻辑行未创建）。
	 * </p>
	 * <p>
	 * 该方法会自动处理边界情况：
	 * <ul>
	 *   <li>如果 {@code endRowNum} 大于工作表的最后一行索引，将获取到最后一行截止</li>
	 * </ul>
	 * 如果计算后的有效范围为空（例如请求的范围完全在数据范围之外），则返回空列表。
	 * </p>
	 *
	 * @param sheet       Excel工作表对象，可以为 {@code null}
	 * @param startRowNum 起始行号(从0开始)，必须大于等于0
	 * @param endRowNum   结束行号(包含在内)，必须大于等于 {@code startRowNum}
	 * @return 逻辑行列表，不会返回 {@code null}
	 * @throws IllegalArgumentException 如果 {@code startRowNum} 小于0或大于 {@code endRowNum}
	 * @since 1.0.0
	 */
	public static List<Row> getLogicalRows(final Sheet sheet, final int startRowNum, int endRowNum) {
		Validate.isTrue(startRowNum >= 0, "startRowNum 必须大于等于0");
		Validate.isTrue(startRowNum <= endRowNum, "startRowNum 必须小于等于 endRowNum");

		if (Objects.isNull(sheet)) {
			return Collections.emptyList();
		}
		int lastRowNum = sheet.getLastRowNum();
		endRowNum = Math.min(lastRowNum, endRowNum);
		if (lastRowNum == -1 || startRowNum > endRowNum) {
			return Collections.emptyList();
		}
		return getRows(sheet, startRowNum, endRowNum);
	}

	/**
	 * 构建工作表逻辑行列表的内部方法
	 * <p>
	 * 根据给定的起止行号直接构建逻辑行列表，不进行任何参数校验或边界修正。
	 * 调用方需保证 {@code sheet} 非 {@code null}，且 {@code startRowNum} 与 {@code endRowNum} 已经合法且在边界内。
	 * </p>
	 * <p>
	 * 列表中的元素可能为 {@code null}（如果对应的逻辑行未创建）。
	 * </p>
	 *
	 * @param sheet       Excel工作表对象，不可为 {@code null}
	 * @param startRowNum 起始行号(从0开始)
	 * @param endRowNum   结束行号(包含在内)
	 * @return 逻辑行列表，不会返回 {@code null}
	 * @since 1.0.0
	 */
	protected static List<Row> getRows(final Sheet sheet, final int startRowNum, int endRowNum) {
		int size = endRowNum - startRowNum + 1;
		List<Row> rows = new ArrayList<>(size);
		for (int i = startRowNum; i <= endRowNum; i++) {
			rows.add(sheet.getRow(i));
		}
		return rows;
	}

	/**
	 * 获取行中的所有物理单元格
	 * <p>
	 * 返回行中所有实际存在的单元格（物理单元格）的列表。
	 * 如果行为 {@code null} 则返回空列表。
	 * </p>
	 * <p>
	 * 基于 {@link Row#iterator()} 实现，自动跳过未创建或为空的单元格（列表中不包含 {@code null} 元素）。
	 * </p>
	 *
	 * @param row 行对象，可以为 {@code null}
	 * @return 物理单元格列表，不会返回 {@code null}
	 * @since 1.0.0
	 */
	public static List<Cell> getPhysicalCells(final Row row) {
		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		return IterableUtils.toList(row);
	}

	/**
	 * 获取行中的所有逻辑单元格
	 * <p>
	 * 返回行中从 {@code firstCellNum} 到 {@code lastCellNum} 的逻辑单元格列表，
	 * 使用默认的缺失单元格策略（{@link Row.MissingCellPolicy#RETURN_NULL_AND_BLANK}）。
	 * </p>
	 * <p>
	 * 如果行为 {@code null} 或没有任何单元格，则返回空列表。
	 * 列表中的元素可能为 {@code null}（取决于缺失单元格策略）。
	 * </p>
	 *
	 * @param row 行对象，可以为 {@code null}
	 * @return 逻辑单元格列表，不会返回 {@code null}
	 * @since 1.0.0
	 */
	public static List<Cell> getLogicalCells(final Row row) {
		return getLogicalCells(row, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	/**
	 * 获取行中的所有逻辑单元格
	 * <p>
	 * 返回行中从 {@code firstCellNum} 到 {@code lastCellNum} 的逻辑单元格列表，
	 * 使用指定的缺失单元格策略处理空单元格。
	 * </p>
	 * <p>
	 * 如果行为 {@code null} 或没有任何单元格，则返回空列表。
	 * 列表中的元素可能为 {@code null}（取决于缺失单元格策略）。
	 * </p>
	 *
	 * @param row    行对象，可以为 {@code null}
	 * @param policy 缺失单元格处理策略，不可为 {@code null}
	 * @return 逻辑单元格列表，不会返回 {@code null}
	 * @throws IllegalArgumentException 如果 {@code policy} 为 {@code null}
	 * @since 1.0.0
	 */
	public static List<Cell> getLogicalCells(final Row row, final Row.MissingCellPolicy policy) {
		Validate.notNull(policy, "policy 不可为 null");

		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		int lastCellNum = row.getLastCellNum();
		if (lastCellNum == -1) {
			return Collections.emptyList();
		}
		int firstCellNum = row.getFirstCellNum();
		return getCells(row, firstCellNum, lastCellNum - 1, policy);
	}

	/**
	 * 获取行中从指定列开始的逻辑单元格
	 * <p>
	 * 返回行中从 {@code startCellNum} 开始到最后一列的逻辑单元格列表，
	 * 使用默认的缺失单元格策略（{@link Row.MissingCellPolicy#RETURN_NULL_AND_BLANK}）。
	 * </p>
	 * <p>
	 * 如果行为 {@code null}、没有单元格或 {@code startCellNum} 超过最后一列索引，则返回空列表。
	 * 列表中的元素可能为 {@code null}（取决于缺失单元格策略）。
	 * </p>
	 *
	 * @param row          行对象，可以为 {@code null}
	 * @param startCellNum 起始列号(从0开始)，必须大于等于0
	 * @return 逻辑单元格列表，不会返回 {@code null}
	 * @throws IllegalArgumentException 如果 {@code startCellNum} 小于0
	 * @since 1.0.0
	 */
	public static List<Cell> getLogicalCells(final Row row, final int startCellNum) {
		return getLogicalCells(row, startCellNum, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	/**
	 * 获取行中从指定列开始的逻辑单元格
	 * <p>
	 * 返回行中从 {@code startCellNum} 开始到最后一列的逻辑单元格列表，
	 * 使用指定的缺失单元格策略处理空单元格。
	 * </p>
	 * <p>
	 * 如果行为 {@code null}、没有单元格或 {@code startCellNum} 超过最后一列索引，则返回空列表。
	 * 列表中的元素可能为 {@code null}（取决于缺失单元格策略）。
	 * </p>
	 *
	 * @param row          行对象，可以为 {@code null}
	 * @param startCellNum 起始列号(从0开始)，必须大于等于0
	 * @param policy       缺失单元格处理策略，不可为 {@code null}
	 * @return 逻辑单元格列表，不会返回 {@code null}
	 * @throws IllegalArgumentException 如果 {@code policy} 为 {@code null} 或 {@code startCellNum} 小于0
	 * @since 1.0.0
	 */
	public static List<Cell> getLogicalCells(final Row row, final int startCellNum, final Row.MissingCellPolicy policy) {
		Validate.notNull(policy, "policy 不可为 null");
		Validate.isTrue(startCellNum >= 0, "startCellNum 必须大于等于0");

		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		int lastCellNum = row.getLastCellNum();
		if (lastCellNum == -1 || startCellNum > lastCellNum - 1) {
			return Collections.emptyList();
		}
		return getCells(row, startCellNum, lastCellNum - 1, policy);
	}

	/**
	 * 获取行中指定范围内的逻辑单元格
	 * <p>
	 * 返回行中从 {@code startCellNum} 到 {@code endCellNum} 范围内的逻辑单元格列表，
	 * 使用默认的缺失单元格策略（{@link Row.MissingCellPolicy#RETURN_NULL_AND_BLANK}）。
	 * </p>
	 *
	 * @param row          行对象，可以为 {@code null}
	 * @param startCellNum 起始列号(从0开始)，必须大于等于0
	 * @param endCellNum   结束列号(包含在内)，必须大于等于 {@code startCellNum}
	 * @return 逻辑单元格列表，不会返回 {@code null}
	 * @throws IllegalArgumentException 如果 {@code startCellNum} 小于0或大于 {@code endCellNum}
	 * @since 1.0.0
	 */
	public static List<Cell> getLogicalCells(final Row row, final int startCellNum, final int endCellNum) {
		return getLogicalCells(row, startCellNum, endCellNum, Row.MissingCellPolicy.RETURN_NULL_AND_BLANK);
	}

	/**
	 * 获取行中指定范围内的逻辑单元格
	 * <p>
	 * 返回行中从 {@code startCellNum} 到 {@code endCellNum} 范围内的逻辑单元格列表，
	 * 使用指定的缺失单元格策略处理空单元格。
	 * </p>
	 * <p>
	 * 该方法会自动处理边界情况：
	 * <ul>
	 *   <li>如果 {@code endCellNum} 大于行的最后一列索引，将获取到最后一列截止</li>
	 * </ul>
	 * 如果计算后的有效范围为空（例如请求的范围完全在数据范围之外），则返回空列表。
	 * 列表中的元素可能为 {@code null}（取决于缺失单元格策略）。
	 * </p>
	 *
	 * @param row          行对象，可以为 {@code null}
	 * @param startCellNum 起始列号(从0开始)，必须大于等于0
	 * @param endCellNum   结束列号(包含在内)，必须大于等于 {@code startCellNum}
	 * @param policy       缺失单元格处理策略，不可为 {@code null}
	 * @return 逻辑单元格列表，不会返回 {@code null}
	 * @throws IllegalArgumentException 如果 {@code policy} 为 {@code null}、{@code startCellNum} 小于0或大于 {@code endCellNum}
	 * @since 1.0.0
	 */
	public static List<Cell> getLogicalCells(final Row row, final int startCellNum, int endCellNum, final Row.MissingCellPolicy policy) {
		Validate.notNull(policy, "policy 不可为 null");
		Validate.isTrue(startCellNum >= 0, "startCellNum 必须大于等于0");
		Validate.isTrue(startCellNum <= endCellNum, "startCellNum 必须小于等于 endCellNum");

		if (Objects.isNull(row)) {
			return Collections.emptyList();
		}
		int lastCellNum = row.getLastCellNum();
		endCellNum = Math.min(lastCellNum - 1, endCellNum);
		if (lastCellNum == -1 || startCellNum > endCellNum) {
			return Collections.emptyList();
		}
		return getCells(row, startCellNum, endCellNum, policy);
	}

	/**
	 * 构建行的逻辑单元格列表的内部方法
	 * <p>
	 * 根据给定的起止列号与缺失单元格策略直接构建逻辑单元格列表，
	 * 不进行任何参数校验或边界修正。
	 * 调用方需保证 {@code row} 非 {@code null}，且 {@code startCellNum} 与 {@code endCellNum} 已经合法且在边界内。
	 * </p>
	 * <p>
	 * 列表中的元素可能为 {@code null}（取决于缺失单元格策略）。
	 * </p>
	 *
	 * @param row          行对象，不可为 {@code null}
	 * @param startCellNum 起始列号(从0开始)
	 * @param endCellNum   结束列号(包含在内)
	 * @param policy       缺失单元格处理策略，不可为 {@code null}
	 * @return 逻辑单元格列表，不会返回 {@code null}
	 * @since 1.0.0
	 */
	protected static List<Cell> getCells(final Row row, final int startCellNum, int endCellNum, final Row.MissingCellPolicy policy) {
		int size = endCellNum - startCellNum + 1;
		List<Cell> cells = new ArrayList<>(size);
		for (int i = startCellNum; i <= endCellNum; i++) {
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
		if (Objects.isNull(workbook)) {
			return Stream.empty();
		}
		return StreamSupport.stream(workbook.spliterator(), false);
	}

	/**
	 * 创建工作表中所有物理行的顺序流
	 * <p>
	 * 返回工作表中所有实际存在的行（物理行）的顺序流。如果工作表为 {@code null} 则返回空流。
	 * </p>
	 * <p>
	 * 基于 {@link Sheet#spliterator()} 实现，自动跳过未创建或为空的行（流中不包含 {@code null} 元素）。
	 * </p>
	 *
	 * @param sheet Excel工作表对象，可以为 {@code null}
	 * @return 物理行流，不会返回 {@code null}。如果工作表为 {@code null}，返回空流
	 * @since 1.0.0
	 */
	public static Stream<Row> physicalRowStream(final Sheet sheet) {
		if (Objects.isNull(sheet)) {
			return Stream.empty();
		}
		return StreamSupport.stream(sheet.spliterator(), false);
	}

	/**
	 * 创建行中所有物理单元格的顺序流
	 * <p>
	 * 返回行中所有实际存在的单元格（物理单元格）的顺序流。如果行为 {@code null} 则返回空流。
	 * </p>
	 * <p>
	 * 基于 {@link Row#spliterator()} 实现，自动跳过未创建或为空的单元格（流中不包含 {@code null} 元素）。
	 * </p>
	 *
	 * @param row Excel行对象，可以为 {@code null}
	 * @return 物理单元格流，不会返回 {@code null}。如果行为 {@code null}，返回空流
	 * @since 1.0.0
	 */
	public static Stream<Cell> physicalCellStream(final Row row) {
		if (Objects.isNull(row)) {
			return Stream.empty();
		}
		return StreamSupport.stream(row.spliterator(), false);
	}

	/**
	 * 获取合并单元格区域中的单元格
	 * <p>
	 * 根据指定的行号和列号查找合并单元格区域，如果找到则返回合并区域左上角的单元格，
	 * 否则返回null。如果指定的单元格不在任何合并区域内，则返回null。
	 * </p>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param rowNum    行号(从0开始)
	 * @param columnNum 列号(从0开始)
	 * @return 合并区域左上角的单元格，如果未找到合并区域或单元格不在合并区域内则返回null
	 * @throws IllegalArgumentException 如果sheet为null或row小于0或column小于0
	 * @since 1.0.0
	 */
	public static Cell getMergedRegionCell(final Sheet sheet, final int rowNum, final int columnNum) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");
		Validate.isTrue(columnNum >= 0, "column 必须大于等于0");

		int sheetMergeCount = sheet.getNumMergedRegions();
		for (int i = 0; i < sheetMergeCount; i++) {
			CellRangeAddress cellRangeAddress = sheet.getMergedRegion(i);
			int firstCellNum = cellRangeAddress.getFirstColumn();
			int lastCellNum = cellRangeAddress.getLastColumn();
			int firstRowNum = cellRangeAddress.getFirstRow();
			int lastRowNum = cellRangeAddress.getLastRow();
			if (rowNum >= firstRowNum && rowNum <= lastRowNum && columnNum >= firstCellNum && columnNum <= lastCellNum) {
				Row firstRow = sheet.getRow(firstRowNum);
				if (Objects.isNull(firstRow)) {
					return null;
				}
				return firstRow.getCell(firstCellNum);
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
					try {
						return Double.valueOf(cell.getStringCellValue());
					} catch (final NumberFormatException e) {
						return defaultValue;
					}
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
		switch (cellValue.getCellType()) {
			case NUMERIC:
				return cellValue.getNumberValue();
			case STRING:
				try {
					return Double.valueOf(cellValue.getStringValue());
				} catch (final NumberFormatException e) {
					return defaultValue;
				}
			case BOOLEAN:
				return BooleanUtils.toIntegerObject(cellValue.getBooleanValue()).doubleValue();
			default:
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
		switch (cellValue.getCellType()) {
			case NUMERIC:
				return cellValue.getNumberValue() > 0;
			case STRING:
				return BooleanUtils.toBoolean(cellValue.getStringValue());
			case BOOLEAN:
				return cellValue.getBooleanValue();
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
	 * 获取工作表的逻辑行跨度（用于循环遍历）
	 * <p>
	 * 返回值 = {@code lastRowNum - firstRowNum + 1}，表示从第一行到最后一行的索引范围大小。
	 * 此值适用于 for 循环的终止条件，即使中间存在未创建的空行也会被包含。
	 * </p>
	 * <p>
	 * 注意：
	 * <ul>
	 *   <li><b>此值不等于物理行数</b>（{@link Sheet#getPhysicalNumberOfRows()}），
	 *       也不代表实际包含数据的行数；它仅反映行索引的跨度。</li>
	 *   <li>例如：若第 1 行（索引 0）和第 100 行（索引 99）被创建，
	 *       则本方法返回 {@code 100}，即使中间 98 行完全为空。</li>
	 *   <li>若工作表中无任何行（{@code getLastRowNum() == -1}），则返回 {@code 0}。</li>
	 *   <li>典型用途是在 for 循环中遍历所有可能的行位置，配合 {@code sheet.getRow(rowIndex)} 使用。</li>
	 * </ul>
	 * </p>
	 *
	 * @param sheet Excel工作表对象，不可为null
	 * @return 行跨度（&gt;= 0），若工作表无任何行则返回 0
	 * @throws IllegalArgumentException 当 sheet 为 null
	 * @see Sheet#getFirstRowNum()
	 * @see Sheet#getLastRowNum()
	 * @since 1.0.0
	 */
	public static int getRowSpan(final Sheet sheet) {
		Validate.notNull(sheet, "sheet 不可为 null");

		int lastRowNum = sheet.getLastRowNum();
		if (lastRowNum == -1) {
			return 0;
		}
		return lastRowNum - sheet.getFirstRowNum() + 1;
	}

	/**
	 * 获取行的逻辑列跨度（用于循环遍历）
	 * <p>
	 * 返回值等同于 {@link Row#getLastCellNum()}，但保证结果不小于 0。
	 * 此值表示该行中可能包含单元格的最大列索引（不含），适用于 for 循环的终止条件。
	 * </p>
	 * <p>
	 * 注意：
	 * <ul>
	 *   <li>此值包含中间未创建的空单元格（例如 A 列和 C 列有单元格，则返回 3）</li>
	 *   <li>即使单元格存在但内容为空（BLANK 或空字符串），只要被创建过，也会被计入跨度</li>
	 *   <li>若行中无任何单元格，则返回 0</li>
	 * </ul>
	 * </p>
	 *
	 * @param row Excel 行对象，不可为 null
	 * @return 列跨度（&gt;= 0），表示从第 0 列到最后一列的逻辑宽度
	 * @throws IllegalArgumentException 当 row 为 null 时抛出
	 * @see Row#getLastCellNum()
	 * @see Row#getPhysicalNumberOfCells()
	 * @since 1.0.0
	 */
	public static int getColumnSpan(final Row row) {
		Validate.notNull(row, "row 不可为 null");
		return Math.max(row.getLastCellNum(), 0);
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
	public static Row createRowIfAbsent(final Sheet sheet, final int rowNum) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Row row = sheet.getRow(rowNum);
		return Objects.nonNull(row) ? row : sheet.createRow(rowNum);
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
	public static Cell createCellIfAbsent(final Row row, final int cellNum) {
		Validate.notNull(row, "row 不可为 null");
		Validate.isTrue(cellNum >= 0, "cellNum 必须大于等于0");

		Cell cell = row.getCell(cellNum);
		return Objects.nonNull(cell) ? cell : row.createCell(cellNum);
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
		Row row = createRowIfAbsent(sheet, rowNum);
		if (Objects.nonNull(rowStyle)) {
			row.setRowStyle(rowStyle);
		}
		for (int i = 0; i < titles.length; i++) {
			Cell cell = createCellIfAbsent(row, i);
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
		Row row = createRowIfAbsent(sheet, rowNum);
		if (Objects.nonNull(rowStyle)) {
			row.setRowStyle(rowStyle);
		}
		for (int i = 0; i < titles.size(); i++) {
			Cell cell = createCellIfAbsent(row, i);
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
	 * 在指定位置写入行数据并应用消费者操作
	 * <p>
	 * 在指定行索引位置写入数据（如果行不存在则创建），并将该行传递给消费者进行处理。
	 * </p>
	 *
	 * @param sheet    Excel工作表对象，不可为null
	 * @param rowNum   行索引，必须大于等于0
	 * @param consumer 行消费者，用于处理该行，不可为null
	 * @throws IllegalArgumentException 如果sheet或consumer为null，或rowNum小于0
	 * @since 1.0.0
	 */
	public static void writeRowAt(final Sheet sheet, final int rowNum, final Consumer<Row> consumer) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.notNull(consumer, "consumer 不可为 null");

		Row row = createRowIfAbsent(sheet, rowNum);
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
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
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

		writeRowAt(sheet, sheet.getLastRowNum() + 1, null, values);
	}

	/**
	 * 在指定位置写入行数据
	 * <p>
	 * 在指定行索引位置写入数据（如果行不存在则创建），并使用提供的值填充单元格。
	 * 使用默认样式填充单元格。
	 * 如果指定行的单元格存在值则会替换掉原本的值。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param rowNum 行索引，必须大于等于0
	 * @param values 单元格值数组，可以为空
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static void writeRowAt(final Sheet sheet, final int rowNum, final Object... values) {
		writeRowAt(sheet, rowNum, null, values);
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
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
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

		writeRowAt(sheet, sheet.getLastRowNum() + 1, cellStyle, values);
	}

	/**
	 * 在指定位置写入行数据
	 * <p>
	 * 在指定行索引位置写入数据（如果行不存在则创建），并使用提供的值和指定样式填充单元格。
	 * 如果指定行的单元格存在值则会替换掉原本的值。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
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
	public static void writeRowAt(final Sheet sheet, final int rowNum, final CellStyle cellStyle, final Object... values) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Row row = createRowIfAbsent(sheet, rowNum);
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
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param values 单元格值列表，可以为空
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final List<?> values) {
		Validate.notNull(sheet, "sheet 不可为 null");

		writeRowAt(sheet, values, sheet.getLastRowNum() + 1, null);
	}

	/**
	 * 在指定位置写入行数据
	 * <p>
	 * 在指定行索引位置写入数据（如果行不存在则创建），并使用提供的列表值填充单元格。
	 * 使用默认样式填充单元格。
	 * 如果指定行的单元格存在值则会替换掉原本的值。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet  Excel工作表对象，不可为null
	 * @param values 单元格值列表，可以为空
	 * @param rowNum 行索引，必须大于等于0
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static void writeRowAt(final Sheet sheet, final List<?> values, final int rowNum) {
		writeRowAt(sheet, values, rowNum, null);
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
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet     Excel工作表对象，不可为null
	 * @param values    单元格值列表，可以为空
	 * @param cellStyle 单元格样式，可以为null
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final List<?> values, final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");

		writeRowAt(sheet, values, sheet.getLastRowNum() + 1, cellStyle);
	}

	/**
	 * 在指定位置写入行数据
	 * <p>
	 * 在指定行索引位置写入数据（如果行不存在则创建），并使用提供的列表值和指定样式填充单元格。
	 * 如果指定行的单元格存在值则会替换掉原本的值。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
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
	public static void writeRowAt(final Sheet sheet, final List<?> values, final int rowNum, final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Row row = createRowIfAbsent(sheet, rowNum);
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
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet           Excel工作表对象，不可为null
	 * @param valueIndexPairs 单元格值-索引对集合，可以为空
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final Collection<Pair<?, Integer>> valueIndexPairs) {
		Validate.notNull(sheet, "sheet 不可为 null");

		writeRowAt(sheet, valueIndexPairs, sheet.getLastRowNum() + 1, null);
	}

	/**
	 * 在指定位置写入行数据
	 * <p>
	 * 在指定行索引位置写入数据（如果行不存在则创建），并使用提供的值-索引对集合填充单元格。
	 * 使用默认样式填充单元格。
	 * 如果指定行的单元格存在值则会替换掉原本的值。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet           Excel工作表对象，不可为null
	 * @param valueIndexPairs 单元格值-索引对集合，可以为空
	 * @param rowNum          行索引，必须大于等于0
	 * @throws IllegalArgumentException 如果sheet为null或rowNum小于0
	 * @since 1.0.0
	 */
	public static void writeRowAt(final Sheet sheet, final Collection<Pair<?, Integer>> valueIndexPairs,
								 final int rowNum) {
		writeRowAt(sheet, valueIndexPairs, rowNum, null);
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
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
	 *   <li>其他类型: 转换为JSON字符串</li>
	 * </ul>
	 *
	 * @param sheet           Excel工作表对象，不可为null
	 * @param valueIndexPairs 单元格值-索引对集合，可以为空
	 * @param cellStyle       单元格样式，可以为null
	 * @throws IllegalArgumentException 如果sheet为null
	 * @since 1.0.0
	 */
	public static void addRow(final Sheet sheet, final Collection<Pair<?, Integer>> valueIndexPairs,
							  final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");

		writeRowAt(sheet, valueIndexPairs, sheet.getLastRowNum() + 1, cellStyle);
	}

	/**
	 * 在指定位置写入行数据
	 * <p>
	 * 在指定行索引位置写入数据（如果行不存在则创建），并使用提供的值-索引对集合和指定样式填充单元格。
	 * 如果指定行的单元格存在值则会替换掉原本的值。
	 * </p>
	 * <p>
	 * 支持处理多种数据类型：
	 * <ul>
	 *   <li>null: 设置为空白单元格</li>
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
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
	public static void writeRowAt(final Sheet sheet, final Collection<Pair<?, Integer>> valueIndexPairs,
								 final int rowNum, final CellStyle cellStyle) {
		Validate.notNull(sheet, "sheet 不可为 null");
		Validate.isTrue(rowNum >= 0, "rowNum 必须大于等于0");

		Row row = createRowIfAbsent(sheet, rowNum);
		if (CollectionUtils.isEmpty(valueIndexPairs)) {
			return;
		}

		for (Pair<?, Integer> valueIndexPair : valueIndexPairs) {
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
	 *   <li>BigDecimal: 设置为字符串（保留精度）</li>
	 *   <li>BigInteger: 设置为字符串（保留精度）</li>
	 *   <li>Number: 设置为数值（Double）</li>
	 *   <li>Boolean: 设置为布尔值</li>
	 *   <li>String: 设置为字符串</li>
	 *   <li>Date/LocalDate/LocalDateTime/Calendar: 设置为日期（HSSF模式下自动转换Java8日期类型）</li>
	 *   <li>RichTextString: 设置为富文本</li>
	 *   <li>Hyperlink: 设置为超链接</li>
	 *   <li>URI/URL: 自动创建超链接并设置内容</li>
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

		Cell cell = createCellIfAbsent(row, i);
		if (Objects.isNull(value)) {
			cell.setBlank();
		} else if (value instanceof BigDecimal) {
			cell.setCellValue(((BigDecimal) value).toPlainString());
		} else if (value instanceof BigInteger) {
			cell.setCellValue(value.toString());
		} else if (value instanceof Number) {
			cell.setCellValue(((Number) value).doubleValue());
		} else if (value instanceof Boolean) {
			cell.setCellValue((Boolean) value);
		} else if (value instanceof String) {
			cell.setCellValue((String) value);
		} else if (value instanceof Date) {
			cell.setCellValue((Date) value);
		} else if (value instanceof LocalDate) {
			if (row instanceof HSSFRow) {
				cell.setCellValue(Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant()));
			} else {
				cell.setCellValue((LocalDate) value);
			}
		} else if (value instanceof LocalDateTime) {
			if (row instanceof HSSFRow) {
				cell.setCellValue(Date.from(((LocalDateTime) value).atZone(ZoneId.systemDefault()).toInstant()));
			} else {
				cell.setCellValue((LocalDateTime) value);
			}
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
			if (Strings.CS.equals(uri.getScheme(), "file")) {
				hyperlink = creationHelper.createHyperlink(HyperlinkType.FILE);
				hyperlink.setAddress(uri.toString());
			} else {
				hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
				try {
					hyperlink.setAddress(uri.toURL().toString());
				} catch (MalformedURLException e) {
					throw ExceptionUtils.asRuntimeException(e);
				}
			}
			hyperlink.setLabel(uri.toString());
			cell.setHyperlink(hyperlink);
			cell.setCellValue(uri.toString());
		} else if (value instanceof URL) {
			URL url = (URL) value;
			CreationHelper creationHelper = row.getSheet().getWorkbook().getCreationHelper();
			Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
			hyperlink.setLabel(url.toString());
			hyperlink.setAddress(url.toString());
			cell.setHyperlink(hyperlink);
			cell.setCellValue(url.toString());
		} else {
			cell.setCellValue(JsonUtils.toString(value));
		}
		if (Objects.nonNull(style)) {
			cell.setCellStyle(style);
		}
	}
}
