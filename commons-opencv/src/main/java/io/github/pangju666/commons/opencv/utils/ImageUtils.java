package io.github.pangju666.commons.opencv.utils;

import io.github.pangju666.commons.io.lang.IOConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import io.github.pangju666.commons.io.utils.FilenameUtils;
import io.github.pangju666.commons.io.utils.IOUtils;
import io.github.pangju666.commons.opencv.lang.OpencvConstants;
import org.apache.commons.io.input.UnsynchronizedBufferedInputStream;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.Validate;
import org.bytedeco.javacpp.BytePointer;
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
	protected ImageUtils() {}

	public static boolean canRead(final File file) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		return opencv_imgcodecs.haveImageReader(file.getAbsolutePath());
	}

	public static boolean canWrite(final String format) {
		Validate.notBlank(format, "format 不可为空");

		return opencv_imgcodecs.haveImageWriter(format.startsWith(FilenameUtils.EXTENSION_SEPARATOR_STR) ?
			StringUtils.EMPTY : FilenameUtils.EXTENSION_SEPARATOR + format);
	}

	public static Size getSize(final File file) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		try (Mat mat = opencv_imgcodecs.imread(file.getAbsolutePath())) {
			if (mat.isNull() || mat.empty()) {
				return null;
			}
			return mat.size();
		}
	}

	public static Mat read(final File file) throws IOException {
		return read(file, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static Mat read(final File file, final int flags) throws IOException {
		Validate.isTrue(FileUtils.isImageType(file), "file 不是一个图像文件");

		return opencv_imgcodecs.imread(file.getAbsolutePath(), flags);
	}

	public static Mat read(final InputStream inputStream) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return read(inputStream.readAllBytes(), OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static Mat read(final InputStream inputStream, final int flags) throws IOException {
		Validate.notNull(inputStream, "inputStream 不可为 null");

		return read(inputStream.readAllBytes(), flags);
	}

	public static Mat read(final byte[] bytes) {
		return read(bytes, OpencvConstants.DEFAULT_IMAGE_COLOR_TYPE);
	}

	public static Mat read(final byte[] bytes, final int flags) {
		Validate.isTrue(ArrayUtils.isNotEmpty(bytes), "bytes 不可为空");

		String mimeType = IOConstants.getDefaultTika().detect(bytes);
		Validate.isTrue(Strings.CS.startsWith(mimeType, IOConstants.IMAGE_MIME_TYPE_PREFIX),
			"file 不是一个图像文件");

		try (BytePointer bytePointer = new BytePointer(bytes);
		     Mat bufMat = new Mat(1, bytes.length, opencv_core.CV_8UC1, bytePointer)) {
			return opencv_imgcodecs.imdecode(bufMat, flags);
		}
	}
}
