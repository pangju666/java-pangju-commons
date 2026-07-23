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
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Objects;

/**
 * TAR 压缩文件资源类
 * <p>
 * 用于封装和管理 TAR 格式的压缩文件资源，支持从多种来源加载 TAR 文件，
 * 包括文件路径、File 对象、字节数组和输入流。
 * 支持自定义字符编码以正确处理不同编码的文件名。
 * </p>
 * <p>
 * <b>支持的特性：</b>
 * </p>
 * <ul>
 *   <li>从文件、字节数组、输入流加载 TAR 资源</li>
 *   <li>支持自定义字符编码</li>
 *   <li>自动验证资源类型</li>
 *   <li>提供 TarArchiveInputStream 和 TarFile 访问接口</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * </p>
 * <pre>{@code
 * // 1) 从文件路径加载并解压
 * TarResource resource = new TarResource("/path/to/archive.tar");
 * TarUtils.uncompress(resource, new File("/path/to/output"));
 *
 * // 2) 使用自定义编码加载
 * TarResource resource = new TarResource("/path/to/archive.tar", StandardCharsets.GBK);
 * TarUtils.uncompress(resource, new File("/path/to/output"));
 *
 * // 3) 打开 TarFile 进行细粒度操作
 * try (TarFile tarFile = resource.openTarFile()) {
 *     // 遍历文件条目
 *     for (TarArchiveEntry entry : tarFile.getEntries()) {
 *         System.out.println(entry.getName());
 *     }
 * }
 *
 * // 4) 打开 TarArchiveInputStream 进行流式处理
 * try (TarArchiveInputStream tais = resource.openTarArchiveInputStream()) {
 *     TarArchiveEntry entry;
 *     while ((entry = tais.getNextTarEntry()) != null) {
 *         System.out.println(entry.getName());
 *     }
 * }
 * }</pre>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class TarResource extends IOResource {
	/**
	 * TAR 文件编码
	 * <p>用于读取 TAR 文件中的文件名，默认为 null（使用系统默认编码）</p>
	 *
	 * @since 1.1.0
	 */
	protected final Charset encoding;

	/**
	 * 从 IOResource 构造 TAR 资源
	 * <p>
	 * 如果传入的资源已经是 TarResource，则直接复用其编码设置。
	 * 否则，仅验证资源类型，编码设为 null。
	 * </p>
	 *
	 * @param resource IO 资源对象
	 * @throws IOException                  读取资源失败时抛出
	 * @throws UnsupportedResourceException 资源不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(IOResource resource) throws IOException {
		super(resource);

		if (resource instanceof TarResource) {
			this.encoding = ((TarResource) resource).encoding;
		} else {
			validateType("resource 不是 tar 资源");

			this.encoding = null;
		}
	}

	/**
	 * 从文件路径构造 TAR 资源，使用默认编码
	 *
	 * @param filePath TAR 文件路径
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(String filePath) throws IOException {
		this(filePath, null);
	}

	/**
	 * 从 File 对象构造 TAR 资源，使用默认编码
	 *
	 * @param file TAR 文件对象
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(File file) throws IOException {
		this(file, null);
	}

	/**
	 * 从字节数组构造 TAR 资源，使用默认编码
	 *
	 * @param bytes TAR 格式的字节数组
	 * @throws IOException                  读取数据失败时抛出
	 * @throws UnsupportedResourceException 数据不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(byte[] bytes) throws IOException {
		this(bytes, null);
	}

	/**
	 * 从输入流构造 TAR 资源，使用默认编码
	 *
	 * @param inputStream TAR 格式的输入流
	 * @throws IOException                  读取流失败时抛出
	 * @throws UnsupportedResourceException 流数据不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(InputStream inputStream) throws IOException {
		this(inputStream, null);
	}

	/**
	 * 从 IOResource 构造 TAR 资源，指定编码
	 * <p>
	 * 传入的资源必须是 TarResource 类型，否则抛出异常。
	 * </p>
	 *
	 * @param resource IO 资源对象，必须是 TarResource 类型
	 * @param encoding 字符编码，可为 null
	 * @throws IOException                  读取资源失败时抛出
	 * @throws UnsupportedResourceException 资源不是 TAR 格式或不是 TarResource 类型时抛出
	 * @since 1.1.0
	 */
	public TarResource(IOResource resource, Charset encoding) throws IOException {
		super(resource);

		if (!(resource instanceof TarResource)) {
			validateType("resource 不是 tar 资源");
		}

		this.encoding = encoding;
	}

	/**
	 * 从文件路径构造 TAR 资源，指定编码
	 *
	 * @param filePath TAR 文件路径
	 * @param encoding 字符编码，可为 null
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(String filePath, Charset encoding) throws IOException {
		super(filePath);

		validateType("filePath 不是 tar 文件路径");

		this.encoding = encoding;
	}

	/**
	 * 从 File 对象构造 TAR 资源，指定编码
	 *
	 * @param file     TAR 文件对象
	 * @param encoding 字符编码，可为 null
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(File file, Charset encoding) throws IOException {
		super(file);

		validateType("file 不是 tar 文件");

		this.encoding = encoding;
	}

	/**
	 * 从字节数组构造 TAR 资源，指定编码
	 *
	 * @param bytes    TAR 格式的字节数组
	 * @param encoding 字符编码，可为 null
	 * @throws IOException                  读取数据失败时抛出
	 * @throws UnsupportedResourceException 数据不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(byte[] bytes, Charset encoding) throws IOException {
		super(bytes);

		validateType("bytes 不是 tar 数据");

		this.encoding = encoding;
	}

	/**
	 * 从输入流构造 TAR 资源，指定编码
	 *
	 * @param inputStream TAR 格式的输入流
	 * @param encoding    字符编码，可为 null
	 * @throws IOException                  读取流失败时抛出
	 * @throws UnsupportedResourceException 流数据不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	public TarResource(InputStream inputStream, Charset encoding) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 tar 数据输入流");

		this.encoding = encoding;
	}

	/**
	 * 打开 TAR 归档输入流
	 * <p>
	 * 如果设置了编码，则使用指定编码创建输入流；否则使用默认编码。
	 * 调用此方法前需要确保资源未被关闭。
	 * </p>
	 *
	 * @return TAR 归档输入流
	 * @throws IOException 资源已关闭或打开流失败时抛出
	 * @since 1.1.0
	 */
	public TarArchiveInputStream openTarArchiveInputStream() throws IOException {
		checkClosed();

		if (Objects.nonNull(encoding)) {
			return new TarArchiveInputStream(newBufferedInputStream(), encoding.name());
		} else {
			return new TarArchiveInputStream(newBufferedInputStream());
		}
	}

	/**
	 * 打开 TAR 文件对象
	 * <p>
	 * 如果资源来源于文件，则基于文件创建 TarFile；
	 * 如果资源来源于字节数组或输入流，则基于字节数组创建 TarFile。
	 * 如果设置了编码，则使用指定编码；否则使用默认编码。
	 * 调用此方法前需要确保资源未被关闭。
	 * </p>
	 *
	 * @return TAR 文件对象
	 * @throws IOException 资源已关闭或打开文件失败时抛出
	 * @since 1.1.0
	 */
	public TarFile openTarFile() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {

			if (Objects.nonNull(encoding)) {
				return new TarFile(file, encoding.name());
			} else {
				return new TarFile(file);
			}
		} else {
			if (Objects.nonNull(encoding)) {
				return new TarFile(file, encoding.name());
			} else {
				return new TarFile(byteArrayOutputStream.toByteArray());
			}
		}
	}

	/**
	 * 验证资源类型是否为 TAR 格式
	 * <p>
	 * 通过检查 MIME 类型是否为 TAR 格式来验证资源类型。
	 * 如果验证失败，抛出 UnsupportedResourceException 异常。
	 * </p>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 资源不是 TAR 格式时抛出
	 * @since 1.1.0
	 */
	protected void validateType(String message) {
		if (!CompressConstants.TAR_MIME_TYPE.equals(mimeType)) {
			throw new UnsupportedResourceException(message);
		}
	}
}
