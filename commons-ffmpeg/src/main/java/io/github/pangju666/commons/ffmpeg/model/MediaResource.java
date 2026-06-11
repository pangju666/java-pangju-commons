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

package io.github.pangju666.commons.ffmpeg.model;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.io.output.UnsynchronizedByteArrayOutputStream;

import java.io.*;
import java.util.UUID;

/**
 * 媒体资源封装类，统一处理 File、byte[]、InputStream 三种类型的媒体资源，
 * 提供类型转换、MIME 类型检测等能力，适配 FFmpeg 媒体处理场景。
 *
 * @author pangju666
 * @since 1.1.0
 */
public class MediaResource {
	/**
	 * 媒体资源来源，支持 File、byte[]、InputStream 三种类型
	 *
	 * @since 1.1.0
	 */
	protected Object source;
	/**
	 * 媒体资源的 MIME 类型（如 video/mp4、audio/mpeg 等）
	 *
	 * @since 1.1.0
	 */
	protected String mimeType;

	protected MediaResource() {
	}

	/**
	 * 从 {@link File} 对象创建 MediaResource 实例
	 *
	 * @param file 媒体文件对象，不能为空
	 * @return 封装后的 MediaResource 实例
	 * @throws IOException 文件读取失败或 MIME 类型检测失败时抛出
	 * @since 1.1.0
	 */
	public static MediaResource of(File file) throws IOException {
		MediaResource mediaResource = new MediaResource();
		mediaResource.source = file;
		mediaResource.mimeType = FileUtils.getMimeType(file);
		return mediaResource;
	}

	/**
	 * 从字节数组创建 MediaResource 实例
	 *
	 * @param bytes 媒体资源的字节数组，不能为空
	 * @return 封装后的 MediaResource 实例
	 * @since 1.1.0
	 */
	public static MediaResource of(byte[] bytes) {
		MediaResource mediaResource = new MediaResource();
		mediaResource.source = bytes;
		mediaResource.mimeType = IOConstants.getDefaultTika().detect(bytes);
		return mediaResource;
	}

	/**
	 * 从 {@link InputStream} 创建 MediaResource 实例
	 * <p>
	 * 为保证流可重复读取，会将输入流转换为内存中的字节数组流：
	 * 1. 若为 {@link ByteArrayInputStream}/{@link UnsynchronizedByteArrayInputStream}，直接复用并重置流指针
	 * 2. 其他类型流会先缓冲为字节数组，再转换为 {@link ByteArrayInputStream}
	 * </p>
	 *
	 * @param inputStream 媒体资源的输入流，不能为空
	 * @return 封装后的 MediaResource 实例
	 * @throws IOException 流读取失败或 MIME 类型检测失败时抛出
	 * @since 1.1.0
	 */
	public static MediaResource of(InputStream inputStream) throws IOException {
		MediaResource mediaResource = new MediaResource();
		if (inputStream instanceof ByteArrayInputStream || inputStream instanceof UnsynchronizedByteArrayInputStream) {
			mediaResource.source = inputStream;
			mediaResource.mimeType = IOConstants.getDefaultTika().detect(inputStream);
			inputStream.reset();
			return mediaResource;
		}

		if (inputStream instanceof BufferedInputStream ||
			inputStream instanceof UnsynchronizedBufferedInputStream) {
			UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(inputStream);
			mediaResource.source = outputStream.toInputStream();
			mediaResource.mimeType = IOConstants.getDefaultTika().detect(outputStream.toInputStream());
			return mediaResource;
		} else {
			try (UnsynchronizedBufferedInputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
				UnsynchronizedByteArrayOutputStream outputStream = IOUtils.toUnsynchronizedByteArrayOutputStream(bufferedInputStream);
				mediaResource.source = outputStream.toInputStream();
				mediaResource.mimeType = IOConstants.getDefaultTika().detect(outputStream.toInputStream());
				return mediaResource;
			}
		}
	}

	/**
	 * 获取媒体资源对应的 File 对象
	 * <p>
	 * 若原始源不是 File，会创建临时文件存储内容：
	 * <ul>
	 * <li>1. byte[] 类型：将字节写入临时文件</li>
	 * <li>2. InputStream 类型：将流内容复制到临时文件</li>
	 * </ul>
	 * </p>
	 *
	 * @return 媒体资源对应的 File 对象，若源类型不支持则返回 null
	 * @throws IOException 创建临时文件、写入文件失败时抛出
	 * @since 1.1.0
	 */
	public File getFile() throws IOException {
		if (source instanceof File file) {
			return file;
		} else if (source instanceof byte[] bytes) {
			File tmpFile = File.createTempFile(UUID.randomUUID().toString(), null);
			FileUtils.writeByteArrayToFile(tmpFile, bytes);
			return tmpFile;
		} else if (source instanceof InputStream inputStream) {
			File tmpFile = File.createTempFile(UUID.randomUUID().toString(), null);
			FileUtils.copyInputStreamToFile(inputStream, tmpFile);
			return tmpFile;
		} else {
			return null;
		}
	}

	/**
	 * 获取媒体资源对应的 InputStream 输入流
	 * <p>
	 * <ul>
	 * 不同源类型的转换规则：
	 * <li>1. File 类型：打开文件输入流</li>
	 * <li>2. byte[] 类型：创建 ByteArrayInputStream</li>
	 * <li>3. InputStream 类型：直接返回原始流</li>
	 * </ul>
	 * </p>
	 *
	 * @return 媒体资源对应的输入流，若源类型不支持则返回 null
	 * @throws IOException 文件流打开失败时抛出
	 * @since 1.1.0
	 */
	public InputStream getInputStream() throws IOException {
		if (source instanceof File file) {
			return FileUtils.openInputStream(file);
		} else if (source instanceof byte[] bytes) {
			return new ByteArrayInputStream(bytes);
		} else if (source instanceof InputStream inputStream) {
			return inputStream;
		} else {
			return null;
		}
	}

	/**
	 * 获取媒体资源的字节数组
	 * <p>
	 * 不同源类型的转换规则：
	 * <ul>
	 * <li>1. File 类型：读取文件所有字节</li>
	 * <li>2. byte[] 类型：直接返回原始字节数组</li>
	 * <li>3. InputStream 类型：读取流所有字节（注意：流会被消费）</li>
	 * </ul>
	 * </p>
	 *
	 * @return 媒体资源的字节数组，若源类型不支持则返回 null
	 * @throws IOException 读取文件/流失败时抛出
	 * @since 1.1.0
	 */
	public byte[] getBytes() throws IOException {
		if (source instanceof File file) {
			return FileUtils.readFileToByteArray(file);
		} else if (source instanceof byte[] bytes) {
			return bytes;
		} else if (source instanceof InputStream inputStream) {
			return inputStream.readAllBytes();
		} else {
			return null;
		}
	}

	/**
	 * 获取媒体资源的 MIME 类型
	 * <p>
	 * MIME 类型通过 Tika 检测，不同源类型的检测规则：
	 * <ul>
	 * <li>1. File：基于文件扩展名/内容检测</li>
	 * <li>2. byte[]：基于字节内容检测</li>
	 * <li>3. InputStream：基于流内容检测</li>
	 * </p>
	 *
	 * @return 媒体资源的 MIME 类型，如 video/mp4、audio/mpeg 等
	 * @since 1.1.0
	 */
	public String getMimeType() {
		return mimeType;
	}
}
