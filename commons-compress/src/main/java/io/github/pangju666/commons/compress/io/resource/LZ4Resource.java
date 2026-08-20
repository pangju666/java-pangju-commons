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
import io.github.pangju666.commons.compress.utils.LZ4Utils;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * LZ4 格式资源类。
 * <p>封装 LZ4 格式的压缩资源，支持多种输入源（文件、字节数组、输入流等），并通过 MIME 类型检测验证格式。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>多输入源</strong>：支持文件路径、File 对象、字节数组、输入流、IOResource。</li>
 *   <li><strong>格式校验</strong>：通过 MIME 类型检测验证输入源是否为 LZ4 格式。</li>
 *   <li><strong>资源管理</strong>：继承自 {@link IOResource}，支持资源关闭检查。</li>
 *   <li><strong>缓冲流</strong>：自动使用缓冲输入流提高性能。</li>
 *   <li><strong>双格式支持</strong>：支持 Framed LZ4 和 Block LZ4 两种压缩格式的解压。</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1) 从文件创建 LZ4 资源（Framed 格式）
 * try (LZ4Resource resource = new LZ4Resource(new File("data.lz4"));
 *      FramedLZ4CompressorInputStream lis = resource.openFramedLZ4CompressorInputStream()) {
 *     // 读取解压后的数据
 * }
 *
 * // 2) 从字节数组创建 LZ4 资源（Block 格式）
 * byte[] lz4Bytes = ...;
 * try (LZ4Resource resource = new LZ4Resource(lz4Bytes);
 *      BlockLZ4CompressorInputStream lis = resource.openBlockLZ4CompressorInputStream()) {
 *     // 读取解压后的数据
 * }
 *
 * // 3) 从输入流创建 LZ4 资源
 * try (InputStream in = new FileInputStream("data.lz4");
 *      LZ4Resource resource = new LZ4Resource(in);
 *      FramedLZ4CompressorInputStream lis = resource.openFramedLZ4CompressorInputStream()) {
 *     // 读取解压后的数据
 * }
 * }</pre>
 *
 * @author pangju666
 * @apiNote LZ4 提供两种压缩格式：Framed LZ4（常用，含帧头校验）和 Block LZ4（原始块格式）。
 * 如不确定数据格式，优先尝试 Framed LZ4。
 * @see IOResource
 * @see FramedLZ4CompressorInputStream
 * @see BlockLZ4CompressorInputStream
 * @see LZ4Utils
 * @since 1.1.0
 */
public class LZ4Resource extends IOResource {
	/**
	 * 从 IOResource 创建 LZ4 资源。
	 * <p>如果传入的资源不是 LZ4Resource 实例，会验证其 MIME 类型是否为 LZ4 格式。</p>
	 *
	 * @param resource IOResource 对象，必须非 null
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 LZ4 格式时抛出
	 * @since 1.1.0
	 */
	public LZ4Resource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof LZ4Resource)) {
			validateType("resource 不是 lz4 资源");
		}
	}

	/**
	 * 从文件路径创建 LZ4 资源。
	 * <p>会自动检测文件的 MIME 类型，验证是否为 LZ4 格式。</p>
	 *
	 * @param filePath 文件路径，必须非 null 且指向存在的 LZ4 文件
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 LZ4 格式时抛出
	 * @since 1.1.0
	 */
	public LZ4Resource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是 lz4 文件路径");
	}

	/**
	 * 从 File 对象创建 LZ4 资源。
	 * <p>会自动检测文件的 MIME 类型，验证是否为 LZ4 格式。</p>
	 *
	 * @param file File 对象，必须非 null 且指向存在的 LZ4 文件
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 LZ4 格式时抛出
	 * @since 1.1.0
	 */
	public LZ4Resource(File file) throws IOException {
		super(file);

		validateType("file 不是 lz4 文件");
	}

	/**
	 * 从字节数组创建 LZ4 资源。
	 * <p>会自动检测字节数组的 MIME 类型，验证是否为 LZ4 格式。</p>
	 *
	 * @param bytes LZ4 格式的字节数组，必须非 null
	 * @throws IOException                  当检测 MIME 类型失败时抛出
	 * @throws UnsupportedResourceException 当字节数组不是 LZ4 格式时抛出
	 * @since 1.1.0
	 */
	public LZ4Resource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是 lz4 数据");
	}

	/**
	 * 从输入流创建 LZ4 资源。
	 * <p>会自动检测输入流的 MIME 类型，验证是否为 LZ4 格式。</p>
	 *
	 * @param inputStream 输入流，必须非 null
	 * @throws IOException                  当检测 MIME 类型失败时抛出
	 * @throws UnsupportedResourceException 当输入流不是 LZ4 格式时抛出
	 * @since 1.1.0
	 */
	public LZ4Resource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 lz4 数据输入流");
	}

	/**
	 * 打开 Framed LZ4 压缩输入流。
	 * <p>返回一个缓冲的 Framed LZ4 压缩输入流，用于读取解压后的数据。
	 * Framed LZ4 是标准的 LZ4 帧格式，包含帧头和校验和。</p>
	 *
	 * @return Framed LZ4 压缩输入流
	 * @throws IOException 当资源已关闭或打开输入流失败时抛出
	 * @since 1.1.0
	 */
	public FramedLZ4CompressorInputStream openFramedLZ4CompressorInputStream() throws IOException {
		checkClosed();

		return new FramedLZ4CompressorInputStream(newBufferedInputStream());
	}

	/**
	 * 打开 Block LZ4 压缩输入流。
	 * <p>返回一个缓冲的 Block LZ4 压缩输入流，用于读取解压后的数据。
	 * Block LZ4 是原始的 LZ4 块格式，不包含帧头和校验和。</p>
	 *
	 * @return Block LZ4 压缩输入流
	 * @throws IOException 当资源已关闭或打开输入流失败时抛出
	 * @since 1.1.0
	 */
	public BlockLZ4CompressorInputStream openBlockLZ4CompressorInputStream() throws IOException {
		checkClosed();

		return new BlockLZ4CompressorInputStream(newBufferedInputStream());
	}

	/**
	 * 验证资源类型是否为 LZ4 格式。
	 * <p>通过检查 MIME 类型是否为 {@link CompressConstants#LZ4_MIME_TYPE} 来验证。</p>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当 MIME 类型不是 LZ4 格式时抛出
	 * @since 1.1.0
	 */
	protected void validateType(String message) {
		if (!CompressConstants.LZ4_MIME_TYPE.equals(mimeType)) {
			throw new UnsupportedResourceException(message);
		}
	}
}
