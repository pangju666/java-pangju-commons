<p align="center">
  <a href="https://github.com/pangju666/java-pangju-common/releases">
    <img alt="GitHub release" src="https://img.shields.io/github/release/pangju666/java-pangju-common.svg?style=flat-square&include_prereleases" />
  </a>

  <a href="https://central.sonatype.com/search?q=g:io.github.pangju666%20%20a:pangju-common-bom&smo=true">
    <img alt="maven" src="https://img.shields.io/maven-central/v/io.github.pangju666/pangju-common-bom.svg?style=flat-square">
  </a>

  <a href="https://www.apache.org/licenses/LICENSE-2.0">
    <img alt="code style" src="https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square">
  </a>
</p>

# Pangju Commons 工具库

## 简介

模块化的 Java 工具库集合，涵盖压缩、加密、地理、图像、IO、语言工具、PDF、POI、校验等能力，支持按需引入。

## 快速开始

1. 引入 BOM（推荐）

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pangju666</groupId>
            <artifactId>pangju-common-bom</artifactId>
            <version>最新版本</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

2. 引入模块

```xml

<dependencies>
    <!-- 加密、签名与密钥工具 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-crypto</artifactId>
    </dependency>
    <!-- 压缩与解压（zip/7z 等） -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-compress</artifactId>
    </dependency>
    <!-- 地理坐标转换与几何运算 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-geo</artifactId>
    </dependency>
    <!-- 图像类型检测、读写与处理 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-image</artifactId>
    </dependency>
    <!-- JAI 插件扩展 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-imageio</artifactId>
    </dependency>
    <!-- 文件名、文件与流操作工具 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-io</artifactId>
    </dependency>
    <!-- 常用语言工具集合 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-lang</artifactId>
    </dependency>
    <!-- PDF 读写与操作 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-pdf</artifactId>
    </dependency>
    <!-- Excel/Word 等 Office 文档工具 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-poi</artifactId>
    </dependency>
    <!-- 校验注解与工具 -->
    <dependency>
        <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-validation</artifactId>
    </dependency>
</dependencies>
```

3. 或者一次性引入全部模块

```xml

<dependencies>
    <dependency>
    <groupId>io.github.pangju666</groupId>
        <artifactId>pangju-common-all</artifactId>
        <version>最新版本</version>
    </dependency>
</dependencies>
```

## 许可证

本项目采用 Apache License 2.0 许可证 - 详情请参阅 [LICENSE](LICENSE) 文件。

## 致谢

感谢所有为项目做出贡献的开发者，以及项目所使用的开源框架和工具。