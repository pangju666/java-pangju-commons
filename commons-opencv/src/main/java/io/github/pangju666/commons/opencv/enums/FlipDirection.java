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

package io.github.pangju666.commons.opencv.enums;

/**
 * 图像翻转方向枚举
 *
 * <p>提供 OpenCV 图像翻转操作的方向常量，支持水平和垂直两种翻转方式。</p>
 * <p>用于 {@code opencv_core.flip()} 方法的参数。</p>
 *
 * <h2>翻转说明</h2>
 * <ul>
 *   <li>{@link #HORIZONTAL}：沿垂直轴水平翻转（镜像左右）</li>
 *   <li>{@link #VERTICAL}：沿水平轴垂直翻转（镜像上下）</li>
 * </ul>
 *
 * @author pangju666
 * @see org.bytedeco.opencv.global.opencv_core#flip
 * @since 1.1.0
 */
public enum FlipDirection {
	/**
	 * 水平翻转（沿垂直轴）
	 *
	 * <p>对应 OpenCV 的 flipCode = 1，图像左右镜像</p>
	 *
	 * @since 1.1.0
	 */
	HORIZONTAL(1),

	/**
	 * 垂直翻转（沿水平轴）
	 *
	 * <p>对应 OpenCV 的 flipCode = -1，图像上下镜像</p>
	 *
	 * @since 1.1.0
	 */
	VERTICAL(-1);

	/**
	 * OpenCV 内部翻转代码
	 */
	private final int code;

	/**
	 * 私有构造函数
	 *
	 * @param code OpenCV 翻转代码
	 * @since 1.1.0
	 */
	FlipDirection(int code) {
		this.code = code;
	}

	/**
	 * 获取 OpenCV 内部翻转代码
	 *
	 * @return 翻转代码值
	 * @since 1.1.0
	 */
	public int getCode() {
		return code;
	}
}
