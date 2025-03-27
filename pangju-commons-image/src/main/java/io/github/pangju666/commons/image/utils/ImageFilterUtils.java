package io.github.pangju666.commons.image.utils;

import io.github.pangju666.commons.io.utils.IOUtils;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.apache.commons.lang3.ArrayUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class ImageFilterUtils {
	public static final int DEFAULT_MEDIAN_FILTER_SIZE = 3;
	public static final int DEFAULT_GAUSSIAN_KERNEL_SIZE = 5;
	public static final double DEFAULT_GAUSSIAN_SIGMA = 1.4;

	protected ImageFilterUtils() {
	}

	public static BufferedImage medianFiltering(final byte[] bytes) throws IOException {
		return medianFiltering(bytes, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	public static BufferedImage medianFiltering(final byte[] bytes, final int filterSize) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return medianFiltering(bufferedImage, filterSize);
	}

	public static BufferedImage medianFiltering(final ImageInputStream imageInputStream) throws IOException {
		return medianFiltering(imageInputStream, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	public static BufferedImage medianFiltering(final ImageInputStream imageInputStream, final int filterSize) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return medianFiltering(bufferedImage, filterSize);
	}

	public static BufferedImage medianFiltering(final File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return medianFiltering(bufferedImage, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	public static BufferedImage medianFiltering(final File file, final int filterSize) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return medianFiltering(bufferedImage, filterSize);
	}

	public static BufferedImage medianFiltering(final InputStream inputStream) throws IOException {
		return medianFiltering(inputStream, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	public static BufferedImage medianFiltering(final InputStream inputStream, final int filterSize) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return medianFiltering(bufferedImage, filterSize);
	}

	public static BufferedImage medianFiltering(final BufferedImage image) {
		return medianFiltering(image, DEFAULT_MEDIAN_FILTER_SIZE);
	}

	/**
	 * 中值滤波去噪
	 *
	 * @param image      输入图像
	 * @param filterSize 滤波器大小
	 * @return 输出图像
	 */
	public static BufferedImage medianFiltering(final BufferedImage image, final int filterSize) {
		// 获取图像宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建输出图像
		BufferedImage medianFilteringImage = new BufferedImage(width, height, image.getType());
		// 定义滤波器大小（这里使用3x3滤波器）
		int radius = filterSize / 2;

		// 遍历每个像素并进行中值滤波
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int[] pixels = new int[filterSize * filterSize];
				int index = 0;

				// 收集邻域像素
				for (int fy = -radius; fy <= radius; fy++) {
					for (int fx = -radius; fx <= radius; fx++) {
						int nx = x + fx;
						int ny = y + fy;
						if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
							pixels[index++] = image.getRGB(nx, ny) & 0xff;
						}
					}
				}

				// 计算中值
				int medianValue = findMedian(pixels, index);

				// 设置新的像素值
				int rgb = (medianValue << 16) + (medianValue << 8) + medianValue;
				medianFilteringImage.setRGB(x, y, rgb);
			}
		}
		return medianFilteringImage;
	}

	protected static int findMedian(final int[] array, final int length) {
		int[] sortedArray = new int[length];
		System.arraycopy(array, 0, sortedArray, 0, length);
		java.util.Arrays.sort(sortedArray);
		return sortedArray[length / 2];
	}

	public static BufferedImage gaussianFiltering(final byte[] bytes) throws IOException {
		return gaussianFiltering(bytes, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	public static BufferedImage gaussianFiltering(final byte[] bytes, final int kernelSize, final double sigma) throws IOException {
		if (ArrayUtils.isEmpty(bytes)) {
			return null;
		}
		UnsynchronizedByteArrayInputStream inputStream = IOUtils.toUnsynchronizedByteArrayInputStream(bytes);
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return gaussianFiltering(bufferedImage, kernelSize, sigma);
	}

	public static BufferedImage gaussianFiltering(final ImageInputStream imageInputStream) throws IOException {
		return gaussianFiltering(imageInputStream, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	public static BufferedImage gaussianFiltering(final ImageInputStream imageInputStream, final int kernelSize, final double sigma) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(imageInputStream);
		return gaussianFiltering(bufferedImage, kernelSize, sigma);
	}

	public static BufferedImage gaussianFiltering(final File file) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return gaussianFiltering(bufferedImage, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	public static BufferedImage gaussianFiltering(final File file, final int kernelSize, final double sigma) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(file);
		return gaussianFiltering(bufferedImage, kernelSize, sigma);
	}

	public static BufferedImage gaussianFiltering(final InputStream inputStream) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return gaussianFiltering(bufferedImage, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	public static BufferedImage gaussianFiltering(final InputStream inputStream, final int kernelSize, final double sigma) throws IOException {
		BufferedImage bufferedImage = ImageIO.read(inputStream);
		return gaussianFiltering(bufferedImage, kernelSize, sigma);
	}

	public static BufferedImage gaussianFiltering(final BufferedImage image) {
		return gaussianFiltering(image, DEFAULT_GAUSSIAN_KERNEL_SIZE, DEFAULT_GAUSSIAN_SIGMA);
	}

	/**
	 * 高斯滤波去噪
	 *
	 * @param image      输入图像
	 * @param kernelSize 高斯核大小
	 * @param sigma      标准差
	 * @return 输出图像
	 */
	public static BufferedImage gaussianFiltering(final BufferedImage image, final int kernelSize, final double sigma) {
		// 获取图像宽度和高度
		int width = image.getWidth();
		int height = image.getHeight();
		// 创建输出图像
		BufferedImage gaussianFilteringImage = new BufferedImage(width, height, image.getType());

		// 生成高斯核
		double[][] kernel = generateGaussianKernel(kernelSize, sigma);
		int radius = kernel.length / 2;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double rSum = 0, gSum = 0, bSum = 0;

				// 应用卷积
				for (int ky = -radius; ky <= radius; ky++) {
					for (int kx = -radius; kx <= radius; kx++) {
						int nx = x + kx;
						int ny = y + ky;
						if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
							int rgb = image.getRGB(nx, ny);
							int r = (rgb >> 16) & 0xff;
							int g = (rgb >> 8) & 0xff;
							int b = rgb & 0xff;

							double weight = kernel[ky + radius][kx + radius];

							rSum += r * weight;
							gSum += g * weight;
							bSum += b * weight;
						}
					}
				}

				// 设置新的像素值
				int rgb = ((int) rSum << 16) | ((int) gSum << 8) | (int) bSum;
				gaussianFilteringImage.setRGB(x, y, rgb);
			}
		}
		return gaussianFilteringImage;
	}

	protected static double[][] generateGaussianKernel(final int size, final double sigma) {
		double[][] kernel = new double[size][size];
		int radius = size / 2;
		double sum = 0.0;

		for (int y = -radius; y <= radius; y++) {
			for (int x = -radius; x <= radius; x++) {
				kernel[y + radius][x + radius] = Math.exp(-(x * x + y * y) / (2 * sigma * sigma));
				sum += kernel[y + radius][x + radius];
			}
		}

		// 归一化
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				kernel[y][x] /= sum;
			}
		}

		return kernel;
	}
}