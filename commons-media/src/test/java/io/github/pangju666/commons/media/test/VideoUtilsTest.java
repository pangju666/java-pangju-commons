package io.github.pangju666.commons.media.test;

import io.github.pangju666.commons.media.model.MediaResource;
import io.github.pangju666.commons.media.model.Video;
import io.github.pangju666.commons.media.utils.VideoUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

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
		File outputVideo = new File("F:\\IDMDownload\\transcode_video.mkv");

		VideoUtils.transcode(MediaResource.of(inputVideo), outputVideo, Video.MKV_1080P);
	}
}
