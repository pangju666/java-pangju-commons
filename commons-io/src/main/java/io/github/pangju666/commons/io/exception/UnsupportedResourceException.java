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

import org.apache.commons.lang3.StringUtils;

import java.io.File;

/**
 * 不支持的资源异常
 * <p>当资源类型与当前处理器不匹配，或资源内容虽具备对应 MIME 类型但实际无法被目标组件读取时抛出。</p>
 *
 * <p>支持在异常消息中附加文件绝对路径，便于定位问题资源。</p>
 *
 * @author pangju666
 * @since 2.1.0
 */
public class UnsupportedResourceException extends RuntimeException {
	/**
	 * 使用异常消息、MIME 类型和格式创建 UnsupportedResourceException
	 *
	 * @param message  异常消息
	 * @param mimeType MIME 类型
	 * @param format   资源格式
	 * @since 2.1.0
	 */
	public UnsupportedResourceException(String message, String mimeType, String format) {
		super(String.format("%s，格式：%s，类型：%s", message, StringUtils.defaultString(mimeType),
			StringUtils.defaultString(format)));
	}

	/**
	 * 使用异常消息、文件、MIME 类型和格式创建 UnsupportedResourceException
	 *
	 * @param message  异常消息
	 * @param file     文件对象
	 * @param mimeType MIME 类型
	 * @param format   资源格式
	 * @since 2.1.0
	 */
	public UnsupportedResourceException(String message, File file, String mimeType, String format) {
		super(String.format("%s，文件路径：%s，格式：%s，类型：%s", message,
			StringUtils.defaultString(file.getAbsolutePath()), StringUtils.defaultString(mimeType),
			StringUtils.defaultString(format)));
	}

	/**
	 * 使用异常消息、MIME 类型、格式和原始异常创建 UnsupportedResourceException
	 *
	 * @param message  异常消息
	 * @param mimeType MIME 类型
	 * @param format   资源格式
	 * @param cause    原始异常
	 * @since 2.1.0
	 */
	public UnsupportedResourceException(String message, String mimeType, String format, Throwable cause) {
		super(String.format("%s，格式：%s，类型：%s", message, StringUtils.defaultString(mimeType),
			StringUtils.defaultString(format)), cause);
	}

	/**
	 * 使用异常消息、文件、MIME 类型、格式和原始异常创建 UnsupportedResourceException
	 *
	 * @param message  异常消息
	 * @param file     文件对象
	 * @param mimeType MIME 类型
	 * @param format   资源格式
	 * @param cause    原始异常
	 * @since 2.1.0
	 */
	public UnsupportedResourceException(String message, File file, String mimeType, String format, Throwable cause) {
		super(String.format("%s，文件路径：%s，格式：%s，类型：%s", message,
			StringUtils.defaultString(file.getAbsolutePath()), StringUtils.defaultString(mimeType),
			StringUtils.defaultString(format)), cause);
	}

	/**
	 * 使用异常消息创建 UnsupportedResourceException
	 *
	 * @param message 异常消息
	 * @since 2.1.0
	 */
	public UnsupportedResourceException(String message) {
		super(message);
	}

	/**
	 * 使用异常消息和原因创建 UnsupportedResourceException
	 *
	 * @param message 异常消息
	 * @param cause   原始异常
	 * @since 2.1.0
	 */
	public UnsupportedResourceException(String message, Throwable cause) {
		super(message, cause);
	}
}
