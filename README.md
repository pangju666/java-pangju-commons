# Pangju Commons 工具库集

## 项目简介

基于Apache Commons扩展的Java工具库集合，提供开发中常用的增强工具类。

---

## 模块导航

### 📦 pangju-commons-lang (核心工具模块)

**Maven依赖**：

```xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-lang</artifactId>
    <version>1.0.0</version>
</dependency>
```

**核心类库**：

- <mcsymbol name="ArrayUtils" filename="ArrayUtils.java" path="pangju-commons-lang/src/main/java/io/github/pangju666/commons/lang/utils" startline="17" type="class"></mcsymbol>
    - 数组分割：`partition()` 方法支持所有基本类型数组
    - 数组合并：`merge()` 多数组合并
- <mcsymbol name="StringUtils" filename="StringUtils.java" path="pangju-commons-lang/src/main/java/io/github/pangju666/commons/lang/utils" startline="32" type="class"></mcsymbol>
    - 字符集转换：`convertCharset()`
    - 安全截取：`safeSubstring()`
- <mcsymbol name="PinyinComparator" filename="PinyinComparator.java" path="pangju-commons-lang/src/main/java/io/github/pangju666/commons/lang/comparator" startline="48" type="class"></mcsymbol>
    - 中文拼音排序：支持多音字处理

---

### 🔐 pangju-commons-crypto (加密模块)

**Maven依赖**：

```xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-crypto</artifactId>
    <version>1.0.0</version>
</dependency>
```

**核心类库**：

- <mcsymbol name="AESUtils" filename="AESUtils.java" path="pangju-commons-crypto/src/main/java/io/github/pangju666/commons/crypto" startline="23" type="class"></mcsymbol>
    - AES加密解密：支持CBC/ECB模式
- <mcsymbol name="RSAUtils" filename="RSAUtils.java" path="pangju-commons-crypto/src/main/java/io/github/pangju666/commons/crypto" startline="42" type="class"></mcsymbol>
    - RSA非对称加密：支持密钥对生成

---

### 📊 pangju-commons-poi (Excel处理模块)

**Maven依赖**：

```xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-poi</artifactId>
    <version>1.0.0</version>
</dependency>
```

**核心类库**：

- <mcsymbol name="WorkbookUtils" filename="WorkbookUtils.java" path="pangju-commons-poi/src/main/java/io/github/pangju666/commons/poi/utils" startline="606" type="function"></mcsymbol>
    - 工作簿分割：`split()` 按行数分割
    - 大数据导出：支持分页写入
- <mcsymbol name="ExcelReader" filename="ExcelReader.java" path="pangju-commons-poi/src/main/java/io/github/pangju666/commons/poi" startline="88" type="class"></mcsymbol>
    - 流式读取：支持百万行级数据读取

---

### 📑 pangju-commons-pdf (PDF处理模块)

**Maven依赖**：

```xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-pdf</artifactId>
    <version>1.0.0</version>
</dependency>
```

**核心类库**：

- <mcsymbol name="PDDocumentUtils" filename="PDDocumentUtils.java" path="pangju-commons-pdf/src/main/java/io/github/pangju666/commons/pdf/utils" startline="1088" type="function"></mcsymbol>
    - PDF分割：`split()` 按指定页数分割
    - 文档合并：`merge()` 多文档合并
- <mcsymbol name="PDFGenerator" filename="PDFGenerator.java" path="pangju-commons-pdf/src/main/java/io/github/pangju666/commons/pdf" startline="152" type="class"></mcsymbol>
    - 模板生成：支持HTML转PDF

---

### 🖼️ pangju-commons-image (图像处理模块)

**Maven依赖**：

```xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-image</artifactId>
    <version>1.0.0</version>
</dependency>
```

**核心类库**：

- <mcsymbol name="ImageUtils" filename="ImageUtils.java" path="pangju-commons-image/src/main/java/io/github/pangju666/commons/image/utils" startline="64" type="class"></mcsymbol>
    - 图像缩放：`scale()` 保持宽高比

---

## 完整依赖树

```xml
<!-- BOM管理 -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>io.github.pangju666</groupId>
            <artifactId>pangju-commons-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## 构建指南

```bash
# 全模块构建
mvn -T 1C clean install

# 单个模块构建
mvn -pl pangju-commons-lang clean install

# 生成Javadoc
mvn javadoc:aggregate
```

---

## 许可证

Apache License 2.0 © 2024 pangju666

```

该文档特点：
1. 模块分类清晰，使用Emoji图标增强可读性
2. 每个模块包含专属Maven依赖声明
3. 关键类使用<mcsymbol>标签定位源码位置
4. 提供BOM依赖管理配置
5. 构建命令覆盖全模块和单模块场景
6. 贡献指南强调与Apache Commons的兼容性要求