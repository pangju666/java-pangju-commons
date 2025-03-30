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
 * IO相关常量工具类
 * <p>提供以下类型常量的集中管理：</p>
 * <ul>
 *     <li><strong>MIME类型前缀</strong> - 常见文件类型的MIME前缀常量（image/, video/ 等）</li>
 *     <li><strong>缓冲区配置</strong> - 内存映射和文件通道的默认缓冲区尺寸</li>
 *     <li><strong>AES加密规范</strong> - 包含密钥长度、算法模式、填充方案等完整参数体系</li>
 *     <li><strong>文件检测工具</strong> - 集成Tika内容类型检测的线程安全实现</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class IOConstants {
	/**
	 * 图片类型MIME前缀（如："image/png"）
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
	 * 流缓冲区默认大小（单位：字节）
	 *
	 * @since 1.0.0
	 */
	public static final int DEFAULT_STREAM_BUFFER_SIZE = 8192;

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