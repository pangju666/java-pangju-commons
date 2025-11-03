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

package io.github.pangju666.commons.compress.lang;

/**
 * 文件压缩相关常量
 *
 * @author pangju666
 * @since 1.0.0
 */
public class CompressConstants {
	/**
	 * zip压缩文件MIME类型
	 *
	 * @since 1.0.0
	 */
	public static final String ZIP_MIME_TYPE = "application/zip";
	/**
	 * 7z压缩文件MIME类型
	 *
	 * @since 1.0.0
	 */
	public static final String SEVEN_Z_MIME_TYPE = "application/x-7z-compressed";
	/**
	 * 压缩文件路径分隔符
	 *
	 * @since 1.0.0
	 */
	public static final String PATH_SEPARATOR = "/";

	protected CompressConstants() {
	}
}
