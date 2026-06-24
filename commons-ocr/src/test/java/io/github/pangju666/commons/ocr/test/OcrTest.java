package io.github.pangju666.commons.ocr.test;

import io.github.pangju666.commons.ocr.utils.OcrUtils;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;

public class OcrTest {
	@Test
	void testOcr() throws Exception {
		System.out.println(OcrUtils.ocrImage(ImageIO.read(new File("E:\\Roaming\\frame.jpg"))));
	}
}