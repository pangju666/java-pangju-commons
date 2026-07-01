package io.github.pangju666.commons.ffmpeg.test;

import io.github.pangju666.commons.ffmpeg.model.MediaResource;
import io.github.pangju666.commons.ffmpeg.model.Video;
import io.github.pangju666.commons.ffmpeg.utils.FFmpegFiltersBuilder;
import io.github.pangju666.commons.ffmpeg.utils.VideoUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class VideoUtilsTest {
	@Test
	void testTranscode() throws IOException {
		// 视频路径
		File inputVideo = new File("F:\\IDMDownload\\1416529-uhd_3840_2160_30fps.avi");
		// 输出图片
		File outputVideo = new File("E:\\Roaming\\output\\transcode_video.mkv");

		VideoUtils.transcode(MediaResource.of(inputVideo), outputVideo, Video.MKV_1080P);
	}

	@Test
	void testExtractVideo() throws IOException {
		// 视频路径
		File inputVideo = new File("F:\\IDMDownload\\video_audio.mp4");
		// 输出图片
		File outputVideo = new File("E:\\Roaming\\output\\extract_video.mkv");

		VideoUtils.extractVideo(MediaResource.of(inputVideo), outputVideo);
	}

	@Test
	void testExtractAudio() throws IOException {
		// 视频路径
		File inputVideo = new File("F:\\IDMDownload\\video_audio.mp4");
		// 输出图片
		File outputVideo = new File("E:\\Roaming\\output\\extract_audio.aac");

		VideoUtils.extractAudio(MediaResource.of(inputVideo), outputVideo);
	}

	@Test
	void testCutEnd() throws IOException {
		VideoUtils.cut(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
			new File("E:\\Roaming\\output\\cut_end_video_output.mp4"), Duration.ofSeconds(8));
	}

	@Test
	void testCutStartEnd() throws IOException {
		VideoUtils.cut(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
			new File("E:\\Roaming\\output\\cut_start_end_video_output.mp4"),
			Duration.ofSeconds(10), Duration.ofSeconds(20));
	}

	@Test
	void testConcat() throws IOException {
		VideoUtils.concat(List.of(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
				MediaResource.of(new File("F:\\IDMDownload\\1416529-uhd_2560_1440_30fps.mov"))),
			new File("E:\\Roaming\\output\\concat_video_output.mp4"));
	}

	@Test
	void testAdjustSpeed() throws IOException {
		VideoUtils.adjustSpeed(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
			new File("E:\\Roaming\\output\\adjust_speed_video_output.mp4"), 0.5f);
	}

	@Test
	void testGrabImageAtTimestamp() throws IOException {
		VideoUtils.grabImageAtTimestamp(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
			Duration.ofSeconds(5), new File("E:\\Roaming\\output\\image.png"));
	}

	@Test
	void testGrabImagePeriodically() throws IOException {
		VideoUtils.grabImagePeriodically(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
			5, TimeUnit.SECONDS, "png", new File("E:\\Roaming\\output\\video_images"));
	}

	@Test
	void testCropByCenter() throws IOException {
		VideoUtils.cropByCenter(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
			new File("E:\\Roaming\\output\\crop_center_video.mp4"), 200, 200, false);
	}

	@Test
	void testCropByRect() throws IOException {
		VideoUtils.cropByRect(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
			new File("E:\\Roaming\\output\\crop_rect_video.mp4"), 50, 50, 200, 200,
			false);
	}

	@Test
	void testCropByOffset() throws IOException {
		VideoUtils.cropByOffset(MediaResource.of(new File("F:\\IDMDownload\\video_audio.mp4")),
			new File("E:\\Roaming\\output\\crop_offset_video.mp4"), 50, 50,
			50, 50, false);
	}

	@Test
	void testReplaceAudio() throws IOException {
		VideoUtils.replaceAudio(MediaResource.of(new File("E:\\Roaming\\output\\video_audio.mp4")),
			MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			new File("E:\\Roaming\\output\\set_audio_video.mp4"), true);
	}

	@Test
	void testAddBgm() throws IOException {
		VideoUtils.addBgm(MediaResource.of(new File("E:\\Roaming\\output\\video_audio.mp4")),
			MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			new File("E:\\Roaming\\output\\bgm_video.mp4"));
	}

	@Test
	void testAddTextWatermark() throws IOException {
		VideoUtils.addTextWatermark(MediaResource.of(new File("E:\\Roaming\\output\\video_audio.mp4")),
			new File("E:\\Roaming\\output\\text_watermark_output.mp4"), null, "DEMO",
			new File("E:/Roaming/output/simkai.ttf"));
	}

	@Test
	void testAddImageWatermark() throws IOException {
		VideoUtils.addImageWatermark(MediaResource.of(new File("E:\\Roaming\\output\\video_audio.mp4")),
			new File("E:\\Roaming\\output\\image_watermark_output.mp4"),
			new File("E:/Roaming/output/watermark.jpg"));
	}

	@Test
	void a() throws IOException {
		String textWatermarkFilter = FFmpegFiltersBuilder.video()
			.addInput("v2", "test=1")
			.addFileSource("v1", new File("E:/Roaming/output/watermark.jpg"), "aresample=44100")
			//.addInput("v2", "test=1")
			.addInput()
			.addGlobalFilter("amix", "inputs=2", "dropout_transition=0", "duration=first", "weights=1 1")
			.build();

		System.out.println(textWatermarkFilter);
	}
}
