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
	protected static final int DEFAULT_MEMORY_MAPPED_BUFFER_SIZE = 256 * 1024;
	protected static final int DEFAULT_BUFFERED_FILE_CHANNEL_BUFFER_SIZE = 4096;

	protected FileUtils() {
	}

	public static MemoryMappedFileInputStream openMemoryMappedFileInputStream(File file) throws IOException {
		return openMemoryMappedFileInputStream(file, DEFAULT_MEMORY_MAPPED_BUFFER_SIZE);
	}

	public static MemoryMappedFileInputStream openMemoryMappedFileInputStream(File file, int bufferSize) throws IOException {
		validateFile(file, "file不可为 null");
		return MemoryMappedFileInputStream
			.builder()
			.setFile(file)
			.setBufferSize(bufferSize)
			.get();
	}

	public static BufferedFileChannelInputStream openBufferedFileChannelInputStream(File file) throws IOException {
		return openBufferedFileChannelInputStream(file, DEFAULT_BUFFERED_FILE_CHANNEL_BUFFER_SIZE);
	}

	public static BufferedFileChannelInputStream openBufferedFileChannelInputStream(File file, int bufferSize) throws IOException {
		validateFile(file, "file不可为 null");
		return BufferedFileChannelInputStream
			.builder()
			.setFile(file)
			.setBufferSize(bufferSize)
			.get();
	}

	public static void forceDeleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			forceDelete(file);
		}
	}

	public static void deleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			delete(file);
		}
	}

	public static File rename(final File srcFile, final String newName) throws IOException {
		Validate.notNull(srcFile, "srcFile 不可为 null");
		Validate.notBlank(newName, "newName 不可为空");

		if (!srcFile.exists()) {
			throw new NoSuchFileException(srcFile.getAbsolutePath());
		}
		String fullPath = FilenameUtils.getFullPath(srcFile.getAbsolutePath());
		File destFile = new File(fullPath, newName);
		if (!srcFile.renameTo(destFile)) {
			throw new IOException();
		}
		return destFile;
	}

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

	public static boolean exist(final File file) {
		return Objects.nonNull(file) && file.exists();
	}

	public static boolean notExist(final File file) {
		return Objects.isNull(file) || !file.exists();
	}

	public static String getMimeType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file);
	}

	public static boolean isImageType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.IMAGE_MIME_TYPE_PREFIX);
	}

	public static boolean isTextType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.TEXT_MIME_TYPE_PREFIX);
	}

	public static boolean isVideoType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.VIDEO_MIME_TYPE_PREFIX);
	}

	public static boolean isAudioType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.AUDIO_MIME_TYPE_PREFIX);
	}

	public static boolean isApplicationType(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		return Constants.DEFAULT_TIKA.detect(file).startsWith(Constants.APPLICATION_MIME_TYPE_PREFIX);
	}

	public static boolean isMimeType(final File file, final String mimeType) throws IOException {
		Validate.notBlank(mimeType, "mimeType 不可为空");
		validateFile(file, "file 不可为 null");
		String fileMimeType = Constants.DEFAULT_TIKA.detect(file);
		return mimeType.equalsIgnoreCase(fileMimeType);
	}

	public static boolean isAnyMimeType(final File file, final String... mimeTypes) throws IOException {
		validateFile(file, "file 不可为 null");
		if (ArrayUtils.isEmpty(mimeTypes)) {
			return false;
		}
		String fileMimeType = Constants.DEFAULT_TIKA.detect(file);
		return StringUtils.equalsAnyIgnoreCase(fileMimeType, mimeTypes);
	}

	public static Map<String, String> parseMetaData(final File file) throws IOException {
		validateFile(file, "file 不可为 null");
		Metadata metadata = new Metadata();
		try (Reader reader = Constants.DEFAULT_TIKA.parse(file, metadata)) {
			return Arrays.stream(metadata.names())
				.map(name -> Pair.of(name, metadata.get(name)))
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
		}
	}

	public static void validateFile(final File file, final String message) throws NoSuchFileException {
		Validate.notNull(file, message);
		if (!file.exists() || !file.isFile()) {
			throw new NoSuchFileException(file.getAbsolutePath());
		}
	}

	public static void validateFileOrDir(final File file, final String message) throws NoSuchFileException {
		Validate.notNull(file, message);
		if (!file.exists()) {
			throw new NoSuchFileException(file.getAbsolutePath());
		}
	}

	public static void validateFiles(final Collection<File> files, final String message) throws NoSuchFileException {
		Validate.noNullElements(files, message);
		for (File file : files) {
			if (!file.exists() || !file.isFile()) {
				throw new NoSuchFileException(file.getAbsolutePath());
			}
		}
	}

	public static void validateFilesOrDirs(final Collection<File> files, final String message) throws NoSuchFileException {
		Validate.noNullElements(files, message);
		for (File file : files) {
			if (!file.exists()) {
				throw new NoSuchFileException(file.getAbsolutePath());
			}
		}
	}
}