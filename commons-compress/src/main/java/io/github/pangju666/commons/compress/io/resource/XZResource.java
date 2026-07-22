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
import io.github.pangju666.commons.compress.utils.XZUtils;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.model.DataSize;
import io.github.pangju666.commons.io.resource.IOResource;
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * XZ 格式资源类。
 * <p>封装 XZ 格式的压缩资源，支持多种输入源（文件、字节数组、输入流等），并通过 MIME 类型检测验证格式。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>多输入源</strong>：支持文件路径、File 对象、字节数组、输入流、IOResource。</li>
 *   <li><strong>格式校验</strong>：通过 MIME 类型检测验证输入源是否为 XZ 格式。</li>
 *   <li><strong>资源管理</strong>：继承自 {@link IOResource}，支持资源关闭检查。</li>
 *   <li><strong>缓冲流</strong>：自动使用缓冲输入流提高性能。</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1) 从文件创建 XZ 资源
 * try (XZResource resource = new XZResource(new File("data.xz"));
 *      XZCompressorInputStream xis = resource.openXZCompressorInputStream()) {
 *     // 读取解压后的数据
 * }
 *
 * // 2) 从字节数组创建 XZ 资源
 * byte[] xzBytes = ...;
 * try (XZResource resource = new XZResource(xzBytes);
 *      XZCompressorInputStream xis = resource.openXZCompressorInputStream()) {
 *     // 读取解压后的数据
 * }
 *
 * // 3) 从输入流创建 XZ 资源
 * try (InputStream in = new FileInputStream("data.xz");
 *      XZResource resource = new XZResource(in);
 *      XZCompressorInputStream xis = resource.openXZCompressorInputStream()) {
 *     // 读取解压后的数据
 * }
 * }</pre>
 *
 * @author pangju666
 * @see IOResource
 * @see XZCompressorInputStream
 * @see XZUtils
 * @since 2.1.0
 */
public class XZResource extends IOResource {
	/**
	 * 从 IOResource 创建 XZ 资源。
	 * <p>如果传入的资源不是 XZResource 实例，会验证其 MIME 类型是否为 XZ 格式。</p>
	 *
	 * @param resource IOResource 对象，必须非 null
	 * @throws IOException                  当读取资源失败时抛出
	 * @throws UnsupportedResourceException 当资源不是 XZ 格式时抛出
	 * @since 2.1.0
	 */
	public XZResource(IOResource resource) throws IOException {
		super(resource);

		if (!(resource instanceof XZResource)) {
			validateType("resource 不是 xz 资源");
		}
	}

	/**
	 * 从文件路径创建 XZ 资源。
	 * <p>会自动检测文件的 MIME 类型，验证是否为 XZ 格式。</p>
	 *
	 * @param filePath 文件路径，必须非 null 且指向存在的 XZ 文件
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 XZ 格式时抛出
	 * @since 2.1.0
	 */
	public XZResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是 xz 文件路径");
	}

	/**
	 * 从 File 对象创建 XZ 资源。
	 * <p>会自动检测文件的 MIME 类型，验证是否为 XZ 格式。</p>
	 *
	 * @param file File 对象，必须非 null 且指向存在的 XZ 文件
	 * @throws IOException                  当读取文件失败时抛出
	 * @throws UnsupportedResourceException 当文件不是 XZ 格式时抛出
	 * @since 2.1.0
	 */
	public XZResource(File file) throws IOException {
		super(file);

		validateType("file 不是 xz 文件");
	}

	/**
	 * 从字节数组创建 XZ 资源。
	 * <p>会自动检测字节数组的 MIME 类型，验证是否为 XZ 格式。</p>
	 *
	 * @param bytes XZ 格式的字节数组，必须非 null
	 * @throws IOException                  当检测 MIME 类型失败时抛出
	 * @throws UnsupportedResourceException 当字节数组不是 XZ 格式时抛出
	 * @since 2.1.0
	 */
	public XZResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是 xz 数据");
	}

	/**
	 * 从输入流创建 XZ 资源。
	 * <p>会自动检测输入流的 MIME 类型，验证是否为 XZ 格式。</p>
	 *
	 * @param inputStream 输入流，必须非 null
	 * @throws IOException                  当检测 MIME 类型失败时抛出
	 * @throws UnsupportedResourceException 当输入流不是 XZ 格式时抛出
	 * @since 2.1.0
	 */
	public XZResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 xz 数据输入流");
	}

	/**
	 * 打开 XZ 压缩输入流。
	 * <p>返回一个缓冲的 XZ 压缩输入流，用于读取解压后的数据。</p>
	 *
	 * @return XZ 压缩输入流
	 * @throws IOException 当资源已关闭或打开输入流失败时抛出
	 * @since 2.1.0
	 */
	public XZCompressorInputStream openXZCompressorInputStream() throws IOException {
		checkClosed();

		return new XZCompressorInputStream(newBufferedInputStream());
	}

	/**
	 * 打开 XZ 压缩输入流并设置内存限制。
	 * <p>返回一个缓冲的 XZ 压缩输入流，用于读取解压后的数据。
	 * 设置内存限制可以防止解压过程中消耗过多内存。
	 * 如果内存限制小于等于 0 KB，则设置为 -1（表示无限制）。</p>
	 *
	 * @param maxMemoryLimit 最大内存限制，必须非 null
	 * @return XZ 压缩输入流
	 * @throws IOException          当资源已关闭或打开输入流失败时抛出
	 * @throws NullPointerException 当 maxMemoryLimit 为 null 时抛出
	 * @since 2.1.0
	 */
	public XZCompressorInputStream openXZCompressorInputStream(DataSize maxMemoryLimit) throws IOException {
		checkClosed();

		Validate.notNull(maxMemoryLimit, "maxMemoryLimit 不可为 null");

		int maxMemoryLimitKb = (int) maxMemoryLimit.toKilobytes();
		return XZCompressorInputStream.builder()
			.setMemoryLimitKiB(maxMemoryLimitKb > 0 ? maxMemoryLimitKb : -1)
			.setInputStream(newBufferedInputStream())
			.get();
	}

	/**
	 * 验证资源类型是否为 XZ 格式。
	 * <p>通过检查 MIME 类型是否为 {@link CompressConstants#XZ_MIME_TYPE} 来验证。</p>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 当 MIME 类型不是 XZ 格式时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		if (!CompressConstants.XZ_MIME_TYPE.equals(mimeType)) {
			throw new UnsupportedResourceException(message);
		}
	}
}
