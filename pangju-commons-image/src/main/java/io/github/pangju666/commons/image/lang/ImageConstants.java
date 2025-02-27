package io.github.pangju666.commons.image.lang;

import io.github.pangju666.commons.io.lang.IOConstants;

import javax.imageio.ImageIO;
import java.util.Set;

public class ImageConstants extends IOConstants {
	public static final Set<String> SUPPORT_IMAGE_TYPES = Set.of(ImageIO.getReaderMIMETypes());
	public static int WHITE_HEX_RGB = 0xFFFFFFFF;
	public static int BLACK_HEX_RGB = 0xFF000000;
}
