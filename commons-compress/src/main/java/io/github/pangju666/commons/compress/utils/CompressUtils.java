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

package io.github.pangju666.commons.compress.utils;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;

/**
 * 综合压缩/解压分发工具。
 * <p>
 * 根据输出或输入文件扩展名，将调用分发到具体实现：
 * {@link GZipUtils}、{@link XZUtils}、{@link SevenZUtils}、{@link ZipUtils}、{@link TarUtils}，
 * 并支持组合格式 <code>tgz</code>/<code>tar.gz</code>（通过 TAR 打包再 GZIP 压缩的串联方式实现）。
 * </p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li>格式分发：基于文件扩展名进行分发，不进行内容嗅探；如需严格的格式校验请使用各具体工具的 <code>is*</code> 方法。</li>
 *   <li>组合格式支持：<code>tgz</code>/<code>tar.gz</code> 通过中间 TAR 文件再 GZIP 压缩实现，临时 TAR 文件会在完成后删除。</li>
 *   <li>多输入源：支持单文件、目录（递归）、以及文件集合（集合仅支持归档格式）。</li>
 *   <li>资源管理：方法内部创建的流会在方法内正确关闭；调用方传入的 <code>OutputStream</code> 不会被自动关闭。</li>
 *   <li>异常一致性：不支持的格式抛出 {@link UnsupportedOperationException}；底层 IO 失败抛出 {@link IOException}。</li>
 *   <li>线程安全：类本身无共享状态；并发写入同一路径可能产生冲突。</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 1) 压缩目录为 ZIP
 * CompressUtils.compress(new File("inputDir"), new File("archive.zip"));
 *
 * // 2) 压缩目录为 TGZ（先 TAR，再 GZIP）
 * CompressUtils.compress(new File("inputDir"), new File("archive.tgz"));
 *
 * // 3) 批量压缩多个文件为 TAR
 * List<File> files = List.of(new File("a.txt"), new File("b.txt"));
 * CompressUtils.compress(files, new File("bundle.tar"));
 *
 * // 4) 解压 GZ 到文件
 * CompressUtils.uncompress(new File("file.txt.gz"), new File("file.txt"));
 *
 * // 5) 解压 ZIP 到目录
 * CompressUtils.uncompressToDir(new File("archive.zip"), new File("outputDir"));
 *
 * // 6) 解压 TAR.GZ 到目录
 * CompressUtils.uncompressToDir(new File("archive.tar.gz"), new File("outputDir"));
 *
 * // 7) 将 XZ 解压到输出流（输出流由调用方关闭）
 * try (OutputStream out = new FileOutputStream("out.bin")) {
 *     CompressUtils.uncompress(new File("data.xz"), out);
 * }
 * }</pre>
 *
 * @author pangju666
 * @see GZipUtils
 * @see XZUtils
 * @see SevenZUtils
 * @see ZipUtils
 * @see TarUtils
 * @since 1.0.0
 */
public class CompressUtils {
	protected CompressUtils() {
	}

	/**
	 * 根据输出文件扩展名压缩单个输入（文件或目录）。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>gz</code>：调用 {@link GZipUtils#compress(File, File)}（单文件压缩）。</li>
	 *   <li><code>xz</code>：调用 {@link XZUtils#compress(File, File)}（单文件压缩）。</li>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#compress(File, File)}。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#compress(File, File)}。</li>
	 *   <li><code>tar</code>：调用 {@link TarUtils#compress(File, File)}。</li>
	 *   <li><code>tgz</code>/<code>tar.gz</code>：先 TAR 打包到临时文件，再对该 TAR 进行 GZIP 压缩，最后删除临时 TAR 文件。</li>
	 * </ul>
	 * 该方法仅依据输出文件扩展名进行分发，不进行内容嗅探。
	 * </p>
	 *
	 * @param inputFile  输入源，可为单个文件或目录（目录将递归打包，具体行为由目标工具类决定）
	 * @param outputFile 输出目标文件，扩展名决定压缩格式
	 * @throws IllegalArgumentException      当 {@code outputFile} 为 {@code null} 时
	 * @throws UnsupportedOperationException 当扩展名不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 1.0.0
	 */
	public static void compress(File inputFile, File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		String format = FilenameUtils.getExtension(outputFile.getName()).toLowerCase();
		switch (format) {
			case "gz":
				GZipUtils.compress(inputFile, outputFile);
				break;
			case "xz":
				XZUtils.compress(inputFile, outputFile);
				break;
			case "7z":
				SevenZUtils.compress(inputFile, outputFile);
				break;
			case "zip":
				ZipUtils.compress(inputFile, outputFile);
				break;
			case "tar":
				TarUtils.compress(inputFile, outputFile);
				break;
			case "tgz":
			case "tar.gz":
				FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
				FileUtils.forceMkdirParent(outputFile);

				File tarFile = new File(outputFile.getParentFile(), FilenameUtils.getBaseName(outputFile.getName()));
				try (FileOutputStream outputStream = FileUtils.openOutputStream(tarFile);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
					TarUtils.compress(inputFile, bufferedOutputStream);
				}
				try (InputStream inputStream = FileUtils.openInputStream(tarFile);
					 FileOutputStream outputStream = FileUtils.openOutputStream(outputFile);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
					GZipUtils.compress(inputStream, bufferedOutputStream);
				} finally {
					FileUtils.forceDelete(tarFile);
				}
			default:
				throw new UnsupportedOperationException("不支持压缩为 " + format + " 格式");
		}
	}

	/**
	 * 根据输出文件扩展名批量压缩多个输入文件。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#compress(Collection, File)}。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#compress(Collection, File)}。</li>
	 *   <li><code>tar</code>：调用 {@link TarUtils#compress(Collection, File)}。</li>
	 *   <li><code>tgz</code>/<code>tar.gz</code>：先将集合打包为 TAR（写入临时文件），再 GZIP 压缩到目标文件，最后删除临时 TAR。</li>
	 * </ul>
	 * 不支持单文件压缩格式 <code>gz</code>/<code>xz</code> 的集合输入。
	 * </p>
	 *
	 * @param inputFiles 输入文件集合，集合内元素应为文件或目录
	 * @param outputFile 输出目标文件，扩展名决定压缩格式
	 * @throws IllegalArgumentException      当 {@code outputFile} 为 {@code null} 时
	 * @throws UnsupportedOperationException 当扩展名不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 1.0.0
	 */
	public static void compress(Collection<File> inputFiles, File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		String format = FilenameUtils.getExtension(outputFile.getName()).toLowerCase();
		switch (format) {
			case "7z":
				SevenZUtils.compress(inputFiles, outputFile);
				break;
			case "zip":
				ZipUtils.compress(inputFiles, outputFile);
				break;
			case "tar":
				TarUtils.compress(inputFiles, outputFile);
				break;
			case "tgz":
			case "tar.gz":
				FileUtils.checkFileIfExist(outputFile, "outputFile 不可为 null");
				FileUtils.forceMkdirParent(outputFile);

				File tarFile = new File(outputFile.getParentFile(), FilenameUtils.getBaseName(outputFile.getName()));
				try (FileOutputStream outputStream = FileUtils.openOutputStream(tarFile);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
					TarUtils.compress(inputFiles, bufferedOutputStream);
				}
				try (InputStream inputStream = FileUtils.openInputStream(tarFile);
					 FileOutputStream outputStream = FileUtils.openOutputStream(outputFile);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
					GZipUtils.compress(inputStream, bufferedOutputStream);
				} finally {
					FileUtils.forceDelete(tarFile);
				}
			default:
				throw new UnsupportedOperationException("不支持压缩为 " + format + " 格式");
		}
	}

	/**
	 * 将单文件格式压缩文件解压到输出流。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>gz</code>：调用 {@link GZipUtils#uncompress(File, OutputStream)}。</li>
	 *   <li><code>xz</code>：调用 {@link XZUtils#uncompress(File, OutputStream)}。</li>
	 * </ul>
	 * 不支持归档格式（如 <code>zip</code>、<code>tar</code>、<code>7z</code>）的输出到流。
	 * </p>
	 *
	 * @param inputFile    压缩文件（单文件压缩格式）
	 * @param outputStream 输出流（方法不会自动关闭该流）
	 * @throws IllegalArgumentException      当 {@code inputFile} 为 {@code null} 时
	 * @throws UnsupportedOperationException 当输入扩展名不是 <code>gz</code>/<code>xz</code> 时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 1.0.0
	 */
	public static void uncompress(File inputFile, OutputStream outputStream) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String format = FilenameUtils.getExtension(inputFile.getName()).toLowerCase();
		switch (format) {
			case "gz":
				GZipUtils.uncompress(inputFile, outputStream);
				break;
			case "xz":
				XZUtils.uncompress(inputFile, outputStream);
				break;
			default:
				throw new UnsupportedOperationException("不支持解压 " + format + " 格式为输出流");
		}
	}

	/**
	 * 将单文件格式压缩文件解压到目标文件。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>gz</code>：调用 {@link GZipUtils#uncompress(File, File)}。</li>
	 *   <li><code>xz</code>：调用 {@link XZUtils#uncompress(File, File)}。</li>
	 * </ul>
	 * 不支持归档格式（如 <code>zip</code>、<code>tar</code>、<code>7z</code>）的解压到单一文件。
	 * </p>
	 *
	 * @param inputFile  压缩文件（单文件压缩格式）
	 * @param outputFile 解压出的目标文件，若父目录不存在会尝试创建
	 * @throws IllegalArgumentException      当 {@code inputFile} 为 {@code null} 时
	 * @throws UnsupportedOperationException 当输入扩展名不是 <code>gz</code>/<code>xz</code> 时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 1.0.0
	 */
	public static void uncompress(File inputFile, File outputFile) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String format = FilenameUtils.getExtension(inputFile.getName()).toLowerCase();
		switch (format) {
			case "gz":
				GZipUtils.uncompress(inputFile, outputFile);
				break;
			case "xz":
				XZUtils.uncompress(inputFile, outputFile);
				break;
			default:
				throw new UnsupportedOperationException("不支持解压 " + format + " 格式到文件");
		}
	}

	/**
	 * 将归档格式解压到目标目录。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#uncompress(File, File)}。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#uncompress(File, File)}。</li>
	 *   <li><code>tar</code>：调用 {@link TarUtils#uncompress(File, File)}。</li>
	 *   <li><code>tgz</code>/<code>tar.gz</code>：先 GZIP 解压得到临时 TAR，再将 TAR 解压到目录，最后删除临时 TAR 文件。</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputFile 压缩归档文件
	 * @param outputDir 目标目录；若不存在会尝试创建
	 * @throws IllegalArgumentException      当 {@code inputFile} 为 {@code null} 或 {@code outputDir} 为 {@code null}（仅 <code>tgz</code>/<code>tar.gz</code> 分支显式校验）时
	 * @throws UnsupportedOperationException 当输入扩展名不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 1.0.0
	 */
	public static void uncompressToDir(File inputFile, File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String format = FilenameUtils.getExtension(inputFile.getName()).toLowerCase();
		switch (format) {
			case "7z":
				SevenZUtils.uncompress(inputFile, outputDir);
				break;
			case "zip":
				ZipUtils.uncompress(inputFile, outputDir);
				break;
			case "tar":
				TarUtils.uncompress(inputFile, outputDir);
				break;
			case "tgz":
			case "tar.gz":
				Validate.notNull(outputDir, "outputDir 不可为 null");
				FileUtils.forceMkdir(outputDir);

				File tarFile = new File(inputFile.getParentFile(), FilenameUtils.getBaseName(inputFile.getName()));
				try (InputStream inputStream = FileUtils.openUnsynchronizedBufferedInputStream(inputFile);
					 FileOutputStream outputStream = FileUtils.openOutputStream(tarFile);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
					GZipUtils.uncompress(inputStream, bufferedOutputStream);
				}
				try (InputStream inputStream = FileUtils.openInputStream(tarFile)) {
					TarUtils.uncompress(inputStream, outputDir);
				} finally {
					FileUtils.forceDelete(tarFile);
				}
			default:
				throw new UnsupportedOperationException("不支持解压 " + format + " 格式到目录");
		}
	}
}
