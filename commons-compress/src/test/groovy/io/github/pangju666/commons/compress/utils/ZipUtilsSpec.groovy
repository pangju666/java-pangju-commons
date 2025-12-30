package io.github.pangju666.commons.compress.utils

import org.apache.commons.compress.archivers.zip.ZipFile
import spock.lang.Specification

class ZipUtilsSpec extends Specification {

	def "ZIP格式检测与目录往返压缩解压"() {
		setup:
		File work = new File("commons-compress/target/test-work/zip")
		work.mkdirs()
		File srcDir = new File(work, "src")
		new File(srcDir, "empty").mkdirs()
		File sub = new File(srcDir, "sub")
		sub.mkdirs()
		File a = new File(srcDir, "a.txt")
		a.text = "A"
		File b = new File(sub, "b.txt")
		b.text = "B"
		File zip = new File(work, "archive.zip")
		File outDir = new File(work, "out")

		when:
		ZipUtils.compress(srcDir, zip)

		then:
		zip.exists()
		ZipUtils.isZip(zip)
		!ZipUtils.isZip(a)

		when:
		ZipUtils.uncompress(zip, outDir)

		then:
		new File(outDir, "src/empty").isDirectory()
		new File(outDir, "src/a.txt").text == "A"
		new File(outDir, "src/sub/b.txt").text == "B"

		when:
		byte[] bytes = zip.bytes

		then:
		ZipUtils.isZip(bytes)

		when:
		File outDir2 = new File(work, "out2")
		ZipUtils.uncompress(new ByteArrayInputStream(bytes), outDir2)

		then:
		new File(outDir2, "src/a.txt").text == "A"
		new File(outDir2, "src/sub/b.txt").text == "B"

		when:
		try (ZipFile zf = new ZipFile(zip)) {
			ZipUtils.uncompress(zf, new File(work, "out3"))
		}

		then:
		new File(work, "out3/src/a.txt").text == "A"
		new File(work, "out3/src/sub/b.txt").text == "B"
	}
}
