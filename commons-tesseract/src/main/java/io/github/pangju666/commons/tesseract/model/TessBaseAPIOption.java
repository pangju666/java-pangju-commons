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

package io.github.pangju666.commons.tesseract.model;

import io.github.pangju666.commons.tesseract.enums.PageSegmentationMode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.tesseract.TessBaseAPI;

import java.util.Objects;

/**
 * TessBaseAPI 配置选项
 * <p>用于配置 Tesseract OCR 识别的各种参数。</p>
 *
 * <p>支持的配置项：</p>
 * <ul>
 *     <li>ppi: 图像分辨率（每英寸像素数）</li>
 *     <li>psm: 页面分割模式</li>
 *     <li>rectLeft/rectTop/rectWidth/rectHeight: 识别区域矩形</li>
 * </ul>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class TessBaseAPIOption {
	/**
	 * 图像分辨率（每英寸像素数）
	 *
	 * @since 2.1.0
	 */
	protected Integer ppi;

	/**
	 * 页面分割模式
	 *
	 * @since 2.1.0
	 */
	protected PageSegmentationMode psm;

	/**
	 * 识别区域左边界坐标
	 *
	 * @since 2.1.0
	 */
	protected Integer rectLeft;

	/**
	 * 识别区域上边界坐标
	 *
	 * @since 2.1.0
	 */
	protected Integer rectTop;

	/**
	 * 识别区域宽度
	 *
	 * @since 2.1.0
	 */
	protected Integer rectWidth;

	/**
	 * 识别区域高度
	 *
	 * @since 2.1.0
	 */
	protected Integer rectHeight;

	/**
	 * 获取图像分辨率
	 *
	 * @return 图像分辨率（每英寸像素数）
	 * @since 2.1.0
	 */
	public Integer getPpi() {
		return ppi;
	}

	/**
	 * 设置图像分辨率
	 * <p>仅接受大于 0 的值，null 或小于等于 0 的值会被忽略。</p>
	 *
	 * @param ppi 图像分辨率（每英寸像素数），必须大于 0
	 * @since 2.1.0
	 */
	public void setPpi(Integer ppi) {
		if (Objects.isNull(ppi)) {
			this.ppi = null;
		} else if (ppi > 0) {
			this.ppi = ppi;
		}
	}

	/**
	 * 获取页面分割模式
	 *
	 * @return 页面分割模式
	 * @since 2.1.0
	 */
	public PageSegmentationMode getPsm() {
		return psm;
	}

	/**
	 * 设置页面分割模式
	 *
	 * @param psm 页面分割模式
	 * @since 2.1.0
	 */
	public void setPsm(PageSegmentationMode psm) {
		this.psm = psm;
	}

	/**
	 * 获取识别区域左边界坐标
	 *
	 * @return 左边界坐标
	 * @since 2.1.0
	 */
	public Integer getRectLeft() {
		return rectLeft;
	}

	/**
	 * 获取识别区域上边界坐标
	 *
	 * @return 上边界坐标
	 * @since 2.1.0
	 */
	public Integer getRectTop() {
		return rectTop;
	}

	/**
	 * 获取识别区域宽度
	 *
	 * @return 识别区域宽度
	 * @since 2.1.0
	 */
	public Integer getRectWidth() {
		return rectWidth;
	}

	/**
	 * 获取识别区域高度
	 *
	 * @return 识别区域高度
	 * @since 2.1.0
	 */
	public Integer getRectHeight() {
		return rectHeight;
	}

	/**
	 * 设置识别区域矩形
	 * <p>仅当所有参数都满足条件时才设置：left 和 top 必须大于等于 0，width 和 height 必须大于 0。</p>
	 *
	 * @param left   左边界坐标，必须大于等于 0
	 * @param top    上边界坐标，必须大于等于 0
	 * @param width  宽度，必须大于 0
	 * @param height 高度，必须大于 0
	 * @since 2.1.0
	 */
	public void setRectangle(int left, int top, int width, int height) {
		if (left >= 0 && top >= 0 && width > 0 && height > 0) {
			this.rectLeft = left;
			this.rectTop = top;
			this.rectWidth = width;
			this.rectHeight = height;
		}
	}

	/**
	 * 清除识别区域矩形
	 * <p>将所有矩形坐标设置为 null，表示使用全图识别。</p>
	 *
	 * @since 2.1.0
	 */
	public void clearRectangle() {
		this.rectLeft = null;
		this.rectTop = null;
		this.rectWidth = null;
		this.rectHeight = null;
	}

	/**
	 * 配置 TessBaseAPI
	 * <p>将当前配置应用到指定的 TessBaseAPI 实例。</p>
	 *
	 * <p>配置顺序：</p>
	 * <ul>
	 *     <li>设置页面分割模式（psm）</li>
	 *     <li>设置图像分辨率（ppi）</li>
	 *     <li>设置识别区域矩形（rectangle）</li>
	 * </ul>
	 *
	 * @param tessBaseAPI 要配置的 TessBaseAPI 实例
	 * @throws NullPointerException     当 tessBaseAPI 为 null 时抛出
	 * @throws IllegalArgumentException 当 tessBaseAPI 为空指针时抛出
	 * @since 2.1.0
	 */
	public void configure(TessBaseAPI tessBaseAPI) {
		Validate.notNull(tessBaseAPI, "tessBaseAPI 不可为 null");
		Validate.isTrue(!tessBaseAPI.isNull(), "tessBaseAPI 不可为 null");

		if (Objects.nonNull(psm)) {
			tessBaseAPI.SetPageSegMode(psm.mode);
		}
		if (Objects.nonNull(ppi)) {
			tessBaseAPI.SetSourceResolution(ppi);
		}
		if (ObjectUtils.allNotNull(rectLeft, rectTop, rectWidth, rectHeight)) {
			tessBaseAPI.SetRectangle(rectLeft, rectTop, rectWidth, rectHeight);
		}
	}
}
