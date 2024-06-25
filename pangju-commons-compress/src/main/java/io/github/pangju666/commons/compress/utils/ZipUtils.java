package io.github.pangju666.commons.compress.utils;

import io.github.pangju666.commons.io.utils.file.FileUtils;
import io.github.pangju666.commons.io.utils.file.FilenameUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipMethod;
import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.*;
import java.util.Collection;
import java.util.Objects;
import java.util.zip.ZipEntry;

public class ZipUtils {
	/**
	 * zip压缩文件MIME类型
	 */
	public static final String ZIP_MIME_TYPE = "application/zip";
	/**
	 * zip压缩文件路径分隔符
	 */
	public static final String PATH_SEPARATOR = "/";
	/**
	 * zip压缩文件拓展名（后缀）
	 */
	public static final String EXTENSION = "zip";

	protected ZipUtils() {
	}

	public static void unCompress(final File compressFile) throws IOException {
		FileUtils.validateFile(compressFile, "compressFile 不可为 null");
		File outputDir = new File(FilenameUtils.removeExtension(compressFile.getAbsolutePath()));
		unCompress(compressFile, outputDir);
	}

	public static void unCompress(final File compressFile, final File outputDir) throws IOException {
		FileUtils.validateFile(compressFile, "compressFile 不可为 null");

		String mimeType = FileUtils.getMimeType(compressFile);
		if (!ZIP_MIME_TYPE.equals(mimeType)) {
			throw new IOException(compressFile.getAbsolutePath() + "不是zip类型文件");
		}
		try (FileInputStream fileInputStream = new FileInputStream(compressFile);
			 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
			unCompress(bufferedInputStream, outputDir);
		}
	}

	public static void unCompress(final InputStream inputStream, final File outputDir) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		if (inputStream instanceof ZipArchiveInputStream zipArchiveInputStream) {
			unCompress(zipArchiveInputStream, outputDir);
		} else {
			try (ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(inputStream)) {
				unCompress(archiveInputStream, outputDir);
			}
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
				FileUtils.forceMkdir(file);
			} else {
				try (FileOutputStream fileOutputStream = FileUtils.openOutputStream(file);
					 BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
					archiveInputStream.transferTo(bufferedOutputStream);
				}
			}
			zipEntry = archiveInputStream.getNextEntry();
		}
	}

	public static void compressFile(final File file) throws IOException {
		compressFile(file, ZipMethod.UNKNOWN);
	}

	public static void compressFile(final File file, final ZipMethod method) throws IOException {
		FileUtils.validateFileOrDir(file, "file 不可为 null");

		String fullFilename = FilenameUtils.removeExtension(file.getAbsolutePath());
		File outputFile = new File(fullFilename + FilenameUtils.EXTENSION_SEPARATOR + EXTENSION);
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compressFile(file, zipArchiveOutputStream, method);
		}
	}

	public static void compressFile(final File file, final File outputFile) throws IOException {
		compressFile(file, outputFile, ZipMethod.UNKNOWN);
	}

	public static void compressFile(final File file, final File outputFile, final ZipMethod method) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compressFile(file, zipArchiveOutputStream, method);
		}
	}

	public static void compressFile(final File file, final OutputStream outputStream) throws IOException {
		compressFile(file, outputStream, ZipMethod.UNKNOWN);
	}

	public static void compressFile(final File file, final OutputStream outputStream, final ZipMethod method) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
			compressFile(file, zipArchiveOutputStream, method);
		} else {
			try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {
				compressFile(file, zipArchiveOutputStream, method);
			}
		}
	}

	public static void compressFile(final File file, final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		compressFile(file, zipArchiveOutputStream, ZipMethod.UNKNOWN);
	}

	public static void compressFile(final File file, final ZipArchiveOutputStream zipArchiveOutputStream,
									final ZipMethod method) throws IOException {
		FileUtils.validateFileOrDir(file, "file 不可为 null");
		Validate.notNull(zipArchiveOutputStream, "zipArchiveOutputStream 不可为 null");

		if (file.isFile()) {
			addFileToArchiveOutputStream(file, zipArchiveOutputStream, null, method);
		} else {
			addDirToArchiveOutputStream(file, zipArchiveOutputStream, null, method);
		}
		zipArchiveOutputStream.finish();
	}

	public static void compressFiles(final Collection<File> files, final File outputFile) throws IOException {
		compressFiles(files, outputFile, ZipMethod.UNKNOWN);
	}

	public static void compressFiles(final Collection<File> files, final File outputFile, final ZipMethod method) throws IOException {
		Validate.notNull(outputFile, "outputFile 不可为 null");

		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compressFiles(files, zipArchiveOutputStream, method);
		}
	}

	public static void compressFiles(final Collection<File> files, final OutputStream outputStream) throws IOException {
		compressFiles(files, outputStream, ZipMethod.UNKNOWN);
	}

	public static void compressFiles(final Collection<File> files, final OutputStream outputStream, final ZipMethod method) throws IOException {
		Validate.notNull(outputStream, "outputStream 不可为 null");

		if (outputStream instanceof ZipArchiveOutputStream zipArchiveOutputStream) {
			compressFiles(files, zipArchiveOutputStream, method);
		} else {
			try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {
				compressFiles(files, zipArchiveOutputStream, method);
			}
		}
	}

	public static void compressFiles(final Collection<File> files, final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		compressFiles(files, zipArchiveOutputStream, ZipMethod.UNKNOWN);
	}

	public static void compressFiles(final Collection<File> files, final ZipArchiveOutputStream zipArchiveOutputStream,
									 final ZipMethod method) throws IOException {
		FileUtils.validateFilesOrDirs(files, "files中存在 null 项");
		Validate.notNull(zipArchiveOutputStream, "zipArchiveOutputStream 不可为 null");

		for (File file : files) {
			if (FileUtils.exist(file)) {
				if (file.isFile()) {
					addFileToArchiveOutputStream(file, zipArchiveOutputStream, null, method);
				} else {
					addDirToArchiveOutputStream(file, zipArchiveOutputStream, null, method);
				}
			}
		}
		zipArchiveOutputStream.finish();
	}

	protected static void addDirToArchiveOutputStream(File file, ZipArchiveOutputStream zipArchiveOutputStream,
													  String parent, ZipMethod method) throws IOException {
		String entryName = file.getName();
		if (StringUtils.isNotBlank(parent)) {
			if (parent.endsWith(PATH_SEPARATOR)) {
				entryName = parent + file.getName() + PATH_SEPARATOR;
			} else {
				entryName = parent + PATH_SEPARATOR + file.getName() + PATH_SEPARATOR;
			}
		}
		ZipArchiveEntry archiveEntry = new ZipArchiveEntry(file, entryName);
        if (method.getCode() == ZipMethod.STORED.getCode()) {
            archiveEntry.setMethod(ZipEntry.STORED);
        } else if (method.getCode() == ZipMethod.DEFLATED.getCode()) {
            archiveEntry.setMethod(ZipEntry.DEFLATED);
        }
		zipArchiveOutputStream.putArchiveEntry(archiveEntry);
		zipArchiveOutputStream.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(file.listFiles(), File[].class);
		for (File childFile : childFiles) {
			if (childFile.isFile()) {
				addFileToArchiveOutputStream(childFile, zipArchiveOutputStream, entryName, method);
			} else {
				addDirToArchiveOutputStream(childFile, zipArchiveOutputStream, entryName, method);
			}
		}
	}

	protected static void addFileToArchiveOutputStream(File file, ZipArchiveOutputStream zipArchiveOutputStream,
													   String parent, ZipMethod method) throws IOException {
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
            if (method.getCode() == ZipMethod.STORED.getCode()) {
                archiveEntry.setMethod(ZipEntry.STORED);
            } else if (method.getCode() == ZipMethod.DEFLATED.getCode()) {
                archiveEntry.setMethod(ZipEntry.DEFLATED);
            }
			zipArchiveOutputStream.putArchiveEntry(archiveEntry);
			fileChannelInputStream.transferTo(zipArchiveOutputStream);
			zipArchiveOutputStream.closeArchiveEntry();
		}
	}
}
