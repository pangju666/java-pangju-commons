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

/**
 * 图像旋转方向枚举
 *
 * <p>提供 OpenCV 图像旋转操作的方向常量，支持 90 度顺时针、90 度逆时针和 180 度旋转三种方式。</p>
 * <p>用于 {@code opencv_core.rotate()} 方法的参数。</p>
 *
 * <h2>旋转说明</h2>
 * <ul>
 *   <li>{@link #CLOCKWISE_90}：顺时针旋转 90 度</li>
 *   <li>{@link #COUNTER_CLOCKWISE_90}：逆时针旋转 90 度</li>
 *   <li>{@link #UPSIDE_DOWN}：旋转 180 度（上下颠倒）</li>
 * </ul>
 *
 * @author pangju666
 * @see org.bytedeco.opencv.global.opencv_core#rotate
 * @since 1.1.0
 */
public enum RotateDirection {
	/**
	 * 顺时针旋转 90 度
	 *
	 * <p>对应 OpenCV 的 ROTATE_90_CLOCKWISE</p>
	 *
	 * @since 1.1.0
	 */
	CLOCKWISE_90(opencv_core.ROTATE_90_CLOCKWISE),

	/**
	 * 逆时针旋转 90 度
	 *
	 * <p>对应 OpenCV 的 ROTATE_90_COUNTERCLOCKWISE</p>
	 *
	 * @since 1.1.0
	 */
	COUNTER_CLOCKWISE_90(opencv_core.ROTATE_90_COUNTERCLOCKWISE),

	/**
	 * 旋转 180 度（上下颠倒）
	 *
	 * <p>对应 OpenCV 的 ROTATE_180</p>
	 *
	 * @since 1.1.0
	 */
	UPSIDE_DOWN(opencv_core.ROTATE_180);

	/**
	 * OpenCV 内部旋转代码
	 */
	private final int code;

	/**
	 * 私有构造函数
	 *
	 * @param code OpenCV 旋转代码
	 * @since 1.1.0
	 */
	RotateDirection(int code) {
		this.code = code;
	}

	/**
	 * 获取 OpenCV 内部旋转代码
	 *
	 * @return 旋转代码值
	 * @since 1.1.0
	 */
	public int getCode() {
		return code;
	}
}
