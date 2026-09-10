<p align="center">
  <a href="https://central.sonatype.com/search?q=g:io.github.pangju666.commons%20a:commons-bom&smo=true">
    <img alt="maven" src="https://img.shields.io/maven-central/v/io.github.pangju666.commons/commons-bom.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="license" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>

  <img alt="JDK" src="https://img.shields.io/badge/JDK-17+-blue.svg?style=flat-square">
</p>

# Pangju Commons

## 简介

**Pangju Commons** 是一个基于`Apache Commons`等主流工具库构建的 Java 通用工具类库，涵盖`基础工具`、`文件压缩`、`加解密`、
`地理信息`、
`图像处理`、`IO操作`、`PDF/Office文档处理`、`OCR识别`、`视频处理`和`参数校验`等常用场景。项目基于**Java 17**构建，采用模块化设计，
支持按需引入，为开发者提供统一、规范且易于使用的工具 API。

## 核心特性

- **模块化设计**：支持按需引入，避免引入无关依赖，保持项目精简。
- **开箱即用**：提供 BOM（Bill of Materials）管理依赖，简化版本维护。
- **深度增强**：在主流开源库的基础上，针对常见业务场景进行了二次封装和功能扩展。
- **规范统一**：统一的参数校验、异常处理和 API 风格。

## 模块说明

| 模块名称                 | 描述                                                                                  |
|:---------------------|:------------------------------------------------------------------------------------|
| `commons-lang`       | 基础工具库，包含字符串、日期、集合、JSON、树结构、并发、随机等常用辅助开发工具类                                          |
| `commons-io`         | IO 处理与文件类型识别工具库，基于 Apache Commons IO 和 Apache Tika 构建                               |
| `commons-crypto`     | 加解密与安全处理工具库，提供 RSA 加密、数字签名、密钥管理等功能                                                  |
| `commons-validation` | 基于 Jakarta Validation 的参数校验工具库，提供常用校验注解和验证器                                         |
| `commons-image`      | 图像处理工具库，基于 Metadata Extractor、TwelveMonkeys 和 thumbnailator 构建                      |
| `commons-imageio`    | ImageIO 扩展解析库，集成多种图像格式支持，增强标准 ImageIO 的格式兼容性                                        |
| `commons-compress`   | 压缩与解压工具库，支持 ZIP、TAR、7Z、GZIP、XZ、ZSTD、LZ4 等多种格式，基于 Apache Commons Compress 和 zip4j 构建 |
| `commons-ffmpeg`     | 视频与音频处理工具库，基于 JavaCV 和 FFmpeg 构建，支持视频转码、音频处理等操作                                     |
| `commons-opencv`     | 图像处理工具库，基于 OpenCV 构建，提供计算机视觉和图像算法支持                                                 |
| `commons-tesseract`  | 图像 OCR 工具库，基于 Tesseract 构建，支持多语言文字识别                                                |
| `commons-pdf`        | PDF 处理工具库，基于 Apache PDFBox 构建，支持 PDF 读写、转换等操作                                       |
| `commons-poi`        | Office 文档处理工具库，基于 Apache POI 和 poi-tl 构建，支持 Word、Excel、PPT 等格式读写和模板处理               |
| `commons-geo`        | 地理坐标转换与空间计算工具库，提供坐标系转换、距离计算、空间分析等功能                                                 |
| `commons-bom`        | 公共工具类库依赖清单（BOM），用于统一版本管理，简化依赖引入                                                     |
| `commons-all`        | 公共工具类库全量集成模块，一键引入所有功能，适合快速开发环境                                                      |

## 快速开始

### 1. 引入 BOM（推荐）

在项目的 `pom.xml` 中引入 `commons-bom` 以统一管理版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pangju666.commons</groupId>
            <artifactId>commons-bom</artifactId>
            <version>2.1.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 按需引入模块

根据业务需求引入对应的工具模块：

```xml
<dependencies>
    <!-- 引入基础工具库 -->
    <dependency>
        <groupId>io.github.pangju666.commons</groupId>
        <artifactId>commons-lang</artifactId>
    </dependency>
    <!-- 引入图像处理工具库 -->
    <dependency>
        <groupId>io.github.pangju666.commons</groupId>
        <artifactId>commons-image</artifactId>
    </dependency>
    <!-- 其他模块... -->
</dependencies>
```

### 3. 一键引入所有功能

如果你希望在项目中使用`Pangju Commons`提供的所有功能，可以直接引入`commons-all`：

```xml
<dependencies>
    <dependency>
        <groupId>io.github.pangju666.commons</groupId>
        <artifactId>commons-all</artifactId>
        <version>2.1.0</version>
    </dependency>
</dependencies>
```

## 文档

详细的 API
文档请访问：[Pangju Commons Documentation](https://pangju666.github.io/pangju-java-doc/v2/commons/getting-started.html)

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

---
感谢所有为项目做出贡献的开发者，以及项目所使用的开源框架和工具。