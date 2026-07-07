package io.github.pangju666.commons.io.utils

import net.openhft.hashing.LongHashFunction
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.nio.file.Path

class FileUtilsSpec extends Specification {
	@TempDir
	Path tempDir

	static final byte[] PASSWORD_16 = "1234567890123456".getBytes()
	static final byte[] IV_16 = "1234567890123456".bytes

	def "computeDigest 空文件与修改后不同"() {
		given:
		File f = tempDir.resolve("d.txt").toFile()
		f.createNewFile()

		expect:
		FileUtils.computeDigest(f) == "0000000000000000"

		when:
		f.text = "abc"
		def d1 = FileUtils.computeDigest(f)
		f.text = "abcd"
		def d2 = FileUtils.computeDigest(f)

		then:
		d1.size() == 16
		d1 != d2
	}

	def "computeDigest 小文件读取全部数据"() {
		given:
		File f = tempDir.resolve("small.txt").toFile()
		f.text = "small file content"

		when:
		def digest1 = FileUtils.computeDigest(f)
		def digest2 = FileUtils.computeDigest(f)

		then:
		digest1 == digest2
		digest1.size() == 16
	}

	def "computeDigest 大文件使用三段采样"() {
		given:
		File f = tempDir.resolve("large.dat").toFile()
		def data = new byte[10000]
		new Random().nextBytes(data)
		f.bytes = data

		when:
		def digest1 = FileUtils.computeDigest(f)
		def digest2 = FileUtils.computeDigest(f)

		then:
		digest1 == digest2
		digest1.size() == 16
	}

	def "computeDigest 自定义采样大小"() {
		given:
		File f = tempDir.resolve("sample.dat").toFile()
		def data = new byte[1000]
		new Random().nextBytes(data)
		f.bytes = data

		when:
		def digest1 = FileUtils.computeDigest(f, 100)
		def digest2 = FileUtils.computeDigest(f, 200)

		then:
		digest1 != digest2
	}

	def "computeDigest 自定义哈希函数"() {
		given:
		File f = tempDir.resolve("hash.dat").toFile()
		f.text = "hash test content"
		def hashFunc = LongHashFunction.xx()

		when:
		def digest = FileUtils.computeDigest(f, 1024, hashFunc)

		then:
		digest.size() == 16
	}

	def "computeDigest null文件抛异常"() {
		when:
		FileUtils.computeDigest(null as File)

		then:
		thrown(NullPointerException)
	}

	def "computeDigest null哈希函数抛异常"() {
		given:
		File f = tempDir.resolve("nullhash.txt").toFile()
		f.text = "test"

		when:
		FileUtils.computeDigest(f, 1024, null)

		then:
		thrown(NullPointerException)
	}

	def "computeDigest 非法采样大小抛异常"() {
		given:
		File f = tempDir.resolve("invalidsize.txt").toFile()
		f.text = "test"

		when:
		FileUtils.computeDigest(f, 0)

		then:
		thrown(IllegalArgumentException)
	}

	def "computeDigest 相同内容相同大小摘要相同"() {
		given:
		File f1 = tempDir.resolve("same1.txt").toFile()
		File f2 = tempDir.resolve("same2.txt").toFile()
		f1.text = "identical content"
		f2.text = "identical content"

		when:
		def digest1 = FileUtils.computeDigest(f1)
		def digest2 = FileUtils.computeDigest(f2)

		then:
		digest1 == digest2
	}

	def "computeDigest 相同内容不同大小摘要不同"() {
		given:
		File f1 = tempDir.resolve("diff1.txt").toFile()
		File f2 = tempDir.resolve("diff2.txt").toFile()
		f1.text = "content"
		f2.text = "content "

		when:
		def digest1 = FileUtils.computeDigest(f1)
		def digest2 = FileUtils.computeDigest(f2)

		then:
		digest1 != digest2
	}

	@Unroll
	def "getBufferSize 返回预期大小: size=#size -> buffer=#expected"() {
		given:
		File f = tempDir.resolve("buf.dat").toFile()
		new RandomAccessFile(f, "rw").setLength(size)

		expect:
		FileUtils.getBufferSize(f) == expected

		where:
		size              | expected
		0                 | 4 * 1024
		200 * 1024        | 4 * 1024
		300 * 1024        | 8 * 1024
		2 * 1024 * 1024   | 32 * 1024
		50 * 1024 * 1024  | 64 * 1024
		200 * 1024 * 1024 | 128 * 1024
	}

	@Unroll
	def "getSlidingBufferSize 返回预期: size=#size"() {
		given:
		File f = tempDir.resolve("slide.dat").toFile()
		new RandomAccessFile(f, "rw").setLength(size)

		expect:
		FileUtils.getSlidingBufferSize(f) == expected

		where:
		size                     | expected
		50 * 1024 * 1024         | 4 * 1024 * 1024
		200 * 1024 * 1024        | 16 * 1024 * 1024
		2L * 1024 * 1024 * 1024  | 32 * 1024 * 1024
		12L * 1024 * 1024 * 1024 | 64 * 1024 * 1024
	}

	def "openUnsynchronizedBufferedInputStream 读取一致"() {
		given:
		File f = tempDir.resolve("in.txt").toFile()
		f.text = "hello"

		when:
		def inputStream = FileUtils.openUnsynchronizedBufferedInputStream(f)
		def bytes = inputStream.readAllBytes()
		inputStream.close()

		then:
		new String(bytes) == "hello"
	}

	def "openBufferedFileChannelInputStream 读取一致"() {
		given:
		File f = tempDir.resolve("in2.txt").toFile()
		f.text = "world"

		when:
		def chInputStream = FileUtils.openBufferedFileChannelInputStream(f)
		def bytes = chInputStream.readAllBytes()
		chInputStream.close()

		then:
		new String(bytes) == "world"
	}

	def "openMemoryMappedFileInputStream 读取一致"() {
		given:
		File f = tempDir.resolve("in3.txt").toFile()
		f.text = "mmf"

		when:
		def mmInputStream = FileUtils.openMemoryMappedFileInputStream(f)
		def bytes = mmInputStream.readAllBytes()
		mmInputStream.close()

		then:
		new String(bytes) == "mmf"
	}

	def "getMimeType 与类型判断"() {
		given:
		File txt = tempDir.resolve("t.txt").toFile()
		txt.text = "abc"
		File png = tempDir.resolve("i.png").toFile()
		BufferedImage bi = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB)
		ImageIO.write(bi, "png", png)

		expect:
		FileUtils.isTextType(txt)
		FileUtils.isImageType(png)
		FileUtils.getMimeType(txt).startsWith("text/")
		FileUtils.getMimeType(png).startsWith("image/")
	}

	def "isMimeType 与 isAnyMimeType"() {
		given:
		File txt = tempDir.resolve("t2.txt").toFile()
		txt.text = "xyz"
		def mt = FileUtils.getMimeType(txt)

		expect:
		FileUtils.isMimeType(txt, mt)
		FileUtils.isAnyMimeType(txt, mt, "application/pdf")
		!FileUtils.isMimeType(txt, "application/pdf")
		!FileUtils.isAnyMimeType(txt, "application/pdf", "image/png")
	}

	def "parseMetaData 非空"() {
		given:
		File txt = tempDir.resolve("meta.txt").toFile()
		txt.text = "meta"

		when:
		def md = FileUtils.parseMetaData(txt)

		then:
		md != null
		md.size() > 0
		md.containsKey("Content-Type")
	}

	def "CBC 文件加解密"() {
		given:
		File inputFile = tempDir.resolve("cbc_in.txt").toFile()
		inputFile.text = "CBC-TEST"
		File encrypted = tempDir.resolve("cbc_out.enc").toFile()
		File decrypted = tempDir.resolve("cbc_dec.txt").toFile()

		when:
		FileUtils.encryptFile(inputFile, encrypted, PASSWORD_16, IV_16)
		FileUtils.decryptFile(encrypted, decrypted, PASSWORD_16, IV_16)

		then:
		decrypted.text == "CBC-TEST"
	}

	def "CBC 自定义缓冲区加解密"() {
		given:
		File inputFile2 = tempDir.resolve("cbc_in2.txt").toFile()
		inputFile2.text = "CBC-BUF"
		File encrypted = tempDir.resolve("cbc_out2.enc").toFile()
		File decrypted = tempDir.resolve("cbc_dec2.txt").toFile()

		when:
		FileUtils.encryptFile(inputFile2, encrypted, PASSWORD_16, IV_16, 8192)
		FileUtils.decryptFile(encrypted, decrypted, PASSWORD_16, IV_16, 8192)

		then:
		decrypted.text == "CBC-BUF"
	}

	def "CTR 文件加解密"() {
		given:
		File ctrInput = tempDir.resolve("ctr_in.txt").toFile()
		ctrInput.text = "CTR-TEST"
		File encrypted = tempDir.resolve("ctr_out.enc").toFile()
		File decrypted = tempDir.resolve("ctr_dec.txt").toFile()

		when:
		FileUtils.encryptFileByCtr(ctrInput, encrypted, PASSWORD_16, IV_16)
		FileUtils.decryptFileByCtr(encrypted, decrypted, PASSWORD_16, IV_16)

		then:
		decrypted.text == "CTR-TEST"
	}

	def "CTR 自定义缓冲区加解密"() {
		given:
		File ctrInput2 = tempDir.resolve("ctr_in2.txt").toFile()
		ctrInput2.text = "CTR-BUF"
		File encrypted = tempDir.resolve("ctr_out2.enc").toFile()
		File decrypted = tempDir.resolve("ctr_dec2.txt").toFile()

		when:
		FileUtils.encryptFileByCtr(ctrInput2, encrypted, PASSWORD_16, IV_16, 4096)
		FileUtils.decryptFileByCtr(encrypted, decrypted, PASSWORD_16, IV_16, 4096)

		then:
		decrypted.text == "CTR-BUF"
	}

	def "exist 与 notExist 检查"() {
		given:
		File f = tempDir.resolve("exist.txt").toFile()
		f.text = "x"
		File none = tempDir.resolve("none.txt").toFile()

		expect:
		FileUtils.exist(f)
		!FileUtils.exist(none)
		!FileUtils.notExist(f)
		FileUtils.notExist(none)
	}

	def "existFile 与 notExistFile 检查"() {
		given:
		File f = tempDir.resolve("isfile.txt").toFile()
		f.text = "x"
		File d = tempDir.resolve("dir").toFile()
		d.mkdirs()

		expect:
		FileUtils.existFile(f)
		!FileUtils.existFile(d)
		!FileUtils.notExistFile(f)
		FileUtils.notExistFile(d)
	}

	def "forceDeleteIfExist 与 deleteIfExist"() {
		given:
		File f1 = tempDir.resolve("del1.txt").toFile()
		f1.text = "a"
		File f2 = tempDir.resolve("del2.txt").toFile()
		f2.text = "b"

		when:
		FileUtils.forceDeleteIfExist(f1)
		FileUtils.deleteIfExist(f2)

		then:
		!f1.exists()
		!f2.exists()
	}

	def "rename 替换基名与扩展名"() {
		given:
		File f = tempDir.resolve("a.txt").toFile()
		f.text = "y"

		when:
		File r = FileUtils.rename(f, "b.txt")

		then:
		r.exists()
		r.name == "b.txt"

		when:
		File r2 = FileUtils.replaceBaseName(r, "c")

		then:
		r2.exists()
		r2.name == "c.txt"

		when:
		File r3 = FileUtils.replaceExtension(r2, "log")

		then:
		r3.exists()
		r3.name == "c.log"
	}

	def "check 与 checkFile 与 checkFileIfExist 异常"() {
		given:
		File none = tempDir.resolve("noneX.txt").toFile()
		File dir = tempDir.resolve("d2").toFile()
		dir.mkdirs()

		when:
		FileUtils.check(none, "x")

		then:
		thrown(FileNotFoundException)

		when:
		FileUtils.checkFile(dir, "x")

		then:
		thrown(IllegalArgumentException)

		when:
		FileUtils.checkFileIfExist(null, "x")

		then:
		thrown(NullPointerException)

		when:
		FileUtils.checkFileIfExist(dir, "x")

		then:
		thrown(IllegalArgumentException)
	}
}

