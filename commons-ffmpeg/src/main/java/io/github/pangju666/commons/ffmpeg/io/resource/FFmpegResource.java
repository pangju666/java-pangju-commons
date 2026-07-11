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

package io.github.pangju666.commons.ffmpeg.io.resource;

import io.github.pangju666.commons.io.resource.IOResource;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * FFmpeg 媒体资源封装类
 * <p>继承 {@link IOResource} 的通用 IO 能力，并在此基础上增加音频/视频资源校验与 {@link FFmpegFrameGrabber} 打开能力。</p>
 *
 * <h3>核心特性：</h3>
 * <ul>
 *     <li><strong>多数据源支持</strong> - 支持文件、字节数组、输入流等多种媒体来源</li>
 *     <li><strong>自动类型检测</strong> - 基于 Apache Tika 自动识别媒体 MIME 类型</li>
 *     <li><strong>媒体类型校验</strong> - 构造时仅允许音频或视频资源，避免非媒体数据误用</li>
 *     <li><strong>可重复读取</strong> - 输入流会自动缓冲为可重复读取的内存流，避免单次消费</li>
 *     <li><strong>FFmpeg 集成</strong> - 提供便捷的 {@link #openFrameGrabber()} 方法用于创建媒体抓取器</li>
 *     <li><strong>类型快速判断</strong> - 继承父类的 MIME 判断能力，可快速识别音频和视频资源</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *     <li>视频文件读取与解析</li>
 *     <li>音频文件读取与解析</li>
 *     <li>媒体流处理</li>
 *     <li>基于 JavaCV / FFmpeg 的媒体元数据或帧数据读取</li>
 * </ul>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *     <li>资源关闭后禁止执行任何操作</li>
 *     <li>临时文件在资源关闭时自动删除</li>
 *     <li>大文件建议使用文件模式且不缓存以避免内存占用过高</li>
 *     <li><strong>媒体类型限制</strong> - 构造函数会校验资源是否为音频或视频，非媒体资源会抛出 {@link IllegalArgumentException}</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class FFmpegResource extends IOResource {
	/**
	 * 从 IOResource 创建 FFmpegResource
	 * <p>等同于 {@code new FFmpegResource(resource, false)}。</p>
	 *
	 * @param resource IOResource 实例，不可为 null，且其 MIME 类型必须为音频或视频
	 * @throws IOException              当读取资源失败时抛出
	 * @throws IllegalArgumentException 当 resource 不是音频或视频资源时抛出
	 * @since 2.1.0
	 */
	public FFmpegResource(IOResource resource) throws IOException {
		super(resource);

		validateType("resource 不是音频或视频资源");
	}

	/**
	 * 从 IOResource 创建 FFmpegResource
	 * <p>基于现有 {@link IOResource} 创建 FFmpegResource，并在构造完成后校验资源类型是否为音频或视频。</p>
	 *
	 * @param resource     IOResource 实例，不可为 null，且其 MIME 类型必须为音频或视频
	 * @param cacheContent 是否缓存内容到内存
	 * @throws IOException              当读取资源失败时抛出
	 * @throws IllegalArgumentException 当 resource 不是音频或视频资源时抛出
	 * @since 2.1.0
	 */
	public FFmpegResource(IOResource resource, boolean cacheContent) throws IOException {
		super(resource, cacheContent);

		validateType("resource 不是音频或视频资源");
	}

	/**
	 * 从文件路径创建 FFmpegResource
	 *
	 * @param filePath 文件路径，不可为 null，且对应文件必须为音频或视频资源
	 * @throws IOException              当读取文件失败时抛出
	 * @throws IllegalArgumentException 当 filePath 对应资源不是音频或视频时抛出
	 * @since 2.1.0
	 */
	public FFmpegResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是音频或视频文件路径");
	}

	/**
	 * 从文件路径创建 FFmpegResource
	 *
	 * @param filePath     文件路径，不可为 null，且对应文件必须为音频或视频资源
	 * @param cacheContent 是否缓存内容到内存
	 * @throws IOException              当读取文件失败时抛出
	 * @throws IllegalArgumentException 当 filePath 对应资源不是音频或视频时抛出
	 * @since 2.1.0
	 */
	public FFmpegResource(String filePath, boolean cacheContent) throws IOException {
		super(filePath, cacheContent);

		validateType("filePath 不是音频或视频文件路径");
	}

	/**
	 * 从文件创建 FFmpegResource
	 *
	 * @param file 文件对象，不可为 null，且必须为音频或视频文件
	 * @throws IOException              当读取文件失败时抛出
	 * @throws IllegalArgumentException 当 file 对应资源不是音频或视频时抛出
	 * @since 2.1.0
	 */
	public FFmpegResource(File file) throws IOException {
		super(file);

		validateType("file 不是音频或视频文件");
	}

	/**
	 * 从文件创建 FFmpegResource
	 *
	 * @param file         文件对象，不可为 null，且必须为音频或视频文件
	 * @param cacheContent 是否缓存内容到内存
	 * @throws IOException              当读取文件失败时抛出
	 * @throws IllegalArgumentException 当 file 对应资源不是音频或视频时抛出
	 * @since 2.1.0
	 */
	public FFmpegResource(File file, boolean cacheContent) throws IOException {
		super(file, cacheContent);

		validateType("file 不是音频或视频文件");
	}

	/**
	 * 从字节数组创建 FFmpegResource
	 *
	 * @param bytes 字节数组，不可为 null，且内容必须可识别为音频或视频资源
	 * @throws IOException              当处理字节数组失败时抛出
	 * @throws IllegalArgumentException 当 bytes 不是音频或视频数据时抛出
	 * @since 2.1.0
	 */
	public FFmpegResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是音频或视频数据");
	}

	/**
	 * 从输入流创建 FFmpegResource
	 * <p>
	 * 输入流会自动缓冲为可重复读取的内存流，并在构造完成后校验其是否为音频或视频资源。
	 * </p>
	 *
	 * @param inputStream 输入流，不可为 null，且内容必须可识别为音频或视频资源
	 * @throws IOException              当读取输入流失败时抛出
	 * @throws IllegalArgumentException 当 inputStream 不是音频或视频数据时抛出
	 * @since 2.1.0
	 */
	public FFmpegResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是音频或视频数据输入流");
	}

	/**
	 * 打开 FFmpegFrameGrabber 用于读取媒体内容
	 * <p>根据当前资源的存储模式自动选择合适的输入源创建 {@link FFmpegFrameGrabber}。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>文件模式：直接使用文件对象创建 {@link FFmpegFrameGrabber}，避免额外内存复制</li>
	 *     <li>字节数组/输入流模式：使用新的缓冲输入流创建 {@link FFmpegFrameGrabber}</li>
	 *     <li>自动检查资源是否已关闭</li>
	 * </ul>
	 *
	 * @return FFmpegFrameGrabber 实例，调用方使用完毕后应手动关闭
	 * @throws IOException 当资源已关闭，或创建输入流失败时抛出
	 * @since 2.1.0
	 */
	public FFmpegFrameGrabber openFrameGrabber() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return new FFmpegFrameGrabber(file);
		} else {
			return new FFmpegFrameGrabber(newBufferedInputStream());
		}
	}

	/**
	 * 校验当前资源是否为音频或视频资源
	 *
	 * @param message 校验失败时使用的异常消息
	 * @throws IllegalArgumentException 当当前资源既不是音频也不是视频时抛出
	 * @since 2.1.0
	 */
	protected void validateType(String message) {
		Validate.isTrue(isVideo() || isAudio(), message);
	}
}
