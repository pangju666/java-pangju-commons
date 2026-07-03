package io.github.pangju666.commons.opencv.utils;

import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Size;

import java.io.*;
import java.nio.IntBuffer;

public class ImageUtils {
	/*protected static final IntPointer METADATA_TYPES = new IntPointer(opencv_imgcodecs.IMAGE_METADATA_EXIF,
		opencv_imgcodecs.IMAGE_METADATA_XMP);*/

	protected ImageUtils() {}

	public static boolean canRead(final File file) throws IOException {
		FileUtils.isImageType(file);

		return opencv_imgcodecs.haveImageReader(file.getAbsolutePath());
	}

	public static void main(String[] args) {
		Loader.load(opencv_core.class);
		isSupportReadFormat(",png");
	}

	public static boolean isSupportReadFormat(final String format) {
		Validate.notBlank(format, "format 不可为空");

		return opencv_imgcodecs.haveImageReader(format.startsWith(".") ? StringUtils.EMPTY :
			FilenameUtils.EXTENSION_SEPARATOR + format);
	}

	public static boolean isSupportWriteFormat(final String format) {
		Validate.notBlank(format, "format 不可为空");

		return opencv_imgcodecs.haveImageWriter(format.startsWith(".") ? StringUtils.EMPTY :
			FilenameUtils.EXTENSION_SEPARATOR + format);
	}

	public static Size getSize(final File file) throws IOException {
		FileUtils.isImageType(file);

		try (Mat mat = opencv_imgcodecs.imread(file.getAbsolutePath())) {
			return mat.size();
		}
	}

	/*public static MatVector readMetadata(final File file) throws IOException {
		FileUtils.isImageType(file);

		MatVector metadata = new MatVector();
		try (Mat ignored = opencv_imgcodecs.imreadWithMetadata(file.getAbsolutePath(),
			METADATA_TYPES, metadata)) {
			return metadata;
		}
	}

	public static Mat readWithMetadata(final File file, MatVector metadata) throws IOException {
		FileUtils.isImageType(file);

		return opencv_imgcodecs.imreadWithMetadata(file.getAbsolutePath(), METADATA_TYPES, metadata);
	}*/
}
