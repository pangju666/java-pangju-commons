package io.github.pangju666.commons.compress.utils

import spock.lang.Specification
import spock.lang.Unroll

class CompressUtilsSpec extends Specification {

	def "单文件压缩格式 gz/xz 往返"() {
		setup:
		File work = new File("commons-compress/target/test-work/dispatch/single")
		work.mkdirs()
		File input = new File(work, "plain.txt")
		input.text = "dispatch test 单文件"
		File out1 = new File(work, "plain.out.txt")
		File out2 = new File(work, "plain.out2.txt")
		File gz = new File(work, "plain.txt.gz")
		File xz = new File(work, "plain.txt.xz")

		when:
		CompressUtils.compress(input, gz)
		CompressUtils.compress(input, xz)

		then:
		gz.exists()
		xz.exists()

		when:
		CompressUtils.uncompress(gz, out1)
		ByteArrayOutputStream bos = new ByteArrayOutputStream()
		CompressUtils.uncompress(xz, bos)
		out2.bytes = bos.toByteArray()

		then:
		out1.text == input.text
		out2.text == input.text
	}

	def "归档格式 zip/tar/7z 往返"() {
		setup:
		File work = new File("commons-compress/target/test-work/dispatch/archive")
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
		File tar = new File(work, "archive.tar")
		File sevenz = new File(work, "archive.7z")

		when:
		CompressUtils.compress(srcDir, zip)
		CompressUtils.compress(srcDir, tar)
		CompressUtils.compress(srcDir, sevenz)

		then:
		zip.exists()
		tar.exists()
		sevenz.exists()

		when:
		File outZip = new File(work, "out-zip")
		File outTar = new File(work, "out-tar")
		File out7z = new File(work, "out-7z")
		CompressUtils.uncompressToDir(zip, outZip)
		CompressUtils.uncompressToDir(tar, outTar)
		CompressUtils.uncompressToDir(sevenz, out7z)

		then:
		new File(outZip, "src/empty").isDirectory()
		new File(outZip, "src/a.txt").text == "A"
		new File(outZip, "src/sub/b.txt").text == "B"
		new File(outTar, "src/empty").isDirectory()
		new File(outTar, "src/a.txt").text == "A"
		new File(outTar, "src/sub/b.txt").text == "B"
		new File(out7z, "src/empty").isDirectory()
		new File(out7z, "src/a.txt").text == "A"
		new File(out7z, "src/sub/b.txt").text == "B"
	}

	def "组合格式 tgz 与 tar.gz 往返"() {
		setup:
		File work = new File("commons-compress/target/test-work/dispatch/combined")
		work.mkdirs()
		File srcDir = new File(work, "src")
		new File(srcDir, "empty").mkdirs()
		File sub = new File(srcDir, "sub")
		sub.mkdirs()
		File a = new File(srcDir, "a.txt")
		a.text = "A"
		File b = new File(sub, "b.txt")
		b.text = "B"
		File tgz = new File(work, "archive.tgz")
		File tarGz = new File(work, "archive.tar.gz")

		when:
		CompressUtils.compress(srcDir, tgz)
		CompressUtils.compress(srcDir, tarGz)

		then:
		tgz.exists()
		tarGz.exists()

		when:
		File out1 = new File(work, "out-tgz")
		File out2 = new File(work, "out-targz")
		CompressUtils.uncompressToDir(tgz, out1)
		CompressUtils.uncompressToDir(tarGz, out2)

		then:
		new File(out1, "src/empty").isDirectory()
		new File(out1, "src/a.txt").text == "A"
		new File(out1, "src/sub/b.txt").text == "B"
		new File(out2, "src/empty").isDirectory()
		new File(out2, "src/a.txt").text == "A"
		new File(out2, "src/sub/b.txt").text == "B"
	}

	def "集合压缩到归档 zip/tar/7z/tgz/tar.gz 往返"() {
		setup:
		File work = new File("commons-compress/target/test-work/dispatch/collection")
		work.mkdirs()
		File dir = new File(work, "dir")
		dir.mkdirs()
		new File(dir, "d.txt").text = "D"
		File f1 = new File(work, "f1.txt")
		f1.text = "F1"
		File f2 = new File(work, "f2.txt")
		f2.text = "F2"
		Collection<File> inputs = [f1, f2, dir]
		File zip = new File(work, "bundle.zip")
		File tar = new File(work, "bundle.tar")
		File sevenz = new File(work, "bundle.7z")
		File tgz = new File(work, "bundle.tgz")
		File tarGz = new File(work, "bundle.tar.gz")

		when:
		CompressUtils.compress(inputs, zip)

		then:
		zip.exists()

		when:
		CompressUtils.compress(inputs, tar)

		then:
		tar.exists()

		when:
		CompressUtils.compress(inputs, sevenz)

		then:
		sevenz.exists()

		when:
		CompressUtils.compress(inputs, tgz)

		then:
		tgz.exists()

		when:
		CompressUtils.compress(inputs, tarGz)

		then:
		tarGz.exists()

		when:
		File outZip = new File(work, "out-zip")
		File outTar = new File(work, "out-tar")
		File out7z = new File(work, "out-7z")
		File outTgz = new File(work, "out-tgz")
		File outTarGz = new File(work, "out-targz")
		CompressUtils.uncompressToDir(zip, outZip)
		CompressUtils.uncompressToDir(tar, outTar)
		CompressUtils.uncompressToDir(sevenz, out7z)
		CompressUtils.uncompressToDir(tgz, outTgz)
		CompressUtils.uncompressToDir(tarGz, outTarGz)

		then:
		new File(outZip, "f1.txt").text == "F1"
		new File(outZip, "f2.txt").text == "F2"
		new File(outZip, "dir/d.txt").text == "D"
		new File(outTar, "f1.txt").text == "F1"
		new File(outTar, "f2.txt").text == "F2"
		new File(outTar, "dir/d.txt").text == "D"
		new File(out7z, "f1.txt").text == "F1"
		new File(out7z, "f2.txt").text == "F2"
		new File(out7z, "dir/d.txt").text == "D"
		new File(outTgz, "f1.txt").text == "F1"
		new File(outTgz, "f2.txt").text == "F2"
		new File(outTgz, "dir/d.txt").text == "D"
		new File(outTarGz, "f1.txt").text == "F1"
		new File(outTarGz, "f2.txt").text == "F2"
		new File(outTarGz, "dir/d.txt").text == "D"
	}

	@Unroll
	def "非法分发到流/文件抛出异常: #format"() {
		setup:
		File work = new File("commons-compress/target/test-work/dispatch/errors")
		work.mkdirs()
		File srcDir = new File(work, "src")
		srcDir.mkdirs()
		new File(srcDir, "x.txt").text = "X"
		ByteArrayOutputStream bos = new ByteArrayOutputStream()
		File archive = new File(work, "archive." + format)
		CompressUtils.compress(srcDir, archive)

		when:
		CompressUtils.uncompress(archive, bos)

		then:
		thrown(UnsupportedOperationException)

		where:
		format << ["zip", "tar", "7z"]
	}

	@Unroll
	def "非法分发到单文件抛出异常: #format"() {
		setup:
		File work = new File("commons-compress/target/test-work/dispatch/errors2")
		work.mkdirs()
		File srcDir = new File(work, "src")
		srcDir.mkdirs()
		new File(srcDir, "y.txt").text = "Y"
		File outFile = new File(work, "out.txt")
		File archive = new File(work, "archive." + format)
		CompressUtils.compress(srcDir, archive)

		when:
		CompressUtils.uncompress(archive, outFile)

		then:
		thrown(UnsupportedOperationException)

		where:
		format << ["zip", "tar", "7z"]
	}
}
