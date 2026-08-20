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

package io.github.pangju666.commons.compress.utils;

import io.github.pangju666.commons.compress.io.resource.LZ4Resource;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.BlockLZ4CompressorOutputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorInputStream;
import org.apache.commons.compress.compressors.lz4.FramedLZ4CompressorOutputStream;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.lang3.Validate;

import java.io.*;

/**
 * LZ4 压缩/解压工具类。
 * <p>面向单文件或流数据的高速压缩格式（不包含归档目录结构）。基于 Apache Commons Compress 的
 * {@link FramedLZ4CompressorInputStream} 与 {@link FramedLZ4CompressorOutputStream} 实现，
 * 采用标准的 Framed LZ4 帧格式。</p>
 *
 * <h3>核心特性</h3>
 * <ul>
 *   <li><strong>超高速压缩</strong>：LZ4 以极快的压缩和解压速度著称，适合对性能要求高的场景。</li>
 *   <li><strong>多输入与输出</strong>：支持 {@link IOResource} 与 {@link InputStream} 输入，输出到 {@link OutputStream} 或 {@link File}。</li>
 *   <li><strong>自定义参数</strong>：支持通过 {@link FramedLZ4CompressorOutputStream.Parameters} 配置压缩块大小等参数。</li>
 *   <li><strong>性能优化</strong>：广泛使用缓冲与 {@link InputStream#transferTo(OutputStream)}。</li>
 *   <li><strong>资源管理</strong>：采用 try-with-resources 自动释放内部创建的包装流。</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>类无共享状态，方法均为静态；并发处理不同文件/流是安全的。对同一路径或同一输出目标并发写入可能产生冲突。</p>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1) 压缩输入流到输出流（默认 Framed LZ4 格式）
 * try (InputStream in = new FileInputStream("input.txt");
 *      OutputStream out = new FileOutputStream("output.lz4")) {
 *     LZ4Utils.compress(in, out);
 * }
 *
 * // 2) 压缩并使用自定义压缩参数
 * FramedLZ4CompressorOutputStream.Parameters params =
 *     new FramedLZ4CompressorOutputStream.Parameters(FramedLZ4CompressorOutputStream.BlockSize.K64);
 * try (InputStream in = new FileInputStream("input.txt");
 *      OutputStream out = new FileOutputStream("output.lz4")) {
 *     LZ4Utils.compress(in, out, params);
 * }
 *
 * // 3) 压缩 IOResource 到文件（父目录自动创建）
 * LZ4Utils.compress(IOResource.of(new File("input.txt")), new File("output.lz4"));
 *
 * // 4) 使用 LZ4Resource 解压到输出流
 * try (LZ4Resource resource = new LZ4Resource(new File("data.lz4"));
 *      OutputStream out = new FileOutputStream("output.txt")) {
 *     LZ4Utils.uncompress(resource, out);
 * }
 *
 * // 5) 解压 LZ4Resource 到文件
 * try (LZ4Resource resource = new LZ4Resource(new File("data.lz4"))) {
 *     LZ4Utils.uncompress(resource, new File("output.txt"));
 * }
 * }</pre>
 *
 * @author pangju666
 * @see FramedLZ4CompressorInputStream
 * @see FramedLZ4CompressorOutputStream
 * @see BlockLZ4CompressorOutputStream
 * @see LZ4Resource
 * @since 2.1.0
 */
public class LZ4Utils {
	/**
	 * 受保护的构造函数，防止实例化。
	 *
	 * @since 2.1.0
	 */
	protected LZ4Utils() {
	}

	/**
	 * 将输入流压缩为 Framed LZ4 格式并写入到输出流（使用默认压缩参数）。
	 * <p>
	 * - 当 {@code outputStream} 已是 {@link FramedLZ4CompressorOutputStream} 或
	 * {@link BlockLZ4CompressorOutputStream} 时，方法不会关闭该对象，
	 * 仅调用 {@link CompressorOutputStream#finish()}。<br>
	 * - 当方法内部创建包装流（如缓冲流、压缩流）时，这些包装流会在方法结束时关闭，
	 * 可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputStream  待压缩的输入流，必须非 null
	 * @param outputStream 目标输出流，必须非 null
	 * @throws NullPointerException 当 {@code inputStream} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误或归档完成时抛出
	 * @since 2.1.0
	 */
	public static void compress(final InputStream inputStream, final OutputStream outputStream) throws IOException {
		compress(inputStream, outputStream, FramedLZ4CompressorOutputStream.Parameters.DEFAULT);
	}

	/**
	 * 将输入流压缩为 Framed LZ4 格式并写入到输出流（指定压缩参数）。
	 * <p>
	 * - 当 {@code outputStream} 已是 {@link FramedLZ4CompressorOutputStream} 或
	 * {@link BlockLZ4CompressorOutputStream} 时，方法不会关闭该对象，
	 * 仅调用 {@link CompressorOutputStream#finish()}。<br>
	 * - 当方法内部创建包装流（如缓冲流、压缩流）时，这些包装流会在方法结束时关闭，
	 * 可能导致底层输出流被关闭。
	 * </p>
	 *
	 * @param inputStream  待压缩的输入流，必须非 null
	 * @param outputStream 目标输出流，必须非 null
	 * @param parameters   Framed LZ4 压缩参数（块大小、校验等），必须非 null
	 * @throws NullPointerException 当 {@code inputStream}、{@code outputStream} 或 {@code parameters} 为 null 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误或归档完成时抛出
	 * @since 2.1.0
	 */
	public static void compress(final InputStream inputStream, final OutputStream outputStream,
								final FramedLZ4CompressorOutputStream.Parameters parameters) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof FramedLZ4CompressorOutputStream || outputStream instanceof BlockLZ4CompressorOutputStream) {
			if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
				inputStream.transferTo(outputStream);
			} else {
				try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
					bufferedInputStream.transferTo(outputStream);
				}
			}
			((CompressorOutputStream<?>) outputStream).finish();
		} else {
			try (BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream);
				 FramedLZ4CompressorOutputStream compressorOutputStream =
					 new FramedLZ4CompressorOutputStream(bufferedOutputStream, parameters)) {
				if (inputStream instanceof BufferedInputStream || inputStream instanceof UnsynchronizedBufferedInputStream) {
					inputStream.transferTo(compressorOutputStream);
				} else {
					try (InputStream bufferedInputStream = IOUtils.unsynchronizedBuffer(inputStream)) {
						bufferedInputStream.transferTo(compressorOutputStream);
					}
				}
				compressorOutputStream.finish();
			}
		}
	}

	/**
	 * 压缩 IOResource 到输出流（使用默认压缩参数）。
	 * <p>从 IOResource 读取数据并压缩为 Framed LZ4 格式写入输出流。方法会自动关闭资源打开的输入流。</p>
	 *
	 * @param resource     待压缩的 IOResource，必须非 null
	 * @param outputStream 目标输出流，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final OutputStream outputStream) throws IOException {
		compress(resource, outputStream, FramedLZ4CompressorOutputStream.Parameters.DEFAULT);
	}

	/**
	 * 压缩 IOResource 到输出流（指定压缩参数）。
	 * <p>从 IOResource 读取数据并压缩为 Framed LZ4 格式写入输出流。方法会自动关闭资源打开的输入流。</p>
	 *
	 * @param resource     待压缩的 IOResource，必须非 null
	 * @param outputStream 目标输出流，必须非 null
	 * @param parameters   Framed LZ4 压缩参数，必须非 null
	 * @throws NullPointerException 当 {@code resource}、{@code outputStream} 或 {@code parameters} 为 null 时抛出
	 * @throws IOException          当读取/写入发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final OutputStream outputStream,
								final FramedLZ4CompressorOutputStream.Parameters parameters) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (InputStream inputStream = resource.newBufferedInputStream()) {
			compress(inputStream, outputStream, parameters);
		}
	}

	/**
	 * 压缩输入流到指定文件（使用默认压缩参数）。
	 * <p>将输入流数据压缩为 Framed LZ4 格式写入目标文件。自动创建目标文件的父目录；
	 * 若目标文件已存在且为非文件类型（如目录），将抛出异常。</p>
	 *
	 * @param inputStream 待压缩的输入流，必须非 null
	 * @param outputFile  目标文件，若已存在则必须为文件类型；父目录会自动创建
	 * @throws NullPointerException     当 {@code inputStream} 或 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputFile} 已存在但不是一个文件时抛出
	 * @throws IOException              当读取/写入发生 I/O 错误或创建目录失败时抛出
	 * @since 2.1.0
	 */
	public static void compress(final InputStream inputStream, final File outputFile) throws IOException {
		compress(inputStream, outputFile, FramedLZ4CompressorOutputStream.Parameters.DEFAULT);
	}

	/**
	 * 压缩输入流到指定文件（指定压缩参数）。
	 * <p>将输入流数据压缩为 Framed LZ4 格式写入目标文件。自动创建目标文件的父目录；
	 * 若目标文件已存在且为非文件类型（如目录），将抛出异常。</p>
	 *
	 * @param inputStream 待压缩的输入流，必须非 null
	 * @param outputFile  目标文件，若已存在则必须为文件类型；父目录会自动创建
	 * @param parameters  Framed LZ4 压缩参数，必须非 null
	 * @throws NullPointerException     当 {@code inputStream}、{@code outputFile} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputFile} 已存在但不是一个文件时抛出
	 * @throws IOException              当读取/写入发生 I/O 错误或创建目录失败时抛出
	 * @since 2.1.0
	 */
	public static void compress(final InputStream inputStream, final File outputFile,
								final FramedLZ4CompressorOutputStream.Parameters parameters) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			compress(inputStream, bufferedOutputStream, parameters);
		}
	}

	/**
	 * 压缩 IOResource 到指定文件（使用默认压缩参数）。
	 * <p>从 IOResource 读取数据并压缩为 Framed LZ4 格式写入目标文件。自动创建目标文件的父目录。
	 * 方法会自动关闭资源打开的输入流和内部创建的输出流。</p>
	 *
	 * @param resource   待压缩的 IOResource，必须非 null
	 * @param outputFile 目标文件，若已存在则必须为文件类型；父目录会自动创建
	 * @throws NullPointerException     当 {@code resource} 或 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputFile} 已存在但不是一个文件时抛出
	 * @throws IOException              当读取/写入发生 I/O 错误或创建目录失败时抛出
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final File outputFile) throws IOException {
		compress(resource, outputFile, FramedLZ4CompressorOutputStream.Parameters.DEFAULT);
	}

	/**
	 * 压缩 IOResource 到指定文件（指定压缩参数）。
	 * <p>从 IOResource 读取数据并压缩为 Framed LZ4 格式写入目标文件。自动创建目标文件的父目录。
	 * 方法会自动关闭资源打开的输入流和内部创建的输出流。</p>
	 *
	 * @param resource   待压缩的 IOResource，必须非 null
	 * @param outputFile 目标文件，若已存在则必须为文件类型；父目录会自动创建
	 * @param parameters Framed LZ4 压缩参数，必须非 null
	 * @throws NullPointerException     当 {@code resource}、{@code outputFile} 或 {@code parameters} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputFile} 已存在但不是一个文件时抛出
	 * @throws IOException              当读取/写入发生 I/O 错误或创建目录失败时抛出
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final File outputFile,
								final FramedLZ4CompressorOutputStream.Parameters parameters) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (InputStream inputStream = resource.newBufferedInputStream();
			 BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			compress(inputStream, bufferedOutputStream, parameters);
		}
	}

	/**
	 * 解压 LZ4Resource 到输出流（使用 Framed LZ4 格式）。
	 * <p>从 LZ4Resource 读取 Framed LZ4 压缩数据并解压，将解压后的数据写入目标输出流。
	 * 方法使用缓冲输出流提高写入性能，并会自动关闭压缩输入流。</p>
	 *
	 * @param resource     LZ4 格式资源，必须非 null
	 * @param outputStream 解压目标输出流，必须非 null
	 * @throws NullPointerException 当 {@code resource} 或 {@code outputStream} 为 null 时抛出
	 * @throws IOException          当读取压缩数据或写入解压数据发生 I/O 错误时抛出
	 * @since 2.1.0
	 */
	public static void uncompress(final LZ4Resource resource, final OutputStream outputStream) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		Validate.notNull(outputStream, "outputStream 不可为 null");

		try (FramedLZ4CompressorInputStream compressorInputStream = resource.openFramedLZ4CompressorInputStream();
			 BufferedOutputStream bufferedOutputStream = IOUtils.buffer(outputStream)) {
			compressorInputStream.transferTo(bufferedOutputStream);
		}
	}

	/**
	 * 解压 LZ4Resource 到指定文件（使用 Framed LZ4 格式）。
	 * <p>从 LZ4Resource 读取 Framed LZ4 压缩数据并解压，将解压后的数据写入目标文件。
	 * 自动创建目标文件的父目录；若目标文件已存在且为非文件类型（如目录），将抛出异常。</p>
	 *
	 * @param resource   LZ4 格式资源，必须非 null
	 * @param outputFile 解压目标文件，若已存在则必须为文件类型；父目录会自动创建
	 * @throws NullPointerException     当 {@code resource} 或 {@code outputFile} 为 null 时抛出
	 * @throws IllegalArgumentException 当 {@code outputFile} 已存在但不是一个文件时抛出
	 * @throws IOException              当读取压缩数据、写入解压数据或创建目录失败时抛出
	 * @since 2.1.0
	 */
	public static void uncompress(final LZ4Resource resource, final File outputFile) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");
		FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");

		FileUtils.forceMkdirParent(outputFile);

		try (FramedLZ4CompressorInputStream compressorInputStream = resource.openFramedLZ4CompressorInputStream();
			 BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(outputFile)) {
			compressorInputStream.transferTo(bufferedOutputStream);
		}
	}
}
