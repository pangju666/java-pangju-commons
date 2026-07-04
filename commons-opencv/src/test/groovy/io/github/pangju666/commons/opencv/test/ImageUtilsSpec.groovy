package io.github.pangju666.commons.opencv.test

import io.github.pangju666.commons.opencv.utils.ImageEditor
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.IntPointer
import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.MatVector
import spock.lang.Specification

class ImageUtilsSpec extends Specification {
	def test() throws IOException {
		setup:
		File inputFile = new File("E:\\Roaming\\test.png")
		File outputFile = new File("E:\\Roaming\\output.png")

		/*Mat mat = ImageEditor.of(inputFile, opencv_imgcodecs.IMREAD_ANYCOLOR)
			.transparency(0.3)
			.toMat()
		opencv_imgcodecs.imwrite(outputFile.getAbsolutePath(), mat)*/

		IntPointer typePtr = new IntPointer(opencv_imgcodecs.IMAGE_METADATA_EXIF, opencv_imgcodecs.IMAGE_METADATA_XMP)
		MatVector metaVec = new MatVector();
		opencv_imgcodecs.imreadWithMetadata(inputFile.getAbsolutePath(), typePtr, metaVec,
			opencv_imgcodecs.IMREAD_UNCHANGED)

		// 遍历所有元数据类型，找到EXIF块
		int typeCount = (int) typePtr.limit();
		Mat exifRawMat = null;
		for (int i = 0; i < typeCount; i++) {
			int type = typePtr.get(i);
			if (type == opencv_imgcodecs.IMAGE_METADATA_EXIF) {
				exifRawMat = metaVec.get(i);
				break;
			}
		}


	}
}
