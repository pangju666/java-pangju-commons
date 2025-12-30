package io.github.pangju666.commons.compress.utils

import spock.lang.Specification

class TarUtilsSpec extends Specification {

	def "TAR格式检测与目录往返压缩解压"() {
		setup:
		File work = new File("commons-compress/target/test-work/tar")
		work.mkdirs()
		File srcDir = new File(work, "src")
		new File(srcDir, "empty").mkdirs()
		File sub = new File(srcDir, "sub")
		sub.mkdirs()
		File a = new File(srcDir, "a.txt")
		a.text = "A"
		File b = new File(sub, "b.txt")
		b.text = "B"
		File tar = new File(work, "archive.tar")
		File outDir = new File(work, "out")

		when:
		TarUtils.compress(srcDir, tar)

		then:
		tar.exists()
		TarUtils.isTar(tar)
		!TarUtils.isTar(a)

		when:
		TarUtils.uncompress(tar, outDir)

		then:
		new File(outDir, "src/empty").isDirectory()
		new File(outDir, "src/a.txt").text == "A"
		new File(outDir, "src/sub/b.txt").text == "B"

		when:
		byte[] bytes = tar.bytes

		then:
		TarUtils.isTar(bytes)

		when:
		File outDir2 = new File(work, "out2")
		TarUtils.uncompress(new ByteArrayInputStream(bytes), outDir2)

		then:
		new File(outDir2, "src/a.txt").text == "A"
		new File(outDir2, "src/sub/b.txt").text == "B"
	}
}
