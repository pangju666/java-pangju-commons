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

package io.github.pangju666.commons.ffmpeg.io;

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.ffmpeg.model.Media;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.SeekableByteArrayOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * FFmpeg 输出流包装类
 * <p>
 * 继承自 {@link SeekableByteArrayOutputStream}，用于包装原始输出流，根据媒体格式自动决定是否使用缓冲。
 * 某些 FFmpeg 格式（如 MP4、MOV）需要 seek 功能，对于不支持 seek 的输出流，
 * 会自动使用父类的缓冲功能，在关闭时一次性写入底层输出流。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>根据格式自动判断是否需要缓冲</li>
 *     <li>支持多种构造方式（直接输出流、指定格式、指定媒体对象、指定抓取器和媒体）</li>
 *     <li>线程安全，所有写入方法都使用 synchronized 同步</li>
 *     <li>透明包装，使用方式与普通 OutputStream 一致</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 直接包装输出流（默认使用缓冲）
 * FFmpegOutputStream stream = new FFmpegOutputStream(outputStream);
 *
 * // 指定格式（自动判断是否需要缓冲）
 * FFmpegOutputStream stream = new FFmpegOutputStream(outputStream, "mp4");
 *
 * // 指定媒体对象
 * FFmpegOutputStream stream = new FFmpegOutputStream(outputStream, media);
 *
 * // 指定抓取器和媒体对象
 * FFmpegOutputStream stream = new FFmpegOutputStream(outputStream, media, grabber);
 *
 * // 仅指定抓取器（从抓取器获取格式）
 * FFmpegOutputStream stream = new FFmpegOutputStream(outputStream, grabber);
 * }</pre>
 *
 * @author pangju666
 * @see SeekableByteArrayOutputStream
 * @see FFmpegConstants#REQUIRE_SEEK_OUTPUT_FORMATS
 * @since 2.1.0
 */
public final class FFmpegOutputStream extends SeekableByteArrayOutputStream {
	/**
	 * 底层输出流
	 * <p>所有数据最终都会写入此输出流</p>
	 *
	 * @since 2.1.0
	 */
	private final OutputStream outputStream;

	/**
	 * 是否需要 seek 功能
	 * <p>当格式需要 seek 且底层输出流不支持时，使用父类缓冲</p>
	 *
	 * @since 2.1.0
	 */
	private final boolean needSeek;

	/**
	 * 构造函数（默认使用缓冲）
	 * <p>创建 FFmpeg 输出流包装，默认使用 {@link SeekableByteArrayOutputStream} 进行缓冲</p>
	 *
	 * @param outputStream 底层输出流，不可为 null
	 * @throws IllegalArgumentException 当 outputStream 为 null 时抛出
	 * @since 2.1.0
	 */
	public FFmpegOutputStream(OutputStream outputStream) {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		this.outputStream = outputStream;
		this.needSeek = true;
	}

	/**
	 * 构造函数（根据格式判断是否使用缓冲）
	 * <p>
	 * 根据指定的格式判断是否需要 seek 功能：
	 * <ul>
	 *   <li>如果格式在 {@link FFmpegConstants#REQUIRE_SEEK_OUTPUT_FORMATS} 中且底层输出流不支持 seek，则使用缓冲</li>
	 *   <li>否则直接写入底层输出流</li>
	 * </ul>
	 * </p>
	 *
	 * @param outputStream 底层输出流，不可为 null
	 * @param format       媒体格式，不可为空白
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 2.1.0
	 */
	public FFmpegOutputStream(OutputStream outputStream, String format) {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(format, "format 不可为 null");

		this.outputStream = outputStream;
		this.needSeek = FFmpegConstants.REQUIRE_SEEK_OUTPUT_FORMATS.contains(FFmpegUtils.parseFormat(format)) &&
			!(outputStream instanceof SeekableByteArrayOutputStream);
	}

	/**
	 * 构造函数（根据媒体对象判断是否使用缓冲）
	 * <p>
	 * 根据媒体对象的格式判断是否需要 seek 功能，逻辑同 {@link #FFmpegOutputStream(OutputStream, String)}
	 * </p>
	 *
	 * @param outputStream 底层输出流，不可为 null
	 * @param media        媒体对象，不可为 null
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 2.1.0
	 */
	public FFmpegOutputStream(OutputStream outputStream, Media media) {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(media, "media 不可为 null");

		this.outputStream = outputStream;
		String format = FFmpegUtils.parseFormat(media.getFormat());
		this.needSeek = FFmpegConstants.REQUIRE_SEEK_OUTPUT_FORMATS.contains(format) &&
			!(outputStream instanceof SeekableByteArrayOutputStream);
	}

	/**
	 * 构造函数（根据抓取器判断是否使用缓冲）
	 * <p>
	 * 从抓取器中获取格式并判断是否需要 seek 功能。
	 * 如果抓取器未启动，会自动启动它。
	 * </p>
	 *
	 * @param outputStream 底层输出流，不可为 null
	 * @param grabber      FFmpeg 帧抓取器，不可为 null
	 * @throws IllegalArgumentException     当参数无效时抛出
	 * @throws FFmpegFrameGrabber.Exception 当 grabber 启动失败时抛出
	 * @since 2.1.0
	 */
	public FFmpegOutputStream(OutputStream outputStream, FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(grabber, "grabber 不可为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		this.outputStream = outputStream;
		String format = FFmpegUtils.parseFormat(grabber.getFormat());
		this.needSeek = FFmpegConstants.REQUIRE_SEEK_OUTPUT_FORMATS.contains(format) &&
			!(outputStream instanceof SeekableByteArrayOutputStream);
	}

	/**
	 * 构造函数（根据抓取器和媒体对象判断是否使用缓冲）
	 * <p>
	 * 如果 media 不为 null，使用 media 的格式；否则从 grabber 中获取格式。
	 * 如果 grabber 未启动，会自动启动它。
	 * </p>
	 *
	 * @param outputStream 底层输出流，不可为 null
	 * @param media        媒体对象，可为 null（为 null 时从 grabber 获取格式）
	 * @param grabber      FFmpeg 帧抓取器，不可为 null
	 * @throws IllegalArgumentException     当参数无效时抛出
	 * @throws FFmpegFrameGrabber.Exception 当 grabber 启动失败时抛出
	 * @since 2.1.0
	 */
	public FFmpegOutputStream(OutputStream outputStream, Media media, FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(grabber, "grabber 不可为 null");

		String format;
		if (Objects.nonNull(media)) {
			format = FFmpegUtils.parseFormat(media.getFormat());
		} else {
			if (FFmpegUtils.isNotStarted(grabber)) {
				grabber.start();
			}
			format = FFmpegUtils.parseFormat(grabber.getFormat());
		}

		this.outputStream = outputStream;
		this.needSeek = FFmpegConstants.REQUIRE_SEEK_OUTPUT_FORMATS.contains(format) &&
			!(outputStream instanceof SeekableByteArrayOutputStream);
	}

	/**
	 * 写入单个字节
	 * <p>如果需要缓冲，写入父类缓冲区；否则直接写入底层输出流</p>
	 * <p>此方法是线程安全的</p>
	 *
	 * @param b 要写入的字节
	 * @since 2.1.0
	 */
	@Override
	public synchronized void write(int b) {
		if (needSeek) {
			super.write(b);
		} else {
			try {
				outputStream.write(b);
			} catch (IOException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
	}

	/**
	 * 写入字节数组
	 * <p>如果需要缓冲，写入父类缓冲区；否则直接写入底层输出流</p>
	 * <p>此方法是线程安全的</p>
	 *
	 * @param b 要写入的字节数组
	 * @since 2.1.0
	 */
	@Override
	public synchronized void write(byte[] b) throws IOException {
		if (needSeek) {
			super.write(b);
		} else {
			outputStream.write(b);
		}
	}

	/**
	 * 写入字节数组的一部分
	 * <p>如果需要缓冲，写入父类缓冲区；否则直接写入底层输出流</p>
	 * <p>此方法是线程安全的</p>
	 *
	 * @param b   要写入的字节数组
	 * @param off 起始偏移量
	 * @param len 要写入的长度
	 * @since 2.1.0
	 */
	@Override
	public synchronized void write(byte[] b, int off, int len) {
		if (needSeek) {
			super.write(b, off, len);
		} else {
			try {
				outputStream.write(b, off, len);
			} catch (IOException e) {
				throw ExceptionUtils.asRuntimeException(e);
			}
		}
	}

	/**
	 * 刷新输出流
	 * <p>如果需要缓冲，先将缓冲区内容一次性写入底层输出流，然后刷新底层输出流</p>
	 * <p>此方法是线程安全的</p>
	 *
	 * @since 2.1.0
	 */
	@Override
	public synchronized void flush() throws IOException {
		if (needSeek) {
			byte[] bytes = toByteArray();
			outputStream.write(bytes);
		}
		outputStream.flush();
	}

	/**
	 * 关闭输出流
	 * <p>
	 * 如果需要缓冲，将缓冲区内容一次性写入底层输出流。
	 * 注意：此方法不会关闭底层输出流，由调用者负责关闭。
	 * </p>
	 * <p>此方法是线程安全的</p>
	 *
	 * @since 2.1.0
	 */
	@Override
	public synchronized void close() throws IOException {
		if (needSeek) {
			byte[] bytes = toByteArray();
			outputStream.write(bytes);
		}
	}
}
