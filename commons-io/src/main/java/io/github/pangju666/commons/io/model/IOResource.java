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

package io.github.pangju666.commons.io.model;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Objects;
import java.util.UUID;

/**
 * IO资源封装类
 * <p>提供统一的IO资源抽象，支持文件、字节数组、输入流等多种数据源的封装和管理。</p>
 *
 * <h3>核心特性：</h3>
 * <ul>
 *     <li><strong>多数据源支持</strong> - 支持文件、字节数组、输入流等多种数据源</li>
 *     <li><strong>自动类型检测</strong> - 基于Apache Tika自动识别MIME类型</li>
 *     <li><strong>可选缓存</strong> - 文件模式可选择是否将内容缓存到内存</li>
 *     <li><strong>摘要计算</strong> - 支持基于三段采样策略的摘要计算</li>
 *     <li><strong>资源管理</strong> - 实现Closeable接口，支持资源自动清理</li>
 *     <li><strong>临时文件管理</strong> - 自动管理临时文件的创建和删除</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>文件上传处理</li>
 *     <li>流式数据处理</li>
 *     <li>内存缓存管理</li>
 *     <li>类型检测和验证</li>
 * </ul>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>资源关闭后禁止执行任何操作</li>
 *     <li>临时文件在资源关闭时自动删除</li>
 *     <li>大文件建议使用文件模式且不缓存以避免内存占用过高</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class IOResource implements Closeable {
	/**
	 * 临时文件前缀
	 *
	 * @since 2.1.0
	 */
	protected static final String TMP_FILE_PREFIX = "io-resource-";
	/**
	 * 临时文件目录
	 *
	 * @since 2.1.0
	 */
	protected static final String TEMP_FILE_DIRECTORY = FileUtils.getTempDirectory().getPath();
	/**
	 * 临时文件扩展名
	 *
	 * @since 2.1.0
	 */
	protected static final String TMP_FILE_EXTENSION = "tmp";

	/**
	 * MIME类型
	 *
	 * @since 2.1.0
	 */
	protected final String mimeType;
	/**
	 * 资源大小（字节）
	 *
	 * @since 2.1.0
	 */
	protected final long size;
	/**
	 * 字节数组缓存
	 *
	 * @since 2.1.0
	 */
	protected final ByteArrayOutputStream byteArrayOutputStream;

	/**
	 * 文件引用
	 *
	 * @since 2.1.0
	 */
	protected volatile File file;
	/**
	 * 摘要值
	 *
	 * @since 2.1.0
	 */
	protected volatile String digest;
	/**
	 * 是否为临时文件
	 *
	 * @since 2.1.0
	 */
	protected volatile boolean tempFileFlag = false;
	/**
	 * 是否已关闭
	 *
	 * @since 2.1.0
	 */
	protected volatile boolean closed = false;

	/**
	 * 复制构造函数（不缓存内容）
	 * <p>等同于 {@code new IOResource(resource, false)}。</p>
	 *
	 * @param resource 源资源（必须非null且未关闭）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当resource已关闭时抛出
	 * @since 2.1.0
	 */
	public IOResource(IOResource resource) throws IOException {
		this(resource, false);
	}

	/**
	 * 复制构造函数
	 * <p>从现有IOResource创建副本。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>源资源必须未关闭</li>
	 *     <li>非临时文件且不缓存时共享文件引用</li>
	 *     <li>非临时文件且缓存时读取文件内容到字节数组</li>
	 *     <li>临时文件或字节数组模式读取内容到字节数组</li>
	 * </ul>
	 *
	 * @param resource     源资源（必须非null且未关闭）
	 * @param cacheContent 是否缓存内容（仅对文件模式有效）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当resource已关闭时抛出
	 * @since 2.1.0
	 */
	public IOResource(IOResource resource, boolean cacheContent) throws IOException {
		if (resource.closed) {
			throw new IllegalArgumentException("resource 已关闭");
		}

		this.size = resource.size;
		this.mimeType = resource.mimeType;
		this.digest = resource.digest;

		if (Objects.nonNull(resource.file) && !resource.isTempFile()) {
			this.file = resource.file;
			if (cacheContent) {
				this.byteArrayOutputStream = new ByteArrayOutputStream(IOUtils.getBufferSize(file.length()));
				try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(file)) {
					this.byteArrayOutputStream.write(inputStream);
				}
			} else {
				this.byteArrayOutputStream = null;
			}
		} else {
			this.byteArrayOutputStream = new ByteArrayOutputStream(IOUtils.getBufferSize(resource.size));
			this.byteArrayOutputStream.write(resource.newBufferedInputStream());
		}
	}

	/**
	 * 基于文件路径构造IOResource（不缓存内容）
	 * <p>等同于 {@code new IOResource(filePath, false)}。</p>
	 *
	 * @param filePath 文件路径（必须非空）
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当filePath为空时抛出
	 * @since 2.1.0
	 */
	public IOResource(String filePath) throws IOException {
		this(filePath, false);
	}

	/**
	 * 基于文件路径构造IOResource
	 * <p>自动检测文件MIME类型和大小。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>cacheContent为true时，构造时立即读取文件内容到内存</li>
	 *     <li>大文件建议设置为false以避免内存占用过高</li>
	 * </ul>
	 *
	 * @param filePath     文件路径（必须非空）
	 * @param cacheContent 是否缓存内容
	 * @throws IOException              当文件读取失败时抛出
	 * @throws IllegalArgumentException 当filePath为空时抛出
	 * @since 2.1.0
	 */
	public IOResource(String filePath, boolean cacheContent) throws IOException {
		Validate.notBlank(filePath, "filePath 不可为空");

		this.file = new File(filePath);
		FileUtils.checkFile(file, StringUtils.EMPTY);

		this.mimeType = FileUtils.getMimeType(file);
		this.size = file.length();

		if (cacheContent) {
			this.byteArrayOutputStream = new ByteArrayOutputStream(IOUtils.getBufferSize(file.length()));
			try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(file)) {
				this.byteArrayOutputStream.write(inputStream);
			}
		} else {
			this.byteArrayOutputStream = null;
		}
	}

	/**
	 * 基于File对象构造IOResource（不缓存内容）
	 * <p>等同于 {@code new IOResource(file, false)}。</p>
	 *
	 * @param file 文件对象（必须非null）
	 * @throws IOException 当文件读取失败时抛出
	 * @since 2.1.0
	 */
	public IOResource(File file) throws IOException {
		this(file, false);
	}

	/**
	 * 基于File对象构造IOResource
	 * <p>自动检测文件MIME类型和大小。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>cacheContent为true时，构造时立即读取文件内容到内存</li>
	 *     <li>大文件建议设置为false以避免内存占用过高</li>
	 * </ul>
	 *
	 * @param file         文件对象（必须非null）
	 * @param cacheContent 是否缓存内容
	 * @throws IOException 当文件读取失败时抛出
	 * @since 2.1.0
	 */
	public IOResource(File file, boolean cacheContent) throws IOException {
		this.file = file;
		this.mimeType = FileUtils.getMimeType(file);
		this.size = file.length();

		if (cacheContent) {
			this.byteArrayOutputStream = new ByteArrayOutputStream(IOUtils.getBufferSize(file.length()));
			try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(file)) {
				this.byteArrayOutputStream.write(inputStream);
			}
		} else {
			this.byteArrayOutputStream = null;
		}
	}

	/**
	 * 基于字节数组构造IOResource
	 * <p>自动检测MIME类型，内容存储在内存中。</p>
	 *
	 * @param bytes 字节数组（必须非空）
	 * @throws IllegalArgumentException 当bytes为空时抛出
	 * @since 2.1.0
	 */
	public IOResource(byte[] bytes) throws IOException {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		this.byteArrayOutputStream = new ByteArrayOutputStream(IOUtils.getBufferSize(bytes.length));
		this.byteArrayOutputStream.write(bytes);

		this.size = bytes.length;
		try (InputStream inputStream = this.byteArrayOutputStream.toInputStream()) {
			this.mimeType = IOConstants.getDefaultTika().detect(inputStream);
		}
	}

	/**
	 * 基于输入流构造IOResource
	 * <p>读取输入流全部内容到内存，自动检测MIME类型。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>输入流会被完全读取，但不会自动关闭</li>
	 *     <li>大流可能导致内存不足，建议使用文件模式</li>
	 * </ul>
	 *
	 * @param inputStream 输入流（必须非null）
	 * @throws IOException              当流读取失败时抛出
	 * @throws IllegalArgumentException 当inputStream为null时抛出
	 * @since 2.1.0
	 */
	public IOResource(InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		this.byteArrayOutputStream = new ByteArrayOutputStream();
		this.byteArrayOutputStream.write(inputStream);

		this.size = this.byteArrayOutputStream.size();
		try (InputStream tmpInputStream = this.byteArrayOutputStream.toInputStream()) {
			this.mimeType = IOConstants.getDefaultTika().detect(tmpInputStream);
		}
	}

	/**
	 * 基于{@link ByteArrayOutputStream}构造IOResource
	 * <p>用于子类内部构造，自动检测MIME类型（如果未指定）。</p>
	 *
	 * @param byteArrayOutputStream 字节数组输出流（必须非null）
	 * @param mimeType              MIME类型（可为空，为空时自动检测）
	 * @throws IllegalArgumentException 当byteArrayOutputStream为null时抛出
	 * @since 2.1.0
	 */
	protected IOResource(ByteArrayOutputStream byteArrayOutputStream, String mimeType) throws IOException {
		Validate.notNull(byteArrayOutputStream, "byteArrayOutputStream 不可为 null");

		this.byteArrayOutputStream = byteArrayOutputStream;

		this.size = this.byteArrayOutputStream.size();
		if (StringUtils.isBlank(mimeType)) {
			try (InputStream tmpInputStream = this.byteArrayOutputStream.toInputStream()) {
				this.mimeType = IOConstants.getDefaultTika().detect(tmpInputStream);
			}
		} else {
			this.mimeType = mimeType;
		}
	}


	/**
	 * 获取资源摘要
	 * <p>使用三段采样策略计算摘要，结果会被缓存。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>首次计算后缓存结果</li>
	 *     <li>文件模式使用{@link FileUtils#computeDigest}</li>
	 *     <li>字节数组模式使用{@link IOUtils#computeDigest}</li>
	 * </ul>
	 *
	 * @return 摘要字符串
	 * @throws IOException 当资源读取失败或已关闭时抛出
	 * @since 2.1.0
	 */
	public String getDigest() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(digest)) {
				return digest;
			}

			if (Objects.nonNull(file)) {
				digest = FileUtils.computeDigest(file);
			} else {
				try (InputStream inputStream = byteArrayOutputStream.toInputStream()) {
					digest = IOUtils.computeDigest(inputStream, size);
				}
			}
			return digest;
		}
	}

	/**
	 * 设置摘要值
	 * <p>用于手动设置已计算的摘要值。</p>
	 *
	 * @param digest 摘要字符串
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public void setDigest(String digest) throws IOException {
		checkClosed();

		this.digest = digest;
	}

	/**
	 * 获取资源大小
	 *
	 * @return 资源大小（字节）
	 * @since 2.1.0
	 */
	public long getSize() {
		return size;
	}

	/**
	 * 获取MIME类型
	 *
	 * @return MIME类型字符串
	 * @since 2.1.0
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * 获取文件对象
	 * <p>如果当前为字节数组模式，会创建临时文件。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>字节数组模式首次调用时创建临时文件</li>
	 *     <li>临时文件在资源关闭时自动删除</li>
	 *     <li>文件模式直接返回文件引用</li>
	 * </ul>
	 *
	 * @return 文件对象
	 * @throws IOException 当文件创建失败或资源已关闭时抛出
	 * @since 2.1.0
	 */
	public File getFile() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(file)) {
				return file;
			}

			File tempFile = new File(FileUtils.getTempDirectory(),
				TMP_FILE_PREFIX + UUID.randomUUID() + FilenameUtils.EXTENSION_SEPARATOR_STR + TMP_FILE_EXTENSION);
			try (InputStream inputStream = byteArrayOutputStream.toInputStream()) {
				FileUtils.copyInputStreamToFile(inputStream, tempFile);
			}
			file = tempFile;
			tempFileFlag = true;
			return file;
		}
	}

	/**
	 * 创建新的输入流
	 * <p>根据资源类型返回相应的输入流。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>文件模式使用{@link FileUtils#openInputStream}</li>
	 *     <li>字节数组模式使用{@link ByteArrayOutputStream#toByteArray()}</li>
	 *     <li>每次调用返回新的输入流实例</li>
	 * </ul>
	 *
	 * @return 输入流
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public InputStream openInputStream() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return FileUtils.openInputStream(file);
		} else {
			return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
		}
	}

	/**
	 * 创建新的缓冲输入流
	 * <p>始终返回缓冲输入流以提高读取性能。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>文件模式使用{@link FileUtils#openBufferedFileChannelInputStream}</li>
	 *     <li>字节数组模式使用{@link ByteArrayOutputStream#toInputStream()}</li>
	 *     <li>每次调用返回新的输入流实例</li>
	 * </ul>
	 *
	 * @return 缓冲输入流
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	public InputStream newBufferedInputStream() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return FileUtils.openBufferedFileChannelInputStream(file);
		} else {
			return byteArrayOutputStream.toInputStream();
		}
	}

	/**
	 * 获取字节数组
	 * <p>如果当前为文件模式且未缓存，会读取文件内容。</p>
	 *
	 * <p>注意事项：</p>
	 * <ul>
	 *     <li>文件模式且已缓存时直接返回字节数组</li>
	 *     <li>文件模式且未缓存时读取文件内容（不缓存）</li>
	 *     <li>字节数组模式直接返回字节数组</li>
	 * </ul>
	 *
	 * @return 字节数组
	 * @throws IOException 当文件读取失败或资源已关闭时抛出
	 * @since 2.1.0
	 */
	public byte[] getBytes() throws IOException {
		checkClosed();

		if (Objects.nonNull(byteArrayOutputStream)) {
			return byteArrayOutputStream.toByteArray();
		}
		return FileUtils.readFileToByteArray(file);
	}

	/**
	 * 判断是否为音频资源
	 *
	 * @return 如果MIME类型以audio/开头返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isAudio() {
		return Strings.CS.startsWith(mimeType, IOConstants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为视频资源
	 *
	 * @return 如果MIME类型以video/开头返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isVideo() {
		return Strings.CS.startsWith(mimeType, IOConstants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为图片资源
	 *
	 * @return 如果MIME类型以image/开头返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isImage() {
		return Strings.CS.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为文本资源
	 *
	 * @return 如果MIME类型以text/开头返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isText() {
		return Strings.CS.startsWith(mimeType, IOConstants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为临时文件
	 * <p>判断标准：</p>
	 * <ul>
	 *     <li>文件名以 {@link #TMP_FILE_PREFIX} 开头</li>
	 *     <li>文件扩展名为 {@link #TMP_FILE_EXTENSION}</li>
	 *     <li>父目录为 {@link #TEMP_FILE_DIRECTORY}</li>
	 * </ul>
	 *
	 * @return 如果是临时文件返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isTempFile() {
		if (Objects.isNull(file)) {
			return false;
		}
		String parent = file.getParent();
		String filename = file.getName();
		String extension = FilenameUtils.getExtension(filename);
		return filename.startsWith(TMP_FILE_PREFIX) && TMP_FILE_EXTENSION.equals(extension) && TEMP_FILE_DIRECTORY.equals(parent);
	}

	/**
	 * 关闭资源
	 * <p>释放资源并清理临时文件。</p>
	 *
	 * <p>清理操作：</p>
	 * <ul>
	 *     <li>如果是临时文件，删除临时文件</li>
	 *     <li>清空文件、字节数组、摘要引用</li>
	 *     <li>标记为已关闭状态</li>
	 * </ul>
	 *
	 * @throws IOException 当文件删除失败时抛出
	 * @since 2.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (isClosed()) {
			return;
		}

		if (tempFileFlag && Objects.nonNull(file)) {
			FileUtils.forceDeleteIfExist(file);
		}

		if (Objects.nonNull(byteArrayOutputStream)) {
			byteArrayOutputStream.close();
		}

		file = null;
		digest = null;
		tempFileFlag = false;
		closed = true;
	}

	/**
	 * 判断资源是否已关闭
	 *
	 * @return 如果已关闭返回true，否则返回false
	 * @since 2.1.0
	 */
	public boolean isClosed() {
		return closed;
	}

	/**
	 * 检查资源是否已关闭
	 * <p>如果已关闭则抛出IOException。</p>
	 *
	 * @throws IOException 当资源已关闭时抛出
	 * @since 2.1.0
	 */
	protected void checkClosed() throws IOException {
		if (closed) {
			throw new IOException("IOResource 已关闭，禁止执行操作");
		}
	}
}
