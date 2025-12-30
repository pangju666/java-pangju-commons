package io.github.pangju666.commons.image.model

import io.github.pangju666.commons.image.lang.ImageConstants
import spock.lang.Specification
import spock.lang.Title
import spock.lang.Unroll

@Title("ImageSize 单元测试")
class ImageSizeSpec extends Specification {

	@Unroll
	def "构造函数参数校验：width=#w height=#h"() {
		when:
		new ImageSize(w as int, h as int)

		then:
		thrown(IllegalArgumentException)

		where:
		w  | h
		0  | 10
		10 | 0
		-1 | 10
		10 | -1
	}

	@Unroll
	def "方向范围校验：orientation=#orientation"() {
		when:
		new ImageSize(100, 100, orientation as int)

		then:
		thrown(IllegalArgumentException)

		where:
		orientation << [0, 9, -1, 100]
	}

	def "getVisualSize 方向>=5交换宽高"() {
		given:
		def size = new ImageSize(100, 200, 6)

		when:
		def visual = size.getVisualSize()

		then:
		visual.getWidth() == 200
		visual.getHeight() == 100
		visual.getOrientation() == 6

		and: "再次获取可视化尺寸返回自身"
		visual.getVisualSize().is(visual)
	}

	def "getVisualSize 无方向保持原尺寸并设置默认方向"() {
		given:
		def size = new ImageSize(300, 400)

		when:
		def visual = size.getVisualSize()

		then:
		visual.getWidth() == 300
		visual.getHeight() == 400
		visual.getOrientation() == ImageConstants.NORMAL_EXIF_ORIENTATION
	}

	def "scaleByWidth 宽>高按比例缩放"() {
		given:
		def size = new ImageSize(200, 100, 3)

		when:
		def scaled = size.scaleByWidth(100)

		then:
		scaled.getWidth() == 100
		scaled.getHeight() == 50
		scaled.getOrientation() == 3
	}

	def "scaleByWidth 宽<=高按比例缩放"() {
		given:
		def size = new ImageSize(100, 200, 3)

		when:
		def scaled = size.scaleByWidth(50)

		then:
		scaled.getWidth() == 50
		scaled.getHeight() == 100
		scaled.getOrientation() == 3
	}

	def "scaleByHeight 宽>高按比例缩放"() {
		given:
		def size = new ImageSize(200, 100, 2)

		when:
		def scaled = size.scaleByHeight(50)

		then:
		scaled.getWidth() == 100
		scaled.getHeight() == 50
		scaled.getOrientation() == 2
	}

	def "scaleByHeight 宽<=高按比例缩放"() {
		given:
		def size = new ImageSize(100, 200, 2)

		when:
		def scaled = size.scaleByHeight(50)

		then:
		scaled.getWidth() == 25
		scaled.getHeight() == 50
		scaled.getOrientation() == 2
	}

	def "scale 双约束优先适配宽度"() {
		given:
		def size = new ImageSize(200, 100)

		when:
		def scaled = size.scale(100, 60)

		then:
		scaled.getWidth() == 100
		scaled.getHeight() == 50
	}

	def "scale 双约束改为适配高度"() {
		given:
		def size = new ImageSize(200, 100)

		when:
		def scaled = size.scale(100, 40)

		then:
		scaled.getWidth() == 80
		scaled.getHeight() == 40
	}

	def "scale 比例缩放正常"() {
		given:
		def size = new ImageSize(100, 50, 7)

		when:
		def scaled = size.scale(1.5d)

		then:
		scaled.getWidth() == 150
		scaled.getHeight() == 75
		scaled.getOrientation() == 7
	}

	def "scale 比例过小导致尺寸为0抛出异常"() {
		given:
		def size = new ImageSize(100, 50)

		when:
		size.scale(0.004d)

		then:
		thrown(IllegalArgumentException)
	}

	@Unroll
	def "scale 比例校验：ratio=#ratio"() {
		given:
		def size = new ImageSize(100, 50)

		when:
		size.scale(ratio as double)

		then:
		thrown(IllegalArgumentException)

		where:
		ratio << [0, -1, -0.1d]
	}

	def "resize 非等比调整正常"() {
		given:
		def size = new ImageSize(100, 50, 4)

		when:
		def resized = size.resize(80, 30)

		then:
		resized.getWidth() == 80
		resized.getHeight() == 30
		resized.getOrientation() == 4
	}

	@Unroll
	def "resize 参数校验：w=#w h=#h"() {
		given:
		def size = new ImageSize(100, 50)

		when:
		size.resize(w as int, h as int)

		then:
		thrown(IllegalArgumentException)

		where:
		w  | h
		0  | 10
		10 | 0
		-1 | 10
		10 | -1
	}
}

