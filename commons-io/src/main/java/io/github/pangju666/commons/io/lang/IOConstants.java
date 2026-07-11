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

import net.openhft.hashing.LongHashFunction;
import org.apache.tika.Tika;
import org.apache.tika.mime.MimeTypes;

import java.util.Objects;

/**
 * IO相关常量
 * <p>
 * 提供IO操作中常用的常量定义，包括：
 * <ul>
 *   <li>MIME类型前缀常量：图像、视频、音频、模型、文本、应用等类型前缀</li>
 *   <li>摘要相关常量：默认采样字节数、哈希函数、摘要格式等</li>
 *   <li>线程安全单例：Tika和MimeTypes实例，用于文件内容类型和MIME类型检测</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.0.0
 */

public class IOConstants {
	/**
	 * 默认采样字节数
	 *
	 * @since 1.1.0
	 */
	public static final int DEFAULT_SAMPLE_SIZE = 64;
	/**
	 * 默认64 位 xxHash 函数
	 * <p>用于快速计算摘要，兼顾性能与较低碰撞率。</p>
	 *
	 * @since 1.1.0
	 */
	public static final LongHashFunction DEFAULT_HASH_FUNC = LongHashFunction.xx();
	/**
	 * 空摘要固定值
	 *
	 * @since 1.1.0
	 */
	public static final String EMPTY_DIGEST = "0000000000000000";
	/**
	 * 摘要输出格式
	 * <p>使用 16 位十六进制、左侧 0 填充（`%016x`）。</p>
	 *
	 * @since 1.1.0
	 */
	public static final String DIGEST_FORMAT = "%016x";
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
	 * @see Tika
	 * @since 1.0.0
	 */
	private static volatile Tika DEFAULT_TIKA;

	/**
	 * 默认MimeTypes实例（双重校验锁实现线程安全单例）
	 *
	 * @see MimeTypes
	 * @since 1.1.0
	 */
	private static volatile MimeTypes DEFAULT_MIME_TYPES;

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

	/**
	 * 获取线程安全的MimeTypes单例实例
	 * <p>
	 * 实现特点：
	 * <ul>
	 *     <li>双重校验锁保证线程安全</li>
	 *     <li>延迟初始化节省资源</li>
	 *     <li>同步块粒度最小化保证性能</li>
	 * </ul>
	 *
	 * @return 配置好的MimeTypes实例，用于文件MIME类型检测
	 * @since 1.1.0
	 */
	public static MimeTypes getDefaultMimeTypes() {
		if (Objects.isNull(DEFAULT_MIME_TYPES)) {
			synchronized (IOConstants.class) {
				if (Objects.isNull(DEFAULT_MIME_TYPES)) {
					DEFAULT_MIME_TYPES = MimeTypes.getDefaultMimeTypes();
				}
			}
		}
		return DEFAULT_MIME_TYPES;
	}
}