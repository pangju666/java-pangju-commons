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
 * OCR引擎模式枚举
 * <p>定义Tesseract OCR引擎的不同运行模式。</p>
 *
 * <p>模式说明：</p>
 * <ul>
 *     <li>TESSERACT_ONLY: 仅使用传统Tesseract引擎</li>
 *     <li>LSTM_ONLY: 仅使用LSTM神经网络引擎</li>
 *     <li>TESSERACT_AND_LSTM: 同时使用传统引擎和LSTM引擎</li>
 *     <li>DEFAULT: 默认模式，根据版本自动选择</li>
 * </ul>
 *
 * @author pangju666
 * @see <a href="https://github.com/tesseract-ocr/tesseract/blob/main/doc/tesseract.1.asc">官方文档</a>
 * @since 1.1.0
 */
public enum OcrEngineMode {
	/**
	 * 仅使用传统Tesseract引擎
	 *
	 * @since 1.1.0
	 */
	TESSERACT_ONLY(0),
	/**
	 * 仅使用LSTM神经网络引擎
	 *
	 * @since 1.1.0
	 */
	LSTM_ONLY(1),
	/**
	 * 同时使用传统引擎和LSTM引擎
	 *
	 * @since 1.1.0
	 */
	TESSERACT_AND_LSTM(2),
	/**
	 * 默认模式
	 *
	 * @since 1.1.0
	 */
	DEFAULT(3);

	/**
	 * 模式值
	 */
	public final int mode;

	OcrEngineMode(int mode) {
		this.mode = mode;
	}
}
