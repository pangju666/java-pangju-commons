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
import io.github.pangju666.commons.io.model.DataSize;
import io.github.pangju666.commons.io.resource.IOResource;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 7Z 压缩文件资源类
 * <p>
 * 用于封装和管理 7Z 格式的压缩文件资源，支持从多种来源加载 7Z 文件，
 * 包括文件路径、File 对象、字节数组和输入流。
 * 支持加密的 7Z 文件（需要密码）。
 * </p>
 * <p>
 * <b>支持的特性：</b>
 * </p>
 * <ul>
 *   <li>从文件、字节数组、输入流加载 7Z 资源</li>
 *   <li>自动验证资源类型</li>
 * </ul>
 * <p>
 * <b>使用示例：</b>
 * </p>
 * <pre>{@code
 * // 1) 从文件路径加载并解压
 * SevenZResource resource = new SevenZResource("/path/to/archive.7z");
 * SevenZUtils.uncompress(resource, new File("/path/to/output"));
 *
 * // 2) 加载加密的 7Z 文件并解压
 * SevenZResource encryptedResource = new SevenZResource("/path/to/encrypted.7z", "password");
 * SevenZUtils.uncompress(encryptedResource, new File("/path/to/output"));
 *
 * // 3) 打开 SevenZFile 进行细粒度操作
 * try (SevenZFile sevenZFile = resource.openSevenZFile()) {
 *     // 遍历文件条目
 *     for (SevenZArchiveEntry entry : sevenZFile.getEntries()) {
 *         System.out.println(entry.getName());
 *     }
 * }
 * }</pre>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class SevenZResource extends IOResource {
	/**
	 * 7Z 文件密码
	 * <p>用于打开加密的 7Z 文件，如果为 null 表示文件未加密</p>
	 *
	 * @since 2.1.0
	 */
	protected final String password;

	/**
	 * 从 IOResource 构造 7Z 资源
	 * <p>
	 * 如果传入的资源已经是 SevenZResource，则直接复用其密码。
	 * 否则，仅验证资源类型，密码设为 null。
	 * </p>
	 *
	 * @param resource IO 资源对象
	 * @throws IOException                  读取资源失败时抛出
	 * @throws UnsupportedResourceException 资源不是 7Z 格式时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(IOResource resource) throws IOException {
		super(resource);

		if (resource instanceof SevenZResource) {
			this.password = ((SevenZResource) resource).password;
		} else {
			validateType("resource 不是 7z 资源");

			this.password = null;
		}
	}

	/**
	 * 从文件路径构造 7Z 资源
	 * <p>
	 * 仅验证文件类型，密码设为 null。
	 * </p>
	 *
	 * @param filePath 7Z 文件路径
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是 7Z 格式时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是 7z 文件路径");

		this.password = null;
	}

	/**
	 * 从 File 对象构造 7Z 资源
	 * <p>
	 * 仅验证文件类型，密码设为 null。
	 * </p>
	 *
	 * @param file 7Z 文件对象
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是 7Z 格式时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(File file) throws IOException {
		super(file);

		validateType("file 不是 7z 文件");

		this.password = null;
	}

	/**
	 * 从字节数组构造 7Z 资源
	 * <p>
	 * 仅验证数据类型，密码设为 null。
	 * </p>
	 *
	 * @param bytes 7Z 数据字节数组
	 * @throws IOException                  读取数据失败时抛出
	 * @throws UnsupportedResourceException 数据不是 7Z 格式时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是 7z 数据");

		this.password = null;
	}

	/**
	 * 从输入流构造 7Z 资源
	 * <p>
	 * 仅验证流类型，密码设为 null。
	 * </p>
	 *
	 * @param inputStream 7Z 数据输入流
	 * @throws IOException                  读取流失败时抛出
	 * @throws UnsupportedResourceException 流数据不是 7Z 格式时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 7z 数据输入流");

		this.password = null;
	}

	/**
	 * 从 IOResource 构造加密的 7Z 资源
	 * <p>
	 * 验证资源类型并设置密码。
	 * </p>
	 *
	 * @param resource IO 资源对象
	 * @param password 7Z 文件密码，不可为空
	 * @throws IOException                  读取资源失败时抛出
	 * @throws UnsupportedResourceException 资源不是 7Z 格式时抛出
	 * @throws IllegalArgumentException     当 password 为空时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(IOResource resource, String password) throws IOException {
		super(resource);

		if (!(resource instanceof SevenZResource)) {
			validateType("resource 不是 7z 资源");
		}

		Validate.notBlank(password, "password 不可为空");
		this.password = password;
	}

	/**
	 * 从文件路径构造加密的 7Z 资源
	 * <p>
	 * 验证文件类型并设置密码。
	 * </p>
	 *
	 * @param filePath 7Z 文件路径
	 * @param password 7Z 文件密码，不可为空
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是 7Z 格式时抛出
	 * @throws IllegalArgumentException     当 password 为空时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(String filePath, String password) throws IOException {
		super(filePath);

		validateType("filePath 不是 7z 文件路径");

		Validate.notBlank(password, "password 不可为空");
		this.password = password;
	}

	/**
	 * 从 File 对象构造加密的 7Z 资源
	 * <p>
	 * 验证文件类型并设置密码。
	 * </p>
	 *
	 * @param file     7Z 文件对象
	 * @param password 7Z 文件密码，不可为空
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是 7Z 格式时抛出
	 * @throws IllegalArgumentException     当 password 为空时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(File file, String password) throws IOException {
		super(file);

		validateType("file 不是 7z 文件");

		Validate.notBlank(password, "password 不可为空");
		this.password = password;
	}

	/**
	 * 从字节数组构造加密的 7Z 资源
	 * <p>
	 * 验证数据类型并设置密码。
	 * </p>
	 *
	 * @param bytes    7Z 数据字节数组
	 * @param password 7Z 文件密码，不可为空
	 * @throws IOException                  读取数据失败时抛出
	 * @throws UnsupportedResourceException 数据不是 7Z 格式时抛出
	 * @throws IllegalArgumentException     当 password 为空时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(byte[] bytes, String password) throws IOException {
		super(bytes);

		validateType("bytes 不是 7z 数据");

		Validate.notBlank(password, "password 不可为空");
		this.password = password;
	}

	/**
	 * 从输入流构造加密的 7Z 资源
	 * <p>
	 * 验证流类型并设置密码。
	 * </p>
	 *
	 * @param inputStream 7Z 数据输入流
	 * @param password    7Z 文件密码，不可为空
	 * @throws IOException                  读取流失败时抛出
	 * @throws UnsupportedResourceException 流数据不是 7Z 格式时抛出
	 * @throws IllegalArgumentException     当 password 为空时抛出
	 * @since 2.1.0
	 */
	public SevenZResource(InputStream inputStream, String password) throws IOException {
		super(inputStream);

		validateType("inputStream 不是 7z 数据输入流");

		Validate.notBlank(password, "password 不可为空");
		this.password = password;
	}

	/**
	 * 打开 7Z 文件。
	 * <p>根据资源类型创建相应的 SevenZFile 实例。</p>
	 * <p>如果资源来自文件，使用文件路径创建；否则使用字节数组创建。</p>
	 * <p>如果设置了密码，会自动应用到 SevenZFile。</p>
	 * <p>并非所有编解码器都支持此设置。目前仅支持 LZMA 和 LZMA2。</p>
	 *
	 * @return SevenZFile 实例
	 * @throws IOException 资源已关闭时抛出
	 * @since 2.1.0
	 */
	public SevenZFile openSevenZFile() throws IOException {
		checkClosed();

		SevenZFile.Builder builder = SevenZFile.builder();
		if (Objects.nonNull(file)) {
			builder.setFile(file);
		} else {
			builder.setByteArray(byteArrayOutputStream.toByteArray());
		}
		if (Objects.nonNull(password)) {
			builder.setPassword(password);
		}
		return builder.get();
	}

	/**
	 * 打开 7Z 文件并设置内存限制。
	 * <p>根据资源类型创建相应的 SevenZFile 实例。</p>
	 * <p>如果资源来自文件，使用文件路径创建；否则使用字节数组创建。</p>
	 * <p>如果设置了密码，会自动应用到 SevenZFile。</p>
	 * <p>设置内存限制可以防止解压过程中消耗过多内存。
	 * 仅当内存限制大于等于 1 KB 时才设置限制。</p>
	 * <p>并非所有编解码器都支持此设置。目前仅支持 LZMA 和 LZMA2。</p>
	 *
	 * @param maxMemoryLimit 最大内存限制，必须非 null
	 * @return SevenZFile 实例
	 * @throws IOException          资源已关闭时抛出
	 * @throws NullPointerException 当 maxMemoryLimit 为 null 时抛出
	 * @since 2.1.0
	 */
	public SevenZFile openSevenZFile(DataSize maxMemoryLimit) throws IOException {
		checkClosed();

		Validate.notNull(maxMemoryLimit, "maxMemoryLimit 不可为 null");

		SevenZFile.Builder builder = SevenZFile.builder();
		int maxMemoryLimitKib = (int) maxMemoryLimit.toKilobytes();
		if (maxMemoryLimitKib < 1) {
			builder.setMaxMemoryLimitKiB(maxMemoryLimitKib);
		}
		if (Objects.nonNull(file)) {
			builder.setFile(file);
		} else {
			builder.setByteArray(byteArrayOutputStream.toByteArray());
		}
		if (Objects.nonNull(password)) {
			builder.setPassword(password);
		}
		return builder.get();
	}

	/**
	 * 验证资源类型
	 * <p>
	 * 检查资源是否为 7Z 格式，如果不是则抛出异常。
	 * </p>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 资源不是 7Z 格式时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		if (!CompressConstants.SEVEN_Z_MIME_TYPE.equals(mimeType)) {
			throw new UnsupportedResourceException(message);
		}
	}
}
