
package io.github.pangju666.commons.image.utils;

import io.github.pangju666.commons.image.lang.ImageConstants;
import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageProcessUtils {
	protected ImageProcessUtils() {
	}

	public static BufferedImage binarization(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binarization(bytes, computeBinarizationThreshold(bufferedImage));
	}

	public static BufferedImage binarization(final byte[] bytes, final int Threshold) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binarization(bufferedImage, Threshold);
	}

	public static BufferedImage binarization(final ImageInputStream imageInputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return binarization(imageInputStream, computeBinarizationThreshold(bufferedImage));
	}

	public static BufferedImage binarization(final ImageInputStream imageInputStream, final int threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return binarization(bufferedImage, threshold);
	}

	public static BufferedImage binarization(final File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return binarization(bufferedImage, computeBinarizationThreshold(bufferedImage));
	}

	public static BufferedImage binarization(final File file, final int threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return binarization(bufferedImage, threshold);
	}

	public static BufferedImage binarization(final InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binarization(bufferedImage, computeBinarizationThreshold(bufferedImage));
	}

	public static BufferedImage binarization(final InputStream inputStream, final int threshold) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return binarization(bufferedImage, threshold);
	}

	public static BufferedImage binarization(final BufferedImage image) {
		return binarization(image, computeBinarizationThreshold(image));
	}

	/**
	 * 图像二值化
	 *
	 * @param image     输入图像
	 * @param threshold 阈值
	 * @return 输出图像
	 */
	public static BufferedImage binarization(final BufferedImage image, int threshold) {
		// 获取图像宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建输出图像（TYPE_BYTE_BINARY表示创建二值化图像）
		BufferedImage binarizationImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

		// 遍历每个像素并进行二值化处理
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// 获取该点的颜色
				int color = image.getRGB(x, y);

				// 提取灰度值
				int grayLevel = color & 0xff;

				// 根据阈值决定是黑色还是白色
				if (grayLevel > threshold) {
					binarizationImage.setRGB(x, y, ImageConstants.WHITE_HEX_RGB); // 白色
				} else {
					binarizationImage.setRGB(x, y, ImageConstants.BLACK_HEX_RGB); // 黑色
				}
			}
		}
		return binarizationImage;
	}

	public static BufferedImage grayscaleConversion(final byte[] bytes) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return grayscaleConversion(bufferedImage);
	}

	public static BufferedImage grayscaleConversion(final ImageInputStream imageInputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return grayscaleConversion(bufferedImage);
	}

	public static BufferedImage grayscaleConversion(final File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return grayscaleConversion(bufferedImage);
	}

	public static BufferedImage grayscaleConversion(final InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return grayscaleConversion(bufferedImage);
	}

	public static BufferedImage grayscaleConversion(final BufferedImage image) {
		// 获取图像宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建输出图像（TYPE_BYTE_GRAY表示创建灰度图像）
		BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		// 遍历每个像素并进行灰度化处理
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// 获取该点的颜色
				int color = image.getRGB(x, y);

				// 提取RGB分量
				int red = (color >> 16) & 0xff;
				int green = (color >> 8) & 0xff;
				int blue = color & 0xff;

				// 计算灰度值（这里使用了加权平均法，权重反映了人眼对不同颜色的敏感度）
				int grayLevel = (int) (0.3 * red + 0.59 * green + 0.11 * blue);

				// 将灰度值设置回像素（灰度图像是单通道，因此只需要一个值）
				int grayColor = (grayLevel << 16) + (grayLevel << 8) + grayLevel;
				grayImage.setRGB(x, y, grayColor);
			}
		}
		return grayImage;
	}

	protected static int computeBinarizationThreshold(final BufferedImage image) {
		int[] histogram = new int[256];
		int total = image.getWidth() * image.getHeight();

		// 计算直方图
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int color = image.getRGB(x, y);
				int grayLevel = color & 0xff;
				histogram[grayLevel]++;
			}
		}

		double sum = 0;
		for (int i = 0; i < 256; i++) sum += i * histogram[i];

		double sumB = 0;
		int wB = 0;
		int wF;

		double varMax = 0;
		int threshold = 0;

		for (int t = 0; t < 256; t++) {
			wB += histogram[t];               // Weight Background
			if (wB == 0) continue;

			wF = total - wB;                  // Weight Foreground
			if (wF == 0) break;

			sumB += t * histogram[t];

			double mB = sumB / wB;            // Mean Background
			double mF = (sum - sumB) / wF;    // Mean Foreground

			// Calculate Between Class Variance
			double varBetween = (double) wB * (double) wF * (mB - mF) * (mB - mF);

			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}
		return threshold;
	}
}