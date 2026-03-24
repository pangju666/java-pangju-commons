<p align="center">
  <a href="https://github.com/pangju666/java-pangju-commons/releases">
    <img alt="GitHub release" src="https://img.shields.io/github/release/pangju666/java-pangju-commons.svg?style=flat-square&include_prereleases" />
  </a>

  <a href="https://central.sonatype.com/search?q=g:io.github.pangju666.commons%20a:commons-bom&smo=true">
    <img alt="maven" src="https://img.shields.io/maven-central/v/io.github.pangju666.commons/commons-bom.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="license" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

# Pangju Commons

## 简介

**Pangju Commons** 是一个模块化的 Java 公共工具类库集合，旨在提供一套统一、规范且易于使用的工具 API。项目基于 Java 11
构建，涵盖了从基础语言增强到高级文件处理的方方面面。

## 核心特性

- **模块化设计**：支持按需引入，避免引入无关依赖，保持项目精简。
- **开箱即用**：提供 BOM（Bill of Materials）管理依赖，简化版本维护。
- **深度增强**：在主流开源库的基础上，针对常见业务场景进行了二次封装和功能扩展。
- **规范统一**：统一的参数校验、异常处理和 API 风格。

## 模块说明

| 模块名称                 | 描述                                                   |
|:---------------------|:-----------------------------------------------------|
| `commons-lang`       | 基础工具库                                                |
| `commons-io`         | 基于 Apache Tika 和 Commons IO 的 IO 处理与文件类型识别工具库        |
| `commons-crypto`     | 基于 Jasypt 的加解密与安全处理工具库                               |
| `commons-validation` | 基于 Jakarta Validation 的公共校验工具库                       |
| `commons-image`      | 基于 Metadata Extractor 和 TwelveMonkeys 的图像处理与元数据提取工具库 |
| `commons-imageio`    | ImageIO 扩展解析库，集成多种图像格式支持                             |
| `commons-compress`   | 基于 Apache Commons Compress 的压缩与解压工具库                 |
| `commons-pdf`        | 基于 Apache PDFBox 的 PDF 处理工具库                         |
| `commons-poi`        | 基于 Apache POI 和 poi-tl 的 Office 文档处理工具库              |
| `commons-geo`        | 地理坐标转换与空间计算工具库                                       |
| `commons-bom`        | 公共工具类库依赖清单（BOM），用于统一版本管理                             |
| `commons-all`        | 公共工具类库全量集成模块，一键引入所有功能                                |

## 快速开始

### 1. 引入 BOM（推荐）

在项目的 `pom.xml` 中引入 `commons-bom` 以统一管理版本：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pangju666.commons</groupId>
            <artifactId>commons-bom</artifactId>
            <version>1.0.0</version>
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
    </dependency>
</dependencies>
```

## 文档

详细的 API
文档请访问：[Pangju Commons Documentation](https://pangju666.github.io/pangju-java-doc/commons/getting-started.html)

## 📄 许可证

本项目采用 [Apache License 2.0](https://www.apache.org/licenses/LICENSE-2.0) 许可证。

---
感谢所有为项目做出贡献的开发者，以及项目所使用的开源框架和工具。