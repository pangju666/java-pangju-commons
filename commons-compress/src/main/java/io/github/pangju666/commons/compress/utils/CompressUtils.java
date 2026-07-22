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

import io.github.pangju666.commons.compress.io.resource.*;
import io.github.pangju666.commons.compress.lang.CompressConstants;
import io.github.pangju666.commons.io.resource.IOResource;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * 综合压缩/解压分发工具。
 * <p>
 * 根据输出或输入文件扩展名，将调用分发到具体实现：
 * {@link GzipUtils}、{@link XZUtils}、{@link SevenZUtils}、{@link ZipUtils}、{@link TarUtils}、{@link ZstdUtils}，
 * 并支持组合格式 <code>tgz</code>/<code>tar.gz</code>、<code>txz</code>/<code>tar.xz</code>、<code>tzst</code>/<code>tar.zst</code>
 * （通过 TAR 打包再压缩的串联方式实现）。
 * </p>
 *
 * <h3>功能特性</h3>
 * <ul>
 *   <li>格式分发：基于文件扩展名进行分发，不进行内容嗅探；如需严格的格式校验请使用各具体工具的 <code>is*</code> 方法。</li>
 *   <li>组合格式支持：<code>tgz</code>/<code>tar.gz</code>、<code>txz</code>/<code>tar.xz</code>、<code>tzst</code>/<code>tar.zst</code> 通过中间 TAR 文件再压缩实现，临时 TAR 文件会在完成后删除。</li>
 *   <li>多输入源：支持单文件、目录（递归）、以及文件集合（集合仅支持归档格式）。</li>
 *   <li>资源管理：方法内部创建的流会在方法内正确关闭；调用方传入的 <code>OutputStream</code> 不会被自动关闭。</li>
 *   <li>异常一致性：不支持的格式抛出 {@link UnsupportedOperationException}；底层 IO 失败抛出 {@link IOException}。</li>
 *   <li>线程安全：类本身无共享状态；并发写入同一路径可能产生冲突。</li>
 * </ul>
 *
 * <h3>格式选择建议</h3>
 * <ul>
 *   <li><strong>压缩率优先</strong>：选择 <code>xz</code> 或 <code>7z</code>，提供最高的压缩比。</li>
 *   <li><strong>速度优先</strong>：选择 <code>zip</code> 或 <code>zstd</code>，提供更快的压缩和解压速度。</li>
 *   <li><strong>兼容性优先</strong>：选择 <code>gzip</code> 或 <code>zip</code>，在大多数系统上都有广泛支持。</li>
 *   <li><strong>zstd vs gzip</strong>：在不考虑兼容性的情况下，<code>zstd</code> 无论在压缩率还是速度上都优于 <code>gzip</code>。</li>
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
 * // 4) 使用密码压缩为 7Z
 * CompressUtils.compress(new File("inputDir"), new File("archive.7z"), "password");
 *
 * // 5) 使用 CompressResource 解压 ZIP 到目录
 * CompressUtils.uncompress(new ZipResource(new File("archive.zip")), new File("outputDir"));
 *
 * // 6) 使用 CompressResource 解压 TAR.GZ 到目录
 * CompressUtils.uncompress(new GzipResource(new File("archive.tar.gz")), new File("outputDir"));
 *
 * // 7) 使用 CompressResource 解压 GZ 到文件
 * CompressUtils.uncompress(new GzipResource(new File("file.txt.gz")), new File("file.txt"));
 *
 * // 8) 使用密码解压 ZIP
 * CompressUtils.uncompress(new ZipResource(new File("archive.zip")), new File("outputDir"), "password");
 *
 * // 9) 将 XZ 解压到输出流（输出流由调用方关闭）
 * try (OutputStream out = new FileOutputStream("out.bin")) {
 *     CompressUtils.uncompress(new XZResource(new File("data.xz")), out);
 * }
 * }</pre>
 *
 * @author pangju666
 * @see GzipUtils
 * @see XZUtils
 * @see SevenZUtils
 * @see ZipUtils
 * @see TarUtils
 * @see ZstdUtils
 * @since 1.0.0
 */
public class CompressUtils {
	/**
	 * 私有构造函数，防止实例化。
	 */
	protected CompressUtils() {
	}

	/**
	 * 根据输出文件扩展名将输入流压缩为单文件格式。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>gz</code>：调用 {@link GzipUtils#compress(InputStream, File)}。</li>
	 *   <li><code>xz</code>：调用 {@link XZUtils#compress(InputStream, File)}。</li>
	 *   <li><code>zst</code>：调用 {@link ZstdUtils#compress(InputStream, File)}。</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputStream 输入流，必须非 null
	 * @param outputFile  输出目标文件，扩展名决定压缩格式
	 * @throws IllegalArgumentException      当 {@code outputFile} 为 {@code null} 时
	 * @throws UnsupportedOperationException 当扩展名不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 2.1.0
	 */
	public static void compress(final InputStream inputStream, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		String outputFormat = FilenameUtils.getExtension(outputFile.getName()).toLowerCase();
		switch (outputFormat) {
			case "gz" -> GzipUtils.compress(inputStream, outputFile);
			case "xz" -> XZUtils.compress(inputStream, outputFile);
			case "zst" -> ZstdUtils.compress(inputStream, outputFile);
			default -> throw new UnsupportedOperationException("不支持压缩为 " + outputFormat + " 格式");
		}
	}

	/**
	 * 根据输出文件扩展名将 IO 资源压缩为单文件格式。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>gz</code>：调用 {@link GzipUtils#compress(IOResource, File)}。</li>
	 *   <li><code>xz</code>：调用 {@link XZUtils#compress(IOResource, File)}。</li>
	 *   <li><code>zst</code>：调用 {@link ZstdUtils#compress(IOResource, File)}。</li>
	 * </ul>
	 * </p>
	 *
	 * @param resource   IO 资源对象，必须非 null
	 * @param outputFile 输出目标文件，扩展名决定压缩格式
	 * @throws IllegalArgumentException      当 {@code outputFile} 为 {@code null} 时
	 * @throws UnsupportedOperationException 当扩展名不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 2.1.0
	 */
	public static void compress(final IOResource resource, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		String outputFormat = FilenameUtils.getExtension(outputFile.getName()).toLowerCase();
		switch (outputFormat) {
			case "gz" -> GzipUtils.compress(resource, outputFile);
			case "xz" -> XZUtils.compress(resource, outputFile);
			case "zst" -> ZstdUtils.compress(resource, outputFile);
			default -> throw new UnsupportedOperationException("不支持压缩为 " + outputFormat + " 格式");
		}
	}

	/**
	 * 根据输出文件扩展名压缩单个输入（文件或目录）。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>gz</code>：调用 {@link GzipUtils#compress(IOResource, File)}（单文件压缩）。</li>
	 *   <li><code>xz</code>：调用 {@link XZUtils#compress(IOResource, File)}（单文件压缩）。</li>
	 *   <li><code>zst</code>：调用 {@link ZstdUtils#compress(IOResource, File)}（单文件压缩）。</li>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#archive(File, File)}。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#archive(File, File)}。</li>
	 *   <li><code>tar</code>：调用 {@link TarUtils#archive(File, File)}。</li>
	 *   <li><code>tgz</code>/<code>tar.gz</code>：先 TAR 打包到系统临时目录的临时文件，再对该 TAR 进行 GZIP 压缩，最后删除临时 TAR 文件。</li>
	 *   <li><code>txz</code>/<code>tar.xz</code>：先 TAR 打包到系统临时目录的临时文件，再对该 TAR 进行 XZ 压缩，最后删除临时 TAR 文件。</li>
	 *   <li><code>tzst</code>/<code>tar.zst</code>：先 TAR 打包到系统临时目录的临时文件，再对该 TAR 进行 Zstandard 压缩，最后删除临时 TAR 文件。</li>
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
	public static void compress(final File inputFile, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		String outputFilename = outputFile.getName();
		String outputFormat = getOutputFormat(outputFilename);
		switch (outputFormat) {
			case "gz" -> GzipUtils.compress(new IOResource(inputFile), outputFile);
			case "xz" -> XZUtils.compress(new IOResource(inputFile), outputFile);
			case "zst" -> ZstdUtils.compress(new IOResource(inputFile), outputFile);
			case "7z" -> SevenZUtils.archive(inputFile, outputFile);
			case "zip" -> ZipUtils.archive(inputFile, outputFile);
			case "tar" -> TarUtils.archive(inputFile, outputFile);
			case "tgz", "tar.gz" -> {
				File tarFile = compressTmpTarFile(inputFile, outputFile);

				try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tarFile)) {
					GzipUtils.compress(inputStream, outputFile);
				} finally {
					FileUtils.forceDeleteIfExist(tarFile);
				}
			}
			case "txz", "tar.xz" -> {
				File tarFile = compressTmpTarFile(inputFile, outputFile);

				try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tarFile)) {
					XZUtils.compress(inputStream, outputFile);
				} finally {
					FileUtils.forceDeleteIfExist(tarFile);
				}
			}
			case "tzst", "tar.zst" -> {
				File tarFile = compressTmpTarFile(inputFile, outputFile);

				try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tarFile)) {
					ZstdUtils.compress(inputStream, outputFile);
				} finally {
					FileUtils.forceDeleteIfExist(tarFile);
				}
			}
			default -> throw new UnsupportedOperationException("不支持压缩为 " + outputFormat + " 格式");
		}
	}

	/**
	 * 根据输出文件扩展名批量压缩多个输入文件。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#archive(Collection, File)}。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#archive(Collection, File)}。</li>
	 *   <li><code>tar</code>：调用 {@link TarUtils#archive(Collection, File)}。</li>
	 *   <li><code>tgz</code>/<code>tar.gz</code>：先将集合打包为 TAR（写入系统临时目录的临时文件），再 GZIP 压缩到目标文件，最后删除临时 TAR。</li>
	 *   <li><code>txz</code>/<code>tar.xz</code>：先将集合打包为 TAR（写入系统临时目录的临时文件），再 XZ 压缩到目标文件，最后删除临时 TAR。</li>
	 *   <li><code>tzst</code>/<code>tar.zst</code>：先将集合打包为 TAR（写入系统临时目录的临时文件），再 Zstandard 压缩到目标文件，最后删除临时 TAR。</li>
	 * </ul>
	 * 不支持单文件压缩格式 <code>gz</code>/<code>xz</code>/<code>zst</code> 的集合输入。
	 * </p>
	 *
	 * @param inputFiles 输入文件集合，集合内元素应为文件或目录
	 * @param outputFile 输出目标文件，扩展名决定压缩格式
	 * @throws IllegalArgumentException      当 {@code outputFile} 为 {@code null} 时
	 * @throws UnsupportedOperationException 当扩展名不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 1.0.0
	 */
	public static void compress(final Collection<File> inputFiles, final File outputFile) throws IOException {
		String outputFilename = outputFile.getName();
		String outputFormat = getOutputFormat(outputFilename);
		switch (outputFormat) {
			case "7z" -> SevenZUtils.archive(inputFiles, outputFile);
			case "zip" -> ZipUtils.archive(inputFiles, outputFile);
			case "tar" -> TarUtils.archive(inputFiles, outputFile);
			case "tgz", "tar.gz" -> {
				File tarFile = compressTmpTarFile(inputFiles, outputFile);

				try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tarFile)) {
					GzipUtils.compress(inputStream, outputFile);
				} finally {
					FileUtils.forceDeleteIfExist(tarFile);
				}
			}
			case "txz", "tar.xz" -> {
				File tarFile = compressTmpTarFile(inputFiles, outputFile);

				try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tarFile)) {
					XZUtils.compress(inputStream, outputFile);
				} finally {
					FileUtils.forceDeleteIfExist(tarFile);
				}
			}
			case "tzst", "tar.zst" -> {
				File tarFile = compressTmpTarFile(inputFiles, outputFile);

				try (InputStream inputStream = FileUtils.newUnsynchronizedBufferedInputStream(tarFile)) {
					ZstdUtils.compress(inputStream, outputFile);
				} finally {
					FileUtils.forceDeleteIfExist(tarFile);
				}
			}
			default -> throw new UnsupportedOperationException("不支持压缩为 " + outputFormat + " 格式");
		}
	}

	/**
	 * 根据输出文件扩展名使用密码压缩单个输入（文件或目录）。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#archive(File, File, String)}。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#archive(File, File, String)}。</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputFile  输入源，可为单个文件或目录
	 * @param outputFile 输出目标文件，扩展名决定压缩格式
	 * @param password   压缩密码，必须非空
	 * @throws IllegalArgumentException      当 {@code outputFile} 或 {@code password} 为 {@code null} 时
	 * @throws IllegalArgumentException      当 {@code password} 为空时
	 * @throws UnsupportedOperationException 当扩展名不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 2.1.0
	 */
	public static void compress(final File inputFile, final File outputFile, final String password) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		String format = FilenameUtils.getExtension(outputFile.getName()).toLowerCase();
		switch (format) {
			case "7z" -> SevenZUtils.archive(inputFile, outputFile, password);
			case "zip" -> ZipUtils.archive(inputFile, outputFile, password);
			default -> throw new UnsupportedOperationException("不支持使用密码压缩为 " + format + " 格式");
		}
	}

	/**
	 * 根据输出文件扩展名使用密码批量压缩多个输入文件。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#archive(Collection, File, String)}。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#archive(List, File, String)}。</li>
	 * </ul>
	 * </p>
	 *
	 * @param inputFiles 输入文件集合，集合内元素应为文件或目录
	 * @param outputFile 输出目标文件，扩展名决定压缩格式
	 * @param password   压缩密码，必须非空
	 * @throws IllegalArgumentException      当 {@code outputFile} 或 {@code password} 为 {@code null} 时
	 * @throws IllegalArgumentException      当 {@code password} 为空时
	 * @throws UnsupportedOperationException 当扩展名不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 2.1.0
	 */
	public static void compress(final List<File> inputFiles, final File outputFile, final String password) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		String format = FilenameUtils.getExtension(outputFile.getName()).toLowerCase();
		switch (format) {
			case "7z" -> SevenZUtils.archive(inputFiles, outputFile, password);
			case "zip" -> ZipUtils.archive(inputFiles, outputFile, password);
			default -> throw new UnsupportedOperationException("不支持使用密码压缩为 " + format + " 格式");
		}
	}

	/**
	 * 将压缩资源解压到输出流。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>gz</code>：调用 {@link GzipUtils#uncompress(GzipResource, OutputStream)}。</li>
	 *   <li><code>xz</code>：调用 {@link XZUtils#uncompress(XZResource, OutputStream)}。</li>
	 *   <li><code>zst</code>：调用 {@link ZstdUtils#uncompress(ZstdResource, OutputStream)}。</li>
	 * </ul>
	 * 不支持归档格式（如 <code>zip</code>、<code>tar</code>、<code>7z</code>）的输出到流。
	 * </p>
	 *
	 * @param resource     压缩资源对象，必须非 null
	 * @param outputStream 输出流（方法不会自动关闭该流）
	 * @throws NullPointerException          当 {@code resource} 为 {@code null} 时
	 * @throws UnsupportedOperationException 当资源格式不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 2.1.0
	 */
	public static void uncompress(final CompressResource resource, final OutputStream outputStream) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		if (resource.isGzip()) {
			GzipUtils.uncompress(new GzipResource(resource), outputStream);
		} else if (resource.isXz()) {
			XZUtils.uncompress(new XZResource(resource), outputStream);
		} else if (resource.isZstd()) {
			ZstdUtils.uncompress(new ZstdResource(resource), outputStream);
		} else {
			throw new UnsupportedOperationException("不支持解压 " + resource.getFormat() + " 格式");
		}
	}

	/**
	 * 将压缩资源解压到目标位置。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#extract(SevenZResource, File)}，目标位置为目录。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#extract(ZipResource, File)}，目标位置为目录。</li>
	 *   <li><code>tar</code>：调用 {@link TarUtils#extract(TarResource, File)}，目标位置为目录。</li>
	 *   <li><code>gz</code>/<code>xz</code>/<code>zst</code>：先解压到临时 TAR 文件，然后：
	 *     <ul>
	 *       <li>若解压后的文件为 TAR 格式（通过 MIME 类型检测），则自动解压 TAR 内容到目标位置。</li>
	 *       <li>若解压后的文件不是 TAR 格式，则检查目标位置是否为文件；若目标位置为目录则抛出 {@link IllegalArgumentException}，否则将临时文件移动到目标位置。</li>
	 *     </ul>
	 *   </li>
	 * </ul>
	 * </p>
	 *
	 * @param resource    压缩资源对象，必须非 null
	 * @param destination 目标位置：归档格式（7z、zip、tar）为目录，压缩格式（gz、xz、zst）为文件
	 * @throws NullPointerException          当 {@code resource} 为 {@code null} 时
	 * @throws IllegalArgumentException      当解压后的文件不是 TAR 格式且目标位置为目录时抛出
	 * @throws UnsupportedOperationException 当资源格式不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 2.1.0
	 */
	public static void uncompress(final CompressResource resource, final File destination) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		if (resource.is7z()) {
			SevenZUtils.extract(new SevenZResource(resource), destination);
		} else if (resource.isZip()) {
			ZipUtils.extract(new ZipResource(resource), destination);
		} else if (resource.isTar()) {
			TarUtils.extract(new TarResource(resource), destination);
		} else if (resource.isXz() || resource.isZstd() || resource.isGzip()) {
			String baseName = FilenameUtils.getBaseName(destination.getName());
			File outputFile = new File(destination.getParent(), baseName + "-" + UUID.randomUUID() + ".tar");

			if (resource.isGzip()) {
				GzipUtils.uncompress(new GzipResource(resource), outputFile);
			} else if (resource.isXz()) {
				XZUtils.uncompress(new XZResource(resource), outputFile);
			} else if (resource.isZstd()) {
				ZstdUtils.uncompress(new ZstdResource(resource), outputFile);
			}

			if (FileUtils.isMimeType(outputFile, CompressConstants.TAR_MIME_TYPE)) {
				try (TarFile tarFile = new TarFile(outputFile)) {
					TarUtils.extract(tarFile, destination);
				} finally {
					FileUtils.forceDeleteIfExist(outputFile);
				}
			} else {
				if (destination.exists() && destination.isDirectory()) {
					FileUtils.forceDeleteIfExist(outputFile);
					throw new IllegalArgumentException(destination.getAbsolutePath() + " 不是一个文件路径");
				}
				FileUtils.moveFile(outputFile, destination);
			}
		} else {
			throw new UnsupportedOperationException("不支持解压 " + resource.getFormat() + " 格式");
		}
	}

	/**
	 * 使用密码将加密压缩资源解压到目标文件。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>7z</code>：调用 {@link SevenZUtils#extract(SevenZResource, File)}。</li>
	 *   <li><code>zip</code>：调用 {@link ZipUtils#extract(ZipResource, File, String)}。</li>
	 * </ul>
	 * </p>
	 *
	 * @param resource   压缩资源对象，必须非 null
	 * @param outputFile 解压出的目标文件，若父目录不存在会尝试创建
	 * @param password   解压密码，必须非空
	 * @throws NullPointerException          当 {@code resource} 或 {@code password} 为 {@code null} 时
	 * @throws IllegalArgumentException      当 {@code password} 为空时
	 * @throws UnsupportedOperationException 当资源格式不在支持列表中时
	 * @throws IOException                   底层读写失败或目标工具类抛出的 IO 异常
	 * @since 2.1.0
	 */
	public static void uncompress(final CompressResource resource, final File outputFile, final String password) throws IOException {
		Validate.notNull(resource, "resource 不可为 null");

		if (resource.is7z()) {
			SevenZUtils.extract(new SevenZResource(resource, password), outputFile);
		} else if (resource.isZip()) {
			ZipUtils.extract(new ZipResource(resource), outputFile, password);
		} else {
			throw new UnsupportedOperationException("不支持解压 " + resource.getFormat() + " 格式");
		}
	}

	/**
	 * 将单文件格式压缩文件解压到输出流。
	 * <p>
	 * 支持格式：
	 * <ul>
	 *   <li><code>gz</code>：调用 {@link GzipUtils#uncompress(File, OutputStream)}。</li>
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
	 * @deprecated 请使用 {@link #uncompress(CompressResource, OutputStream)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final File inputFile, final OutputStream outputStream) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String format = FilenameUtils.getExtension(inputFile.getName()).toLowerCase();
		switch (format) {
			case "gz":
				GzipUtils.uncompress(inputFile, outputStream);
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
	 *   <li><code>gz</code>：调用 {@link GzipUtils#uncompress(File, File)}。</li>
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
	 * @deprecated 请使用 {@link #uncompress(CompressResource, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompress(final File inputFile, final File outputFile) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String format = FilenameUtils.getExtension(inputFile.getName()).toLowerCase();
		switch (format) {
			case "gz":
				GzipUtils.uncompress(inputFile, outputFile);
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
	 * @deprecated 请使用 {@link #uncompress(CompressResource, File)} 代替
	 */
	@Deprecated(forRemoval = true, since = "2.1.0")
	public static void uncompressToDir(final File inputFile, final File outputDir) throws IOException {
		Validate.notNull(inputFile, "inputFile 不可为 null");

		String format;
		String filename = inputFile.getName();
		if (filename.endsWith(".tar.gz")) {
			format = "tar.gz";
		} else {
			format = FilenameUtils.getExtension(filename).toLowerCase();
		}
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

				String baseName = FilenameUtils.getBaseName(filename);
				File tarFile = new File(inputFile.getParentFile(), baseName + ".tmp.tar");
				try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(inputFile);
				     BufferedOutputStream bufferedOutputStream = FileUtils.newBufferedOutputStream(tarFile)) {
					GzipUtils.uncompress(inputStream, bufferedOutputStream);
				}
				try (InputStream inputStream = FileUtils.openBufferedFileChannelInputStream(tarFile)) {
					TarUtils.uncompress(inputStream, outputDir);
				} finally {
					FileUtils.forceDelete(tarFile);
				}
				break;
			default:
				throw new UnsupportedOperationException("不支持解压 " + format + " 格式到目录");
		}
	}

	/**
	 * 从文件名获取输出格式。
	 * <p>支持组合格式识别：<code>tar.gz</code>、<code>tar.xz</code>、<code>tar.zst</code>。</p>
	 *
	 * @param outputFilename 输出文件名，必须非空
	 * @return 格式字符串（如 "gz"、"tar.gz" 等）
	 * @throws IllegalArgumentException 当 {@code outputFilename} 为空时
	 * @since 2.1.0
	 */
	private static String getOutputFormat(final String outputFilename) {
		Validate.notBlank(outputFilename, "outputFilename 不可为空");

		if (outputFilename.endsWith(".tar.gz")) {
			return "tar.gz";
		} else if (outputFilename.endsWith(".tar.xz")) {
			return "tar.xz";
		} else if (outputFilename.endsWith(".tar.zst")) {
			return "tar.zst";
		} else {
			return FilenameUtils.getExtension(outputFilename).toLowerCase();
		}
	}

	/**
	 * 将文件集合压缩为临时 TAR 文件。
	 * <p>临时文件创建在系统临时目录，文件名包含 UUID 以避免冲突。</p>
	 *
	 * @param inputFiles 输入文件集合
	 * @param outputFile 输出目标文件（用于生成临时文件名）
	 * @return 临时 TAR 文件
	 * @throws IOException 压缩失败时抛出
	 * @since 2.1.0
	 */
	private static File compressTmpTarFile(final Collection<File> inputFiles, final File outputFile) throws IOException {
		String baseName = FilenameUtils.getBaseName(outputFile.getName());
		File tmpTarFile = new File(FileUtils.getTempDirectory(), baseName + "-" + UUID.randomUUID() + ".tar");

		TarUtils.archive(inputFiles, tmpTarFile);

		return tmpTarFile;
	}

	/**
	 * 将单个文件或目录压缩为临时 TAR 文件。
	 * <p>临时文件创建在系统临时目录，文件名包含 UUID 以避免冲突。</p>
	 *
	 * @param inputFile  输入文件或目录
	 * @param outputFile 输出目标文件（用于生成临时文件名）
	 * @return 临时 TAR 文件
	 * @throws IOException 压缩失败时抛出
	 * @since 2.1.0
	 */
	private static File compressTmpTarFile(final File inputFile, final File outputFile) throws IOException {
		String baseName = FilenameUtils.getBaseName(outputFile.getName());
		File tmpTarFile = new File(FileUtils.getTempDirectory(), baseName + "-" + UUID.randomUUID() + ".tar");

		TarUtils.archive(inputFile, tmpTarFile);

		return tmpTarFile;
	}
}
