package io.github.pangju666.commons.tesseract.test;

import io.github.pangju666.commons.tesseract.utils.OcrUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

public class OcrTest {
	@Test
	void testOcr() throws Exception {
		System.out.println(OcrUtils.ocrImage(new File("E:\\Roaming\\frame.jpg")));
	}
}