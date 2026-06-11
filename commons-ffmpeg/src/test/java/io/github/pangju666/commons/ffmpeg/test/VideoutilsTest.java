package io.github.pangju666.commons.ffmpeg.test;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Hello world!
 *
 */
public class VideoutilsTest {
	@Test
	void testOutput() {
		// 视频路径
		String videoPath = "E:\\视频素材\\视频素材\\心形烟花.mp4";
		// 输出图片
		String outputImage = "E:\\Roaming\\frame.jpg";

		try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
			grabber.start();

			// 跳到第 2 秒（单位：微秒）
			grabber.setTimestamp(2 * 1000_000L);
			Frame frame = grabber.grabImage(); // 抓取一帧

			// 转成图片并保存
			Java2DFrameConverter converter = new Java2DFrameConverter();
			BufferedImage image = converter.convert(frame);
			ImageIO.write(image, "jpg", new File(outputImage));

			System.out.println("帧提取完成：" + outputImage);
			grabber.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
