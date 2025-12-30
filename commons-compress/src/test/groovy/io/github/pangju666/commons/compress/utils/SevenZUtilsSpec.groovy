package io.github.pangju666.commons.compress.utils

import org.apache.commons.compress.archivers.sevenz.SevenZFile
import spock.lang.Specification

class SevenZUtilsSpec extends Specification {

	def "7z格式检测与目录往返压缩解压"() {
		setup:
		File work = new File("commons-compress/target/test-work/7z")
		work.mkdirs()
		File srcDir = new File(work, "src")
		new File(srcDir, "empty").mkdirs()
		File sub = new File(srcDir, "sub")
		sub.mkdirs()
		File a = new File(srcDir, "a.txt")
		a.text = "A"
		File b = new File(sub, "b.txt")
		b.text = "B"
		File sevenz = new File(work, "archive.7z")
		File outDir = new File(work, "out")

		when:
		SevenZUtils.compress(srcDir, sevenz)

		then:
		sevenz.exists()
		SevenZUtils.is7z(sevenz)
		!SevenZUtils.is7z(a)

		when:
		SevenZUtils.uncompress(sevenz, outDir)

		then:
		new File(outDir, "src/empty").isDirectory()
		new File(outDir, "src/a.txt").text == "A"
		new File(outDir, "src/sub/b.txt").text == "B"

		when:
		try (SevenZFile zf = SevenZFile.builder().setFile(sevenz).get()) {
			SevenZUtils.uncompress(zf, new File(work, "out2"))
		}

		then:
		new File(work, "out2/src/a.txt").text == "A"
		new File(work, "out2/src/sub/b.txt").text == "B"
	}
}
