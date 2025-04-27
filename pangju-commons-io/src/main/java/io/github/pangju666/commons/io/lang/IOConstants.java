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

package io.github.pangju666.commons.io.lang;

import org.apache.tika.Tika;

import java.util.Objects;

/**
 * IO相关常量
 *
 * @author pangju666
 * @since 1.0.0
 */
public class IOConstants {
	/**
	 * 任意文件类型
	 *
	 * @since 1.0.0
	 */
	public static final String ANY_MIME_TYPE = "*/*";
	/**
	 * 图像类型MIME前缀（如："image/png"）
	 *
	 * @since 1.0.0
	 */
	public static final String IMAGE_MIME_TYPE_PREFIX = "image/";
	/**
	 * 视频类型MIME前缀（如："video/mp4"）
	 *
	 * @since 1.0.0
	 */
	public static final String VIDEO_MIME_TYPE_PREFIX = "video/";
	/**
	 * 音频类型MIME前缀（如："audio/mpeg"）
	 *
	 * @since 1.0.0
	 */
	public static final String AUDIO_MIME_TYPE_PREFIX = "audio/";
	/**
	 * 模型类型MIME前缀（如："mode/obj"）
	 *
	 * @since 1.0.0
	 */
	public static final String MODEL_MIME_TYPE_PREFIX = "model/";
	/**
	 * 文本类型MIME前缀（如："text/plain"）
	 *
	 * @since 1.0.0
	 */
	public static final String TEXT_MIME_TYPE_PREFIX = "text/";
	/**
	 * 应用类型MIME前缀（如："application/pdf"）
	 *
	 * @since 1.0.0
	 */
	public static final String APPLICATION_MIME_TYPE_PREFIX = "application/";

	/**
	 * 默认Tika实例（双重校验锁实现线程安全单例）
	 *
	 * @since 1.0.0
	 * @see Tika
	 */
	private static volatile Tika DEFAULT_TIKA;

	protected IOConstants() {
	}

	/**
	 * 获取线程安全的Tika单例实例
	 * <p>
	 * 实现特点：
	 * <ul>
	 *     <li>双重校验锁保证线程安全</li>
	 *     <li>延迟初始化节省资源</li>
	 *     <li>同步块粒度最小化保证性能</li>
	 * </ul>
	 *
	 * @return 配置好的Tika实例，用于文件内容类型检测
	 * @since 1.0.0
	 */
	public static Tika getDefaultTika() {
		if (Objects.isNull(DEFAULT_TIKA)) {
			synchronized (IOConstants.class) {
				if (Objects.isNull(DEFAULT_TIKA)) {
					DEFAULT_TIKA = new Tika();
				}
			}
		}
		return DEFAULT_TIKA;
	}
}