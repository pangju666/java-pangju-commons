package io.github.pangju666.commons.compress.utils

import spock.lang.Specification

class XZUtilsSpec extends Specification {

	def "XZ格式检测与往返压缩解压"() {
		setup:
		File work = new File("commons-compress/target/test-work/xz")
		work.mkdirs()
		File input = new File(work, "hello.txt")
		input.text = "xz content 数据"
		File xz = new File(work, "hello.txt.xz")
		File outFile = new File(work, "hello.out.txt")

		when:
		XZUtils.compress(input, xz)

		then:
		xz.exists()
		XZUtils.isXZ(xz)
		!XZUtils.isXZ(input)

		when:
		XZUtils.uncompress(xz, outFile)

		then:
		outFile.exists()
		outFile.text == input.text

		when:
		ByteArrayOutputStream bos = new ByteArrayOutputStream()
		XZUtils.compress(new ByteArrayInputStream(input.text.getBytes("UTF-8")), bos)
		byte[] xzBytes = bos.toByteArray()

		then:
		XZUtils.isXZ(xzBytes)

		when:
		ByteArrayOutputStream plain = new ByteArrayOutputStream()
		XZUtils.uncompress(new ByteArrayInputStream(xzBytes), plain)

		then:
		new String(plain.toByteArray(), "UTF-8") == input.text
	}
}
