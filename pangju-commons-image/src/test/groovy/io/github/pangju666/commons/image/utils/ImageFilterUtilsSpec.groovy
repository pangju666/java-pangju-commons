package io.github.pangju666.commons.image.utils

import boofcv.alg.filter.blur.BlurImageOps
import boofcv.alg.filter.blur.MedianImageOps
import boofcv.io.image.UtilImageIO
import boofcv.struct.image.GrayU8
import spock.lang.Specification

import javax.imageio.ImageIO

class ImageFilterUtilsSpec extends Specification {

	def "gray"() {
		setup:
		def inputFile = new File("src\\test\\resources\\large.png")
		File outputFile = new File("src\\test\\resources\\gray.png")
		/*def output = ImageFilterUtils.grayscale(ImageIO.read(inputFile))
		println ImageIO.write(output, "png", outputFile)*/

		// 将 BufferedImage 转换为 BoofCV 的 GrayU8 对象
		GrayU8 input = UtilImageIO.convertFrom(ImageIO.read(inputFile), (GrayU8) null);

		// 中值滤波
		GrayU8 medianFiltered = new GrayU8(input.width, input.height);
		MedianImageOps.median(input, 5, medianFiltered); // 5 是窗口大小

		// 高斯滤波
		GrayU8 gaussianFiltered = new GrayU8(input.width, input.height);
		BlurImageOps.gaussian(input, gaussianFiltered, -1, 5, null); // 5 是标准差

		// 保存结果
		UtilImageIO.saveImage(medianFiltered, "median_filtered.jpg");
		UtilImageIO.saveImage(gaussianFiltered, "gaussian_filtered.jpg");
	}
}