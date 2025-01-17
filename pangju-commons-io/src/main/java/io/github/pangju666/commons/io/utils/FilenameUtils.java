package io.github.pangju666.commons.io.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import jakarta.activation.MimetypesFileTypeMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * 文件名称工具类
 *
 * @author pangju
 * @see org.apache.commons.io.FilenameUtils
 * @since 1.0.0
 */
public class FilenameUtils extends org.apache.commons.io.FilenameUtils {
	protected static final MimetypesFileTypeMap MIME_TYPE_MAP = new MimetypesFileTypeMap();

	protected FilenameUtils() {
	}

	/**
	 * 根据文件名称获取其MIME类型
	 *
	 * @param fileName 文件名称，不可为空
	 * @return MIME类型
	 * @since 1.0.0
	 */
	public static String getMimeType(final String fileName) {
		Validate.notBlank(fileName, "fileName 不可为空");
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase());
	}

	/**
	 * 根据文件名称判断是否为图片MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名称
	 * @return 是图片类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isImageType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(IOConstants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为文本MIME类型（忽略大小写）
	 *
	 * @param fileName 文件名称
	 * @return 是文本类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isTextType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(IOConstants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为视频MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名称
	 * @return 是视频类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isVideoType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(IOConstants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为音频MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名称
	 * @return 是音频类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isAudioType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(IOConstants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为应用MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名称
	 * @return 是应用类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isApplicationType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(IOConstants.APPLICATION_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为该MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名称
	 * @param mimeType MIME类型，如：image/jpeg、application/json、video/mp4 等
	 * @return MIME类型一致则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isMimeType(final String fileName, final String mimeType) {
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		String fileMimeType = MIME_TYPE_MAP.getContentType(fileName.toLowerCase());
		return mimeType.equalsIgnoreCase(fileMimeType);
	}

	/**
	 * 根据文件名称判断是否为任何一个MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName  文件名称
	 * @param mimeTypes MIME类型集合，如：image/jpeg、application/json、video/mp4 等
	 * @return 与任何一个MIME类型一致则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isAnyMimeType(final String fileName, final String... mimeTypes) {
		if (StringUtils.isBlank(fileName) || ArrayUtils.isEmpty(mimeTypes)) {
			return false;
		}
		String fileMimeType = MIME_TYPE_MAP.getContentType(fileName.toLowerCase());
		return StringUtils.equalsAnyIgnoreCase(fileMimeType, mimeTypes);
	}
}