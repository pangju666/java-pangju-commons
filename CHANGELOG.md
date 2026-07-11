# CHANGELOG

## [1.1.0] 2026.6.7

- chore: 升级pangju-dependencies至1.3.0
- feat(ffmpeg): 新增基于ffmpeg的音视频处理模块
- feat(tessearct): 新增基于tessearct的图像ocr模块
- feat(opencv): 新增基于opencv的图像处理模块
- feat(io): 模块mime.types文件新增部分类型映射
- feat(io): 新增IOResource抽象IO资源
- perf(io): 优化文件摘要计算逻辑
- perf(io): IOUtils新增摘要计算方法
- feat(io): FileUtils新增newBufferedOutputStream和newUnsynchronizedBufferedInputStream方法
- perf(io): FileUtils废弃openUnsynchronizedBufferedInputStream方法和设置缓冲区的文件加解密方法
- perf(io): FileUtils优化文件加解密方法和解析元数据方法
- perf(compress): 优化参数校验
- perf(compress): 将输入输出流改为带缓冲区的流
- perf(pdf): 优化参数校验
- perf(pdf): 废弃从流中读取PDF文档
- perf(poi): 优化参数校验
- perf(poi): 将文件输入流改为带缓冲区的文件输入流
- perf(crypto): 优化RSABinaryEncryptor底层逻辑
- chore(imageio): 去除commons-io依赖
- perf(image): 优化参数校验
- chore(image): 新增thumbnailator依赖
- feat(image): 新增继承自IOResource的ImageIOResource表示可以被ImageIO读取的图像资源
- feat(image): ImageSize新增判断是否为正常exif方向方法
- feat(image): ImageUtils新增isImage方法判断是否为图片文件
- feat(image): ImageUtils新增矫正图像方向方法
- perf(image): 优化ImageUtils中部分代码逻辑
- perf(image): 废弃水印方位枚举
- feat(image): RotateDirection新增getRadians方法
- perf(image): 优化图像水印和文字水印配置类，废弃部分字段并新增部分新字段适配thumbnailator的Caption和Watermark
- perf(image): 优化文字水印字体大小计算策略并修改默认字体样式
- feat(image): 废弃ImageEditor，新增ImageProcessor代替