package io.github.pangju666.commons.image.processor

import com.drew.imaging.ImageProcessingException
import com.twelvemonkeys.image.GrayFilter
import com.twelvemonkeys.image.ResampleOp
import io.github.pangju666.commons.image.enums.FlipDirection
import io.github.pangju666.commons.image.enums.RotateDirection
import io.github.pangju666.commons.image.lang.ImageConstants
import io.github.pangju666.commons.image.io.resource.ImageIOResource
import io.github.pangju666.commons.image.model.ImageSize
import io.github.pangju666.commons.image.model.ImageWatermarkOption
import io.github.pangju666.commons.image.model.TextWatermarkOption
import io.github.pangju666.commons.image.utils.ImageUtils
import io.github.pangju666.commons.io.utils.IOUtils
import net.coobird.thumbnailator.filters.Caption
import net.coobird.thumbnailator.filters.Watermark
import net.coobird.thumbnailator.geometry.Positions
import spock.lang.Specification
import spock.lang.TempDir
import spock.lang.Unroll

import javax.imageio.ImageIO
import javax.imageio.stream.ImageOutputStream
import java.awt.Color
import java.awt.Font
import java.nio.file.Path

class ImageProcessorSpec extends Specification {
	@TempDir
	Path tempDir

	static final String TEST_IMAGES_DIR = "src/test/resources/images"
	static final List<String> ALL_IMAGES = [
		"camera.jpg",
		"test.bmp",
		"test.gif",
		"test.ico",
		"test.jpg",
		"test.png",
		"test.svg",
		"test.tiff",
		"test.webp",
		"watermark.png",
	]
	static final List<String> NORMAL_IMAGES = [
		"camera.jpg",
		"test.bmp",
		"test.gif",
		"test.ico",
		"test.jpg",
		"test.png",
		"test.svg",
		"test.tiff",
		"test.webp",
	]
	static final Map<String, List<Integer>> IMAGE_SIZE_EXPECTED = [
		"camera.jpg": [3016, 4032],
		"test.bmp"  : [71, 96],
		"test.gif"  : [478, 448],
		"test.ico"  : [32, 32],
		"test.jpg"  : [1125, 877],
		"test.png"  : [4095, 2559],
		"test.svg"  : [512, 512],
		"test.tiff" : [1200, 1200],
		"test.webp" : [550, 368],
	]

	@Unroll
	def "scaleByWidth 按宽度等比缩放：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scaleByWidth(200)
				def img = editor.toBufferedImage()
				assert img.getWidth() == 200
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				def expectedH = Math.max(1, (int) Math.round(oh * (200d / ow)))
				assert img.getHeight() == expectedH
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES.findAll { it != "test.svg" }
	}

	@Unroll
	def "scaleByHeight 按高度等比缩放：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scaleByHeight(300)
				def img = editor.toBufferedImage()
				assert img.getHeight() == 300
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				def expectedW = Math.max(1, (int) Math.round(ow * (300d / oh)))
				assert img.getWidth() == expectedW
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "resize 强制缩放到指定尺寸：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.resize(128, 128)
				def img = editor.toBufferedImage()
				assert img.getWidth() == 128
				assert img.getHeight() == 128
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "scale(width,height) 在范围内等比缩放：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scale(200, 150)
				def img = editor.toBufferedImage()
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				def factor = Math.min(200d / ow, 150d / oh)
				def expectedW = Math.max(1, (int) Math.round(ow * factor))
				def expectedH = Math.max(1, (int) Math.round(oh * factor))
				assert img.getWidth() == expectedW
				assert img.getHeight() == expectedH
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "cropByCenter 居中裁剪到目标尺寸：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def origin = ImageIO.read(file)
				def w = Math.max(1, Math.min(100, origin.getWidth()))
				def h = Math.max(1, Math.min(100, origin.getHeight()))
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.cropByCenter(w, h)
				def img = editor.toBufferedImage()
				assert img.getWidth() == w
				assert img.getHeight() == h
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "cropByOffset 按边距裁剪：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				def pre = editor.toBufferedImage()
				def w0 = pre.getWidth()
				def h0 = pre.getHeight()
				def l = Math.min(1, Math.max(0, w0 > 2 ? 1 : 0))
				def r = l
				def t = Math.min(1, Math.max(0, h0 > 2 ? 1 : 0))
				def b = t
				editor.cropByOffset(t, b, l, r)
				def img = editor.toBufferedImage()
				assert img.getWidth() == w0 - l - r
				assert img.getHeight() == h0 - t - b
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "cropByRect 非法参数抛异常：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		if (!canRead) {
			ImageProcessor.of(new ImageIOResource(file, true))
		} else {
			def editor = ImageProcessor.of(new ImageIOResource(file, true))
			editor.cropByRect(-1, 0, 10, 10)
		}

		then:
		"验证异常"
		thrown(IllegalArgumentException)

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "rotate 与 flip 操作可执行：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				if (ext != "ICO") {
					editor.rotate(RotateDirection.CLOCKWISE_90)
				}
				editor.flip(FlipDirection.HORIZONTAL)
				def img = editor.toBufferedImage()
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				if (ext != "ICO") {
					assert img.getWidth() == oh
					assert img.getHeight() == ow
				} else {
					assert img.getWidth() == ow
					assert img.getHeight() == oh
				}
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "grayscale 转灰度并成功输出：#name"() {
		given:
		"准备源文件与输出文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def canWrite = !["SVG", "WEBP"].contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.grayscale()
				def img = editor.toBufferedImage()
				boolean ok = true
				def out = new File(tempDir.toFile(), "gray-${ext}.png")
				if (canWrite) {
					ok = editor.outputFormat("PNG").toFile(out)
					assert out.exists()
					assert out.length() > 0
				}
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				assert img.getWidth() == ow
				assert img.getHeight() == oh
				assert ok
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证输出文件"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "brightness 与 contrast 输出文件成功：#name"() {
		given:
		"准备源文件与输出文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def canWrite = !["SVG", "WEBP"].contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				boolean ok = true
				def out = new File(tempDir.toFile(), "bc-${ext}.jpg")
				editor.brightness(0.2f).contrast(0.3f)
				if (canWrite) {
					ok = editor.outputFormat("JPG").toFile(out)
					assert out.exists()
					assert out.length() > 0
				}
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				assert editor.toBufferedImage().getWidth() == ow
				assert editor.toBufferedImage().getHeight() == oh
				assert ok
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证输出文件"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "filter 自定义滤镜可用：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.filter(new GrayFilter())
				def img = editor.toBufferedImage()
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				assert img.getWidth() == ow
				assert img.getHeight() == oh
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "addImageWatermark 使用文件与方向：#name"() {
		given:
		"准备源文件与输出文件"
		def src = new File("${TEST_IMAGES_DIR}/${name}")
		def watermark = new File("${TEST_IMAGES_DIR}/watermark.png")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def canWrite = !["SVG", "WEBP"].contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(src))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(src))
				def option = new ImageWatermarkOption()
				option.setDirection(Positions.BOTTOM_RIGHT)
				boolean ok = true
				def out = new File(tempDir.toFile(), "wm-${ext}.png")
				editor.addImageWatermark(ImageIO.read(watermark), option)
				if (canWrite) {
					ok = editor.outputFormat("PNG").toFile(out)
					assert out.exists()
					assert out.length() > 0
				}
				assert ok
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证输出文件"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "addTextWatermark 文本水印居中：#name"() {
		given:
		"准备源文件与输出文件"
		def src = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def canWrite = !["SVG", "WEBP"].contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(src))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(src))
				def option = new TextWatermarkOption()
				option.setDirection(Positions.CENTER)
				boolean ok = true
				def out = new File(tempDir.toFile(), "tw-${ext}.jpg")
				editor.addTextWatermark("TEST", option)
				if (canWrite) {
					ok = editor.outputFormat("JPG").toFile(out)
					assert out.exists()
					assert out.length() > 0
				}
				assert ok
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证输出文件"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "toOutputStream 与 toImageOutputStream 输出成功：#name"() {
		given:
		"准备源文件与输出文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def canWrite = !["SVG", "WEBP"].contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				def baos = IOUtils.toUnsynchronizedByteArrayOutputStream(IOUtils.DEFAULT_BUFFER_SIZE)
				def f = new File(tempDir.toFile(), "ios-${ext}.png")
				ImageOutputStream ios = ImageIO.createImageOutputStream(f)
				boolean ok1
				boolean ok2
				if (canWrite) {
					ok1 = editor.outputFormat("PNG").toOutputStream(baos)
					ok2 = editor.outputFormat("PNG").toImageOutputStream(ios)
					ios.close()
					assert ok1
					assert ok2
					assert baos.size() > 0
					assert f.exists()
					assert f.length() > 0
				}
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				assert editor.toBufferedImage().getWidth() == ow
				assert editor.toBufferedImage().getHeight() == oh
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证输出"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "of(File) 遍历图片做基本处理：#name"() {
		given:
		"准备源文件与输出文件"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		boolean wrote = false
		boolean okWrite = false
		def out = new File(tempDir.toFile(), "out-${ext}.png")

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scaleByWidth(64).grayscale()
				if (ext != "ICO") {
					editor.rotate(90d)
				}
				def img = editor.toBufferedImage()
				if (ext != "SVG" && ext != "WEBP") {
					okWrite = editor.outputFormat("PNG").toFile(out)
					wrote = true
				}
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				def scaledH = Math.max(1, (int) Math.round(oh * (64d / ow)))
				if (ext != "ICO") {
					assert img.getWidth() == scaledH
					assert img.getHeight() == 64
				} else {
					assert img.getWidth() == 64
					assert img.getHeight() == scaledH
				}
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证输出文件与尺寸"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}
		if (canRead && wrote) {
			assert okWrite
			assert out.exists()
			assert out.length() > 0
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "rotate(double) 与 flip 执行成功：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				if (ext != "ICO") {
					editor.rotate(45d)
				}
				editor.flip(FlipDirection.VERTICAL)
				def img = editor.toBufferedImage()
				assert img != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "blur 与 sharpen 默认强度：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.blur().sharpen()
				def img = editor.toBufferedImage()
				assert img != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "contrast 默认值：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.contrast()
				def img = editor.toBufferedImage()
				assert img != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "scale(比例) 等比缩放：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scale(0.5d)
				def img = editor.toBufferedImage()
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				assert img.getWidth() == Math.max(1, (int) Math.round(ow * 0.5d))
				assert img.getHeight() == Math.max(1, (int) Math.round(oh * 0.5d))
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "addImageWatermark(BufferedImage,direction) 与 (x,y)：#name"() {
		given:
		"准备源文件与输出文件"
		def src = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def canWrite = !["SVG", "WEBP"].contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(src))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(src))
				def wm = ImageIO.read(new File("${TEST_IMAGES_DIR}/watermark.png"))
				def directionOption = new ImageWatermarkOption()
				directionOption.setDirection(Positions.TOP_RIGHT)
				def posOption = new ImageWatermarkOption()
				posOption.setX(10)
				posOption.setY(10)
				boolean ok = true
				def out = new File(tempDir.toFile(), "wm2-${ext}.png")
				editor.addImageWatermark(wm, directionOption)
				editor.addImageWatermark(wm, posOption)
				if (canWrite) {
					ok = editor.outputFormat("PNG").toFile(out)
					assert out.exists()
					assert out.length() > 0
				}
				assert ok
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证输出文件"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "addTextWatermark 使用坐标定位：#name"() {
		given:
		"准备源文件与输出文件"
		def src = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def canWrite = !["SVG", "WEBP"].contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(src))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(src))
				def option = new TextWatermarkOption()
				option.setX(20)
				option.setY(30)
				boolean ok = true
				def out = new File(tempDir.toFile(), "tw2-${ext}.jpg")
				editor.addTextWatermark("HELLO", option)
				if (canWrite) {
					ok = editor.outputFormat("JPG").toFile(out)
					assert out.exists()
					assert out.length() > 0
				}
				assert ok
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证输出文件"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "of(File,boolean) 自动校正方向：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def origin = ImageIO.read(file)
				def orientation = ImageConstants.NORMAL_EXIF_ORIENTATION
				try {
					orientation = ImageUtils.getExifOrientation(file)
				} catch (ImageProcessingException ignored) {
				}
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				def img = editor.toBufferedImage()
				assert orientation in (1..8)
				if (orientation in [5, 6, 7, 8]) {
					assert img.getWidth() == origin.getHeight()
					assert img.getHeight() == origin.getWidth()
				} else {
					assert img.getWidth() == origin.getWidth()
					assert img.getHeight() == origin.getHeight()
				}
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "of(File,int) 指定方向校正：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, 6))
			} else {
				def origin = ImageIO.read(file)
				def editor = ImageProcessor.of(new ImageIOResource(file, 6))
				def img = editor.toBufferedImage()
				assert img.getWidth() == origin.getHeight()
				assert img.getHeight() == origin.getWidth()
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "of(InputStream) 与 of(InputStream,boolean)：#name"() {
		given:
		"准备源文件流与参数"
		def f = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def is1 = new FileInputStream(f)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(is1)
			} else {
				def e1 = ImageProcessor.of(is1)
				def i1 = e1.toBufferedImage()
				assert i1 != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof Exception
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "of(InputStream,int) 指定方向：#name"() {
		given:
		"准备源文件流与参数"
		def f = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def is = new FileInputStream(f)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(is, 6)
			} else {
				def origin = ImageIO.read(f)
				def e = ImageProcessor.of(is, 6)
				def img = e.toBufferedImage()
				assert img.getWidth() == origin.getHeight()
				assert img.getHeight() == origin.getWidth()
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof Exception
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "of(ImageInputStream) 与 of(ImageInputStream,int)：#name"() {
		given:
		"准备图像输入流与参数"
		def f = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)
		def iis1 = ImageIO.createImageInputStream(f)
		def iis2 = ImageIO.createImageInputStream(f)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(iis1)
			} else {
				def origin = ImageIO.read(f)
				def e1 = ImageProcessor.of(iis1)
				def e2 = ImageProcessor.of(iis2, 6)
				def i1 = e1.toBufferedImage()
				def i2 = e2.toBufferedImage()
				assert i1 != null
				assert i2.getWidth() == origin.getHeight()
				assert i2.getHeight() == origin.getWidth()
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof Exception
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "of(BufferedImage) 与 of(BufferedImage,int)：#name"() {
		given:
		"准备 BufferedImage 与参数"
		def f = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(ImageIO.read(f))
			} else {
				def bi = ImageIO.read(f)
				def e1 = ImageProcessor.of(bi)
				def e2 = ImageProcessor.of(bi, 6)
				def i1 = e1.toBufferedImage()
				def i2 = e2.toBufferedImage()
				assert i1 != null
				assert i2.getWidth() == bi.getHeight()
				assert i2.getHeight() == bi.getWidth()
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof Exception
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "reset 恢复到初始状态尺寸：#name"() {
		given:
		"准备源文件与参数"
		def f = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(f))
			} else {
				def origin = ImageIO.read(f)
				def editor = ImageProcessor.of(new ImageIOResource(f))
				editor.scaleByWidth(64)
				editor.reset()
				def img = editor.toBufferedImage()
				assert img.getWidth() == origin.getWidth()
				assert img.getHeight() == origin.getHeight()
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "of(ImageIOResource) 使用 ImageIOResource 构建：#name"() {
		given:
		"准备 ImageIOResource 与参数"
		def f = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(f))
			} else {
				def resource = new ImageIOResource(f)
				def editor = ImageProcessor.of(resource)
				def img = editor.toBufferedImage()
				assert img != null
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				assert img.getWidth() == ow
				assert img.getHeight() == oh
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof Exception
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES.subList(0, ALL_IMAGES.size() - 2)
	}

	def "of(ImageIOResource) null 资源抛异常"() {
		when:
		"传入 null 资源"
		ImageProcessor.of(null as ImageIOResource)

		then:
		"抛出 NullPointerException"
		thrown(NullPointerException)
	}

	@Unroll
	def "of(ImageIOResource) 自动解析 EXIF 方向：#name"() {
		given:
		"准备 ImageIOResource 与参数"
		def f = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(f, true))
			} else {
				def origin = ImageIO.read(f)
				def resource = new ImageIOResource(f, true)
				def editor = ImageProcessor.of(resource)
				def img = editor.toBufferedImage()
				assert img != null
				def orientation = resource.getImageSize().getOrientation()
				if (orientation in [5, 6, 7, 8]) {
					assert img.getWidth() == origin.getHeight()
					assert img.getHeight() == origin.getWidth()
				} else {
					assert img.getWidth() == origin.getWidth()
					assert img.getHeight() == origin.getHeight()
				}
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof Exception
		} else {
			assert err == null
		}

		where:
		name << ALL_IMAGES
	}

	@Unroll
	def "transparency 调整透明度：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file))
				editor.transparency(0.5f)
				def img = editor.toBufferedImage()
				assert img != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	def "transparency 超出范围抛异常"() {
		given:
		"准备源文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(file))
		editor.transparency(1.5f)

		then:
		"抛出异常"
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "addImageWatermark(BufferedImage) 使用默认配置：#name"() {
		given:
		"准备源文件与水印"
		def src = new File("${TEST_IMAGES_DIR}/${name}")
		def watermark = new File("${TEST_IMAGES_DIR}/watermark.png")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(src))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(src))
				def wm = ImageIO.read(watermark)
				editor.addImageWatermark(wm)
				def img = editor.toBufferedImage()
				assert img != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "addImageWatermark(BufferedImage,ImageWatermarkOption) 使用指定配置：#name"() {
		given:
		"准备源文件与水印"
		def src = new File("${TEST_IMAGES_DIR}/${name}")
		def watermark = new File("${TEST_IMAGES_DIR}/watermark.png")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(src))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(src))
				def wm = ImageIO.read(watermark)
				def option = new ImageWatermarkOption()
				option.setDirection(Positions.TOP_LEFT)
				editor.addImageWatermark(wm, option)
				def img = editor.toBufferedImage()
				assert img != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	def "addImageWatermark(BufferedImage,ImageWatermarkOption) null option 抛异常"() {
		given:
		"准备源文件与水印"
		def src = new File("${TEST_IMAGES_DIR}/test.jpg")
		def watermark = new File("${TEST_IMAGES_DIR}/watermark.png")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(src))
		def wm = ImageIO.read(watermark)
		editor.addImageWatermark(wm, null)

		then:
		"抛出异常"
		thrown(NullPointerException)
	}

	@Unroll
	def "addTextWatermark(String) 使用默认配置：#name"() {
		given:
		"准备源文件"
		def src = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(src))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(src))
				editor.addTextWatermark("TEST")
				def img = editor.toBufferedImage()
				assert img != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "addTextWatermark(String,TextWatermarkOption) 使用指定配置：#name"() {
		given:
		"准备源文件"
		def src = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(src))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(src))
				def option = new TextWatermarkOption()
				option.setDirection(Positions.BOTTOM_RIGHT)
				editor.addTextWatermark("HELLO", option)
				def img = editor.toBufferedImage()
				assert img != null
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	def "addTextWatermark(String,TextWatermarkOption) null option 抛异常"() {
		given:
		"准备源文件"
		def src = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(src))
		editor.addTextWatermark("TEST", null)

		then:
		"抛出异常"
		thrown(NullPointerException)
	}

	def "release 释放图像资源"() {
		given:
		"准备源文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(file))
		editor.scaleByWidth(100)
		editor.release()
		editor.scaleByWidth(100)

		then:
		"验证释放后无法使用"
		thrown(NullPointerException)
	}

	def "apply 自定义图像操作"() {
		given:
		"准备源文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(file))
		editor.apply { img ->
			ImageSize targetImageSize = new ImageSize(img.getWidth(), img.getHeight()).scaleByWidth(100);
			return new ResampleOp(targetImageSize.getWidth(), targetImageSize.getHeight())
				.filter(img, null)
		}
		def img = editor.toBufferedImage()

		then:
		"验证结果"
		img.getWidth() == 100
	}

	def "apply null 操作抛异常"() {
		given:
		"准备源文件"
		def file = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(file))
		editor.apply(null)

		then:
		"抛出异常"
		thrown(NullPointerException)
	}

	def "addImageWatermark(Watermark) 使用预创建对象"() {
		given:
		"准备源文件与水印"
		def src = new File("${TEST_IMAGES_DIR}/test.jpg")
		def watermark = new File("${TEST_IMAGES_DIR}/watermark.png")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(src))
		def wm = ImageIO.read(watermark)
		def watermarkFilter = new Watermark(Positions.CENTER, wm, 0.5f)
		editor.addImageWatermark(watermarkFilter)
		def img = editor.toBufferedImage()

		then:
		"验证结果"
		img != null
	}

	def "addImageWatermark(Watermark) null 抛异常"() {
		given:
		"准备源文件"
		def src = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(src))
		editor.addImageWatermark((Watermark) null)

		then:
		"抛出异常"
		thrown(NullPointerException)
	}

	def "addTextWatermark(Caption) 使用预创建对象"() {
		given:
		"准备源文件"
		def src = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(src))
		def caption = new Caption("TEST", new Font("Arial", Font.BOLD, 20), Color.BLACK, Positions.CENTER, 20)
		editor.addTextWatermark(caption)
		def img = editor.toBufferedImage()

		then:
		"验证结果"
		img != null
	}

	def "addTextWatermark(Caption) null 抛异常"() {
		given:
		"准备源文件"
		def src = new File("${TEST_IMAGES_DIR}/test.jpg")

		when:
		"执行处理"
		def editor = ImageProcessor.of(new ImageIOResource(src))
		editor.addTextWatermark((Caption) null)

		then:
		"抛出异常"
		thrown(NullPointerException)
	}

	@Unroll
	def "scaleByWidth 带重采样滤波器：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scaleByWidth(200, ResampleOp.FILTER_BOX)
				def img = editor.toBufferedImage()
				assert img.getWidth() == 200
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				def expectedH = Math.max(1, (int) Math.round(oh * (200d / ow)))
				assert img.getHeight() == expectedH
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES.findAll { it != "test.svg" }
	}

	@Unroll
	def "scaleByHeight 带重采样滤波器：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scaleByHeight(300, ResampleOp.FILTER_TRIANGLE)
				def img = editor.toBufferedImage()
				assert img.getHeight() == 300
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				def expectedW = Math.max(1, (int) Math.round(ow * (300d / oh)))
				assert img.getWidth() == expectedW
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "scale(double,int) 带重采样滤波器：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scale(0.5d, ResampleOp.FILTER_CUBIC)
				def img = editor.toBufferedImage()
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				assert img.getWidth() == Math.max(1, (int) Math.round(ow * 0.5d))
				assert img.getHeight() == Math.max(1, (int) Math.round(oh * 0.5d))
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "scale(int,int,int) 带重采样滤波器：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.scale(200, 150, ResampleOp.FILTER_MITCHELL)
				def img = editor.toBufferedImage()
				def ow = IMAGE_SIZE_EXPECTED[name][0]
				def oh = IMAGE_SIZE_EXPECTED[name][1]
				def factor = Math.min(200d / ow, 150d / oh)
				def expectedW = Math.max(1, (int) Math.round(ow * factor))
				def expectedH = Math.max(1, (int) Math.round(oh * factor))
				assert img.getWidth() == expectedW
				assert img.getHeight() == expectedH
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}

	@Unroll
	def "resize 带重采样滤波器：#name"() {
		given:
		"准备源文件与参数"
		def file = new File("${TEST_IMAGES_DIR}/${name}")
		def ext = name.substring(name.lastIndexOf('.') + 1).toUpperCase()
		def canRead = ImageConstants.getSupportedReadImageFormats().contains(ext)

		when:
		"执行处理"
		def err = null
		try {
			if (!canRead) {
				ImageProcessor.of(new ImageIOResource(file, true))
			} else {
				def editor = ImageProcessor.of(new ImageIOResource(file, true))
				editor.resize(128, 128, ResampleOp.FILTER_CATROM)
				def img = editor.toBufferedImage()
				assert img.getWidth() == 128
				assert img.getHeight() == 128
			}
		} catch (Throwable e) {
			err = e
		}

		then:
		"验证结果"
		if (!canRead) {
			assert err instanceof IllegalArgumentException
		} else {
			assert err == null
		}

		where:
		name << NORMAL_IMAGES
	}
}
