package io.github.pangju666.commons.poi.io.resource;

import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.poi.lang.PoiConstants;
import org.apache.commons.lang3.Strings;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Microsoft Excel 表格资源类
 * <p>
 * 该类用于处理 .xls 和 .xlsx 格式的 Excel 表格，基于 Apache POI 的 Workbook 实现。
 * 支持从文件路径、File 对象、字节数组、输入流或 IOResource 创建表格资源。
 * 自动识别表格格式（.xls 或 .xlsx）并使用相应的实现类（HSSFWorkbook 或 XSSFWorkbook）。
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class ExcelResource extends IOResource {
	/**
	 * Workbook 工作簿对象
	 *
	 * @since 2.1.0
	 */
	protected volatile Workbook workbook;

	/**
	 * 使用 IOResource 创建 ExcelResource
	 * <p>如果传入的资源不是 ExcelResource 实例，则验证其 MIME 类型。</p>
	 *
	 * @param resource IOResource 资源对象
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 excel 表格类型时抛出
	 * @since 2.1.0
	 */
	public ExcelResource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof ExcelResource)) {
			validateType("resource 不是 excel 表格资源");
		}
	}

	/**
	 * 使用文件路径创建 ExcelResource
	 *
	 * @param filePath excel 表格文件路径
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 excel 表格类型时抛出
	 * @since 2.1.0
	 */
	public ExcelResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是 excel 表格文件路径");
	}

	/**
	 * 使用 File 对象创建 ExcelResource
	 *
	 * @param file excel 表格文件对象
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 excel 表格类型时抛出
	 * @since 2.1.0
	 */
	public ExcelResource(File file) throws IOException {
		super(file);

		validateType("file 不是 excel 表格文件");
	}

	/**
	 * 使用字节数组创建 ExcelResource
	 *
	 * @param bytes excel 表格字节数组
	 * @throws IOException                  当读取字节数组失败时抛出
	 * @throws UnsupportedResourceException 当字节数组不是 excel 表格类型时抛出
	 * @since 2.1.0
	 */
	public ExcelResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是 excel 表格数据");
	}

	/**
	 * 使用输入流创建 ExcelResource
	 *
	 * @param inputStream excel 表格输入流
	 * @throws IOException                  当读取输入流失败时抛出
	 * @throws UnsupportedResourceException 当输入流不是 excel 表格类型时抛出
	 * @since 2.1.0
	 */
	public ExcelResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 excel 表格输入流");
	}

	/**
	 * 获取 Workbook 工作簿对象
	 * <p>
	 * 该方法采用懒加载模式，首次调用时创建工作簿对象，后续调用返回缓存的实例。
	 * 根据 MIME 类型自动选择对应的实现：
	 * <ul>
	 *   <li>.xls 格式：使用 HSSFWorkbook</li>
	 *   <li>.xlsx 格式：使用 XSSFWorkbook</li>
	 * </ul>
	 * 如果资源来源于文件，则从文件系统加载；否则从输入流加载。
	 * </p>
	 *
	 * @return Workbook 工作簿对象
	 * @throws IOException                  当读取工作簿失败时抛出
	 * @throws UnsupportedResourceException 当文件格式不正确时抛出
	 * @since 2.1.0
	 */
	public synchronized Workbook getWorkbook() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(workbook)) {
				return workbook;
			}

			if (isXls()) {
				if (Objects.nonNull(file)) {
					try (POIFSFileSystem fs = new POIFSFileSystem(file)) {
						workbook = new HSSFWorkbook(fs);
					}
				} else {
					try (InputStream inputStream = newBufferedInputStream();
					     POIFSFileSystem fs = new POIFSFileSystem(inputStream)) {
						workbook = new HSSFWorkbook(fs);
					}
				}
			} else {
				if (Objects.nonNull(file)) {
					try {
						workbook = new XSSFWorkbook(OPCPackage.open(file));
					} catch (InvalidFormatException e) {
						throw new UnsupportedResourceException("file 不是 xlsx 格式表格文件", e);
					}
				} else {
					workbook = new XSSFWorkbook(newBufferedInputStream(), true);
				}
			}
			return workbook;
		}
	}

	/**
	 * 判断是否为 .xls 格式表格
	 *
	 * @return 如果是 .xls 格式返回 true，否则返回 false
	 * @since 2.1.0
	 */
	public boolean isXls() {
		return PoiConstants.XLS_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 判断是否为 .xlsx 格式表格
	 *
	 * @return 如果是 .xlsx 格式返回 true，否则返回 false
	 * @since 2.1.0
	 */
	public boolean isXlsx() {
		return PoiConstants.XLSX_MIME_TYPE.equals(mimeType);
	}

	/**
	 * 验证资源类型是否为 excel 表格
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当 MIME 类型不是 excel 表格类型时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		if (!Strings.CS.equalsAny(mimeType, PoiConstants.XLS_MIME_TYPE, PoiConstants.XLSX_MIME_TYPE)) {
			throw new UnsupportedResourceException(message);
		}
	}

	/**
	 * 关闭资源并释放工作簿对象
	 * <p>
	 * 先关闭 Workbook 工作簿对象并将引用置为 null，然后调用父类关闭方法。
	 * </p>
	 *
	 * @throws IOException 当关闭工作簿失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (Objects.nonNull(workbook)) {
			workbook.close();
		}
		this.workbook = null;

		super.close();
	}
}
