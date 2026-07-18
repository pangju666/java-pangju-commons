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

import io.github.pangju666.commons.ffmpeg.model.Audio;
import io.github.pangju666.commons.io.exception.UnsupportedResourceException;
import io.github.pangju666.commons.io.resource.IOResource;
import org.bytedeco.javacv.FFmpegFrameGrabber;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * 音频资源类
 * <p>
 * 用于封装和管理音频资源，支持从多种来源加载音频文件，
 * 包括文件路径、File 对象、字节数组和输入流。
 * 使用 FFmpeg 解析音频信息，提供音频元数据访问。
 * </p>
 * <p>
 * <b>延迟加载：</b>
 * 音频信息采用延迟加载策略，只有在首次调用 {@link #getAudio()} 或 {@link #openFrameGrabber()} 时才会解析。
 * 使用双重检查锁定（Double-Checked Locking）确保线程安全。
 * </p>
 * <p>
 * <b>支持的音频格式：</b>
 * MP3、AAC、FLAC、OGG、WAV、M4A 等常见音频格式
 * </p>
 * <p>
 * <b>使用示例：</b>
 * </p>
 * <pre>{@code
 * // 从文件路径加载
 * AudioResource audioResource = new AudioResource("/path/to/audio.mp3");
 * Audio audio = audioResource.getAudio();
 * System.out.println("采样率: " + audio.getSampleRate());
 *
 * // 从 File 对象加载
 * File audioFile = new File("/path/to/audio.mp3");
 * AudioResource resource = new AudioResource(audioFile);
 *
 * // 从字节数组加载
 * byte[] audioBytes = Files.readAllBytes(Paths.get("/path/to/audio.mp3"));
 * AudioResource resource = new AudioResource(audioBytes);
 *
 * // 从输入流加载
 * try (InputStream inputStream = new FileInputStream("/path/to/audio.mp3")) {
 *     AudioResource resource = new AudioResource(inputStream);
 * }
 * }</pre>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class AudioResource extends IOResource {
	/**
	 * 音频信息对象（延迟加载）
	 * <p>
	 * 使用 volatile 修饰符确保多线程环境下的可见性。
	 * 音频信息在首次访问时通过 FFmpeg 解析。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected volatile Audio audio;

	/**
	 * 从 IOResource 构造音频资源
	 * <p>
	 * 如果传入的资源已经是 AudioResource，则直接复用其音频信息。
	 * 否则，仅验证资源类型，音频信息将在首次访问时延迟加载。
	 * </p>
	 *
	 * @param resource IO 资源对象
	 * @throws IOException                  读取资源失败时抛出
	 * @throws UnsupportedResourceException 资源不是音频格式时抛出
	 * @since 1.1.0
	 */
	public AudioResource(IOResource resource) throws IOException {
		super(resource);

		if (resource instanceof AudioResource) {
			this.audio = ((AudioResource) resource).audio;
		} else {
			validateType("resource 不是音频资源");
		}
	}

	/**
	 * 从文件路径构造音频资源
	 * <p>
	 * 仅验证文件类型，音频信息将在首次访问时延迟加载。
	 * </p>
	 *
	 * @param filePath 音频文件路径
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是音频格式时抛出
	 * @since 1.1.0
	 */
	public AudioResource(String filePath) throws IOException {
		super(filePath);

		validateType("filePath 不是音频文件路径");
	}

	/**
	 * 从 File 对象构造音频资源
	 * <p>
	 * 仅验证文件类型，音频信息将在首次访问时延迟加载。
	 * </p>
	 *
	 * @param file 音频文件对象
	 * @throws IOException                  读取文件失败时抛出
	 * @throws UnsupportedResourceException 文件不是音频格式时抛出
	 * @since 1.1.0
	 */
	public AudioResource(File file) throws IOException {
		super(file);

		validateType("file 不是音频文件");
	}

	/**
	 * 从字节数组构造音频资源
	 * <p>
	 * 仅验证数据类型，音频信息将在首次访问时延迟加载。
	 * </p>
	 *
	 * @param bytes 音频数据字节数组
	 * @throws IOException                  读取数据失败时抛出
	 * @throws UnsupportedResourceException 数据不是音频格式时抛出
	 * @since 1.1.0
	 */
	public AudioResource(byte[] bytes) throws IOException {
		super(bytes);

		validateType("bytes 不是音频数据");
	}

	/**
	 * 从输入流构造音频资源
	 * <p>
	 * 仅验证流类型，音频信息将在首次访问时延迟加载。
	 * </p>
	 *
	 * @param inputStream 音频数据输入流
	 * @throws IOException                  读取流失败时抛出
	 * @throws UnsupportedResourceException 流数据不是音频格式时抛出
	 * @since 1.1.0
	 */
	public AudioResource(InputStream inputStream) throws IOException {
		super(inputStream);

		validateType("inputStream 不是音频输入流");
	}

	/**
	 * 打开 FFmpeg 帧抓取器
	 * <p>
	 * 根据资源类型创建相应的 FFmpegFrameGrabber 实例。
	 * 如果资源来自文件，使用文件路径创建；否则使用缓冲输入流创建。
	 * </p>
	 * <p>
	 * <b>延迟加载：</b>
	 * 如果音频信息尚未加载，此方法将使用双重检查锁定机制
	 * 在同步块中解析音频信息，确保线程安全。
	 * </p>
	 *
	 * @return FFmpeg 帧抓取器实例
	 * @throws IOException                  资源已关闭时抛出
	 * @throws UnsupportedResourceException 资源不存在音频流或读取失败时抛出
	 * @since 1.1.0
	 */
	public FFmpegFrameGrabber openFrameGrabber() throws IOException {
		checkClosed();

		FFmpegFrameGrabber grabber;
		if (Objects.nonNull(file)) {
			grabber = new FFmpegFrameGrabber(file);
		} else {
			grabber = new FFmpegFrameGrabber(newBufferedInputStream());
		}

		synchronized (this) {
			if (Objects.isNull(audio)) {
				try {
					grabber.start();
					if (!grabber.hasAudio()) {
						throw new UnsupportedResourceException("资源不存在音频流");
					}
				} catch (FFmpegFrameGrabber.Exception e) {
					if (Objects.nonNull(file)) {
						throw new UnsupportedResourceException("音频资源读取失败", file, format, mimeType, e);
					} else {
						throw new UnsupportedResourceException("音频资源读取失败", format, mimeType, e);
					}
				}
				audio = Audio.parse(grabber);
			}
		}

		return grabber;
	}

	/**
	 * 验证资源类型
	 * <p>
	 * 检查资源是否为音频格式，如果不是则抛出异常。
	 * </p>
	 *
	 * @param message 验证失败时的错误消息
	 * @throws UnsupportedResourceException 资源不是音频格式时抛出
	 * @since 1.1.0
	 */
	protected void validateType(String message) {
		if (!isAudio()) {
			throw new UnsupportedResourceException(message);
		}
	}

	/**
	 * 获取音频信息
	 * <p>
	 * 如果音频信息尚未加载，将触发延迟加载。
	 * 使用双重检查锁定机制确保线程安全。
	 * </p>
	 *
	 * @return 音频信息对象
	 * @throws IOException                  资源已关闭或读取失败时抛出
	 * @throws UnsupportedResourceException 资源不存在音频流或读取失败时抛出
	 * @since 1.1.0
	 */
	public Audio getAudio() throws IOException {
		checkClosed();

		synchronized (this) {
			if (Objects.nonNull(audio)) {
				return audio;
			}

			try (FFmpegFrameGrabber ignored = openFrameGrabber()) {
				// openFrameGrabber 方法会对audio赋值
			}
			return audio;
		}
	}
}
