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
import io.github.pangju666.commons.ffmpeg.model.OutputOption;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.SeekableByteArrayOutputStream;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * FFmpeg 输出流适配器
 * <p>用于将普通的 {@link OutputStream} 适配为 FFmpeg 可用的输出流。</p>
 *
 * <p>根据输出格式是否需要 seek 操作，本适配器采用不同的处理策略：</p>
 * <ul>
 *   <li>如果格式需要 seek（如 MP4、MOV 等），则使用 {@link SeekableByteArrayOutputStream} 作为中间缓冲区。
 *       若原始 {@link OutputStream} 已经是 {@link SeekableByteArrayOutputStream}，则直接使用；
 *       否则，创建一个新的 {@link SeekableByteArrayOutputStream} 作为内部缓冲区。</li>
 *   <li>如果格式不需要 seek，则直接使用原始 {@link OutputStream}。</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * try (FFmpegOutputStreamAdapter adapter = new FFmpegOutputStreamAdapter(outputStream, "mp4")) {
 *     FFmpegFrameRecorder recorder = adapter.openFFmpegFrameRecorder();
 *     // ...
 * }
 * }</pre>
 *
 * @author pangju666
 * @since 2.1.0
 */
public final class FFmpegOutputStreamAdapter implements Closeable {
	/**
	 * 原始输出流
	 *
	 * @since 2.1.0
	 */
	private final OutputStream outputStream;
	/**
	 * 可查找的字节数组输出流，用作中间缓冲区，
	 * 仅当输出格式需要 seek 且原始输出流不是 SeekableByteArrayOutputStream 时使用。
	 *
	 * @since 2.1.0
	 */
	private final SeekableByteArrayOutputStream seekableByteArrayOutputStream;

	/**
	 * 使用输出流和格式创建适配器
	 *
	 * @param outputStream 输出流
	 * @param format       输出格式（如 mp4、mov 等）
	 * @since 2.1.0
	 */
	public FFmpegOutputStreamAdapter(OutputStream outputStream, String format) {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notBlank(format, "format 不可为 null");

		this.outputStream = outputStream;
		if (FFmpegConstants.REQUIRE_SEEK_OUTPUT_FORMATS.contains(FFmpegUtils.parseFormat(format))) {
			if (outputStream instanceof SeekableByteArrayOutputStream) {
				this.seekableByteArrayOutputStream = (SeekableByteArrayOutputStream) outputStream;
			} else {
				this.seekableByteArrayOutputStream = new SeekableByteArrayOutputStream();
			}
		} else {
			this.seekableByteArrayOutputStream = null;
		}
	}

	/**
	 * 使用输出流和帧抓取器创建适配器
	 * <p>自动从帧抓取器中获取格式信息。</p>
	 *
	 * @param outputStream 输出流
	 * @param grabber      帧抓取器，用于获取格式信息
	 * @throws FFmpegFrameGrabber.Exception 当帧抓取器启动失败时
	 * @since 2.1.0
	 */
	public FFmpegOutputStreamAdapter(OutputStream outputStream, FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(grabber, "grabber 不可为 null");

		if (FFmpegUtils.isNotStarted(grabber)) {
			grabber.start();
		}

		this.outputStream = outputStream;
		if (FFmpegConstants.REQUIRE_SEEK_OUTPUT_FORMATS.contains(FFmpegUtils.parseFormat(grabber.getFormat()))) {
			if (outputStream instanceof SeekableByteArrayOutputStream) {
				this.seekableByteArrayOutputStream = (SeekableByteArrayOutputStream) outputStream;
			} else {
				this.seekableByteArrayOutputStream = new SeekableByteArrayOutputStream();
			}
		} else {
			this.seekableByteArrayOutputStream = null;
		}
	}

	/**
	 * 使用输出流、输出选项和帧抓取器创建适配器
	 * <p>优先使用输出选项中的格式，如果输出选项为空则从帧抓取器中获取格式信息。
	 * 当 outputOption 不为 null 时，不会启动 grabber；当 outputOption 为 null 时，会自动启动 grabber。</p>
	 *
	 * @param outputStream 输出流
	 * @param outputOption 输出选项，可为 null
	 * @param grabber      帧抓取器，用于获取格式信息
	 * @throws FFmpegFrameGrabber.Exception 当帧抓取器启动失败时
	 * @since 2.1.0
	 */
	public FFmpegOutputStreamAdapter(OutputStream outputStream, OutputOption outputOption, FFmpegFrameGrabber grabber) throws FFmpegFrameGrabber.Exception {
		Validate.notNull(outputStream, "outputStream 不可为 null");
		Validate.notNull(grabber, "grabber 不可为 null");

		String format;
		if (Objects.nonNull(outputOption)) {
			format = outputOption.getFormat();
		} else {
			if (FFmpegUtils.isNotStarted(grabber)) {
				grabber.start();
			}
			format = FFmpegUtils.parseFormat(grabber.getFormat());
		}

		this.outputStream = outputStream;
		if (FFmpegConstants.REQUIRE_SEEK_OUTPUT_FORMATS.contains(format)) {
			if (outputStream instanceof SeekableByteArrayOutputStream) {
				this.seekableByteArrayOutputStream = (SeekableByteArrayOutputStream) outputStream;
			} else {
				this.seekableByteArrayOutputStream = new SeekableByteArrayOutputStream();
			}
		} else {
			this.seekableByteArrayOutputStream = null;
		}
	}

	/**
	 * 创建 FFmpeg 帧录制器
	 * <p>如果格式需要 seek，则使用 SeekableByteArrayOutputStream 作为输出，否则使用原始输出流。</p>
	 *
	 * @return 配置好的 FFmpeg 帧录制器
	 * @since 2.1.0
	 */
	public FFmpegFrameRecorder openFFmpegFrameRecorder() {
		return new FFmpegFrameRecorder(ObjectUtils.getIfNull(seekableByteArrayOutputStream,
			outputStream), 0);
	}

	/**
	 * 刷新缓冲区
	 * <p>如果使用了 SeekableByteArrayOutputStream 作为中间缓冲，则将其内容写入原始输出流。</p>
	 *
	 * @throws IOException 当写入失败时
	 * @since 2.1.0
	 */
	public void flushBuffer() throws IOException {
		if (Objects.nonNull(seekableByteArrayOutputStream) && !(outputStream instanceof SeekableByteArrayOutputStream)) {
			outputStream.write(seekableByteArrayOutputStream.toByteArray());
		}
	}

	/**
	 * 关闭适配器
	 * <p>在关闭前自动刷新缓冲区。</p>
	 *
	 * @throws IOException 当刷新或关闭失败时
	 * @since 2.1.0
	 */
	@Override
	public void close() throws IOException {
		flushBuffer();
	}
}
