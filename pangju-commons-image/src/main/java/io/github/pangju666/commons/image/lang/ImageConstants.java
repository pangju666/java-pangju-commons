package io.github.pangju666.commons.image.lang;

import javax.imageio.ImageIO;
import java.util.Objects;
import java.util.Set;

public class ImageConstants {
	public static final String JPEG_MIME_TYPE = "image/jpeg";

	public static int WHITE_HEX_RGB = 0xFFFFFFFF;
	public static int BLACK_HEX_RGB = 0xFF000000;
	public static final String PNG_MIME_TYPE = "image/png";
	public static final String WEBP_MIME_TYPE = "image/webp";
	public static final String BMP_MIME_TYPE = "image/bmp";
	private static volatile Set<String> SUPPORT_IMAGE_TYPES;

	public static Set<String> getSupportImageTypes() {
		if (Objects.isNull(SUPPORT_IMAGE_TYPES)) {
			synchronized (ImageConstants.class) {
				if (Objects.isNull(SUPPORT_IMAGE_TYPES)) {
					SUPPORT_IMAGE_TYPES = Set.of(ImageIO.getReaderMIMETypes());
				}
			}
		}
		return SUPPORT_IMAGE_TYPES;
	}
}
