package io.github.pangju666.commons.io.utils;

import io.github.pangju666.commons.lang.pool.ConstantPool;
import jakarta.activation.MimetypesFileTypeMap;
import org.apache.commons.lang3.StringUtils;

public class FilenameUtils extends org.apache.commons.io.FilenameUtils {
	protected static final MimetypesFileTypeMap MIME_TYPE_MAP = new MimetypesFileTypeMap();

	protected FilenameUtils() {
	}

	public static String getMimeType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return StringUtils.EMPTY;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase());
	}

	public static boolean isImageType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(ConstantPool.IMAGE_MIME_TYPE_PREFIX);
	}

	public static boolean isTextType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(ConstantPool.TEXT_MIME_TYPE_PREFIX);
	}

	public static boolean isVideoType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(ConstantPool.VIDEO_MIME_TYPE_PREFIX);
	}

	public static boolean isAudioType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(ConstantPool.AUDIO_MIME_TYPE_PREFIX);
	}

	public static boolean isApplicationType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(ConstantPool.APPLICATION_MIME_TYPE_PREFIX);
	}

	public static String computeFileName(final String baseName, String extension) {
		if (StringUtils.isBlank(baseName)) {
			return StringUtils.EMPTY;
		}
		if (StringUtils.isBlank(extension)) {
			return baseName;
		}
		return baseName + EXTENSION_SEPARATOR + (extension.startsWith(EXTENSION_SEPARATOR_STR) ? extension.substring(1) : extension);
	}
}