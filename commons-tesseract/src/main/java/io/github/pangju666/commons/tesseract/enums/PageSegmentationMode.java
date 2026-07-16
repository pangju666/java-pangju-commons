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

package io.github.pangju666.commons.tesseract.enums;

/**
 * 页面分割模式枚举
 * <p>定义Tesseract OCR的页面分割模式，控制如何分析和分割图像文本。</p>
 *
 * <p>模式说明：</p>
 * <ul>
 *     <li>OSD_ONLY: 仅进行方向和脚本检测</li>
 *     <li>AUTO_WITH_OSD: 自动页面分割，包含OSD</li>
 *     <li>AUTO_NO_OSD: 自动页面分割，不包含OSD</li>
 *     <li>FULL_AUTO: 完全自动模式</li>
 *     <li>SINGLE_COLUMN: 单列文本</li>
 *     <li>SINGLE_BLOCK_VERTICAL: 单块垂直文本</li>
 *     <li>SINGLE_BLOCK: 单块文本</li>
 *     <li>SINGLE_LINE: 单行文本</li>
 *     <li>SINGLE_WORD: 单词</li>
 *     <li>SINGLE_WORD_CIRCLE: 圆形单词</li>
 *     <li>SINGLE_CHAR: 单个字符</li>
 *     <li>SPARSE_TEXT: 稀疏文本</li>
 *     <li>SPARSE_TEXT_WITH_OSD: 稀疏文本，包含OSD</li>
 *     <li>RAW_LINE: 原始行</li>
 * </ul>
 *
 * @author pangju666
 * @see <a href="https://github.com/tesseract-ocr/tesseract/blob/main/doc/tesseract.1.asc">官方文档</a>
 * @since 1.1.0
 */
public enum PageSegmentationMode {
	/**
	 * 仅进行方向和脚本检测（Orientation and Script Detection）
	 *
	 * @since 1.1.0
	 */
	OSD_ONLY(0),
	/**
	 * 自动页面分割，包含OSD
	 *
	 * @since 1.1.0
	 */
	AUTO_WITH_OSD(1),
	/**
	 * 自动页面分割，不包含OSD
	 *
	 * @since 1.1.0
	 */
	AUTO_NO_OSD(2),
	/**
	 * 完全自动模式（等同于AUTO_NO_OSD）
	 *
	 * @since 1.1.0
	 */
	FULL_AUTO(3),
	/**
	 * 单列文本
	 *
	 * @since 1.1.0
	 */
	SINGLE_COLUMN(4),
	/**
	 * 单块垂直文本
	 *
	 * @since 1.1.0
	 */
	SINGLE_BLOCK_VERTICAL(5),
	/**
	 * 单块文本
	 *
	 * @since 1.1.0
	 */
	SINGLE_BLOCK(6),
	/**
	 * 单行文本
	 *
	 * @since 1.1.0
	 */
	SINGLE_LINE(7),
	/**
	 * 单词
	 *
	 * @since 1.1.0
	 */
	SINGLE_WORD(8),
	/**
	 * 圆形单词
	 *
	 * @since 1.1.0
	 */
	SINGLE_WORD_CIRCLE(9),
	/**
	 * 单个字符
	 *
	 * @since 1.1.0
	 */
	SINGLE_CHAR(10),
	/**
	 * 稀疏文本
	 *
	 * @since 1.1.0
	 */
	SPARSE_TEXT(11),
	/**
	 * 稀疏文本，包含OSD
	 *
	 * @since 1.1.0
	 */
	SPARSE_TEXT_WITH_OSD(12),
	/**
	 * 原始行
	 *
	 * @since 1.1.0
	 */
	RAW_LINE(13);

	/**
	 * 模式值
	 */
	public final int mode;

	PageSegmentationMode(int mode) {
		this.mode = mode;
	}
}
