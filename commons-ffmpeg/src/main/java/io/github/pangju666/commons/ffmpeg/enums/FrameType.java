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

public enum FrameType {
	AUDIO,
	IMAGE,
	VIDEO,
	KEY_FRAME,
	ALL;

	public Frame grabFrame(FFmpegFrameGrabber grabber) throws FrameGrabber.Exception {
		return switch (this) {
			case AUDIO -> grabber.grabSamples();
			case IMAGE -> grabber.grabImage();
			case VIDEO -> grabber.grabFrame(false, true, true, false, true);
			case KEY_FRAME -> grabber.grabKeyFrame();
			case ALL -> grabber.grab();
		};
	}

	public Frame pullFrame(FFmpegFrameFilter filter) throws FFmpegFrameFilter.Exception {
		return switch (this) {
			case AUDIO -> filter.pullSamples();
			case IMAGE -> filter.pullImage();
			default -> filter.pull();
		};
	}
}
