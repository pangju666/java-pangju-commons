# Pangju Commons 工具库集

## 项目简介

基于 Apache Commons、jasypt、tika、twelvemonkeys、poi-tl、hanlp、gson、reflections等知名第三方库扩展的 Java 工具库集合，提供开发中常用的工具类。
项目采用模块化设计，每个模块都专注于特定的功能领域。

## 模块说明

[[_TOC_]]

### 📦 pangju-commons-lang (常用工具模块)

常用工具模块，提供基础的工具类集合。

- **ArrayUtils**: 数组操作工具类，继承并扩展了`Apache Commons ArrayUtils`功能
    - **功能**：
        - 数组分割功能：将数组按指定大小分割成子数组列表
        - 支持所有基本类型数组和泛型数组操作
        - 线程安全的方法实现


- **DateFormatUtils**: 日期格式化工具类，继承并扩展`Apache Commons DateFormatUtils`功能
    - **功能**：
        - 日期对象与字符串相互转换
        - 支持预定义标准格式（ISO格式等）
        - 线程安全的格式化方法


- **DateUtils**: 日期时间工具类，继承并扩展`Apache Commons DateUtils`功能
    - **功能**：
        - 日期转换：支持Date与LocalDate/LocalDateTime互相转换
        - 时间戳处理：Date对象与毫秒级时间戳互转
        - 日期计算：计算两个日期的时间差（毫秒、秒、分钟、小时、天）
        - 年龄计算：根据生日日期计算实际年龄
        - 线程安全：所有方法均为静态方法且线程安全


- **DesensitizationUtils**: 数据脱敏工具类，符合阿里脱敏规则
    - **功能**：
        - 支持13种常见敏感数据类型脱敏（身份证/护照/手机号/邮箱等）
        - 提供环形脱敏、左右保留等多种脱敏策略
        - 符合《阿里Java开发手册》脱敏规范
        - 线程安全的方法实现


- **IdCardUtils**: 身份证验证工具类，符合公安部最新标准
    - **功能**：
        - 验证中国大陆居民身份证号合法性
        - 解析身份证信息（出生日期、性别）
        - 支持15位/18位身份证格式验证
        - 线程安全的方法实现


- **IdUtils**: ID生成工具类
    - **功能**：
        - 支持多种ID生成算法：UUID/MongoDB ObjectId/NanoID/雪花算法
        - 高性能随机UUID生成（基于ThreadLocalRandom）
        - 符合RFC 4122标准的版本4 UUID生成
        - 可配置的雪花算法ID生成（需预配置工作节点）
        - 提供21位URL安全的NanoId生成


- **JsonUtils**: JSON处理工具类（基于Gson实现）
    - **功能**：
        - 提供线程安全的Gson单例实例（基于定制GsonBuilder构建）
        - 支持日期类型（Date/LocalDate/LocalDateTime）的序列化与反序列化
        - 精确处理BigDecimal/BigInteger类型（使用BigDecimalDeserializer实现）
        - Class类型全限定名转换（通过ClassJsonSerializer实现）
        - 空值安全处理机制（保留null字段）
        - 支持复杂集合类型转换（JsonArray/JsonObject）
        - 提供便捷的JSON↔对象转换方法（toString()和fromString()）
        - 支持自定义GsonBuilder扩展


- **ReflectionUtils**: 反射操作增强工具类（继承自`org.reflections.ReflectionUtils`）
    - **功能**：
        - 安全访问私有字段/方法（自动处理访问权限）
        - 支持CGLIB代理类检测与处理
        - 泛型类型参数解析
        - 简化类名获取
        - 常用反射操作封装（字段读写/方法访问控制/对象方法检测）


- **RegExUtils**: 正则表达式增强工具类（继承自`org.apache.commons.lang3.RegExUtils`）
    - **功能**：
        - 支持正则标志位组合计算（RegexFlag枚举）
        - 智能正则表达式编译（自动添加^/$边界）
        - 完全匹配验证（matches系列方法）
        - 批量查找匹配结果（find系列方法）
        - 正则模式复用与重新编译


- **SerializationUtils**: 序列化操作工具类（基于Java序列化机制）
    - **功能**：
        - 对象转换为字节数组（serialize方法）
        - 深度克隆（clone方法）
        - 安全的序列化异常处理
        - 空对象安全处理
        - 来源于Spring框架的`org.springframework.util.SerializationUtils`


- **StringFormatUtils**: 字符串格式转换工具类
    - **功能**：
        - 支持多种命名格式双向转换
        - 转换格式包含：
            - SCREAMING_SNAKE_CASE（全大写下划线）
            - SCREAMING-KEBAB-CASE（全大写中横线）
            - camelCase（小驼峰）
            - PascalCase（大驼峰）
            - snake_case（下划线）
            - kebab-case（中横线）
        - 支持自定义分隔符
        - 智能处理连续大写字母转换


- **StringUtils**: 字符串增强工具类（继承自`org.apache.commons.lang3.StringUtils`）
    - **功能**：
        - 字符集编码转换（支持指定源/目标字符集）
        - 集合/数组非空字符串过滤
        - 唯一非空字符串元素提取


- **TreeUtils**: 树形结构构建工具类
    - **功能**：
        - 扁平数据结构转树形结构
        - 支持单根/多根树构建
        - 递归子节点关系设置
        - 节点转换处理函数支持
        - 适用于菜单/目录等树形数据处理
        - 基于泛型TreeNode模型设计


- **PinyinComparator**: 中文拼音排序比较器(基于`hanlp`实现)
    - **功能**：
        - 支持字符串列表/数组的原地拼音排序
        - 排序优先级规则：
            - null值 > 空字符串 > 空白字符串 > 普通字符串
        - 多音字转拼音处理（基于HanLP实现）
        - 支持自定义拼音分隔符
        - ASCII可打印字符保持原序
        - 线程安全的不可变实现


- **SystemClock**: 高并发时钟优化工具类
    - **功能**：
        - 解决System.currentTimeMillis()性能瓶颈
        - 后台定时更新时钟（1ms精度）
        - 原子化时间戳获取
        - 自动回收守护线程
        - 来源于`com.baomidou.mybatisplus.core.toolkit.SystemClock`
        - 适用于高频时间戳获取场景


- **RegexFlag**: 正则表达式标志位枚举
    - **功能**：
        - 完整封装`java.util.regex.Pattern`的7种匹配模式
        - 支持的正则标志位：
            - UNIX_LINES（Unix行模式）
            - CASE_INSENSITIVE（不区分大小写）
            - COMMENTS（允许注释）
            - MULTILINE（多行模式）
            - DOTALL（点号匹配所有字符）
            - UNICODE_CASE（Unicode感知大小写）
            - CANON_EQ（规范等价匹配）
        - 提供嵌入式标志表达式说明（如：`(?i)`,`(?m)`）
        - 完整注释说明各标志位性能影响
        - 基于Java原生正则实现


- **DataUnit**: 数据存储单位枚举
    - **功能**：
        - 标准数据单位定义（BYTES/KB/MB/GB/TB）
        - 二进制前缀（2的幂次方）计算
        - 单位后缀匹配转换（如："KB"→KILOBYTES）
        - 与DataSize模型配合使用
        - 提供单位基准值获取（size()方法）
        - 来源于`org.springframework.util.unit.DataUnit`
        - 完整单位换算表格文档支持


- **NanoId**: 安全唯一ID生成工具类
    - **功能**：
        - 生成URL安全的唯一标识符
        - 可配置ID长度（默认21字符）
        - 支持自定义随机字母表
        - 密码学安全随机数生成（基于SecureRandom）
        - 高性能（每秒百万级生成速度）
        - 轻量无依赖（仅258字节）
        - 来源于`cn.hutool.core.lang.id.NanoId`
        - 兼容JavaScript版NanoId标准


- **SnowflakeIdWorker**: 分布式唯一ID生成器
    - **功能**：
        - 基于雪花算法生成64位唯一ID
        - ID结构：时间戳（41位） + 数据中心ID（5位） + 机器ID（5位） + 序列号（12位）
        - 支持时钟回拨检测与异常处理
        - 最高单机每秒409.6万ID生成能力
        - 线程安全的ID生成机制
        - 自定义初始时间戳支持
        - 基于SystemClock的高性能时间获取
        - 适用于分布式系统节点标识


- **DataSize**: 数据大小模型类
    - **功能**：
        - 精确表示字节级数据大小（B/KB/MB/GB/TB）
        - 支持二进制前缀计算（2的幂次方）
        - 字符串解析能力（如："128MB"→134217728字节）
        - 提供单位间精确转换方法
        - 不可变且线程安全的设计
        - 负值大小检测与处理
        - 完整单位换算表格文档支持
        - 来源于`org.springframework.util.unit.DataSize`
        - 与DataUnit枚举类协同工作


- **TreeNode**: 通用树形节点接口
    - **功能**：
        - 定义树形数据结构核心操作方法
        - 支持泛型键值类型（K）和业务数据类型（T）
        - 提供父子节点标识键获取方法
        - 子节点集合动态设置能力
        - 适用于任意树形结构业务场景
        - 线程安全接口设计
        - 简洁清晰的树形结构构建基础


- **Constants**: 通用常量池
    - **功能**：
        - 集中管理项目常用静态常量
        - 包含日期格式、字符范围、网络协议、XML/JSON等常量定义
        - 覆盖字符编码、反射代理、HTTP状态码等场景
        - 提供中文字符范围参考（U+4E00到U+9FA5）
        - 线程安全的静态常量访问
        - 不可变的常量值设计
        - 标准化常用字符串和符号常量


- **RegExPool**: 正则表达式常量池
    - **功能**：
        - 预定义50+常用正则表达式常量
        - 覆盖颜色、网络、身份、金融等场景
        - 严格遵循RFC标准（URI/URL/Email等）
        - 支持IPv4/IPv6完整格式校验
        - 包含中文/英文字符校验规则
        - 提供HTTP/FTP/FILE协议URL正则
        - 线程安全的静态常量访问
        - 持续更新维护正则规则


- **RandomArray**: 随机数组生成工具类
- **RandomList**: 随机列表生成工具类
    - **功能**：
        - 支持6种基本类型随机数组、列表生成
        - 提供普通随机数组、列表和唯一元素随机数组、列表
        - 包含布尔、整型、长整型、浮点型等多种数据类型
        - 支持全范围及指定区间随机生成
        - 线程安全的单例模式实现
        - 提供三种安全级别随机算法（insecure/secure/secureStrong）
        - 基于`org.apache.commons.lang3.RandomUtils`实现
        - 自动处理数组、列表长度合法性校验
        - 支持密码学安全随机数生成

### 📂 pangju-commons-io (IO工具模块)

提供文件操作和 IO 流处理的增强工具。

**主要功能类**：

- 文件操作
    - `FileUtils`: 文件操作工具类，提供文件读写、复制、移动等功能
    - `DirectoryUtils`: 目录操作工具类，提供目录创建、删除、遍历等功能
    - `PathUtils`: 路径处理工具类，提供路径解析和操作功能

- MIME 类型处理
    - `MimeTypeUtils`: MIME 类型识别和处理工具类

### 📊 pangju-commons-poi (Excel处理模块)

基于 Apache POI 的 Excel 文档处理工具。

**主要功能类**：

- `WorkbookUtils`: Excel 工作簿操作工具类
    - 支持工作簿分割
    - 提供单元格样式处理
    - 数据导入导出功能

### 📑 pangju-commons-pdf (PDF处理模块)

PDF 文档处理工具集。

**主要功能类**：

- `PDDocumentUtils`: PDF 文档操作工具类
    - PDF 文档创建和编辑
    - 页面操作
    - 文本提取

## 快速开始

### Maven 依赖

```xml
<!-- BOM 依赖管理 -->
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

        <!-- 使用单个模块 -->
<dependency>
<groupId>io.github.pangju666</groupId>
<artifactId>pangju-commons-lang</artifactId>
</dependency>
```

## 构建说明
```bash
# 构建整个项目
mvn clean install

# 构建单个模块
mvn -pl pangju-commons-lang clean install
 ```

## 开源协议

本项目采用 Apache License 2.0 开源协议。