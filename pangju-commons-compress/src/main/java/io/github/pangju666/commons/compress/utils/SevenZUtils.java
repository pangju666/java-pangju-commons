package io.github.pangju666.commons.compress.utils;

import io.github.pangju666.commons.io.utils.file.FileUtils;
import io.github.pangju666.commons.io.utils.file.FilenameUtils;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.Objects;

public class SevenZUtils {
	/**
	 * 7z压缩文件MIME类型
	 */
	public static final String SEVEN_Z_MIME_TYPE = "application/x-7z-compressed";
	/**
	 * 7z压缩文件拓展名（后缀）
	 */
	public static final String EXTENSION = "7z";
	/**
	 * 7z压缩文件路径分隔符
	 */
	public static final String PATH_SEPARATOR = "/";

	protected SevenZUtils() {
	}

	public static void unCompress(final File compressFile) throws IOException {
		FileUtils.validateFile(compressFile, "compressFile 不可为 null");
		File outputDir = new File(FilenameUtils.removeExtension(compressFile.getAbsolutePath()));
		unCompress(compressFile, outputDir);
	}

	public static void unCompress(final File compressFile, final File outputDir) throws IOException {
		FileUtils.validateFile(compressFile, "compressFile 不可为 null");

		String mimeType = FileUtils.getMimeType(compressFile);
		if (!SEVEN_Z_MIME_TYPE.equals(mimeType)) {
			throw new IOException(compressFile.getAbsolutePath() + "不是7z类型文件");
		}
		try (SevenZFile sevenZFile = new SevenZFile.Builder().setFile(compressFile).get()) {
			unCompress(sevenZFile, outputDir);
		}
	}

	public static void unCompress(final SevenZFile sevenZFile, final File outputDir) throws IOException {
		Validate.notNull(sevenZFile, "sevenZFile 不可为 null");
		Validate.notNull(outputDir, "outputDir 不可为 null");

		if (!outputDir.exists()) {
			FileUtils.forceMkdir(outputDir);
		}
		SevenZArchiveEntry zipEntry = sevenZFile.getNextEntry();
		while (Objects.nonNull(zipEntry)) {
			File file = new File(outputDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				try (InputStream inputStream = sevenZFile.getInputStream(zipEntry);
					 FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
					inputStream.transferTo(bufferedOutputStream);
				}
			}
			zipEntry = sevenZFile.getNextEntry();
		}
	}

	public static void compress(final File file) throws IOException {
		FileUtils.validateFileOrDir(file, "file 不可为 null");

		String fullFilename = FilenameUtils.removeExtension(file.getAbsolutePath());
		File outputFile = new File(fullFilename + FilenameUtils.EXTENSION_SEPARATOR + EXTENSION);
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compress(file, sevenZOutputFile);
		}
	}

	public static void compress(final File file, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compress(file, sevenZOutputFile);
		}
	}

	public static void compress(final File file, final SevenZOutputFile sevenZOutputFile) throws IOException {
		FileUtils.validateFileOrDir(file, "file 不可为 null");
		Validate.notNull(sevenZOutputFile, "sevenZOutputFile 不可为 null");

		if (file.isDirectory()) {
			addDirToSevenZOutputFile(file, sevenZOutputFile, null);
		} else {
			addFileToSevenZOutputFile(file, sevenZOutputFile, null);
		}
		sevenZOutputFile.finish();
	}

	public static void compress(final Collection<File> files, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compress(files, sevenZOutputFile);
		}
	}

	public static void compress(final Collection<File> files, final SevenZOutputFile sevenZOutputFile) throws IOException {
		FileUtils.validateFilesOrDirs(files, "files 中元素不可为 null");
		Validate.notNull(sevenZOutputFile, "sevenZOutputFile 不可为 null");

		for (File file : files) {
			if (FileUtils.notExist(file)) {
				throw new NoSuchFileException(file.getAbsolutePath());
			}
			if (file.isDirectory()) {
				addDirToSevenZOutputFile(file, sevenZOutputFile, null);
			} else {
				addFileToSevenZOutputFile(file, sevenZOutputFile, null);
			}
		}
		sevenZOutputFile.finish();
	}

	protected static void addDirToSevenZOutputFile(final File file, final SevenZOutputFile outputFile,
												   final String parent) throws IOException {
		String archiveEntryName = StringUtils.isNotBlank(parent) ?
			parent + PATH_SEPARATOR + file.getName() : file.getName();
		SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(file, archiveEntryName + PATH_SEPARATOR);
		archiveEntry.setDirectory(true);
		outputFile.putArchiveEntry(archiveEntry);
		outputFile.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(file.listFiles(), File[].class);
		if (ArrayUtils.isNotEmpty(childFiles)) {
			String childParent = file.getName();
			if (Objects.nonNull(parent)) {
				childParent = parent + PATH_SEPARATOR + file.getName();
			}
			for (File childFile : childFiles) {
				if (childFile.isDirectory()) {
					addDirToSevenZOutputFile(childFile, outputFile, childParent);
				} else {
					addFileToSevenZOutputFile(childFile, outputFile, childParent);
				}
			}
		}
	}

	protected static void addFileToSevenZOutputFile(final File file, final SevenZOutputFile outputFile,
													final String parent) throws IOException {
		try (BufferedFileChannelInputStream inputStream = FileUtils.openBufferedFileChannelInputStream(file)) {
			String archiveEntryName = StringUtils.isNotBlank(parent) ?
				parent + PATH_SEPARATOR + file.getName() : file.getName();
			SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(file, archiveEntryName);
			outputFile.putArchiveEntry(archiveEntry);
			outputFile.write(inputStream);
			outputFile.closeArchiveEntry();
		}
	}
}