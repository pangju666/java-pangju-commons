package io.github.pangju666.commons.io.utils.compress;

import io.github.pangju666.commons.io.utils.file.FilenameUtils;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Collection;
import java.util.Objects;

public class ZipUtils {
	/**
	 * zip压缩文件MIME类型
	 */
	public static final String ZIP_MIME_TYPE = "application/x-zip-compressed";

	protected ZipUtils() {
	}

	public static void unCompress(final File zipFile, final File outputDir) throws IOException {
		String mimeType = FilenameUtils.getMimeType(zipFile.getName());
		if (!ZIP_MIME_TYPE.equals(mimeType)) {
			throw new IOException(zipFile.getAbsolutePath() + "不是zip类型文件");
		}
		try (FileInputStream fileInputStream = new FileInputStream(zipFile);
			 BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream)) {
			unCompress(bufferedInputStream, outputDir);
		}
	}

	public static void unCompress(final InputStream inputStream, final File outputDir) throws IOException {
		try (ZipArchiveInputStream archiveInputStream = new ZipArchiveInputStream(inputStream)) {
			unCompress(archiveInputStream, outputDir);
		}
	}

	public static void unCompress(final ZipArchiveInputStream archiveInputStream, final File outputDir) throws IOException {
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
		String pathNoEndSeparator = file.isFile() ? FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath()) : file.getAbsolutePath();
		File outputFile = new File(pathNoEndSeparator + ".zip");
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compressFile(file, zipArchiveOutputStream);
		}
	}

	public static void compressFile(final File file, final File outputFile) throws IOException {
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compressFile(file, zipArchiveOutputStream);
		}
	}

	public static void compressFiles(final Collection<File> files, final File outputFile) throws IOException {
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputFile)) {
			compressFiles(files, zipArchiveOutputStream);
		}
	}

	public static void compressFile(final File file, final OutputStream outputStream) throws IOException {
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {
			compressFile(file, zipArchiveOutputStream);
		}
	}

	public static void compressFiles(final Collection<File> files, final OutputStream outputStream) throws IOException {
		try (ZipArchiveOutputStream zipArchiveOutputStream = new ZipArchiveOutputStream(outputStream)) {
			compressFiles(files, zipArchiveOutputStream);
		}
	}

	public static void compressFile(final File file, final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		if (file.isFile()) {
			addFileToArchiveOutputStream(file, zipArchiveOutputStream, null);
		} else {
			addDirToArchiveOutputStream(file, zipArchiveOutputStream, null);
		}
		zipArchiveOutputStream.finish();
	}

	public static void compressFiles(final Collection<File> files, final ZipArchiveOutputStream zipArchiveOutputStream) throws IOException {
		for (File file : files) {
			if (file.isFile()) {
				addFileToArchiveOutputStream(file, zipArchiveOutputStream, null);
			} else {
				addDirToArchiveOutputStream(file, zipArchiveOutputStream, null);
			}
		}
		zipArchiveOutputStream.finish();
	}

	private static void addDirToArchiveOutputStream(File file, ZipArchiveOutputStream zipArchiveOutputStream, String parent) throws IOException {
		String entryName = StringUtils.isNotBlank(parent) ? parent + "/" + file.getName() : file.getName();
		ZipArchiveEntry archiveEntry = new ZipArchiveEntry(file, entryName + "/");
		zipArchiveOutputStream.putArchiveEntry(archiveEntry);
		zipArchiveOutputStream.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(file.listFiles(), File[].class);
		if (ArrayUtils.isNotEmpty(childFiles)) {
			String childParent = file.getName();
			if (Objects.nonNull(parent)) {
				childParent = parent + "/" + file.getName();
			}
			for (File childFile : childFiles) {
				if (childFile.isFile()) {
					addFileToArchiveOutputStream(childFile, zipArchiveOutputStream, childParent);
				} else {
					addDirToArchiveOutputStream(childFile, zipArchiveOutputStream, childParent);
				}
			}
		}
	}

	private static void addFileToArchiveOutputStream(File file, ZipArchiveOutputStream zipArchiveOutputStream, String parent) throws IOException {
		try (InputStream inputStream = new FileInputStream(file);
			 BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
			String entryName = StringUtils.isNotBlank(parent) ? parent + "/" + file.getName() : file.getName();
			ZipArchiveEntry archiveEntry = new ZipArchiveEntry(file, entryName);
			zipArchiveOutputStream.putArchiveEntry(archiveEntry);
			bufferedInputStream.transferTo(zipArchiveOutputStream);
			zipArchiveOutputStream.closeArchiveEntry();
		}
	}
}
