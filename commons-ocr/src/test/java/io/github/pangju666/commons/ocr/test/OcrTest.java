package io.github.pangju666.commons.ocr.test;

import io.github.pangju666.commons.ocr.factory.TessBaseAPIFactory;
import io.github.pangju666.commons.ocr.lang.OcrConstants;
import io.github.pangju666.commons.ocr.utils.OcrUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.bytedeco.tesseract.TessBaseAPI;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.File;

public class OcrTest {
	@Test
	void testOcr() throws Exception {
		try (GenericObjectPool<TessBaseAPI> objectPool = new GenericObjectPool<>(new TessBaseAPIFactory(
			"src/main/resources/data"),
			OcrConstants.DEFAULT_TESS_BASE_API_POOL_CONFIG)) {
			TessBaseAPI tessBaseAPI = objectPool.borrowObject();
			try {
				String result = OcrUtils.ocrImage(tessBaseAPI, ImageIO.read(new File("E:\\Roaming\\frame.jpg")));
				System.out.println(result);
			} finally {
				objectPool.returnObject(tessBaseAPI);
			}
		}
	}
}