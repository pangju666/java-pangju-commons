package io.github.pangju666.commons.io.utils.compress;

import io.github.pangju666.commons.io.utils.file.FilenameUtils;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.archivers.sevenz.SevenZOutputFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.util.Collection;
import java.util.Objects;

public class SevenZUtils {
	/**
	 * 7z压缩文件MIME类型
	 */
	public static final String SEVEN_Z_MIME_TYPE = "application/x-7z-compressed";

	protected SevenZUtils() {
	}

	public static void unCompress(final File compressFile, final File outputDir) throws IOException {
		String mimeType = FilenameUtils.getMimeType(compressFile.getName());
		if (!SEVEN_Z_MIME_TYPE.equals(mimeType)) {
			throw new IOException(compressFile.getAbsolutePath() + "不是7z类型文件");
		}
		try (SevenZFile sevenZFile = new SevenZFile(compressFile)) {
			unCompress(sevenZFile, outputDir);
		}
	}

	public static void unCompress(final SevenZFile sevenZFile, final File outputDir) throws IOException {
		FileUtils.forceMkdir(outputDir);
		SevenZArchiveEntry zipEntry = sevenZFile.getNextEntry();
		while (Objects.nonNull(zipEntry)) {
			File file = new File(outputDir, zipEntry.getName());
			if (zipEntry.isDirectory()) {
				FileUtils.forceMkdir(file);
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

	public static void compressFile(final File file) throws IOException {
		String pathNoEndSeparator = file.isFile() ? FilenameUtils.getFullPathNoEndSeparator(file.getAbsolutePath()) : file.getAbsolutePath();
		File outputFile = new File(pathNoEndSeparator + ".7z");
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compressFile(file, sevenZOutputFile);
		}
	}

	public static void compressFile(final File file, final File outputFile) throws IOException {
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compressFile(file, sevenZOutputFile);
		}
	}

	public static void compressFiles(final Collection<File> files, final File outputFile) throws IOException {
		try (SevenZOutputFile sevenZOutputFile = new SevenZOutputFile(outputFile)) {
			compressFiles(files, sevenZOutputFile);
		}
	}

	public static void compressFile(final File file, final SevenZOutputFile outputFile) throws IOException {
		if (file.isFile()) {
			addFileToArchiveOutputStream(file, outputFile, null);
		} else {
			addDirToArchiveOutputStream(file, outputFile, null);
		}
		outputFile.finish();
	}

	public static void compressFiles(final Collection<File> files, final SevenZOutputFile outputFile) throws IOException {
		for (File file : files) {
			if (file.isFile()) {
				addFileToArchiveOutputStream(file, outputFile, null);
			} else {
				addDirToArchiveOutputStream(file, outputFile, null);
			}
		}
		outputFile.finish();
	}

	private static void addDirToArchiveOutputStream(final File file, final SevenZOutputFile outputFile, final String parent) throws IOException {
		String archiveEntryName = StringUtils.isNotBlank(parent) ? parent + "/" + file.getName() : file.getName();
		SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(file, archiveEntryName + "/");
		archiveEntry.setDirectory(true);
		outputFile.putArchiveEntry(archiveEntry);
		outputFile.closeArchiveEntry();

		File[] childFiles = ArrayUtils.nullToEmpty(file.listFiles(), File[].class);
		if (ArrayUtils.isNotEmpty(childFiles)) {
			String childParent = file.getName();
			if (Objects.nonNull(parent)) {
				childParent = parent + "/" + file.getName();
			}
			for (File childFile : childFiles) {
				if (childFile.isFile()) {
					addFileToArchiveOutputStream(childFile, outputFile, childParent);
				} else {
					addDirToArchiveOutputStream(childFile, outputFile, childParent);
				}
			}
		}
	}

	private static void addFileToArchiveOutputStream(final File file, final SevenZOutputFile outputFile, final String parent) throws IOException {
		try (InputStream inputStream = new FileInputStream(file);
			 BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream)) {
			String archiveEntryName = StringUtils.isNotBlank(parent) ? parent + "/" + file.getName() : file.getName();
			SevenZArchiveEntry archiveEntry = outputFile.createArchiveEntry(file, archiveEntryName);
			outputFile.putArchiveEntry(archiveEntry);
			outputFile.write(bufferedInputStream);
			outputFile.closeArchiveEntry();
		}
	}
}
