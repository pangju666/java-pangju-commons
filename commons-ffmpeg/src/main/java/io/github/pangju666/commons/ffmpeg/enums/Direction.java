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

package io.github.pangju666.commons.ffmpeg.enums;

/**
 * 方向枚举，定义九宫格位置。
 *
 * @author pangju666
 * @since 2.1.0
 */
public enum Direction {
	/**
	 * 左上角，紧贴上边与左边。
	 *
	 * @since 2.1.0
	 */
	TOP_LEFT,
	/**
	 * 顶部居中，紧贴上边并水平居中。
	 *
	 * @since 2.1.0
	 */
	TOP,
	/**
	 * 右上角，紧贴上边与右边。
	 *
	 * @since 2.1.0
	 */
	TOP_RIGHT,
	/**
	 * 右侧居中，紧贴右边并垂直居中。
	 *
	 * @since 2.1.0
	 */
	RIGHT,
	/**
	 * 中心位置，水平与垂直居中。
	 *
	 * @since 2.1.0
	 */
	CENTER,
	/**
	 * 左侧居中，紧贴左边并垂直居中。
	 *
	 * @since 2.1.0
	 */
	LEFT,
	/**
	 * 底部居中，紧贴下边并水平居中。
	 *
	 * @since 2.1.0
	 */
	BOTTOM,
	/**
	 * 右下角，紧贴下边与右边。
	 *
	 * @since 2.1.0
	 */
	BOTTOM_RIGHT,
	/**
	 * 左下角，紧贴下边与左边。
	 *
	 * @since 2.1.0
	 */
	BOTTOM_LEFT
}