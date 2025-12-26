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

package io.github.pangju666.commons.image.enums;

/**
 * 图像翻转方向枚举。
 * <p>
 * 定义图像翻转的两种方向：水平（左右镜像）与垂直（上下镜像）。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum FlipDirection {
	/**
	 * 水平翻转（左右镜像）。
	 *
	 * @since 1.0.0
	 */
	HORIZONTAL(1),
	/**
	 * 垂直翻转（上下镜像）。
	 *
	 * @since 1.0.0
	 */
	VERTICAL(-1);

	/**
	 * 翻转轴
	 *
	 * @since 1.0.0
	 */
	private final int axis;

	FlipDirection(int axis) {
		this.axis = axis;
	}

	/**
	 * 获取翻转轴。
	 *
	 * @return 翻转轴
	 * @since 1.0.0
	 */
	public int getAxis() {
		return axis;
	}
}
