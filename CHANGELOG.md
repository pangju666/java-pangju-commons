# CHANGELOG

## [1.1.0] 2026.6.7

- chore: 升级pangju-dependencies至2.1.0
- feat: 新增ffmpeg音视频处理模块
- feat: 新增tessearct图像ocr模块
- feat: 新增opencv图像处理模块
- feat(io): 模块mime.types文件新增部分类型映射
- chore(imageio): 去除commons-io依赖
- chore(image): 新增thumbnailator依赖
- perf(image): ImageEditor废弃旧的图像水印和文字水印方法，改为使用thumbnailator实现
- perf(compress、image、pdf、poi): 优化参数校验
- feat(image): ImageUtils新增isImage方法判断是否为图片文件
- feat(image): ImageEditor新增transparency方法修改透明度
- perf(image): ImageEditor废弃公共灰度化滤镜和公共亮度对比度滤镜
- perf(image): 优化图像水印和文字水印配置方法，废弃部分字段并新增部分新字段
- perf(image): 废弃水印方位枚举
- fix(image): 修复ImageEditor中代码逻辑错误
- feat(image): ImageEditor新增自定义图像处理操作方法
- feat(image): ImageEditor新增释放图像方法
- perf(image): ImageEditor和ImageUtils优化部分代码逻辑