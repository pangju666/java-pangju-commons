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

package io.github.pangju666.commons.io.exception;

import java.io.File;
import java.util.Objects;

/**
 * 不支持的资源异常
 * <p>当资源类型与当前处理器不匹配，或资源内容虽具备对应 MIME 类型但实际无法被目标组件读取时抛出。</p>
 *
 * <p>支持在异常消息中附加文件绝对路径，便于定位问题资源。</p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class UnsupportedResourceException extends RuntimeException {
	/**
	 * 使用异常消息创建 UnsupportedResourceException
	 *
	 * @param message 异常消息
	 * @since 1.1.0
	 */
	public UnsupportedResourceException(String message) {
		super(message);
	}

	/**
	 * 使用异常消息和原因创建 UnsupportedResourceException
	 *
	 * @param message 异常消息
	 * @param cause   原始异常
	 * @since 1.1.0
	 */
	public UnsupportedResourceException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * 使用文件和异常消息创建 UnsupportedResourceException
	 * <p>当 {@code file} 不为 {@code null} 时，会自动将文件绝对路径拼接到异常消息中。</p>
	 *
	 * @param file    关联文件，可为 {@code null}
	 * @param message 异常消息
	 * @since 1.1.0
	 */
	public UnsupportedResourceException(File file, String message) {
		super(Objects.nonNull(file) ? message + "，文件路径：" + file.getAbsolutePath() : message);
	}

	/**
	 * 使用文件、异常消息和原因创建 UnsupportedResourceException
	 * <p>当 {@code file} 不为 {@code null} 时，会自动将文件绝对路径拼接到异常消息中。</p>
	 *
	 * @param file    关联文件，可为 {@code null}
	 * @param message 异常消息
	 * @param cause   原始异常
	 * @since 1.1.0
	 */
	public UnsupportedResourceException(File file, String message, Throwable cause) {
		super(Objects.nonNull(file) ? message + "，文件路径：" + file.getAbsolutePath() : message, cause);
	}
}
