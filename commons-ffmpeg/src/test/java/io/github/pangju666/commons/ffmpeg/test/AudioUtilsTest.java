package io.github.pangju666.commons.ffmpeg.test;

import io.github.pangju666.commons.ffmpeg.model.Audio;
import io.github.pangju666.commons.ffmpeg.model.MediaResource;
import io.github.pangju666.commons.ffmpeg.utils.AudioUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * Hello world!
 *
 */
public class AudioUtilsTest {
	@Test
	void testTranscode() throws IOException {
		AudioUtils.transcode(MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			new File("E:\\Roaming\\output\\transcode_output.mp3"), Audio.MP3);
	}

	@Test
	void testCutEnd() throws IOException {
		AudioUtils.cut(MediaResource.of(new File("F:\\IDMDownload\\file_example_MP3_5MG.mp3")),
			new File("E:\\Roaming\\output\\cut_end_output.mp3"), Duration.ofSeconds(8));
	}

	@Test
	void testCutStartEnd() throws IOException {
		AudioUtils.cut(MediaResource.of(new File("F:\\IDMDownload\\file_example_MP3_5MG.mp3")),
			new File("E:\\Roaming\\output\\cut_start_end_output.mp3"),
			Duration.ofSeconds(10), Duration.ofSeconds(20));
	}

	@Test
	void testConcat() throws IOException {
		AudioUtils.concat(List.of(MediaResource.of(new File("F:\\IDMDownload\\file_example_MP3_5MG.mp3")),
				MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.flac"))),
			new File("E:\\Roaming\\output\\concat_output.mp3"));
	}

	@Test
	void testAddBgm() throws IOException {
		AudioUtils.addBgm(MediaResource.of(new File("F:\\IDMDownload\\file_example_MP3_5MG.mp3")),
			MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			new File("E:\\Roaming\\output\\bgm_output.wav"));
	}

	@Test
	void testAdjustSpeed() throws IOException {
		AudioUtils.adjustSpeed(MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			new File("E:\\Roaming\\output\\adjust_speed_output.wav"), 0.5f);
	}

	@Test
	void testAdjustVolume() throws IOException {
		AudioUtils.adjustVolume(MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			new File("E:\\Roaming\\output\\adjust_volume_output.wav"), -5);
	}
}
