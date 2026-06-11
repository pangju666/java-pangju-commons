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
			new File("F:\\IDMDownload\\convert_output.mp3"),
			Audio.builder().format("mp3").build());
	}

	@Test
	void testTrimByDuration() throws IOException {
		AudioUtils.cut(MediaResource.of(new File("F:\\IDMDownload\\file_example_MP3_5MG.mp3")),
			new File("F:\\IDMDownload\\cut_output.mp3"),
			Duration.ofSeconds(8));
	}

	@Test
	void testMerge() throws IOException {
		AudioUtils.concat(List.of(MediaResource.of(new File("F:\\IDMDownload\\file_example_MP3_5MG.mp3")),
				MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.flac"))),
			new File("F:\\IDMDownload\\concat_output.mp3"),
			"mp3");
	}

	@Test
	void testRemixShortest() throws IOException {
		AudioUtils.remix(MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			MediaResource.of(new File("F:\\IDMDownload\\file_example_MP3_5MG.mp3")),
			new File("F:\\IDMDownload\\mix_shortest_output.wav"), 0.5f);
	}

	@Test
	void testRemixLongest() throws IOException {
		AudioUtils.remix(MediaResource.of(new File("F:\\IDMDownload\\file_example_MP3_5MG.mp3")),
			MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			new File("F:\\IDMDownload\\mix_longest_output.mp3"), 1f);
	}

	@Test
	void testAdjustSpeed() throws IOException {
		AudioUtils.adjustSpeed(MediaResource.of(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")),
			new File("F:\\IDMDownload\\speed_output.wav"), 2.5f);
	}
}
