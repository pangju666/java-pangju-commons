/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.commons.compress.io.resource;

import io.github.pangju666.commons.compress.lang.CompressConstants;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ZIP 压缩文件资源类
 * <p>
 * 用于封装和管理 ZIP 格式的压缩文件资源，支持从多种来源加载 ZIP 文件，
 * 包括文件路径、File 对象、字节数组和输入流。
 * 支持自定义编码和 Unicode 扩展字段设置。
 * </p>
 * <p>
 * <b>支持的特性：</b>
 * </p>
 * <ul>
 *   <li>从文件、字节数组、输入流加载 ZIP 资源</li>
 *   <li>支持自定义字符编码</li>
 *   <li>支持 Unicode 扩展字段</li>
 *   <li>自动验证资源类型</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * </p>
 * <pre>{@code
 * // 1) 从文件路径加载并解压
 * ZipResource resource = new ZipResource("/path/to/archive.zip");
 * ZipUtils.uncompress(resource, new File("/path/to/output"));
 *
 * // 2) 使用自定义编码加载
 * ZipResource resource = new ZipResource("/path/to/archive.zip", StandardCharsets.GBK);
 * ZipUtils.uncompress(resource, new File("/path/to/output"));
 *
 * // 3) 打开 ZipFile 进行细粒度操作
 * try (ZipFile zipFile = resource.openZipFile()) {
 *     // 遍历文件条目
 *     for (ZipArchiveEntry entry : zipFile.getEntries()) {
 *         System.out.println(entry.getName());
 *     }
 * }
 * }</pre>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class ZipResource extends IOResource {
	/**
	 * ZIP 文件编码。
	 * <p>用于读取 ZIP 文件中的文件名和注释，默认为 UTF-8。</p>
	 *
	 * @since 2.1.0
	 */
	protected final Charset encoding;

	/**
	 * 是否使用 Unicode 扩展字段。
	 * <p>设置为 true 时，将使用 Unicode 扩展字段来正确处理非 ASCII 文件名。</p>
	 *
	 * @since 2.1.0
	 */
	protected final boolean useUnicodeExtraFields;

	/**
	 * 分片文件最大数量。
	 * <p>对于分片 ZIP 文件，表示分片的总数；对于普通 ZIP 文件，值为 1。</p>
	 *
	 * @since 2.1.0
	 */
	protected final int maxNumberOfDisks;

	/**
	 * 从 IOResource 构造 ZIP 资源。
	 * <p>如果传入的资源已经是 ZipResource，则直接复用其编码和 Unicode 扩展字段设置。
	 * 否则，验证资源类型，编码设为 UTF-8，Unicode 扩展字段设为 true。</p>
	 *
	 * @param resource IO 资源对象
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(IOResource resource) throws IOException {
		super(resource);

		if (resource instanceof ZipResource zipResource) {
			this.encoding = zipResource.encoding;
			this.useUnicodeExtraFields = zipResource.useUnicodeExtraFields;
			this.maxNumberOfDisks = zipResource.maxNumberOfDisks;
		} else {
			validateType("resource 不是 zip 资源");

			this.encoding = StandardCharsets.UTF_8;
			this.useUnicodeExtraFields = true;
			if (Objects.nonNull(file)) {
				this.maxNumberOfDisks = getMaxNumberOfDisks(file);
			} else {
				this.maxNumberOfDisks = 1;
			}
		}
	}

	/**
	 * 从文件路径构造 ZIP 资源。
	 * <p>仅验证文件类型，编码设为 UTF-8，Unicode 扩展字段设为 true。</p>
	 *
	 * @param filePath ZIP 文件路径
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(String filePath) throws IOException {
		this(filePath, StandardCharsets.UTF_8, true);
	}

	/**
	 * 从 File 对象构造 ZIP 资源。
	 * <p>仅验证文件类型，编码设为 UTF-8，Unicode 扩展字段设为 true。</p>
	 *
	 * @param file ZIP 文件对象
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(File file) throws IOException {
		this(file, StandardCharsets.UTF_8, true);
	}

	/**
	 * 从字节数组构造 ZIP 资源。
	 * <p>仅验证数据类型，编码设为 UTF-8，Unicode 扩展字段设为 true。</p>
	 *
	 * @param bytes ZIP 数据字节数组
	 * @throws IOException                  当读取数据失败时抛出
	 * @throws UnsupportedResourceException 当数据不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(byte[] bytes) throws IOException {
		this(bytes, StandardCharsets.UTF_8, true);
	}

	/**
	 * 从输入流构造 ZIP 资源。
	 * <p>仅验证流类型，编码设为 UTF-8，Unicode 扩展字段设为 true。</p>
	 *
	 * @param inputStream ZIP 数据输入流
	 * @throws IOException                  当读取流失败时抛出
	 * @throws UnsupportedResourceException 当流数据不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(InputStream inputStream) throws IOException {
		this(inputStream, StandardCharsets.UTF_8, true);
	}

	/**
	 * 从 IOResource 构造 ZIP 资源并设置 Unicode 扩展字段。
	 * <p>如果传入的资源已经是 ZipResource，则直接复用其编码。
	 * 否则，仅验证资源类型，编码设为 UTF-8。</p>
	 *
	 * @param resource              IO 资源对象
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(IOResource resource, boolean useUnicodeExtraFields) throws IOException {
		this(resource, StandardCharsets.UTF_8, useUnicodeExtraFields);
	}

	/**
	 * 从文件路径构造 ZIP 资源并设置 Unicode 扩展字段。
	 * <p>仅验证文件类型，编码设为 UTF-8。</p>
	 *
	 * @param filePath              ZIP 文件路径
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(String filePath, boolean useUnicodeExtraFields) throws IOException {
		this(filePath, StandardCharsets.UTF_8, useUnicodeExtraFields);
	}

	/**
	 * 从 File 对象构造 ZIP 资源并设置 Unicode 扩展字段。
	 * <p>仅验证文件类型，编码设为 UTF-8。</p>
	 *
	 * @param file                  ZIP 文件对象
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(File file, boolean useUnicodeExtraFields) throws IOException {
		this(file, StandardCharsets.UTF_8, useUnicodeExtraFields);
	}

	/**
	 * 从字节数组构造 ZIP 资源并设置 Unicode 扩展字段。
	 * <p>仅验证数据类型，编码设为 UTF-8。</p>
	 *
	 * @param bytes                 ZIP 数据字节数组
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取数据失败时抛出
	 * @throws UnsupportedResourceException 当数据不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(byte[] bytes, boolean useUnicodeExtraFields) throws IOException {
		this(bytes, StandardCharsets.UTF_8, useUnicodeExtraFields);
	}

	/**
	 * 从输入流构造 ZIP 资源并设置 Unicode 扩展字段。
	 * <p>仅验证流类型，编码设为 UTF-8。</p>
	 *
	 * @param inputStream           ZIP 数据输入流
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取流失败时抛出
	 * @throws UnsupportedResourceException 当流数据不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	public ZipResource(InputStream inputStream, boolean useUnicodeExtraFields) throws IOException {
		this(inputStream, StandardCharsets.UTF_8, useUnicodeExtraFields);
	}

	/**
	 * 从 IOResource 构造 ZIP 资源并设置编码。
	 * <p>如果传入的资源已经是 ZipResource，则直接复用其 Unicode 扩展字段设置。
	 * 否则，仅验证资源类型，Unicode 扩展字段设为 true。</p>
	 *
	 * @param resource IO 资源对象
	 * @param encoding ZIP 文件编码，不可为 null
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(IOResource resource, Charset encoding) throws IOException {
		this(resource, encoding, true);
	}

	/**
	 * 从文件路径构造 ZIP 资源并设置编码。
	 * <p>仅验证文件类型，Unicode 扩展字段设为 true。</p>
	 *
	 * @param filePath ZIP 文件路径
	 * @param encoding ZIP 文件编码，不可为 null
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(String filePath, Charset encoding) throws IOException {
		this(filePath, encoding, true);
	}

	/**
	 * 从 File 对象构造 ZIP 资源并设置编码。
	 * <p>仅验证文件类型，Unicode 扩展字段设为 true。</p>
	 *
	 * @param file     ZIP 文件对象
	 * @param encoding ZIP 文件编码，不可为 null
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(File file, Charset encoding) throws IOException {
		this(file, encoding, true);
	}

	/**
	 * 从字节数组构造 ZIP 资源并设置编码。
	 * <p>仅验证数据类型，Unicode 扩展字段设为 true。</p>
	 *
	 * @param bytes    ZIP 数据字节数组
	 * @param encoding ZIP 文件编码，不可为 null
	 * @throws IOException                  当读取数据失败时抛出
	 * @throws UnsupportedResourceException 当数据不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(byte[] bytes, Charset encoding) throws IOException {
		this(bytes, encoding, true);
	}

	/**
	 * 从输入流构造 ZIP 资源并设置编码。
	 * <p>仅验证流类型，Unicode 扩展字段设为 true。</p>
	 *
	 * @param inputStream ZIP 数据输入流
	 * @param encoding    ZIP 文件编码，不可为 null
	 * @throws IOException                  当读取流失败时抛出
	 * @throws UnsupportedResourceException 当流数据不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(InputStream inputStream, Charset encoding) throws IOException {
		this(inputStream, encoding, true);
	}

	/**
	 * 从 IOResource 构造 ZIP 资源并设置编码和 Unicode 扩展字段。
	 * <p>验证资源类型并设置编码和 Unicode 扩展字段。</p>
	 *
	 * @param resource              IO 资源对象
	 * @param encoding              ZIP 文件编码，不可为 null
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(IOResource resource, Charset encoding, boolean useUnicodeExtraFields) throws IOException {
		super(resource);

		if (!(resource instanceof ZipResource)) {
			validateType("resource 不是 zip 资源");
		}

		Validate.notNull(encoding, "encoding 不可为 null");

		this.encoding = encoding;
		this.useUnicodeExtraFields = useUnicodeExtraFields;
		if (Objects.nonNull(file)) {
			this.maxNumberOfDisks = getMaxNumberOfDisks(file);
		} else {
			this.maxNumberOfDisks = 1;
		}
	}

	/**
	 * 从文件路径构造 ZIP 资源并设置编码和 Unicode 扩展字段。
	 * <p>验证文件类型并设置编码和 Unicode 扩展字段。</p>
	 *
	 * @param filePath              ZIP 文件路径
	 * @param encoding              ZIP 文件编码，不可为 null
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(String filePath, Charset encoding, boolean useUnicodeExtraFields) throws IOException {
		super(filePath);

		validateType("filePath 不是 zip 文件路径");

		Validate.notNull(encoding, "encoding 不可为 null");

		this.encoding = encoding;
		this.useUnicodeExtraFields = useUnicodeExtraFields;
		this.maxNumberOfDisks = getMaxNumberOfDisks(file);
	}

	/**
	 * 从 File 对象构造 ZIP 资源并设置编码和 Unicode 扩展字段。
	 * <p>验证文件类型并设置编码和 Unicode 扩展字段。</p>
	 *
	 * @param file                  ZIP 文件对象
	 * @param encoding              ZIP 文件编码，不可为 null
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(File file, Charset encoding, boolean useUnicodeExtraFields) throws IOException {
		super(file);

		validateType("file 不是 zip 文件");

		Validate.notNull(encoding, "encoding 不可为 null");

		this.encoding = encoding;
		this.useUnicodeExtraFields = useUnicodeExtraFields;
		this.maxNumberOfDisks = getMaxNumberOfDisks(file);
	}

	/**
	 * 从字节数组构造 ZIP 资源并设置编码和 Unicode 扩展字段。
	 * <p>验证数据类型并设置编码和 Unicode 扩展字段。</p>
	 *
	 * @param bytes                 ZIP 数据字节数组
	 * @param encoding              ZIP 文件编码，不可为 null
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取数据失败时抛出
	 * @throws UnsupportedResourceException 当数据不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(byte[] bytes, Charset encoding, boolean useUnicodeExtraFields) throws IOException {
		super(bytes);

		validateType("bytes 不是 zip 数据");

		Validate.notNull(encoding, "encoding 不可为 null");

		this.encoding = encoding;
		this.useUnicodeExtraFields = useUnicodeExtraFields;
		this.maxNumberOfDisks = 1;
	}

	/**
	 * 从输入流构造 ZIP 资源并设置编码和 Unicode 扩展字段。
	 * <p>验证流类型并设置编码和 Unicode 扩展字段。</p>
	 *
	 * @param inputStream           ZIP 数据输入流
	 * @param encoding              ZIP 文件编码，不可为 null
	 * @param useUnicodeExtraFields 是否使用 Unicode 扩展字段
	 * @throws IOException                  当读取流失败时抛出
	 * @throws UnsupportedResourceException 当流数据不是 ZIP 格式时抛出
	 * @throws IllegalArgumentException     当 encoding 为 null 时抛出
	 * @since 2.1.0
	 */
	public ZipResource(InputStream inputStream, Charset encoding, boolean useUnicodeExtraFields) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 zip 数据输入流");

		Validate.notNull(encoding, "encoding 不可为 null");

		this.encoding = encoding;
		this.useUnicodeExtraFields = useUnicodeExtraFields;
		this.maxNumberOfDisks = 1;
	}

	/**
	 * 获取分片文件最大数量。
	 * <p>统计指定 ZIP 文件及其所有分片文件（.z01, .z02 等）的总数。
	 * 如果文件没有父目录，返回 1。</p>
	 *
	 * @param file ZIP 文件
	 * @return 分片文件总数（包括主文件）
	 * @see ZipFile.Builder#setMaxNumberOfDisks(long)
	 * @since 2.1.0
	 */
	protected static int getMaxNumberOfDisks(final File file) {
		String originalName = file.getName();
		File parentFile = file.getParentFile();
		if (Objects.isNull(parentFile)) {
			return 1;
		}

		String baseName = FilenameUtils.getBaseName(file.getName());
		Pattern pattern = Pattern.compile(baseName + "\\.z\\d+", Pattern.CASE_INSENSITIVE);

		String[] filenames = parentFile.list((dir, filename) -> {
			if (originalName.equals(filename)) {
				return true;
			}
			Matcher matcher = pattern.matcher(filename);
			return matcher.matches();
		});
		return 1 + ArrayUtils.getLength(filenames);
	}

	/**
	 * 打开 ZIP 归档输入流。
	 * <p>根据资源类型创建相应的 ZipArchiveInputStream 实例，使用配置的编码和 Unicode 扩展字段设置。</p>
	 * <p>如果使用文件构建的话建议使用{@link #openZipFile()}</p>
	 *
	 * @return ZipArchiveInputStream 实例
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public ZipArchiveInputStream openZipArchiveInputStream() throws IOException {
		checkClosed();

		return new ZipArchiveInputStream(newBufferedInputStream(), encoding.name(), useUnicodeExtraFields);
	}

	/**
	 * 打开 ZIP 归档输入流并设置是否允许带数据描述符的存储条目。
	 * <p>根据资源类型创建相应的 ZipArchiveInputStream 实例，使用配置的编码和 Unicode 扩展字段设置。</p>
	 * <p>如果使用文件构建的话建议使用{@link #openZipFile(boolean)}</p>
	 *
	 * @param allowStoredEntriesWithDataDescriptor 是否允许带数据描述符的存储条目
	 * @return ZipArchiveInputStream 实例
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public ZipArchiveInputStream openZipArchiveInputStream(boolean allowStoredEntriesWithDataDescriptor) throws IOException {
		checkClosed();

		return new ZipArchiveInputStream(newBufferedInputStream(), encoding.name(), useUnicodeExtraFields,
			allowStoredEntriesWithDataDescriptor);
	}

	/**
	 * 打开 ZIP 归档输入流并设置是否允许带数据描述符的存储条目和是否尝试跳过开头的 zip 分卷签名。
	 * <p>根据资源类型创建相应的 ZipArchiveInputStream 实例，使用配置的编码和 Unicode 扩展字段设置。</p>
	 *
	 * @param allowStoredEntriesWithDataDescriptor 是否允许带数据描述符的存储条目
	 * @param skipSplitSig                         是否尝试跳过开头的 zip 分卷签名。若要读取分卷归档，需将此参数设为 true。
	 * @return ZipArchiveInputStream 实例
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public ZipArchiveInputStream openZipArchiveInputStream(boolean allowStoredEntriesWithDataDescriptor,
	                                                       boolean skipSplitSig) throws IOException {
		checkClosed();

		return new ZipArchiveInputStream(newBufferedInputStream(), encoding.name(), useUnicodeExtraFields,
			allowStoredEntriesWithDataDescriptor, skipSplitSig);
	}

	/**
	 * 打开 ZIP 文件。
	 * <p>根据资源类型创建相应的 ZipFile 实例。
	 * 如果资源来自文件，使用文件路径创建并设置分片文件数量；否则使用字节数组创建。
	 * 使用配置的编码和 Unicode 扩展字段设置。</p>
	 *
	 * @return ZipFile 实例
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public ZipFile openZipFile() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return ZipFile.builder()
				.setCharset(encoding)
				.setUseUnicodeExtraFields(useUnicodeExtraFields)
				.setMaxNumberOfDisks(maxNumberOfDisks)
				.setFile(file)
				.get();
		} else {
			return ZipFile.builder()
				.setCharset(encoding)
				.setUseUnicodeExtraFields(useUnicodeExtraFields)
				.setByteArray(byteArrayOutputStream.toByteArray())
				.get();
		}
	}

	/**
	 * 打开 ZIP 文件并设置是否忽略本地文件头。
	 * <p>根据资源类型创建相应的 ZipFile 实例。
	 * 如果资源来自文件，使用文件路径创建并设置分片文件数量；否则使用字节数组创建。
	 * 使用配置的编码和 Unicode 扩展字段设置。</p>
	 *
	 * @param ignoreLocalFileHeader 是否忽略本地文件头
	 * @return ZipFile 实例
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public ZipFile openZipFile(boolean ignoreLocalFileHeader) throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return ZipFile.builder()
				.setCharset(encoding)
				.setUseUnicodeExtraFields(useUnicodeExtraFields)
				.setMaxNumberOfDisks(maxNumberOfDisks)
				.setIgnoreLocalFileHeader(ignoreLocalFileHeader)
				.setFile(file)
				.get();
		} else {
			return ZipFile.builder()
				.setCharset(encoding)
				.setUseUnicodeExtraFields(useUnicodeExtraFields)
				.setIgnoreLocalFileHeader(ignoreLocalFileHeader)
				.setByteArray(byteArrayOutputStream.toByteArray())
				.get();
		}
	}

	/**
	 * 打开加密的 ZIP 文件。
	 * <p>使用 Zip4j 库打开加密的 ZIP 文件，根据资源大小自动设置缓冲区大小。
	 *
	 * @param password ZIP 文件密码，必须非空
	 * @return Zip4j ZipFile 实例
	 * @throws NullPointerException     当资源已关闭时抛出
	 * @throws IllegalArgumentException 当 password 为空时抛出
	 * @throws IOException              当打开 ZIP 文件失败时抛出
	 * @since 2.1.0
	 */
	public net.lingala.zip4j.ZipFile openZipFile(String password) throws IOException {
		checkClosed();

		Validate.notBlank(password, "password 不可为空");

		net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(getFile(), password.toCharArray());
		zipFile.setBufferSize(IOUtils.getBufferSize(size.toBytes()));
		return zipFile;
	}

	/**
	 * 获取分片文件最大数量。
	 * <p>对于分片 ZIP 文件，返回分片的总数；对于普通 ZIP 文件，返回 1。</p>
	 *
	 * @return 分片文件最大数量
	 * @since 2.1.0
	 */
	public int getMaxNumberOfDisks() {
		checkClosed();

		return maxNumberOfDisks;
	}

	/**
	 * 验证资源类型。
	 * <p>检查资源是否为 ZIP 格式，如果不是则抛出异常。</p>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当资源不是 ZIP 格式时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		if (!CompressConstants.ZIP_MIME_TYPE.equals(mimeType)) {
			throw new UnsupportedResourceException(message);
		}
	}
}
