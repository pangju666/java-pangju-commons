/*
 *   Copyright 2025 pangju666
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

package io.github.pangju666.commons.opencv.enums;

import org.bytedeco.opencv.global.opencv_core;

public enum RotateDirection {
	CLOCKWISE_90(opencv_core.ROTATE_90_CLOCKWISE),
	COUNTER_CLOCKWISE_90(opencv_core.ROTATE_90_COUNTERCLOCKWISE),
	UPSIDE_DOWN(opencv_core.ROTATE_180);

	private final int code;

	RotateDirection(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}
}
