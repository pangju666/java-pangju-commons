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
 * 图像旋转方向枚举。
 * <p>
 * 定义常见的固定角度旋转方向。
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */
public enum RotateDirection {
	/**
	 * 顺时针旋转 90 度（等价于向右转）
	 *
	 * @since 1.0.0
	 */
	CLOCKWISE_90(90),
	/**
	 * 逆时针旋转 90 度（等价于向左转）。
	 *
	 * @since 1.0.0
	 */
	COUNTER_CLOCKWISE_90(-90),
	/**
	 * 旋转 180 度（上下颠倒）
	 *
	 * @since 1.0.0
	 */
	UPSIDE_DOWN(180);

	/**
	 * 角度值（单位：度）。
	 * <p>符号含义：正数顺时针，负数逆时针；用于底层图像处理引擎的旋转参数。</p>
	 *
	 * @since 1.0.0
	 */
	private final double angle;

	RotateDirection(double angle) {
		this.angle = angle;
	}

	/**
	 * 获取角度值。
	 *
	 * @return 角度值（度）
	 * @since 1.0.0
	 */
	public double getAngle() {
		return angle;
	}
}
