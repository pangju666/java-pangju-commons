package io.github.pangju666.commons.compress.utils;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class ZipUtils {
	/**
	 * zip压缩文件MIME类型
	 */
	public static final String ZIP_MIME_TYPE = "application/zip";
	/**
	 * zip压缩文件拓展名（后缀）
	 */
	public static final String EXTENSION = "zip";
	/**
	 * zip压缩文件路径分隔符
	 */
	public static final String PATH_SEPARATOR = "/";

	protected ZipUtils() {
	}

	public static void unCompress(final File compressFile) throws IOException {
		FileUtils.checkExists(compressFile, "compressFile 不可为 null", true);
		File outputDir = new File(FilenameUtils.removeExtension(compressFile.getAbsolutePath()));
		unCompress(compressFile, outputDir);
	}

	public static void unCompress(final File compressFile, final File outputDir) throws IOException {
		FileUtils.checkExists(compressFile, "compressFile 不可为 null", true);

		String mimeType = FileUtils.getMimeType(compressFile);
		if (!ZIP_MIME_TYPE.equals(mimeType)) {
			throw new IOException(compressFile.getAbsolutePath() + "不是zip类型文件");
		}
		try (ZipFile zipFile = ZipFile.builder().setFile(compressFile).get()) {
			unCompress(zipFile, outputDir);
		}
	}

	public static void unCompress(final InputStream inputStream, final File outputDir) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (!outputDir.exists()) {
			FileUtils.forceMkdir(outputDir);
		}
		if (inputStream instanceof ZipArchiveInputStream zipArchiveInputStream) {
			unCompress(zipArchiveInputStream, outputDir);
		} else {
			try (ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(inputStream)) {
				unCompress(archiveInputStream, outputDir);
			}
		}
	}

	public static void unCompress(final ZipFile zipFile, final File outputDir) throws IOException {
		Validate.notNull(zipFile, "zipFile 不可为 null");
		Validate.notNull(outputDir, "outputDir 不可为 null");

		FileUtils.forceMkdir(outputDir);
		Iterator<ZipArchiveEntry> iterator = zipFile.getEntries().asIterator();
		ZipArchiveEntry zipEntry = iterator.next();
		while (iterator.hasNext()) {
			File file = new File(outputDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
					 InputStream inputStream = zipFile.getInputStream(zipEntry)) {
					inputStream.transferTo(bufferedOutputStream);
				}
			}
			zipEntry = iterator.next();
		}
	}

	public static void unCompress(final ZipArchiveInputStream archiveInputStream, final File outputDir) throws IOException {
		Validate.notNull(archiveInputStream, "archiveInputStream 不可为 null");
		Validate.notNull(outputDir, "outputDir 不可为 null");

		FileUtils.forceMkdir(outputDir);
		ZipArchiveEntry zipEntry = archiveInputStream.getNextEntry();
		while (Objects.nonNull(zipEntry)) {
			File file = new File(outputDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				if (!file.exists()) {
					FileUtils.forceMkdir(file);
				}
			} else {
				try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
					archiveInputStream.transferTo(bufferedOutputStream);
				}
			}
			zipEntry = archiveInputStream.getNextEntry();
		}
	}

	public static void compress(final File file) throws IOException {
		FileUtils.checkExists(file, "file 不可为 null", false);

		String fullFilename = FilenameUtils.removeExtension(file.getAbsolutePath());
		File outputFile = new File(fullFilename + FilenameUtils.EXTENSION_SEPARATOR + EXTENSION);
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compress(file, zipArchiveOutputStream);
		}
	}

	public static void compress(final File file, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compress(file, zipArchiveOutputStream);
		}
	}

	public static void compress(final File file, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
			compress(file, zipArchiveOutputStream);
		} else {
			try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {
				compress(file, zipArchiveOutputStream);
			}
		}
	}

	public static void compress(final File file, final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		FileUtils.checkExists(file, "file 不可为 null", false);
		Validate.notNull(zipArchiveOutputStream, "zipArchiveOutputStream 不可为 null");

		if (file.isDirectory()) {
			addDirToArchiveOutputStream(file, zipArchiveOutputStream, null);
		} else {
			addFileToArchiveOutputStream(file, zipArchiveOutputStream, null);
		}
		zipArchiveOutputStream.finish();
	}

	public static void compress(final Collection<File> files, final File outputFile) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compress(files, zipArchiveOutputStream);
		}
	}

	public static void compress(final Collection<File> files, final OutputStream outputStream) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
			compress(files, zipArchiveOutputStream);
		} else {
			try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {
				compress(files, zipArchiveOutputStream);
			}
		}
	}

	public static void compress(final Collection<File> files,
								final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		//FileUtils.checkExists(files, "files 中元素不可为 null", false);
		Validate.notNull(zipArchiveOutputStream, "zipArchiveOutputStream 不可为 null");

		for (File file : files) {
			if (FileUtils.exist(file)) {
				if (file.isDirectory()) {
					addDirToArchiveOutputStream(file, zipArchiveOutputStream, null);
				} else {
					addFileToArchiveOutputStream(file, zipArchiveOutputStream, null);
				}
			}
		}
		zipArchiveOutputStream.finish();
	}

	protected static void addDirToArchiveOutputStream(File file, ZipArchiveOutputStream zipArchiveOutputStream,
													  String parent) throws IOException {
		String entryName = file.getName();
		if (StringUtils.isNotBlank(parent)) {
			if (parent.endsWith(PATH_SEPARATOR)) {
				entryName = parent + file.getName() + PATH_SEPARATOR;
			} else {
				entryName = parent + PATH_SEPARATOR + file.getName() + PATH_SEPARATOR;
			}
		}
		ZipArchiveEntry archiveEntry = new ZipArchiveEntry(file, entryName);
		zipArchiveOutputStream.putArchiveEntry(archiveEntry);
		zipArchiveOutputStream.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(file.listFiles(), File[].class);
		for (File childFile : childFiles) {
			if (childFile.isDirectory()) {
				addDirToArchiveOutputStream(childFile, zipArchiveOutputStream, entryName);
			} else {
				addFileToArchiveOutputStream(childFile, zipArchiveOutputStream, entryName);
			}
		}
	}

	protected static void addFileToArchiveOutputStream(File file, ZipArchiveOutputStream zipArchiveOutputStream,
													   String parent) throws IOException {
		try (BufferedFileChannelInputStream fileChannelInputStream = FileUtils.openBufferedFileChannelInputStream(file)) {
			String entryName = file.getName();
			if (StringUtils.isNotBlank(parent)) {
				if (parent.endsWith(PATH_SEPARATOR)) {
					entryName = parent + file.getName();
				} else {
					entryName = parent + PATH_SEPARATOR + file.getName();
				}
			}
			ZipArchiveEntry archiveEntry = new ZipArchiveEntry(file, entryName);
			zipArchiveOutputStream.putArchiveEntry(archiveEntry);
			fileChannelInputStream.transferTo(zipArchiveOutputStream);
			zipArchiveOutputStream.closeArchiveEntry();
		}
	}
}