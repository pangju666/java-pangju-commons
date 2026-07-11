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
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * FFmpeg媒体资源封装类
 * <p>专为JavaCV + FFmpeg媒体处理场景设计，提供媒体资源的统一封装和管理能力。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *     <li><strong>多数据源支持</strong> - 支持文件、字节数组、输入流等多种媒体来源</li>
 *     <li><strong>自动类型检测</strong> - 基于Apache Tika自动识别媒体MIME类型</li>
 *     <li><strong>可重复读取</strong> - 输入流自动缓冲为可重复读取的内存流，避免单次消费</li>
 *     <li><strong>格式转换</strong> - 支持文件/字节数组/输入流三种格式互相转换</li>
 *     <li><strong>FFmpeg集成</strong> - 提供便捷的FFmpegFrameGrabber打开方法</li>
 *     <li><strong>类型快速判断</strong> - 支持音频/视频来源快速判断</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *     <li>视频文件处理</li>
 *     <li>音频文件处理</li>
 *     <li>媒体流处理</li>
 *     <li>FFmpeg媒体数据读取</li>
 * </ul>
 *
 * <h3>注意事项</h3>
 * <ul>
 *     <li>资源关闭后禁止执行任何操作</li>
 *     <li>临时文件在资源关闭时自动删除</li>
 *     <li>大文件建议使用文件模式且不缓存以避免内存占用过高</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class FFmpegResource extends IOResource {
	/**
	 * 从 IOResource 创建 FFmpegResource
	 *
	 * @param resource IOResource 实例，不可为 null
	 * @throws IOException 当读取资源失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegResource(IOResource resource) throws IOException {
		super(resource);
	}

	/**
	 * 从 IOResource 创建 FFmpegResource
	 *
	 * @param resource     IOResource 实例，不可为 null
	 * @param cacheContent 是否缓存内容到内存
	 * @throws IOException 当读取资源失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegResource(IOResource resource, boolean cacheContent) throws IOException {
		super(resource, cacheContent);
	}

	/**
	 * 从文件路径创建 FFmpegResource
	 *
	 * @param filePath 文件路径，不可为 null
	 * @throws IOException 当读取文件失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegResource(String filePath) throws IOException {
		super(filePath);
	}

	/**
	 * 从文件路径创建 FFmpegResource
	 *
	 * @param filePath     文件路径，不可为 null
	 * @param cacheContent 是否缓存内容到内存
	 * @throws IOException 当读取文件失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegResource(String filePath, boolean cacheContent) throws IOException {
		super(filePath, cacheContent);
	}

	/**
	 * 从文件创建 FFmpegResource
	 *
	 * @param file 文件对象，不可为 null
	 * @throws IOException 当读取文件失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegResource(File file) throws IOException {
		super(file);
	}

	/**
	 * 从文件创建 FFmpegResource
	 *
	 * @param file         文件对象，不可为 null
	 * @param cacheContent 是否缓存内容到内存
	 * @throws IOException 当读取文件失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegResource(File file, boolean cacheContent) throws IOException {
		super(file, cacheContent);
	}

	/**
	 * 从字节数组创建 FFmpegResource
	 *
	 * @param bytes 字节数组，不可为 null
	 * @throws IOException 当处理字节数组失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegResource(byte[] bytes) throws IOException {
		super(bytes);
	}

	/**
	 * 从输入流创建 FFmpegResource
	 * <p>
	 * 输入流会自动缓冲为可重复读取的内存流
	 * </p>
	 *
	 * @param inputStream 输入流，不可为 null
	 * @throws IOException 当读取输入流失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegResource(InputStream inputStream) throws IOException {
		super(inputStream);
	}

	/**
	 * 打开FFmpegFrameGrabber用于读取媒体内容
	 * <p>根据资源类型自动选择合适的输入源创建FFmpegFrameGrabber。</p>
	 *
	 * <p>实现特性：</p>
	 * <ul>
	 *     <li>文件模式：直接使用文件对象创建FFmpegFrameGrabber，性能更佳</li>
	 *     <li>字节数组/输入流模式：使用缓冲输入流创建FFmpegFrameGrabber</li>
	 *     <li>自动检查资源是否已关闭</li>
	 * </ul>
	 *
	 * @return FFmpegFrameGrabber实例（需手动关闭）
	 * @throws IOException 当资源已关闭或打开抓取器失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegFrameGrabber openFrameGrabber() throws IOException {
		checkClosed();

		if (Objects.nonNull(file)) {
			return new FFmpegFrameGrabber(file);
		} else {
			return new FFmpegFrameGrabber(newBufferedInputStream());
		}
	}
}
