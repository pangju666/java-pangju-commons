package io.github.pangju666.commons.io.utils.file;

import io.github.pangju666.commons.io.lang.Constants;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.apache.commons.io.input.MemoryMappedFileInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tika.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文件工具类
 *
 * @author pangju
 * @see org.apache.commons.io.FileUtils
 * @since 1.0.0
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
	protected FileUtils() {
	}

	/**
	 * 打开指定文件的输入流
	 *
	 * @param file 指定文件
	 * @return 文件输入流
	 * @throws IOException 文件读取失败时抛出
	 * @see MemoryMappedFileInputStream
	 * @since 1.0.0
	 */
	public static MemoryMappedFileInputStream openMemoryMappedFileInputStream(File file) throws IOException {
		return openMemoryMappedFileInputStream(file, (256 * 1024));
	}

	/**
	 * 打开指定文件的输入流
	 *
	 * @param file 指定文件
	 * @param bufferSize 缓冲区大小
	 * @return 文件输入流
	 * @throws IOException 文件读取失败时抛出
	 * @see MemoryMappedFileInputStream
	 * @since 1.0.0
	 */
	public static MemoryMappedFileInputStream openMemoryMappedFileInputStream(File file, int bufferSize) throws IOException {
		validateFile(file, "file不可为 null");
		return MemoryMappedFileInputStream
			.builder()
			.setFile(file)
			.setBufferSize(bufferSize)
			.get();
	}

	/**
	 * 打开指定文件的缓冲区输入流
	 *
	 * @param file 指定文件
	 * @return 文件缓冲区输入流
	 * @throws IOException 文件读取失败时抛出
	 * @see BufferedFileChannelInputStream
	 * @since 1.0.0
	 */
	public static BufferedFileChannelInputStream openBufferedFileChannelInputStream(File file) throws IOException {
		return openBufferedFileChannelInputStream(file, 4096);
	}

	/**
	 * 打开指定文件的缓冲区输入流
	 *
	 * @param file 指定文件
	 * @param bufferSize 缓冲区大小
	 * @return 文件缓冲区输入流
	 * @throws IOException 文件读取失败时抛出
	 * @see BufferedFileChannelInputStream
	 * @since 1.0.0
	 */
	public static BufferedFileChannelInputStream openBufferedFileChannelInputStream(File file, int bufferSize) throws IOException {
		validateFile(file, "file不可为 null");
		return BufferedFileChannelInputStream
			.builder()
			.setFile(file)
			.setBufferSize(bufferSize)
			.get();
	}

	/**
	 * 如果文件存在则强制删除
	 *
	 * @param file 要删除的文件，可能为 null 或不存在
	 * @throws IOException 删除失败则抛出IO异常
	 * @since 1.0.0
	 */
	public static void forceDeleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			forceDelete(file);
		}
	}

	/**
	 * 如果文件存在则删除
	 *
	 * @param file 要删除的文件，可能为 null 或不存在
	 * @throws IOException 删除失败则抛出IO异常
	 * @since 1.0.0
	 */
	public static void deleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			delete(file);
		}
	}

	/**
	 * 重命名文件
	 *
	 * @param srcFile 源文件，不可为 null
	 * @param newName 新文件名称，不可为空
	 * @return 重命名后的新文件
	 * @throws IOException 文件不存在或重命名失败
	 * @since 1.0.0
	 */
	public static File rename(final File srcFile, final String newName) throws IOException {
		validateFileOrDir(srcFile, "srcFile 不可为 null");
		Validate.notBlank(newName, "newName 不可为空");
		String fullPath = FilenameUtils.getFullPath(srcFile.getAbsolutePath());
		File destFile = new File(fullPath, newName);
		if (!srcFile.renameTo(destFile)) {
			throw new IOException();
		}
		return destFile;
	}

	/**
	 * 重命名文件（不包含后缀）
	 *
	 * @param srcFile     源文件，不可为 null
	 * @param newBaseName 新文件名称（无后缀），不可为空
	 * @return 重命名后的新文件
	 * @throws IOException 文件不存在、不是文件或重命名失败
	 * @since 1.0.0
	 */
	public static File renameBaseName(final File srcFile, final String newBaseName) throws IOException {
		Validate.notBlank(newBaseName, "newBaseName 不可为空");
		validateFile(srcFile, "srcFile 不可为 null");
		String fullPath = FilenameUtils.getFullPath(srcFile.getAbsolutePath());
		String extension = FilenameUtils.getExtension(srcFile.getName());
		File destFile = new File(fullPath, newBaseName + FilenameUtils.EXTENSION_SEPARATOR + extension);
		if (!srcFile.renameTo(destFile)) {
			throw new IOException();
		}
		return destFile;
	}

	/**
	 * 重命名文件后缀
	 *
	 * @param srcFile      源文件，不可为 null
	 * @param newExtension 新文件后缀，可能为 null（null 视为无后缀）
	 * @return 重命名后的新文件
	 * @throws IOException 文件不存在、不是文件或重命名失败
	 * @since 1.0.0
	 */
	public static File renameExtension(final File srcFile, final String newExtension) throws IOException {
		validateFile(srcFile, "srcFile 不可为 null");
		String filePathWithoutExtension = FilenameUtils.removeExtension(srcFile.getAbsolutePath());
		File destFile;
		if (StringUtils.isBlank(newExtension)) {
			destFile = new File(filePathWithoutExtension);
		} else if (newExtension.startsWith(FilenameUtils.EXTENSION_SEPARATOR_STR)) {
			destFile = new File(filePathWithoutExtension + newExtension);
		} else {
			destFile = new File(filePathWithoutExtension + FilenameUtils.EXTENSION_SEPARATOR_STR +
				StringUtils.defaultIfBlank(newExtension, StringUtils.EMPTY));
		}
		if (!srcFile.renameTo(destFile)) {
			throw new IOException();
		}
		return destFile;
	}

	/**
	 * 判断文件是否存在
	 *
	 * @param file 要检查的文件，可能为 null
	 * @return 文件存在则返回true，否则为false（如果文件为 null 则返回 false）
	 * @since 1.0.0
	 */
	public static boolean exist(final File file) {
		return Objects.nonNull(file) && file.exists();
	}

	/**
	 * 判断文件是否不存在
	 *
	 * @param file 要检查的文件，可能为 null
	 * @return 文件不存在则返回true，否则为false（如果文件为 null 则返回 true）
	 * @since 1.0.0
	 */
	public static boolean notExist(final File file) {
		return Objects.isNull(file) || !file.exists();
	}

	/**
	 * 获取文件MIME类型
	 *
	 * @param file 要解析的文件，文件不可为 null 且必须存在
	 * @return 文件MIME类型
	 * @throws IOException 文件读取失败
	 * @since 1.0.0
	 */
	public static String getMimeType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file);
	}

	/**
	 * 判断文件是否为图片MIME类型
	 * <p>如果文件为 null 或文件不存在 则返回 false</p>
	 *
	 * @param file 要解析的文件，文件不可为 null 且必须存在
	 * @return 是图片类型则返回 true 否则为 false
	 * @since 1.0.0
	 */
	public static boolean isImageType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断文件是否为文本MIME类型
	 *
	 * @param file 要解析的文件，文件不可为 null 且必须存在
	 * @return 是文本类型则返回 true 否则为 false
	 * @since 1.0.0
	 */
	public static boolean isTextType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断文件是否为视频MIME类型
	 *
	 * @param file 要解析的文件，文件不可为 null 且必须存在
	 * @return 是视频类型则返回 true 否则为 false
	 * @since 1.0.0
	 */
	public static boolean isVideoType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断文件是否为音频MIME类型
	 *
	 * @param file 要解析的文件，文件不可为 null 且必须存在
	 * @return 是音频类型则返回 true 否则为 false
	 * @since 1.0.0
	 */
	public static boolean isAudioType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断文件是否为应用MIME类型
	 *
	 * @param file 要解析的文件，文件不可为 null 且必须存在
	 * @return 是应用类型则返回 true 否则为 false
	 * @since 1.0.0
	 */
	public static boolean isApplicationType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.APPLICATION_MIME_TYPE_PREFIX);
	}

	/**
	 * 判断文件是否为该MIME类型
	 *
	 * @param file     要解析的文件，文件不可为 null 且必须存在
	 * @param mimeType MIME类型，如：image/jpeg、application/json、video/mp4 等
	 * @return MIME类型一致则返回 true 否则为 false
	 * @since 1.0.0
	 */
	public static boolean isMimeType(final File file, final String mimeType) throws IOException {
		Validate.notBlank(mimeType, "mimeType 不可为空");
		validateFile(file, "file 不可为 null");
		String fileMimeType = Constants.DEFAULT_TIKA.detect(file);
		return mimeType.equalsIgnoreCase(fileMimeType);
	}

	/**
	 * 根据文件名称判断是否为任何一个MIME类型
	 * <p>如果文件为 null 或文件不存在 则返回 false</p>
	 *
	 * @param file      要解析的文件，文件不可为 null 且必须存在
	 * @param mimeTypes MIME类型集合，如：image/jpeg、application/json、video/mp4 等
	 * @return 与任何一个MIME类型一致则返回 true 否则为 false
	 * @since 1.0.0
	 */
	public static boolean isAnyMimeType(final File file, final String... mimeTypes) throws IOException {
		validateFile(file, "file 不可为 null");
		if (ArrayUtils.isEmpty(mimeTypes)) {
			return false;
		}
		String fileMimeType = Constants.DEFAULT_TIKA.detect(file);
		return StringUtils.equalsAnyIgnoreCase(fileMimeType, mimeTypes);
	}

	/**
	 * 解析文件元数据
	 *
	 * @param file 要解析的文件，文件必须存在且不可为 null
	 * @return 文件元数据
	 * @throws IOException 文件读取失败
	 * @since 1.0.0
	 */
	public static Map<String, String> parseMetaData(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		Metadata metadata = new Metadata();
		try (Reader reader = Constants.DEFAULT_TIKA.parse(file, metadata)) {
			return Arrays.stream(metadata.names())
				.map(name -> Pair.of(name, metadata.get(name)))
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
		}
	}

	/**
	 * 校验文件是否有效
	 *
	 * @param file 文件
	 * @throws NoSuchFileException 当文件不存在或为目录时
	 * @since 1.0.0
	 */
	public static void validateFile(final File file, final String message) throws NoSuchFileException {
		Validate.notNull(file, message);
		if (!file.exists() || !file.isFile()) {
			throw new NoSuchFileException(file.getAbsolutePath());
		}
	}

	/**
	 * 校验文件是否有效
	 *
	 * @param file 文件
	 * @throws NoSuchFileException 当文件不存在时
	 * @since 1.0.0
	 */
	public static void validateFileOrDir(final File file, final String message) throws NoSuchFileException {
		Validate.notNull(file, message);
		if (!file.exists()) {
			throw new NoSuchFileException(file.getAbsolutePath());
		}
	}

	/**
	 * 校验集合中的所有文件是否有效
	 *
	 * @param files 文件
	 * @throws NoSuchFileException 当文件不存在时
	 * @since 1.0.0
	 */
	public static void validateFiles(final Collection<File> files, final String message) throws NoSuchFileException {
		Validate.noNullElements(files, message);
		for (File file : files) {
			if (!file.exists() || !file.isFile()) {
				throw new NoSuchFileException(file.getAbsolutePath());
			}
		}
	}

	/**
	 * 校验集合中的所有文件是否有效
	 *
	 * @param files 文件
	 * @throws NoSuchFileException 当文件不存在或为目录时
	 * @since 1.0.0
	 */
	public static void validateFilesOrDirs(final Collection<File> files, final String message) throws NoSuchFileException {
		Validate.noNullElements(files, message);
		for (File file : files) {
			if (!file.exists()) {
				throw new NoSuchFileException(file.getAbsolutePath());
			}
		}
	}
}