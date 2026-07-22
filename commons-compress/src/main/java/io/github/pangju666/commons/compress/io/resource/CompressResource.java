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
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * 压缩文件资源类
 * <p>
 * 该类继承自 {@link IOResource}，专门用于处理各种格式的压缩文件资源。
 * 支持 GZIP、XZ、Zstandard、TAR、ZIP、7Z 等常见压缩格式。
 * 通过 MIME 类型自动识别压缩格式，并提供相应的压缩流和归档流打开方法。
 * </p>
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 从文件创建压缩资源
 * CompressResource resource = new CompressResource(new File("archive.gz"));
 *
 * // 判断压缩格式
 * if (resource.isGzip()) {
 *     try (CompressorInputStream in = resource.openCompressorInputStream()) {
 *         // 读取压缩数据
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class CompressResource extends IOResource {
	/**
	 * 从 IOResource 创建压缩资源
	 * <p>
	 * 如果传入的资源已经是 CompressResource，则直接使用；否则会验证其 MIME 类型是否为支持的压缩格式。
	 * </p>
	 *
	 * @param resource IOResource 资源对象
	 * @throws IOException                  当资源读取发生 I/O 异常时抛出
	 * @throws UnsupportedResourceException 当资源不是压缩文件格式时抛出
	 * @since 2.1.0
	 */
	public CompressResource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof CompressResource)) {
			validateType("resource 不是压缩文件资源");
		}
	}

	/**
	 * 从文件路径创建压缩资源
	 * <p>
	 * 会自动验证指定路径的文件是否为支持的压缩格式。
	 * </p>
	 *
	 * @param filePath 压缩文件路径
	 * @throws IOException                  当文件读取发生 I/O 异常时抛出
	 * @throws UnsupportedResourceException 当文件不是压缩文件格式时抛出
	 * @since 2.1.0
	 */
	public CompressResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是压缩文件路径");
	}

	/**
	 * 从 File 对象创建压缩资源
	 * <p>
	 * 会自动验证指定文件是否为支持的压缩格式。
	 * </p>
	 *
	 * @param file 压缩文件对象
	 * @throws IOException                  当文件读取发生 I/O 异常时抛出
	 * @throws UnsupportedResourceException 当文件不是压缩文件格式时抛出
	 * @since 2.1.0
	 */
	public CompressResource(File file) throws IOException {
		super(file);

		validateType("file 不是压缩文件");
	}

	/**
	 * 从字节数组创建压缩资源
	 * <p>
	 * 会自动验证字节数组是否为支持的压缩格式数据。
	 * </p>
	 *
	 * @param bytes 压缩文件数据的字节数组
	 * @throws IOException                  当字节数组读取发生 I/O 异常时抛出
	 * @throws UnsupportedResourceException 当字节数组不是压缩文件格式时抛出
	 * @since 2.1.0
	 */
	public CompressResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是压缩文件数据");
	}

	/**
	 * 从输入流创建压缩资源
	 * <p>
	 * 会自动验证输入流中的数据是否为支持的压缩格式。
	 * </p>
	 *
	 * @param inputStream 压缩文件数据的输入流
	 * @throws IOException                  当输入流读取发生 I/O 异常时抛出
	 * @throws UnsupportedResourceException 当输入流数据不是压缩文件格式时抛出
	 * @since 2.1.0
	 */
	public CompressResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是压缩文件数据输入流");
	}

	/**
	 * 打开压缩输入流
	 * <p>
	 * 根据压缩格式自动选择对应的压缩输入流实现：
	 * <ul>
	 *     <li>GZIP 格式：返回 {@link GzipCompressorInputStream}</li>
	 *     <li>XZ 格式：返回 {@link XZCompressorInputStream}</li>
	 *     <li>Zstandard 格式：返回 {@link ZstdCompressorInputStream}</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 注意：调用者负责关闭返回的输入流。建议使用 try-with-resources 语句确保资源正确释放。
	 * </p>
	 *
	 * @return 压缩输入流
	 * @throws IOException                  当资源已关闭或打开输入流失败时抛出
	 * @throws UnsupportedResourceException 当文件格式不支持读取为压缩输入流时抛出
	 * @since 2.1.0
	 */
	public CompressorInputStream openCompressorInputStream() throws IOException {
		checkClosed();

		if (isGzip()) {
			return new GzipCompressorInputStream(newBufferedInputStream());
		} else if (isXz()) {
			return new XZCompressorInputStream(newBufferedInputStream());
		} else if (isZstd()) {
			return new ZstdCompressorInputStream(newBufferedInputStream());
		} else {
			throw new UnsupportedResourceException("不支持读取为压缩输入流");
		}
	}

	/**
	 * 打开归档输入流
	 * <p>
	 * 根据归档格式自动选择对应的归档输入流实现：
	 * <ul>
	 *     <li>TAR 格式：返回 {@link TarArchiveInputStream}</li>
	 *     <li>ZIP 格式：返回 {@link ZipArchiveInputStream}</li>
	 * </ul>
	 * </p>
	 * <p>
	 * 注意：调用者负责关闭返回的输入流。建议使用 try-with-resources 语句确保资源正确释放。
	 * </p>
	 *
	 * @return 归档输入流
	 * @throws IOException                  当资源已关闭或打开输入流失败时抛出
	 * @throws UnsupportedResourceException 当文件格式不支持读取为归档输入流时抛出
	 * @since 2.1.0
	 */
	public ArchiveInputStream<?> openArchiveInputStream() throws IOException {
		checkClosed();

		if (isTar()) {
			return new TarArchiveInputStream(newBufferedInputStream());
		} else if (isZip()) {
			return new ZipArchiveInputStream(newBufferedInputStream());
		} else {
			throw new UnsupportedResourceException("不支持读取为归档输入流");
		}
	}

	/**
	 * 判断是否为 GZIP 格式
	 *
	 * @return 当且仅当 MIME 类型为 {@code application/gzip} 时返回 {@code true}
	 * @since 2.1.0
	 */
	public boolean isGzip() {
		return mimeType.equals(CompressConstants.GZIP_TYPE);
	}

	/**
	 * 判断是否为 ZIP 格式
	 *
	 * @return 当且仅当 MIME 类型为 {@code application/zip} 时返回 {@code true}
	 * @since 2.1.0
	 */
	public boolean isZip() {
		return mimeType.equals(CompressConstants.ZIP_MIME_TYPE);
	}

	/**
	 * 判断是否为 TAR 格式
	 *
	 * @return 当且仅当 MIME 类型为 {@code application/x-tar} 时返回 {@code true}
	 * @since 2.1.0
	 */
	public boolean isTar() {
		return mimeType.equals(CompressConstants.TAR_MIME_TYPE);
	}

	/**
	 * 判断是否为 XZ 格式
	 *
	 * @return 当且仅当 MIME 类型为 {@code application/x-xz} 时返回 {@code true}
	 * @since 2.1.0
	 */
	public boolean isXz() {
		return mimeType.equals(CompressConstants.XZ_MIME_TYPE);
	}

	/**
	 * 判断是否为 7Z 格式
	 *
	 * @return 当且仅当 MIME 类型为 {@code application/x-7z-compressed} 时返回 {@code true}
	 * @since 2.1.0
	 */
	public boolean is7z() {
		return mimeType.equals(CompressConstants.SEVEN_Z_MIME_TYPE);
	}

	/**
	 * 判断是否为 Zstandard 格式
	 *
	 * @return 当且仅当 MIME 类型为 {@code application/zstd} 时返回 {@code true}
	 * @since 2.1.0
	 */
	public boolean isZstd() {
		return mimeType.equals(CompressConstants.ZSTD_MIME_TYPE);
	}

	/**
	 * 验证 MIME 类型是否为支持的压缩格式
	 * <p>
	 * 支持的压缩格式包括：GZIP、TAR、7Z、ZIP、XZ、Zstandard。
	 * 如果 MIME 类型不在支持列表中，则抛出 {@link UnsupportedResourceException}。
	 * </p>
	 *
	 * @param message 异常消息，当验证失败时使用
	 * @throws UnsupportedResourceException 当 MIME 类型不是支持的压缩格式时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		if (!StringUtils.equalsAny(mimeType, CompressConstants.GZIP_TYPE,
			CompressConstants.TAR_MIME_TYPE, CompressConstants.SEVEN_Z_MIME_TYPE, CompressConstants.ZIP_MIME_TYPE,
			CompressConstants.XZ_MIME_TYPE, CompressConstants.ZSTD_MIME_TYPE)) {
			throw new UnsupportedResourceException(message);
		}
	}
}
