package io.github.pangju666.commons.opencv.test;

import io.github.pangju666.commons.opencv.utils.ImageUtils;
import org.bytedeco.opencv.opencv_core.Size;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class ImageUtilsTest {
	@Test
	void testImageUtils() throws IOException {
		/*Size size = ImageUtils.getSize(new File("E:\\Roaming\\camera.jpg"));
		System.out.println(size);*/
		ImageUtils.isSupportReadFormat(".png");
	}
}
