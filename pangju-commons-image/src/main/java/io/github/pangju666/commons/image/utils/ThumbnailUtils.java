package io.github.pangju666.commons.image.utils;

import io.github.pangju666.commons.image.model.ImageSize;

public class ThumbnailUtils {
	public static ImageSize computeScaleSizeByWidth(final int imageWidth, final int imageHeight, final int targetWidth) {
		if (imageWidth > imageHeight) {
			double ratio = (double) imageWidth / imageHeight;
			return new ImageSize(targetWidth, (int) Math.max(targetWidth / ratio, 1));
		}
		double ratio = (double) imageHeight / imageWidth;
		return new ImageSize(targetWidth, (int) Math.max(targetWidth * ratio, 1));
	}

	public static ImageSize computeScaleSizeByHeight(final int imageWidth, final int imageHeight, final int targetHeight) {
		if (imageWidth > imageHeight) {
			double ratio = (double) imageWidth / imageHeight;
			return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
		}
		double ratio = (double) imageHeight / imageWidth;
		return new ImageSize((int) Math.max(targetHeight / ratio, 1), targetHeight);
	}

	public static ImageSize computeScaleSize(final int imageWidth, final int imageHeight,
											 final int targetWidth, final int targetHeight) {
		double ratio = (double) imageWidth / imageHeight;
		if (imageWidth > imageHeight) {
			double actualHeight = Math.max(targetWidth / ratio, 1);
			if (actualHeight > targetHeight) {
				return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
			}
			return new ImageSize(targetWidth, (int) actualHeight);
		} else {
			double actualWidth = Math.max(targetHeight / ratio, 1);
			if (actualWidth > targetWidth) {
				return new ImageSize((int) Math.max(targetHeight * ratio, 1), targetHeight);
			}
			return new ImageSize((int) actualWidth, targetHeight);
		}
	}
}
