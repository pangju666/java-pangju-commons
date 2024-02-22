package io.github.pangju666.commons.io.utils.file;

import io.github.pangju666.commons.io.lang.Constants;
import jakarta.activation.MimetypesFileTypeMap;
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
	 * @param fileName 文件名称
	 * @return MIME类型（如果文件名称为""、" " 或 null 则返回空字符串）
	 * @since 1.0.0
	 */
	public static String getMimeType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return StringUtils.EMPTY;
		}
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
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(Constants.IMAGE_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为文本MIME类型（忽略大小写）
	 *
	 * @param fileName 文件名
	 * @return 是文本类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isTextType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(Constants.TEXT_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为视频MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名
	 * @return 是视频类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isVideoType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(Constants.VIDEO_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为音频MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名
	 * @return 是音频类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isAudioType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(Constants.AUDIO_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为应用MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名
	 * @return 是应用类型则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isApplicationType(final String fileName) {
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return MIME_TYPE_MAP.getContentType(fileName.toLowerCase()).startsWith(Constants.APPLICATION_MIME_TYPE_PREFIX);
	}

	/**
	 * 根据文件名称判断是否为该MIME类型（忽略大小写）
	 * <p>如果文件名为""、" " 或 null 则返回 false</p>
	 *
	 * @param fileName 文件名
	 * @param mimeType MIME类型，如：image/jpeg、application/json、video/mp4 等
	 * @return MIME类型一致则返回 true 否则为 false（如果文件名称为""、" " 或 null 则返回false）
	 * @since 1.0.0
	 */
	public static boolean isMimeType(final String fileName, final String mimeType) {
		Validate.notBlank(mimeType, "mimeType 不可为空");
		if (StringUtils.isBlank(fileName)) {
			return false;
		}
		return mimeType.equalsIgnoreCase(MIME_TYPE_MAP.getContentType(fileName.toLowerCase()));
	}
}