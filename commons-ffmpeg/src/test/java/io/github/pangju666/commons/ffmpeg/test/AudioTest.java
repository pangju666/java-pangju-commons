package io.github.pangju666.commons.ffmpeg.test;

import io.github.pangju666.commons.ffmpeg.model.Audio;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

/**
 * Hello world!
 *
 */
public class AudioTest {
	@Test
	void testOutput() throws IOException {
		Audio audio = Audio.builder(new File("F:\\IDMDownload\\suzume_no_tojimari.wav")).build();
		System.out.println(audio);
	}
}
