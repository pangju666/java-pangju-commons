package io.github.pangju666.commons.poi.io.resource;

import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.poi.lang.PoiConstants;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Microsoft Word 97-2003 文档资源类
 * <p>
 * 该类用于处理 .doc 格式的 Word 文档，基于 Apache POI 的 HWPFDocument 实现。
 * 支持从文件路径、File 对象、字节数组、输入流或 IOResource 创建文档资源。
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class DocResource extends IOResource {
	/**
	 * HWPFDocument 文档对象
	 *
	 * @since 2.1.0
	 */
	protected volatile HWPFDocument document;

	/**
	 * 使用 IOResource 创建 DocResource
	 * <p>如果传入的资源不是 DocResource 实例，则验证其 MIME 类型。</p>
	 *
	 * @param resource IOResource 资源对象
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 doc 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocResource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof DocResource)) {
			validateType("resource 不是 doc 文档资源");
		}
	}

	/**
	 * 使用文件路径创建 DocResource
	 *
	 * @param filePath doc 文档文件路径
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 doc 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是 doc 文档文件路径");
	}

	/**
	 * 使用 File 对象创建 DocResource
	 *
	 * @param file doc 文档文件对象
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 doc 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocResource(File file) throws IOException {
		super(file);

		validateType("file 不是 doc 文档文件");
	}

	/**
	 * 使用字节数组创建 DocResource
	 *
	 * @param bytes doc 文档字节数组
	 * @throws IOException                  当读取字节数组失败时抛出
	 * @throws UnsupportedResourceException 当字节数组不是 doc 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是 doc 文档数据");
	}

	/**
	 * 使用输入流创建 DocResource
	 *
	 * @param inputStream doc 文档输入流
	 * @throws IOException                  当读取输入流失败时抛出
	 * @throws UnsupportedResourceException 当输入流不是 doc 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 doc 文档输入流");
	}

	/**
	 * 获取 HWPFDocument 文档对象
	 * <p>
	 * 该方法采用懒加载模式，首次调用时创建文档对象，后续调用返回缓存的实例。
	 * 如果资源来源于文件，则从文件系统加载；否则从输入流加载。
	 * </p>
	 *
	 * @return HWPFDocument 文档对象
	 * @throws IOException 当读取文档失败时抛出
	 * @since 2.1.0
	 */
	public synchronized HWPFDocument getDocument() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(document)) {
				return document;
			}

			if (Objects.nonNull(file)) {
				try (POIFSFileSystem fs = new POIFSFileSystem(file)) {
					document = new HWPFDocument(fs);
				}
			} else {
				try (InputStream inputStream = newBufferedInputStream();
				     POIFSFileSystem fs = new POIFSFileSystem(inputStream)) {
					document = new HWPFDocument(fs);
				}
			}
			return document;
		}
	}

	/**
	 * 验证资源类型是否为 doc 文档
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当 MIME 类型不是 doc 文档类型时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		if (!PoiConstants.DOC_MIME_TYPE.equals(mimeType)) {
			throw new UnsupportedResourceException(message);
		}
	}

	/**
	 * 关闭资源并释放文档对象
	 * <p>
	 * 先关闭 HWPFDocument 文档对象并将引用置为 null，然后调用父类关闭方法。
	 * </p>
	 *
	 * @throws IOException 当关闭文档失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (Objects.nonNull(document)) {
			document.close();
		}
		this.document = null;

		super.close();
	}
}
