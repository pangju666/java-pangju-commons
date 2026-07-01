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

package io.github.pangju666.commons.tesseract.lang;

import io.github.pangju666.commons.tesseract.factory.TessBaseAPIFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.bytedeco.tesseract.TessBaseAPI;

import java.io.IOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Set;

/**
 * Tesseract 相关常量类
 * <p>
 * 该类定义了 OCR 功能所需的常量配置，包括：
 * <ul>
 *   <li>Tesseract支持的图片类型和文件格式</li>
 *   <li>TessBaseAPI 对象池的默认配置</li>
 *   <li>单例的 TessBaseAPI 对象池</li>
 * </ul>
 * </p>
 *
 * @author pangju666
 * @since 1.1.0
 */
public class TesseractConstants {
	/**
	 * Tesseract支持的图片 MIME 类型
	 * <p>
	 * 包含了 Tesseract OCR 引擎支持识别的各种图片格式的 MIME 类型，
	 * 用于校验输入的图片资源是否为支持的类型。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of(
		"image/png", "image/jpeg", "image/jp2", "image/tiff", "image/gif", "image/webp", "image/bmp", "image/x-portable-anymap"
	);

	/**
	 * Tesseract支持的图片文件扩展名
	 * <p>
	 * 包含了 Tesseract OCR 引擎支持识别的各种图片格式的文件扩展名，
	 * 用于校验图片文件的后缀名是否为支持的格式。
	 * 所有扩展名均为小写形式。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_IMAGE_FILE_FORMATS = Set.of(
		"png", "jpg", "jpeg", "jp2", "jpf", "tiff", "tif", "gif", "webp", "bmp", "pnm"
	);

	/**
	 * {@link TessBaseAPI} 对象池的默认配置
	 * <p>
	 * 基于 Apache Commons Pool2 的 GenericObjectPoolConfig 实现，
	 * 为 TessBaseAPI 对象池提供优化的默认配置参数。
	 * 配置基于可用 CPU 核心数自动调整，最大总实例数和最大空闲实例数都等于 CPU 核心数。
	 * <p>
	 * 主要配置包括：
	 * <ul>
	 *   <li>最大总实例数：等于CPU核心数（CPU密集型OCR优化）</li>
	 *   <li>最大空闲实例数：等于CPU核心数</li>
	 *   <li>最小常驻空闲实例：0（减少资源占用）</li>
	 *   <li>无可用实例时等待时间：3秒（快速失败，避免长时间等待）</li>
	 *   <li>空闲实例回收时间：5分钟未使用（内存管控）</li>
	 *   <li>空闲实例扫描间隔：1分钟</li>
	 *   <li>借出前校验实例有效性：false（提升性能）</li>
	 *   <li>归还后校验实例有效性：false（提升性能）</li>
	 *   <li>定时空闲校验：true（兜底失效实例，不影响主流程性能）</li>
	 *   <li>池耗尽时阻塞请求：true（对象池默认行为）</li>
	 * </ul>
	 * </p>
	 *
	 * @since 1.1.0
	 */
	public static final GenericObjectPoolConfig<TessBaseAPI> DEFAULT_TESS_BASE_API_POOL_CONFIG = new GenericObjectPoolConfig<>();

	/**
	 * 单例的 {@link TessBaseAPI} 对象池
	 * <p>
	 * 使用双重检查锁定模式实现的线程安全单例对象池，
	 * 基于 {@link #DEFAULT_TESS_BASE_API_POOL_CONFIG} 配置创建。
	 * 该对象池会在首次调用 {@link #getDefaultTessBaseApiPool} 方法时初始化。
	 * </p>
	 *
	 * @since 1.1.0
	 */
	private static volatile GenericObjectPool<TessBaseAPI> DEFAULT_TESS_BASE_API_POOL;

	static {
		int cpuCoreCount = Runtime.getRuntime().availableProcessors();
		// 最大总实例数：CPU密集型OCR，常规服务器推荐8
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMaxTotal(cpuCoreCount);
		// 最大空闲实例数
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMaxIdle(cpuCoreCount);
		// 最小常驻空闲实例（服务预热）
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMinIdle(0);

		// 无可用实例时，等待1分钟后超时（防止线程堆积）
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMaxWait(Duration.ofSeconds(3));
		// 空闲实例60分钟未使用则回收（常规内存管控）
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setMinEvictableIdleDuration(Duration.ofMinutes(5));
		// 每30秒执行一次空闲实例扫描淘汰
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setTimeBetweenEvictionRuns(Duration.ofMinutes(1));
		// 关闭软空闲时间，只用固定时长驱逐
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setSoftMinEvictableIdleDuration(null);

		// 借出前校验实例有效性
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setTestOnBorrow(false);
		// 归还后不额外校验，提升性能
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setTestOnReturn(false);
		// 定时空闲校验，兜底失效实例，不影响主流程性能
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setTestWhileIdle(true);
		// 池耗尽时阻塞请求（对象池默认行为）
		DEFAULT_TESS_BASE_API_POOL_CONFIG.setBlockWhenExhausted(true);
	}

	protected TesseractConstants() {
	}

	/**
	 * 获取默认的 {@link TessBaseAPI} 对象池
	 * <p>
	 * 使用双重检查锁定模式实现的线程安全单例方法，
	 * 首次调用时初始化对象池，后续调用直接返回已初始化的对象池。
	 * 对象池基于 {@link #DEFAULT_TESS_BASE_API_POOL_CONFIG} 配置创建。
	 * </p>
	 *
	 * @return 初始化后的 TessBaseAPI 对象池单例
	 * @throws RuntimeException 当对象池初始化失败时抛出，原因为 IOException
	 * @since 1.1.0
	 */
	public static GenericObjectPool<TessBaseAPI> getDefaultTessBaseApiPool() {
		if (Objects.isNull(DEFAULT_TESS_BASE_API_POOL)) {
			synchronized (TesseractConstants.class) {
				if (Objects.isNull(DEFAULT_TESS_BASE_API_POOL)) {
					try {
						DEFAULT_TESS_BASE_API_POOL = new GenericObjectPool<>(new TessBaseAPIFactory(),
							TesseractConstants.DEFAULT_TESS_BASE_API_POOL_CONFIG);
					} catch (IOException e) {
						throw ExceptionUtils.asRuntimeException(e);
					}
				}
			}
		}
		return DEFAULT_TESS_BASE_API_POOL;
	}
}
