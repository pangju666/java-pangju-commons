package io.github.pangju666.commons.compress.utils

import spock.lang.Specification

class GZipUtilsSpec extends Specification {

	def "GZIP格式检测与往返压缩解压"() {
		setup:
		File work = new File("commons-compress/target/test-work/gzip")
		work.mkdirs()
		File input = new File(work, "hello.txt")
		input.text = "hello world 你好"
		File gz = new File(work, "hello.txt.gz")
		File outFile = new File(work, "hello.out.txt")

		when:
		GZipUtils.compress(input, gz)

		then:
		gz.exists()
		GZipUtils.isGZip(gz)
		!GZipUtils.isGZip(input)

		when:
		GZipUtils.uncompress(gz, outFile)

		then:
		outFile.exists()
		outFile.text == input.text

		when:
		ByteArrayOutputStream bos = new ByteArrayOutputStream()
		GZipUtils.compress(new ByteArrayInputStream(input.text.getBytes("UTF-8")), bos)
		byte[] gzBytes = bos.toByteArray()

		then:
		GZipUtils.isGZip(gzBytes)

		when:
		ByteArrayOutputStream plain = new ByteArrayOutputStream()
		GZipUtils.uncompress(new ByteArrayInputStream(gzBytes), plain)

		then:
		new String(plain.toByteArray(), "UTF-8") == input.text
	}
}
