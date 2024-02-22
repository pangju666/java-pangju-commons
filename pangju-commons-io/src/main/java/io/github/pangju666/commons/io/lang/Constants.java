package io.github.pangju666.commons.io.lang;

import org.apache.tika.Tika;

/**
 * 常量集合
 *
 * @author pangju
 * @since 1.0.0
 */
public class Constants {
	/**
	 * 默认Tika实例
	 */
	public static final Tika DEFAULT_TIKA = new Tika();
	/**
	 * 图片MIME类型前缀
	 */
	public static final String IMAGE_MIME_TYPE_PREFIX = "image/";
	/**
	 * 视频MIME类型前缀
	 */
	public static final String VIDEO_MIME_TYPE_PREFIX = "video/";
	/**
	 * 音频MIME类型前缀
	 */
	public static final String AUDIO_MIME_TYPE_PREFIX = "audio/";
	/**
	 * 文本MIME类型前缀
	 */
	public static final String TEXT_MIME_TYPE_PREFIX = "text/";
	/**
	 * 应用MIME类型前缀
	 */
	public static final String APPLICATION_MIME_TYPE_PREFIX = "application/";

	protected Constants() {
	}
}
