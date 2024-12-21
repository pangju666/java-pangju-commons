package io.github.pangju666.commons.io.lang;

import org.apache.tika.Tika;

import java.util.Objects;

/**
 * 常量集合
 *
 * @author pangju
 * @since 1.0.0
 */
public class IOConstants {
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

	public static final String DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String DOC_MIME_TYPE = "application/msword";

	public static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String XLS_MIME_TYPE = "application/vnd.ms-excel";

	public static final String PPTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	public static final String PPT_MIME_TYPE = "application/vnd.ms-powerpoint";

	/**
	 * 默认Tika实例
	 */
	private static Tika DEFAULT_TIKA;

	protected IOConstants() {
	}

	public static synchronized Tika getDefaultTika() {
		if (Objects.isNull(DEFAULT_TIKA)) {
			synchronized (IOConstants.class) {//第一层锁，保证只有一个线程进入
				if (Objects.isNull(DEFAULT_TIKA)) {
					DEFAULT_TIKA = new Tika();
				}
			}

		}
		return DEFAULT_TIKA;
	}
}