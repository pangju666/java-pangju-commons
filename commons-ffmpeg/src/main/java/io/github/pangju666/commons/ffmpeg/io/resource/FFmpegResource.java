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
 * 媒体资源统一封装类
 * <p>
 * 统一封装 <b>File、byte[]、InputStream</b> 三种媒体来源，提供相互转换、MIME 类型识别、资源大小获取能力；
 * 专为 JavaCV + FFmpeg 媒体处理场景设计，保证资源可重复读取、流安全性。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>输入流会自动缓冲为可重复读取的内存流，避免单次消费无法二次使用</li>
 *     <li>基于 Apache Tika 自动识别媒体 MIME 类型（audio/* / video/* 等）</li>
 *     <li>支持 File / 字节数组 / 输入流 三种格式互相转换</li>
 *     <li>临时文件自动命名，建议使用后手动删除避免磁盘冗余</li>
 *     <li>支持音频/视频/文件来源快速判断</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 从文件创建
 * FFmpegResource resource = new FFmpegResource(new File("video.mp4"));
 *
 * // 从字节数组创建
 * byte[] data = Files.readAllBytes(Paths.get("audio.wav"));
 * FFmpegResource resource = new FFmpegResource(data);
 *
 * // 从输入流创建
 * try (InputStream is = new FileInputStream("media.mp3")) {
 *     FFmpegResource resource = new FFmpegResource(is);
 * }
 *
 * // 转换格式
 * File file = resource.getFile();
 * InputStream is = resource.getInputStream();
 * byte[] bytes = resource.getBytes();
 *
 * // 判断类型
 * if (resource.isVideo()) {
 *     // 处理视频
 * } else if (resource.isAudio()) {
 *     // 处理音频
 * }
 *
 * // 打开 FFmpegFrameGrabber
 * try (FFmpegFrameGrabber grabber = resource.openFrameGrabber()) {
 *     grabber.start();
 *     // 处理媒体
 * }
 * }</pre>
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
	 * 打开 FFmpegFrameGrabber 用于读取媒体文件
	 * <p>
	 * 根据资源类型自动选择合适的输入源：
	 * <ul>
	 *   <li>文件模式：直接使用文件路径创建 FFmpegFrameGrabber</li>
	 *   <li>字节数组/输入流模式：使用缓冲输入流创建 FFmpegFrameGrabber</li>
	 * </ul>
	 * </p>
	 *
	 * @return FFmpegFrameGrabber 实例
	 * @throws IOException 当打开抓取器失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegFrameGrabber openFrameGrabber() throws IOException {
		if (Objects.nonNull(file)) {
			return new FFmpegFrameGrabber(file);
		} else {
			return new FFmpegFrameGrabber(newBufferedInputStream());
		}
	}
}
