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

package io.github.pangju666.commons.ffmpeg.enums;

import org.bytedeco.javacv.FFmpegFrameFilter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

/**
 * 帧类型枚举，用于区分音频帧、视频帧或全部帧
 * <p>
 * 提供针对不同帧类型的抓取和拉取方法，用于 FFmpeg 帧处理操作。
 * </p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public enum FrameType {
	/**
	 * 音频帧类型
	 *
	 * @since 1.1.0
	 */
	AUDIO,

	/**
	 * 视频帧类型
	 *
	 * @since 1.1.0
	 */
	VIDEO,

	/**
	 * 全部帧类型（包含音频和视频）
	 *
	 * @since 1.1.0
	 */
	ALL;

	/**
	 * 从 FFmpegFrameGrabber 中抓取对应类型的帧
	 *
	 * @param grabber FFmpeg 帧抓取器，不可为 null
	 * @return 抓取到的 Frame 对象，可能为 null
	 * @throws FrameGrabber.Exception 抓取过程中发生异常时抛出
	 * @since 1.1.0
	 */
	public Frame grabFrame(FFmpegFrameGrabber grabber) throws FrameGrabber.Exception {
		return switch (this) {
			case AUDIO -> grabber.grabSamples();
			case VIDEO -> grabber.grabFrame(false, true, true, false, true);
			case ALL -> grabber.grab();
		};
	}

	/**
	 * 从 FFmpegFrameFilter 中拉取对应类型的帧
	 *
	 * @param filter FFmpeg 帧过滤器，不可为 null
	 * @return 拉取到的 Frame 对象，可能为 null
	 * @throws FFmpegFrameFilter.Exception 拉取过程中发生异常时抛出
	 * @since 1.1.0
	 */
	public Frame pullFrame(FFmpegFrameFilter filter) throws FFmpegFrameFilter.Exception {
		if (this == AUDIO) {
			return filter.pullSamples();
		}
		return filter.pull();
	}
}
