package io.github.pangju666.commons.poi.io.resource;

import com.deepoove.poi.XWPFTemplate;
import com.deepoove.poi.config.Configure;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.poi.lang.PoiConstants;
import org.apache.commons.lang3.Validate;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * Microsoft Word 2007+ 文档资源类
 * <p>
 * 该类用于处理 .docx 格式的 Word 文档，基于 Apache POI 的 XWPFDocument 和 poi-tl 的 XWPFTemplate 实现。
 * 支持从文件路径、File 对象、字节数组、输入流或 IOResource 创建文档资源。
 * 支持模板编译功能，可用于文档模板填充。
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class DocxResource extends IOResource {
	/**
	 * XWPFDocument 文档对象
	 *
	 * @since 2.1.0
	 */
	protected volatile XWPFDocument document;
	/**
	 * XWPFTemplate 模板对象
	 *
	 * @since 2.1.0
	 */
	protected volatile XWPFTemplate template;

	/**
	 * 使用 IOResource 创建 DocxResource
	 * <p>如果传入的资源不是 DocxResource 实例，则验证其 MIME 类型。</p>
	 *
	 * @param resource IOResource 资源对象
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 docx 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocxResource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof DocxResource)) {
			validateType("resource 不是 docx 文档资源");
		}
	}

	/**
	 * 使用文件路径创建 DocxResource
	 *
	 * @param filePath docx 文档文件路径
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 docx 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocxResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是 docx 文档文件路径");
	}

	/**
	 * 使用 File 对象创建 DocxResource
	 *
	 * @param file docx 文档文件对象
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 docx 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocxResource(File file) throws IOException {
		super(file);

		validateType("file 不是 docx 文档文件");
	}

	/**
	 * 使用字节数组创建 DocxResource
	 *
	 * @param bytes docx 文档字节数组
	 * @throws IOException                  当读取字节数组失败时抛出
	 * @throws UnsupportedResourceException 当字节数组不是 docx 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocxResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是 docx 文档数据");
	}

	/**
	 * 使用输入流创建 DocxResource
	 *
	 * @param inputStream docx 文档输入流
	 * @throws IOException                  当读取输入流失败时抛出
	 * @throws UnsupportedResourceException 当输入流不是 docx 文档类型时抛出
	 * @since 2.1.0
	 */
	public DocxResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 docx 文档输入流");
	}

	/**
	 * 获取 XWPFDocument 文档对象
	 * <p>
	 * 该方法采用懒加载模式，首次调用时创建文档对象，后续调用返回缓存的实例。
	 * </p>
	 *
	 * @return XWPFDocument 文档对象
	 * @throws IOException 当读取文档失败时抛出
	 * @since 2.1.0
	 */
	public synchronized XWPFDocument getDocument() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(document)) {
				return document;
			}

			document = new XWPFDocument(newBufferedInputStream(), true);
			return document;
		}
	}

	/**
	 * 编译文档模板
	 * <p>
	 * 该方法采用懒加载模式，首次调用时编译模板，后续调用返回缓存的模板实例。
	 * 如果已存在文档对象，则直接使用文档对象编译；否则从输入流编译。
	 * </p>
	 *
	 * @return XWPFTemplate 模板对象
	 * @throws IOException 当编译模板失败时抛出
	 * @since 2.1.0
	 */
	public synchronized XWPFTemplate compileTemplate() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(template)) {
				return template;
			}

			if (Objects.nonNull(document)) {
				template = XWPFTemplate.compile(document);
			} else {
				try (InputStream inputStream = newBufferedInputStream()) {
					template = XWPFTemplate.compile(inputStream);
				}
			}
			return template;
		}
	}

	/**
	 * 使用指定配置编译文档模板
	 * <p>
	 * 该方法采用懒加载模式，首次调用时编译模板，后续调用返回缓存的模板实例。
	 * 如果已存在文档对象，则直接使用文档对象编译；否则从输入流编译。
	 * </p>
	 *
	 * @param configure 模板配置对象，不可为 null
	 * @return XWPFTemplate 模板对象
	 * @throws NullPointerException 当 configure 为 null 时抛出
	 * @throws IOException          当编译模板失败时抛出
	 * @since 2.1.0
	 */
	public synchronized XWPFTemplate compileTemplate(Configure configure) throws IOException {
		checkClosed();

		Validate.notNull(configure, "configure 不可为 null");

		synchronized (this) {
			if (Objects.nonNull(template)) {
				return template;
			}

			if (Objects.nonNull(document)) {
				template = XWPFTemplate.compile(document, configure);
			} else {
				try (InputStream inputStream = newBufferedInputStream()) {
					template = XWPFTemplate.compile(inputStream, configure);
				}
			}
			return template;
		}
	}

	/**
	 * 验证资源类型是否为 docx 文档
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当 MIME 类型不是 docx 文档类型时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		if (!PoiConstants.DOCX_MIME_TYPE.equals(mimeType)) {
			throw new UnsupportedResourceException(message);
		}
	}

	/**
	 * 关闭资源并释放文档和模板对象
	 * <p>
	 * 先关闭 XWPFDocument 文档对象和 XWPFTemplate 模板对象，并将引用置为 null，然后调用父类关闭方法。
	 * </p>
	 *
	 * @throws IOException 当关闭文档或模板失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (Objects.nonNull(document)) {
			document.close();
		}
		this.document = null;

		if (Objects.nonNull(template)) {
			template.close();
		}
		this.template = null;

		super.close();
	}
}
