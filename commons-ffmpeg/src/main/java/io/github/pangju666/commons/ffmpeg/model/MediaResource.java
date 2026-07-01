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

package io.github.pangju666.commons.ffmpeg.model;

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.UUID;

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
 * MediaResource resource = MediaResource.of(new File("video.mp4"));
 *
 * // 从字节数组创建
 * byte[] data = Files.readAllBytes(Paths.get("audio.wav"));
 * MediaResource resource = MediaResource.of(data);
 *
 * // 从输入流创建
 * try (InputStream is = new FileInputStream("media.mp3")) {
 *     MediaResource resource = MediaResource.of(is);
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
 * }</pre>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class MediaResource {
	/**
	 * 临时文件前缀，用于区分程序生成的媒体临时文件
	 * <p>
	 * 格式：media-resource-加上UUID，可通过此前缀统一清理临时文件。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	public static final String TMP_FILE_PREFIX = "media-resource-";

	/**
	 * 媒体原始来源对象
	 * <p>
	 * 实际类型仅可能为以下三者之一：
	 * <ul>
	 *     <li>{@link File} - 本地文件来源</li>
	 *     <li>byte[] - 内存字节数组来源</li>
	 *     <li>{@link InputStream} - 可重复读取的输入流来源</li>
	 * </ul>
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected final Object source;

	/**
	 * 媒体资源总大小（单位：字节）
	 * <p>
	 * 该值在实例创建时即确定，不会随源数据变化而更新。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected final long size;

	/**
	 * 媒体 MIME 类型
	 * <p>
	 * 通过 Apache Tika 自动检测获得，格式如：
	 * <ul>
	 *     <li>audio/mpeg - MP3 音频</li>
	 *     <li>audio/wav - WAV 音频</li>
	 *     <li>video/mp4 - MP4 视频</li>
	 *     <li>video/x-msvideo - AVI 视频</li>
	 * </ul>
	 * </p>
	 *
	 * @since 1.1.0
	 */
	protected final String mimeType;

	/**
	 * 私有构造函数，内部专用
	 * <p>直接通过原始源对象、资源大小和 MIME 类型创建实例，主要供工厂方法调用</p>
	 *
	 * @param source 媒体源对象（仅支持 File、byte[]、InputStream 类型）
	 * @param size 资源大小（单位：字节）
	 * @param mimeType 媒体 MIME 类型
	 * @since 1.1.0
	 */
	protected MediaResource(Object source, long size, String mimeType) {
		this.source = source;
		this.size = size;
		this.mimeType = mimeType;
	}

	/**
	 * 从本地文件创建媒体资源实例
	 * <p>
	 * 直接使用文件作为源，不进行内存拷贝，适合大文件场景。
	 * 文件大小和 MIME 类型会在创建时一次性检测并缓存。
	 * </p>
	 *
	 * @param file 媒体文件，<b>不可为 null</b>，必须为有效文件（存在且非目录）
	 * @return 封装后的 MediaResource 实例
	 * @throws IOException              文件读取、MIME 类型检测失败时抛出
	 * @throws IllegalArgumentException 入参为空/非有效文件时抛出
	 * @since 1.1.0
	 */
	public static MediaResource of(final File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		return new MediaResource(file, file.length(), FileUtils.getMimeType(file));
	}

	/**
	 * 从字节数组创建媒体资源实例
	 * <p>
	 * 直接使用字节数组作为源，无需额外缓冲，适合已加载到内存的小到中等规模媒体数据。
	 * MIME 类型会通过 Apache Tika 自动检测。
	 * </p>
	 *
	 * @param bytes 媒体字节数组，<b>不可为 null</b>，长度可以为 0
	 * @return 封装后的 MediaResource 实例
	 * @throws IllegalArgumentException 入参为 null 时抛出
	 * @since 1.1.0
	 */
	public static MediaResource of(final byte[] bytes) {
		Validate.notNull(bytes, "bytes 不可为 null");

		return new MediaResource(bytes, bytes.length, IOConstants.getDefaultTika().detect(bytes));
	}

	/**
	 * 从输入流创建媒体资源实例
	 * <p>
	 * 自动对流进行处理，确保返回的媒体资源支持重复读取：
	 * <ol>
	 *     <li><strong>原生可重置流</strong>：{@link ByteArrayInputStream} 或 {@link UnsynchronizedByteArrayInputStream}，直接复用并重置流指针</li>
	 *     <li><strong>其他流</strong>：先包装为缓冲流，再完全缓冲为内存字节流</li>
	 * </ol>
	 * </p>
	 * <p>
	 * <strong>重要提示</strong>：原始输入流会被本方法完全消费；仅当流为非原生可重置流时，流会被自动关闭。如需保留原流，请提前对其进行拷贝。
	 * </p>
	 *
	 * @param inputStream 媒体输入流，<b>不可为 null</b>
	 * @return 封装后的 MediaResource 实例，内部包含可重复读取的流
	 * @throws IOException              流读取、缓冲、MIME 类型检测失败时抛出
	 * @throws IllegalArgumentException 入参为 null 时抛出
	 * @since 1.1.0
	 */
	public static MediaResource of(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (inputStream instanceof ByteArrayInputStream || inputStream instanceof UnsynchronizedByteArrayInputStream) {
			MediaResource resource = new MediaResource(inputStream, inputStream.available(),
				IOConstants.getDefaultTika().detect(inputStream));

			inputStream.reset();
			return resource;
		}

		try (UnsynchronizedBufferedInputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
			UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(bufferedInputStream);
			InputStream tmpInputStream = outputStream.toInputStream();

			MediaResource resource = new MediaResource(tmpInputStream, outputStream.size(),
				IOConstants.getDefaultTika().detect(tmpInputStream));

			tmpInputStream.reset();
			return resource;
		}
	}

	/**
	 * 将媒体资源转换为本地文件
	 * <p>
	 * 非 File 类型源会自动生成 <b>临时文件</b>，临时文件前缀：{@value #TMP_FILE_PREFIX}；<br />
	 * <b>重要：使用完毕请手动删除临时文件，避免磁盘空间占用</b>。
	 * </p>
	 * <p>
	 * 转换规则：
	 * <ul>
	 *     <li>File 源：直接返回原文件对象</li>
	 *     <li>byte[] 源：写入临时文件后返回</li>
	 *     <li>InputStream 源：完全消费流数据写入临时文件后返回</li>
	 * </ul>
	 * </p>
	 *
	 * @return 媒体对应的文件对象；未知资源类型返回 {@code null}
	 * @throws IOException 临时文件创建、内容写入失败时抛出
	 * @since 1.1.0
	 */
	public File getFile() throws IOException {
		if (source instanceof File file) {
			return file;
		} else if (source instanceof byte[] bytes) {
			File tempFile = File.createTempFile(TMP_FILE_PREFIX + UUID.randomUUID(), null);
			FileUtils.writeByteArrayToFile(tempFile, bytes);
			return tempFile;
		} else if (source instanceof InputStream inputStream) {
			File tempFile = File.createTempFile(TMP_FILE_PREFIX + UUID.randomUUID(), null);
			inputStream.reset();
			FileUtils.copyInputStreamToFile(inputStream, tempFile);
			return tempFile;
		} else {
			return null;
		}
	}

	/**
	 * 获取媒体资源对应的输入流
	 * <p>所有来源均保证返回可重复读取的流</p>
	 * <p>
	 * 转换规则：
	 * <ul>
	 *     <li>File 源：每次调用返回新的文件输入流</li>
	 *     <li>byte[] 源：每次调用返回新的 {@link ByteArrayInputStream}</li>
	 *     <li>InputStream 源：直接返回内部缓冲的可重复读取流</li>
	 * </ul>
	 * </p>
	 *
	 * @return 媒体输入流；未知资源类型返回 {@code null}
	 * @throws IOException 文件流打开失败时抛出
	 * @since 1.1.0
	 */
	public InputStream getInputStream() throws IOException {
		if (source instanceof File file) {
			return FileUtils.openInputStream(file);
		} else if (source instanceof byte[] bytes) {
			return new ByteArrayInputStream(bytes);
		} else if (source instanceof InputStream inputStream) {
			return inputStream;
		} else {
			return null;
		}
	}

	/**
	 * 获取媒体资源完整字节数组
	 * <p>
	 * <b>注意：基于 InputStream 的资源会完全消费流数据</b>，大数据量谨慎使用。
	 * </p>
	 * <p>
	 * 转换规则：
	 * <ul>
	 *     <li>File 源：读取整个文件到内存</li>
	 *     <li>byte[] 源：直接返回原字节数组</li>
	 *     <li>InputStream 源：完全消费流数据并返回字节数组</li>
	 * </ul>
	 * </p>
	 *
	 * @return 媒体字节数组；未知资源类型返回 {@code null}
	 * @throws IOException 读取文件/流失败时抛出
	 * @since 1.1.0
	 */
	public byte[] getBytes() throws IOException {
		if (source instanceof File file) {
			return FileUtils.readFileToByteArray(file);
		} else if (source instanceof byte[] bytes) {
			return bytes;
		} else if (source instanceof InputStream inputStream) {
			byte[] bytes = inputStream.readAllBytes();
			inputStream.reset();
			return bytes;
		} else {
			return null;
		}
	}

	/**
	 * 获取媒体 MIME 类型
	 * <p>该值在实例创建时已通过 Apache Tika 检测并缓存</p>
	 *
	 * @return MIME 类型字符串，例如：audio/wav、video/mp4；检测失败可能返回 null
	 * @since 1.1.0
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * 获取媒体资源总大小
	 * <p>该值在实例创建时已计算并缓存，不会随源数据变化而更新</p>
	 *
	 * @return 资源大小（单位：字节）
	 * @since 1.1.0
	 */
	public long getSize() {
		return size;
	}

	/**
	 * 判断是否为音频资源
	 * <p>通过检查 MIME 类型是否以 "audio/" 开头来判断</p>
	 *
	 * @return true = 音频资源，false = 非音频或 MIME 类型未知
	 * @since 1.1.0
	 */
	public boolean isAudio() {
		return Strings.CS.startsWith(mimeType, IOConstants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断是否为视频资源
	 * <p>通过检查 MIME 类型是否以 "video/" 开头来判断</p>
	 *
	 * @return true = 视频资源，false = 非视频或 MIME 类型未知
	 * @since 1.1.0
	 */
	public boolean isVideo() {
		return Strings.CS.startsWith(mimeType, IOConstants.VIDEO_MIME_TYPE_PREFIX);
	}

	public boolean isImage() {
		return Strings.CS.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	public boolean isSubtitles() {
		return Strings.CS.equals(mimeType, FFmpegConstants.SRT_MIME_TYPE) ||
			Strings.CS.startsWith(mimeType, IOConstants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断媒体资源是否来自本地文件
	 *
	 * @return true = 本地文件来源，false = 字节数组或输入流来源
	 * @since 1.1.0
	 */
	public boolean isFile() {
		return Objects.nonNull(source) && source instanceof File;
	}
}
