package io.github.pangju666.commons.io.utils.file;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.NoSuchFileException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileUtils extends org.apache.commons.io.FileUtils {
	public static final Tika DEFAULT_TIKA = new Tika();

	protected FileUtils() {
	}

	public static void forceDeleteIfExist(final File file) throws IOException {
		if (exist(file)) {
			forceDelete(file);
		}
	}

	public static File rename(final File sourceFile, final String newName) throws IOException {
		if (!sourceFile.exists()) {
			throw new NoSuchFileException(sourceFile.getAbsolutePath());
		}
		return renameTo(sourceFile, newName);
	}

	public static File renameBaseName(final File sourceFile, final String newBaseName) throws IOException {
		if (!sourceFile.exists() || !sourceFile.isFile()) {
			throw new NoSuchFileException(sourceFile.getAbsolutePath());
		}
		String extension = FilenameUtils.getExtension(sourceFile.getName());
		String newFileName = newBaseName + FilenameUtils.EXTENSION_SEPARATOR + extension;
		return renameTo(sourceFile, newFileName);
	}

	public static File renameExtension(final File sourceFile, final String newExtension) throws IOException {
		if (!sourceFile.exists() || !sourceFile.isFile()) {
			throw new NoSuchFileException(sourceFile.getAbsolutePath());
		}
		String fileBaseName = FilenameUtils.getBaseName(sourceFile.getAbsolutePath());
		String newFileName = fileBaseName + FilenameUtils.EXTENSION_SEPARATOR + newExtension.replaceFirst("\\.", "");
		return renameTo(sourceFile, newFileName);
	}

	public static boolean exist(final File file) {
		return Objects.nonNull(file) && file.exists();
	}

	public static boolean notExist(final File file) {
		return Objects.isNull(file) || !file.exists();
	}

	public static String getMimeType(final File file) throws IOException {
		return DEFAULT_TIKA.detect(file);
	}

	public static String getMimeType(final File file, final Tika tika) throws IOException {
		return tika.detect(file);
	}

	public static String getMimeType(final InputStream inputStream) throws IOException {
		return DEFAULT_TIKA.detect(inputStream);
	}

	public static String getMimeType(final InputStream inputStream, final Tika tika) throws IOException {
		return tika.detect(inputStream);
	}

	public static String getMimeType(final byte[] bytes) {
		return DEFAULT_TIKA.detect(bytes);
	}

	public static String getMimeType(final byte[] bytes, final Tika tika) {
		return tika.detect(bytes);
	}

	public static Map<String, String> parseMetaData(final File file) throws IOException {
		return parseMetaData(file, DEFAULT_TIKA);
	}

	public static Map<String, String> parseMetaData(final File file, final Tika tika) throws IOException {
		Metadata metadata = new Metadata();
		try (Reader reader = tika.parse(file, metadata)) {
			return Arrays.stream(metadata.names())
					.map(name -> Pair.of(name, metadata.get(name)))
					.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
		}
	}

	public static Map<String, String> parseMetaData(final InputStream inputStream) throws IOException {
		return parseMetaData(inputStream, DEFAULT_TIKA);
	}

	public static Map<String, String> parseMetaData(final InputStream inputStream, final Tika tika) throws IOException {
		Metadata metadata = new Metadata();
		try (Reader reader = tika.parse(inputStream, metadata)) {
			return Arrays.stream(metadata.names())
				.map(name -> Pair.of(name, metadata.get(name)))
				.collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
		}
	}

	protected static File renameTo(final File sourceFile, final String newName) throws IOException {
		String fullPath = FilenameUtils.getFullPath(sourceFile.getAbsolutePath());
		File newFile = new File(fullPath, newName);
		if (!sourceFile.renameTo(newFile)) {
			throw new IOException();
		}
		return newFile;
	}
}