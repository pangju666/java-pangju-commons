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

package io.github.pangju666.commons.image.lang;

import javax.imageio.ImageIO;
import java.util.Objects;
import java.util.Set;

/**
 * 图像处理相关常量
 *
 * @author pangju666
 * @since 1.0.0
 */
public class ImageConstants {
	/**
	 * 不支持透明通道的图像格式集合
	 *
	 * @see java.awt.image.BufferedImage#TYPE_INT_RGB
	 * @since 1.0.0
	 */
	public static volatile Set<String> NON_TRANSPARENT_IMAGE_FORMATS = Set.of("jpeg", "jpg", "jpeg-lossless", "bmp", "gif");
	/**
	 * 系统支持的可读取图像MIME类型集合（懒加载）
	 * <p>通过ImageIO获取系统注册的可用图像格式</p>
	 * <p>使用双重检查锁实现线程安全初始化</p>
	 *
	 * @since 1.0.0
	 */
	private static volatile Set<String> SUPPORT_READ_IMAGE_TYPES;
	/**
	 * 系统支持的可写入图像MIME类型集合（懒加载）
	 * <p>通过ImageIO获取系统注册的可用图像格式</p>
	 * <p>使用双重检查锁实现线程安全初始化</p>
	 *
	 * @since 1.0.0
	 */
	private static volatile Set<String> SUPPORT_WRITE_IMAGE_TYPES;

	/**
	 * 获取系统支持的可读取图像MIME类型集合
	 * <p>首次调用时初始化集合，后续直接返回缓存结果</p>
	 * <p>线程安全实现特性：</p>
	 * <ul>
	 *   <li>使用volatile保证可见性</li>
	 *   <li>双重检查锁保证初始化安全性</li>
	 *   <li>返回不可变集合保证数据安全</li>
	 * </ul>
	 *
	 * @return ImageIO支持的可读取图像MIME类型集合
	 * @see javax.imageio.ImageIO#getReaderMIMETypes()
	 * @since 1.0.0
	 */
	public static Set<String> getSupportReadImageTypes() {
		if (Objects.isNull(SUPPORT_READ_IMAGE_TYPES)) {
			synchronized (ImageConstants.class) {
				if (Objects.isNull(SUPPORT_READ_IMAGE_TYPES)) {
					SUPPORT_READ_IMAGE_TYPES = Set.of(ImageIO.getReaderMIMETypes());
				}
			}
		}
		return SUPPORT_READ_IMAGE_TYPES;
	}

	/**
	 * 获取系统支持的可写入图像MIME类型集合
	 * <p>首次调用时初始化集合，后续直接返回缓存结果</p>
	 * <p>线程安全实现特性：</p>
	 * <ul>
	 *   <li>使用volatile保证可见性</li>
	 *   <li>双重检查锁保证初始化安全性</li>
	 *   <li>返回不可变集合保证数据安全</li>
	 * </ul>
	 *
	 * @return ImageIO支持的可写入图像MIME类型集合
	 * @see javax.imageio.ImageIO#getReaderMIMETypes()
	 * @since 1.0.0
	 */
	public static Set<String> getSupportWriteImageTypes() {
		if (Objects.isNull(SUPPORT_WRITE_IMAGE_TYPES)) {
			synchronized (ImageConstants.class) {
				if (Objects.isNull(SUPPORT_WRITE_IMAGE_TYPES)) {
					SUPPORT_WRITE_IMAGE_TYPES = Set.of(ImageIO.getWriterMIMETypes());
				}
			}
		}
		return SUPPORT_WRITE_IMAGE_TYPES;
	}
}
