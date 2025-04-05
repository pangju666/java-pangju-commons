[中文](README.md) | [English](README_EN.md)

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

[[_TOC_]]

## 项目简介

基于 Apache Commons、jasypt、tika、twelvemonkeys、poi-tl、hanlp、gson、reflections等知名第三方库扩展的 Java 工具库集合，提供开发中常用的工具类。
项目采用模块化设计，每个模块都专注于特定的功能领域。

## 快速开始

依赖管理

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

## 模块说明

### ⚙️ pangju-commons-lang (常用工具类模块)

常用工具模块，提供基础的工具类集合。

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-lang</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **PinyinComparator**: 基于拼音的字符串比较器
    - **功能**：
        - 提供中文字符串按拼音顺序排序的能力
        - 实现不可变的Comparator接口
        - 支持自定义拼音分隔符
    - **排序规则**：
        - null值具有最高优先级
        - 空字符串""次之
        - 空白字符串" "再次之
        - 其他字符串按拼音顺序排序
    - **核心特性**：
        - 自动识别ASCII可打印字符与中文字符
        - 使用HanLP进行高质量拼音转换
        - 提供静态工具方法简化排序操作
        - 支持List和数组两种集合类型排序
    - **示例**：
        - 排序前："天气如何", null, " ", ""
        - 排序后：null, "", " ", "天气如何"


- **SystemClock**: 高性能系统时钟工具类（拷贝自mybatis-plus)
    - **功能**：
        - 优化高并发场景下System.currentTimeMillis()的性能问题
        - 提供毫秒级的系统当前时间戳获取方法
        - 通过后台线程定时更新时间戳，减少系统调用
    - **核心特性**：
        - **高性能**: 相比直接调用System.currentTimeMillis()性能提升显著
        - **低开销**: 使用单线程定时更新，资源消耗极小
        - **自动回收**: JVM退出时线程自动回收，无需手动清理
        - **线程安全**: 基于AtomicLong实现，保证并发安全
    - **性能对比**：
        - 10亿次调用：提升210.7%
        - 1亿次调用：提升162.0%
        - 1000万次调用：提升40.0%
        - 100万次调用：提升5.0%
    - **使用方法**：
        - 直接调用静态方法SystemClock.now()获取当前时间戳


- **DataUnit**: 标准数据大小单位枚举（拷贝自Spring framework)
    - **功能**：
        - 提供标准化的数据大小单位定义
        - 支持从单位后缀解析对应的枚举值
        - 与DataSize类配合使用进行数据大小表示
    - **支持单位**：
        - **BYTES**: 字节(B)，2^0，1字节
        - **KILOBYTES**: 千字节(KB)，2^10，1,024字节
        - **MEGABYTES**: 兆字节(MB)，2^20，1,048,576字节
        - **GIGABYTES**: 吉字节(GB)，2^30，1,073,741,824字节
        - **TERABYTES**: 太字节(TB)，2^40，1,099,511,627,776字节
    - **核心方法**：
        - **fromSuffix(String)**: 根据单位后缀获取对应的DataUnit枚举
        - **size()**: 获取当前单位对应的DataSize实例
    - **技术特点**：
        - 使用二进制前缀(Binary Prefix)表示法
        - 枚举设计便于类型安全的单位转换
        - 提供友好的异常信息处理


- **RegexFlag**: 正则表达式模式标志位枚举
    - **功能**：
        - 封装Java正则表达式Pattern类的标志位常量
        - 提供枚举类型安全的标志位访问方式
        - 简化正则表达式模式配置
    - **支持标志**：
        - **UNIX_LINES**: 启用Unix行模式，只识别'\n'作为行结束符
        - **CASE_INSENSITIVE**: 启用大小写不敏感匹配
        - **COMMENTS**: 允许在正则表达式中使用空白和注释
        - **MULTILINE**: 启用多行模式，改变^和$的匹配行为
        - **DOTALL**: 启用单行模式，使.能匹配包括行结束符在内的所有字符
        - **UNICODE_CASE**: 启用Unicode感知的大小写折叠
        - **CANON_EQ**: 启用规范等价性匹配
    - **核心方法**：
        - **getValue()**: 获取对应标志位的整数值，用于Pattern编译
    - **使用场景**：
        - 构建高级正则表达式匹配模式
        - 配置正则表达式引擎行为
        - 处理多语言文本匹配
        - 精确控制正则表达式的匹配规则


- **gson**: JSON序列化与反序列化支持
    - **功能**：
        - 提供各种Java类型与JSON之间的转换能力
        - 支持常用数据类型的自定义序列化和反序列化
        - 与Gson库无缝集成
    - **序列化器**：
        - **ClassJsonSerializer**: 将Class对象序列化为完全限定类名字符串
        - **DateJsonSerializer**: 将Date对象序列化为时间戳(毫秒)
        - **LocalDateJsonSerializer**: 将LocalDate对象转换为时间戳
        - **LocalDateTimeJsonSerializer**: 将LocalDateTime对象转换为时间戳
    - **反序列化器**：
        - **BigDecimalDeserializer**: 支持从字符串解析BigDecimal
        - **BigIntegerDeserializer**: 支持从字符串解析BigInteger
        - **ClassJsonDeserializer**: 从类名字符串还原Class对象
        - **DateJsonDeserializer**: 从时间戳或或时间字符串表示还原Date对象
        - **LocalDateJsonDeserializer**: 从时间戳或时间字符串表示还原LocalDate对象
        - **LocalDateTimeJsonDeserializer**: 从时间戳或或时间字符串表示还原LocalDateTime对象
    - **技术特点**：
        - 统一的时间类型处理策略(转换为时间戳)
        - 容错性强的数值类型转换
        - 类型安全的序列化与反序列化
        - 与JsonUtils工具类协同工作


- **NanoId**: 小型、安全、URL友好的唯一字符串ID生成器（拷贝自hu-tool)
    - **功能**：
        - 生成紧凑、安全的随机字符串标识符
        - 提供可定制的ID长度和字符集
        - 支持自定义随机数生成器
    - **核心特性**：
        - **安全性**: 使用加密级随机API，保证符号正确分配
        - **体积小**: 仅258字节大小(压缩后)，无外部依赖
        - **紧凑性**: 使用比UUID更多的符号集(A-Za-z0-9_-)
        - **URL友好**: 生成的ID可直接用于URL而无需编码
    - **主要方法**：
        - **randomNanoId()**: 生成默认长度(21字符)的NanoID
        - **randomNanoId(int)**: 生成指定长度的NanoID
        - **randomNanoId(Random, char[], int)**: 完全自定义生成参数
    - **技术实现**：
        - 基于SecureRandom提供密码学安全性
        - 使用位操作优化随机字符选择
        - 算法复杂度为O(1)，性能稳定


- **SnowflakeIdWorker**: 雪花算法ID生成器（Chat GPT 生成）
    - **功能**：
        - 生成全局唯一的64位长整型ID
        - 支持分布式环境下的高效ID生成
        - 提供线程安全的ID获取机制
    - **ID结构**：
        - **符号位(1位)**: 始终为0，不使用
        - **时间戳(41位)**: 相对于起始时间的毫秒数，可使用约69年
        - **数据中心ID(5位)**: 支持32个数据中心
        - **机器ID(5位)**: 每个数据中心支持32台机器
        - **序列号(12位)**: 每毫秒可生成4096个ID
    - **核心特性**：
        - **高性能**: 单机每秒可生成百万级ID
        - **时钟回拨处理**: 检测并拒绝时钟回拨情况
        - **自定义配置**: 支持自定义起始时间戳和序列号
        - **位运算优化**: 使用位运算提高生成效率


- **DataSize**: 数据大小表示类（拷贝自Spring framework)
    - **功能**：
        - 以字节为基本单位表示和操作数据大小
        - 支持从字符串解析数据大小（如"12MB"）
        - 提供不同数据单位间的转换
        - 不可变且线程安全的设计
    - **核心特性**：
        - **单位支持**: 支持字节(B)、千字节(KB)、兆字节(MB)、吉字节(GB)、太字节(TB)
        - **创建方法**: 提供多种静态工厂方法创建实例
        - **字符串解析**: 支持从"5MB"、"12KB"等字符串解析
        - **比较操作**: 实现Comparable接口，支持大小比较
    - **主要方法**：
        - **ofBytes/ofKilobytes/ofMegabytes/ofGigabytes/ofTerabytes**: 创建指定单位的数据大小
        - **parse(CharSequence)**: 从字符串解析数据大小
        - **toBytes/toKilobytes/toMegabytes/toGigabytes/toTerabytes**: 转换为不同单位
        - **isNegative()**: 检查是否为负值
    - **技术特点**：
        - 基于二进制前缀(1KB=1024B)而非十进制前缀
        - 使用正则表达式解析字符串表示
        - 支持自定义默认单位
        - 提供完整的equals和hashCode实现


- **TreeNode**: 树形结构节点通用接口
    - **功能**：
        - 定义树形数据结构的基本操作
        - 支持泛型化的节点标识和业务数据
        - 提供树形结构构建的核心方法
    - **核心特性**：
        - **泛型支持**: 灵活定义节点键类型(K)和节点数据类型(T)
        - **标准化接口**: 统一树形结构的操作方式
        - **简洁设计**: 仅包含必要的树形结构核心方法
    - **主要方法**：
        - **getNodeKey()**: 获取节点唯一标识键
        - **getParentNodeKey()**: 获取父节点唯一标识键
        - **setChildNodes(Collection<T>)**: 设置子节点集合


- **Constants**: 常用常量集合类
    - **功能**：
        - 提供各类常用常量的集中管理
        - 按功能分类组织常量定义
        - 减少项目中的魔法值使用
    - **常量分类**：
        - **日期时间常量**：
            - 标准日期时间格式(yyyy-MM-dd HH:mm:ss)
            - 标准日期格式(yyyy-MM-dd)
            - 标准时间格式(HH:mm:ss)
        - **字符相关常量**：
            - 中文字符范围('\u4e00'~'\u9fa5')
            - 数字字符范围('0'~'9')
            - 字母字符范围('A'~'Z', 'a'~'z')
            - 下划线字符('_')
        - **网络相关常量**：
            - HTTP/HTTPS协议前缀
            - HTTP成功状态码(200)
            - 路径分隔符('/')
            - Token前缀("Bearer ")
            - 本地IP地址(IPv4/IPv6)
        - **JSON相关常量**：
            - 空JSON对象("{}")
            - 空JSON数组("[]")
        - **文件相关常量**：
            - 通用MIME类型("*/*")
        - **反射相关常量**：
            - 方法前缀(get/set)
            - CGLIB代理相关标识
        - **XML相关常量**：
            - XML特殊字符转义(&nbsp;, &amp;等)


- **RegExPool**: 正则表达式常量池
    - **功能**：
        - 提供各类常用正则表达式的集中管理
        - 包含丰富的预定义正则表达式常量
        - 支持各种数据验证场景
    - **正则分类**：
        - **基础字符**：
            - 字母、数字、中文汉字等基本字符匹配
            - 十六进制颜色代码匹配
        - **网络相关**：
            - IPv4/IPv6地址匹配
            - URL/域名/Email地址验证
            - HTTP/FTP/File URL格式验证
        - **证件号码**：
            - 身份证号码
            - 护照号码
            - 车牌号码
            - 统一社会信用代码
        - **数值格式**：
            - 整数/浮点数
            - 货币金额(支持负数、千分位)
            - 版本号格式
        - **文件路径**：
            - Windows/Linux文件路径
            - 文件名称(含/不含扩展名)
        - **时间日期**：
            - 日期格式(YYYY-MM-DD)
            - 12/24小时制时间


- **random**: 随机数据生成工具包
    - **功能**：
        - 提供多种随机数据生成工具类
        - 支持安全和非安全随机数生成
        - 支持基本数据类型的随机数组和列表生成
    - **主要类**：
        - **RandomArray**: 随机数组生成工具类
            - 支持生成布尔、整数、长整数、浮点数等基本类型数组
            - 提供普通随机数组和元素唯一随机数组生成
            - 支持指定数值范围的随机数组生成
            - 提供三种随机策略：insecure(非安全)、secure(安全)、secureStrong(强安全)
        - **RandomList**: 随机列表生成工具类
            - 支持生成布尔、整数、长整数、浮点数等基本类型列表
            - 提供普通随机列表和元素唯一随机列表生成
            - 支持指定数值范围的随机列表生成
            - 与RandomArray共享相同的三种随机策略


- **ArrayUtils**: 数组操作工具类
    - **功能**：
        - 继承并扩展了Apache Commons Lang的ArrayUtils功能
        - 提供数组分割功能，将数组按指定大小分割成子数组列表
        - 支持所有基本类型数组和泛型数组操作
        - 线程安全的方法实现
    - **核心特性**：
        - **分割功能**: 支持将各种类型数组分割成指定大小的子数组列表
        - **全类型支持**: 覆盖boolean、byte、char、double、float、int、long、short和泛型数组
        - **边界处理**: 自动处理最后一个子数组可能小于指定大小的情况
        - **空值安全**: 对空数组和非法参数提供安全处理
    - **主要方法**：
        - **partition(boolean[], int)**: 分割布尔数组
        - **partition(byte[], int)**: 分割字节数组
        - **partition(char[], int)**: 分割字符数组
        - **partition(double[], int)**: 分割双精度浮点数数组
        - **partition(float[], int)**: 分割单精度浮点数数组
        - **partition(int[], int)**: 分割整数数组
        - **partition(long[], int)**: 分割长整数数组
        - **partition(short[], int)**: 分割短整数数组
        - **partition(T[], int)**: 分割泛型数组


- **DateFormatUtils**: 日期格式化工具类
    - **功能**：
        - 继承并扩展了Apache Commons Lang的DateFormatUtils功能
        - 提供常用日期时间格式化方法
        - 支持null值安全处理
    - **核心特性**：
        - **标准格式化**: 使用预定义的标准格式(yyyy-MM-dd HH:mm:ss等)
        - **多类型支持**: 支持Date对象和时间戳(Long)的格式化
        - **空值安全**: 对null值进行安全处理，返回空字符串
        - **便捷方法**: 提供无参方法获取当前时间的格式化结果
    - **主要方法**：
        - **formatDatetime()**: 格式化当前日期时间为"yyyy-MM-dd HH:mm:ss"
        - **formatDatetime(Date)**: 格式化指定Date为日期时间
        - **formatDatetime(Long)**: 格式化时间戳为日期时间
        - **formatDate()**: 格式化当前日期为"yyyy-MM-dd"
        - **formatDate(Date)**: 格式化指定Date为日期
        - **formatDate(Long)**: 格式化时间戳为日期
        - **formatTime(Date)**: 格式化指定Date为时间"HH:mm:ss"
        - **formatTime(Long)**: 格式化时间戳为时间


- **DateUtils**: 日期时间工具类
    - **功能**：
        - 继承并扩展了Apache Commons Lang的DateUtils功能
        - 提供日期解析、转换、计算等全面功能
        - 支持Java 8日期时间API与传统Date互转
        - 提供丰富的时间差计算方法
    - **核心特性**：
        - **日期解析**: 支持多种格式字符串解析为Date对象
        - **类型转换**: 支持Date、LocalDate、LocalDateTime、时间戳等类型互转
        - **默认值机制**: 所有方法支持空值安全处理和默认值
        - **时间差计算**: 提供毫秒、秒、分钟、小时、天等多种粒度的时间差计算
        - **截断计算**: 支持年、月、日、时、分、秒等字段的截断差值计算
    - **主要方法分类**：
        - **解析方法**: parseDate、parseDateOrDefault等
        - **转换方法**: toDate、toLocalDate、toLocalDateTime、getTime等
        - **时间差计算**: betweenMillis、betweenSeconds、betweenMinutes等
        - **截断计算**: truncateBetweenYears、truncateBetweenMonths等
        - **实用工具**: calculateAge、nowDate等


- **DesensitizationUtils**: 数据脱敏处理工具类
    - **功能**：
        - 符合阿里巴巴脱敏规则标准
        - 提供多种敏感数据的脱敏处理方法
        - 支持自定义脱敏规则和策略
    - **核心特性**：
        - **证件号码脱敏**: 身份证、军官证、护照等证件号码脱敏
        - **联系方式脱敏**: 手机号、固定电话、邮箱地址脱敏
        - **个人信息脱敏**: 姓名、地址、昵称等个人信息脱敏
        - **车辆信息脱敏**: 车牌号、发动机号、车架号等车辆信息脱敏
        - **金融信息脱敏**: 银行卡号、密码等金融信息脱敏
        - **医保社保脱敏**: 社保卡号、医保卡号等信息脱敏
    - **脱敏策略**：
        - **左侧保留**: 保留字符串左侧指定位数，右侧用星号填充
        - **右侧保留**: 保留字符串右侧指定位数，左侧用星号填充
        - **环形保留**: 保留字符串首尾指定位数，中间用星号填充
        - **全部隐藏**: 将整个字符串替换为星号


- **IdCardUtils**: 身份证号码处理工具类
    - **功能**：
        - 提供身份证号码验证功能
        - 支持从身份证号码解析性别信息
        - 支持从身份证号码解析出生日期
    - **核心特性**：
        - **号码验证**: 支持18位身份证号码的合法性校验
        - **校验码验证**: 实现国家标准的身份证校验码算法
        - **日期验证**: 验证身份证中的出生日期是否合法
        - **多格式支持**: 同时支持15位和18位身份证号码解析
    - **主要方法**：
        - **validate(String)**: 验证身份证号码的有效性
        - **parseSex(String)**: 解析身份证持有者性别
        - **parseBirthDate(String)**: 解析身份证中的出生日期


- **IdUtils**: ID生成工具类
    - **功能**：
        - 提供多种分布式ID生成方案
        - 支持UUID、MongoDB ObjectId、NanoId、雪花算法等
        - 包含标准格式和简化格式的ID生成
    - **核心特性**：
        - **UUID生成**:
            - 标准UUID(带连字符)和简化UUID(无连字符)
            - 高性能UUID实现，支持自定义随机源
        - **ObjectId**:
            - 兼容MongoDB的ObjectId生成
            - 基于时间戳、机器码和计数器的复合ID
        - **NanoId**:
            - 提供URL安全的随机字符串ID
            - 支持自定义长度配置
        - **雪花算法ID**:
            - 基于Twitter Snowflake算法的分布式ID
            - 64位长整型，包含时间戳、工作机器ID和序列号
    - **主要方法**：
        - **randomUUID/simpleRandomUUID**: 生成标准/简化UUID
        - **fastUUID/simpleFastUUID**: 高性能标准/简化UUID
        - **objectId**: 生成MongoDB风格的ObjectId
        - **nanoId**: 生成默认或指定长度的NanoId
        - **snowflakeId**: 基于雪花算法生成分布式ID


- **JsonUtils**: JSON处理工具类
    - **功能**：
        - 基于Gson实现的JSON序列化与反序列化工具
        - 提供Java对象与JSON字符串、JsonElement的双向转换
        - 支持集合类型与JsonArray的互相转换
    - **核心特性**：
        - **预配置Gson实例**: 提供默认配置的Gson实例，支持null值序列化和格式化输出
        - **类型适配器**: 内置Date、LocalDate、LocalDateTime、BigDecimal等类型的序列化/反序列化支持
        - **泛型支持**: 完整支持泛型类型的序列化与反序列化
        - **空值安全**: 所有方法对null值进行安全处理，避免NullPointerException
        - **自定义Gson**: 支持使用自定义Gson实例进行操作
    - **主要方法分类**：
        - **字符串操作**: fromString/toString系列方法，处理JSON字符串与Java对象的转换
        - **JsonElement操作**: fromJson/toJson系列方法，处理JsonElement与Java对象的转换
        - **数组操作**: fromJsonArray/toJsonArray系列方法，处理JsonArray与Java集合的转换
        - **GsonBuilder**: 提供createGsonBuilder方法创建预配置的GsonBuilder


- **ReflectionUtils**: 反射操作工具类
    - **功能**：
        - 继承并扩展了org.reflections.ReflectionUtils的功能
        - 提供字段访问、方法处理、类信息获取等反射相关操作
        - 支持对私有字段和方法的安全访问
    - **核心特性**：
        - **字段操作**: 获取/设置对象字段值，支持私有字段访问
        - **类型信息**: 获取类名、泛型类型参数等类型信息
        - **代理处理**: 识别和处理CGLIB代理类
        - **方法识别**: 判断常见方法类型(equals/hashCode/toString等)
        - **访问控制**: 提供字段和方法的访问权限控制
    - **主要方法分类**：
        - **字段访问**: getFieldValue/setFieldValue系列方法
        - **类型信息**: getClassName/getClassGenericType系列方法
        - **代理处理**: getUserClass/isCglibRenamedMethod等方法
        - **方法判断**: isEqualsMethod/isHashCodeMethod/isToStringMethod等
        - **访问控制**: isAccessible/makeAccessible系列方法


- **RegExUtils**: 正则表达式工具类
    - **功能**：
        - 继承并扩展了Apache Commons Lang的RegExUtils功能
        - 提供正则表达式编译、匹配检测、模式查找等增强功能
        - 支持正则表达式标志位的灵活配置
    - **核心特性**：
        - **模式编译**: 支持多种参数组合的正则表达式编译方式
        - **起止匹配**: 自动添加起始(^)和结束($)匹配符号
        - **标志位管理**: 支持通过枚举组合计算正则表达式标志位
        - **匹配检测**: 提供字符串与正则表达式的完全匹配检测
        - **模式查找**: 查找字符串中所有匹配正则表达式的子串
    - **主要方法**：
        - **compile系列**: 多种参数组合的Pattern对象编译方法
        - **matches系列**: 检查字符串是否完全匹配正则表达式
        - **find系列**: 查找字符串中所有匹配的子串并返回列表
        - **computeFlags**: 计算正则表达式标志位的组合值


- **SerializationUtils**: Java对象序列化工具类（拷贝自Spring framework)
    - **功能**：
        - 提供Java对象序列化和反序列化的静态工具方法
        - 基于Java标准序列化机制实现
        - 代码源自Spring Framework的SerializationUtils
    - **核心特性**：
        - **对象序列化**: 将Java对象序列化为字节数组
        - **对象克隆**: 通过序列化和反序列化实现对象的深度克隆
        - **空值处理**: 对null值进行安全处理
        - **异常处理**: 提供友好的异常信息和类型转换
    - **主要方法**：
        - **serialize(Object)**: 将对象序列化为字节数组
        - **clone(T)**: 通过序列化机制实现对象的深度克隆
    - **安全提示**：
        - 应谨慎使用，参考Java编程语言安全编码指南
        - 仅用于可信数据的序列化和反序列化


- **StringFormatUtils**: 字符串格式转换工具类
    - **功能**：
        - 提供多种命名格式转换方法
        - 支持各种编程命名规范之间的互相转换
        - 处理大小写和分隔符的智能转换
    - **核心特性**：
        - **驼峰命名法**: 支持小驼峰(camelCase)和大驼峰(PascalCase)格式转换
        - **下划线命名法**: 支持snake_case和SCREAMING_SNAKE_CASE格式转换
        - **中横线命名法**: 支持kebab-case和SCREAMING-KEBAB-CASE格式转换
        - **自定义分隔符**: 支持使用自定义分隔符进行格式转换
        - **空值安全**: 所有方法对空值进行安全处理
    - **主要方法**：
        - **formatAsCamelCase**: 转换为小驼峰格式(如: userName)
        - **formatAsPascalCase**: 转换为大驼峰格式(如: UserName)
        - **formatAsSnakeCase**: 转换为下划线格式(如: user_name)
        - **formatAsScreamingSnakeCase**: 转换为全大写下划线格式(如: USER_NAME)
        - **formatAsKebabCase**: 转换为中横线格式(如: user-name)
        - **formatAsScreamingKebabCase**: 转换为全大写中横线格式(如: USER-NAME)


- **StringUtils**: 字符串工具类
    - **功能**：
        - 继承并扩展Apache Commons Lang的StringUtils功能
        - 提供字符集转换、集合元素过滤等增强方法
        - 支持字符串集合和数组的非空元素提取
    - **核心特性**：
        - **字符集转换**: 支持字符串在不同字符集之间的转换
        - **集合过滤**: 从集合或数组中提取非空字符串元素
        - **去重处理**: 支持获取唯一且非空的字符串元素
        - **空值安全**: 所有方法对空值进行安全处理
    - **主要方法**：
        - **convertCharset系列**: 在不同字符集之间转换字符串
        - **getNotBlankElements系列**: 获取集合或数组中的非空字符串元素
        - **getUniqueNotBlankElements系列**: 获取集合或数组中唯一且非空的字符串元素


- **TreeUtils**: 树形结构构建工具类
    - **功能**：
        - 提供将扁平数据转换为树形结构的能力
        - 支持单根/多根树构建
        - 适用于菜单、目录等树形数据结构处理
    - **核心特性**：
        - **树形转换**: 将线性集合数据转换为层次结构
        - **灵活配置**: 支持自定义根节点标识
        - **节点处理**: 支持在构建过程中对节点进行转换处理
        - **泛型支持**: 适用于任何实现TreeNode接口的节点类型
    - **主要方法**：
        - **toTree(Collection, rootNodeKey)**: 基础树形结构构建方法
        - **toTree(Collection, rootNodeKey, convertFunc)**: 支持节点转换的树形结构构建方法

### 🔒 pangju-commons-crypto (安全加密模块)

加密工具模块，基于jasypt实现数据加密。

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-crypto</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **CryptoConstants**: 加密算法相关常量类
    - **功能**：
        - 提供加密模块中使用的标准算法名称
        - 定义默认配置参数和合规性检查相关常量
        - 包含多种密码学算法的标准配置参数
    - **常量分类**：
        - **算法名称**：
            - RSA_ALGORITHM: RSA算法标准名称
            - DIFFIE_HELLMAN_ALGORITHM: Diffie-Hellman密钥交换算法标准名称
            - DSA_ALGORITHM: DSA数字签名算法标准名称
        - **密钥长度**：
            - RSA_DEFAULT_KEY_SIZE: RSA默认密钥长度(2048位)
            - RSA_KEY_SIZE_SET: 允许的RSA密钥长度集合(1024/2048/4096位)
            - DIFFIE_HELLMAN_KEY_SIZE_SET: 允许的Diffie-Hellman密钥长度集合
            - DSA_KEY_SIZE_SET: 允许的DSA密钥长度集合
    - **技术特点**：
        - 符合Java密码体系结构(JCA)标准命名
        - 根据行业安全标准设置推荐密钥长度
        - 提供密钥长度合规性验证支持


- **RSAKey**: RSA密钥对不可变容器类
    - **功能**：
        - 封装RSA算法所需的公钥和私钥
        - 提供多种密钥生成和解析方式
        - 确保密钥对象的线程安全性和不可变性
    - **核心特性**：
        - **不可变设计**: 所有字段均为final，确保线程安全
        - **多格式支持**: 支持随机生成、密钥对转换、原始字节和Base64解析
        - **空值安全**: 允许公钥或私钥单独为null
        - **标准兼容**: 严格遵循X.509和PKCS#8标准
    - **核心方法**：
        - **密钥生成**：
            - random(): 生成默认长度(2048位)的随机RSA密钥对
            - random(int): 生成指定长度的随机RSA密钥对(1024/2048/4096位)
        - **密钥转换**：
            - fromKeyPair(): 从现有KeyPair构建RSAKey实例
            - fromRawBytes(): 从原始字节数组构建RSAKey
            - fromBase64String(): 从Base64编码字符串构建RSAKey
    - **技术特点**：
        - 使用Java Record实现不可变对象
        - 提供全面的参数验证
        - 自动处理异常转换
        - 支持多种密钥格式和编码


- **KeyPairUtils**: 密钥对工具类
    - **功能**：
        - 提供密钥对生成、密钥编解码及密钥对象转换功能
        - 封装Java安全体系中的密钥操作
        - 支持多种非对称加密算法(RSA、DSA等)的密钥操作
    - **核心特性**：
        - **对象缓存**: 缓存KeyFactory和KeyPairGenerator实例提高性能
        - **多格式支持**: 支持原始字节、Base64编码字符串等多种密钥格式
        - **标准兼容**: 严格遵循PKCS#8和X.509标准
        - **线程安全**: 所有方法均为线程安全的静态方法
    - **核心方法**：
        - **密钥生成**：
            - generateKeyPair(): 生成指定算法的密钥对(支持默认参数/指定长度/自定义随机源)
        - **密钥解析**：
            - getPrivateKeyFromPKCS8Base64String()/getPrivateKeyFromPKCS8RawBytes(): 解析PKCS#8格式私钥
            - getPublicKeyFromX509Base64String()/getPublicKeyFromX509RawBytes(): 解析X.509格式公钥
        - **工厂获取**：
            - getKeyFactory(): 获取指定算法的密钥工厂(带缓存机制)
    - **技术特点**：
        - 使用ConcurrentHashMap实现对象缓存
        - 严格的参数验证和异常处理
        - 支持多种密钥格式和编码
        - 提供详细的文档和使用示例


- **RSAByteDigester**: RSA数字签名处理器
    - **功能**：
        - 实现基于RSA非对称加密算法的数字签名功能
        - 提供签名生成和验证的完整解决方案
        - 支持多种JCA标准签名算法(SHA256withRSA等)
    - **核心特性**：
        - **签名生成**: 使用私钥对数据进行数字签名
        - **签名验证**: 使用公钥验证签名的有效性
        - **算法扩展**: 支持多种签名算法(SHA256withRSA/SHA384withRSA等)
        - **线程安全**: 所有关键操作都进行了同步控制
        - **延迟初始化**: 按需初始化签名组件，提高资源利用率
    - **构造方法**：
        - 支持默认配置快速创建实例
        - 支持自定义密钥长度(1024/2048/4096位)
        - 支持自定义签名算法
        - 支持使用预生成的RSA密钥对
    - **技术特点**：
        - 实现ByteDigester接口，提供标准化操作
        - 自动处理空值和边界情况
        - 提供详细的异常信息和类型
        - 支持公钥和私钥的独立使用


- **RSAStringDigester**: RSA字符串签名处理器
    - **功能**：
        - 实现基于RSA算法的字符串签名功能
        - 提供字符串消息的签名生成和验证
        - 支持多种编码格式的签名输出和验证
    - **核心特性**：
        - **字符串签名**: 对字符串消息生成数字签名
        - **签名验证**: 验证字符串消息与签名的匹配性
        - **多格式支持**: 支持Base64和十六进制编码格式
        - **线程安全**: 所有操作都进行了同步控制
    - **编码格式**：
        - **Base64编码**: digest()/matches()方法使用
        - **十六进制编码**: digestToHexString()/matchesFromHexString()方法使用
    - **技术特点**：
        - 基于RSAByteDigester实现，复用底层签名逻辑
        - 自动处理字符串编码转换(UTF-8)
        - 提供空值安全处理
        - 实现StringDigester接口，提供标准化操作


- **RSATransformation**: RSA加密转换策略接口
    - **功能**：
        - 定义加密算法模式、填充方式及分块处理逻辑
        - 提供标准化的转换方案定义
        - 支持多种填充模式和分块策略
    - **核心职责**：
        - **算法定义**: 提供标准化的算法/模式/填充名称
        - **分块计算**: 根据密钥规格计算加密/解密分块尺寸
        - **扩展支持**: 允许自定义填充方案实现
    - **主要方法**：
        - getName(): 获取完整的算法转换方案名称
        - getEncryptBlockSize(): 计算公钥加密分块尺寸
        - getDecryptBlockSize(): 计算私钥解密分块尺寸(默认实现)
    - **内置实现**：
        - **RSAPKCS1PaddingTransformation**: PKCS#1 v1.5填充方案实现
        - **RSAOEAPWithSHA256Transformation**: OAEP-SHA256填充方案实现(推荐)
    - **技术特点**：
        - 接口设计支持策略模式
        - 提供默认实现减少重复代码
        - 严格遵循JCE标准命名规范
        - 自动处理分块大小计算


- **RSABinaryEncryptor**: RSA二进制数据加密解密器
    - **功能**：
        - 实现基于RSA算法的二进制数据加密/解密功能
        - 提供分段加密/解密能力，自动处理大数据
        - 支持公钥加密/私钥解密的非对称加密流程
    - **核心特性**：
        - **分段加密/解密**: 自动处理大数据的分块操作
        - **多密钥支持**: 支持公钥加密/私钥解密
        - **算法扩展**: 支持多种填充模式(如OAEPWithSHA-256)
        - **线程安全**: 所有关键操作都进行了同步控制
    - **构造方法**：
        - 支持默认配置快速创建实例
        - 支持自定义密钥长度(1024/2048/4096位)
        - 支持自定义加密方案
        - 支持使用预生成的RSA密钥对
    - **技术特点**：
        - 实现BinaryEncryptor接口，提供标准化操作
        - 延迟初始化设计，提高资源利用率
        - 自动处理空值和边界情况
        - 提供详细的异常信息和类型


- **RSATextEncryptor**: 基于RSA算法的文本加密解密器
    - **功能**：
        - 提供安全的非对称文本加解密能力
        - 实现TextEncryptor接口，专门处理字符串数据
        - 支持两种输出编码格式：Base64和十六进制
    - **核心特性**：
        - **安全算法**: 采用RSA非对称加密，支持2048/3072/4096位密钥强度
        - **编码支持**: Base64编码(网络传输)和十六进制编码(调试日志)
        - **加密方案**: 支持PKCS#1 v1.5、OAEP等多种填充模式
        - **字符编码**: 强制使用UTF-8编码保证跨平台一致性
    - **构造方法**：
        - 支持默认安全配置快速创建实例
        - 支持自定义密钥长度和加密方案
        - 支持使用预生成的RSA密钥对
        - 支持复用已有二进制加密器配置
    - **技术特点**：
        - 线程安全设计，适用于并发环境
        - 委托模式，底层依赖RSABinaryEncryptor
        - 自动处理空值和边界情况
        - 提供详细的异常信息和类型


- **RSADecimalNumberEncryptor**: RSA算法高精度浮点数加密器
    - **功能**：
        - 提供BigDecimal类型的精确加密解密能力
        - 通过分离标度和无标度值实现浮点数无损加密
        - 确保解密后能完全还原原始数值精度
    - **核心特性**：
        - **精度保留**: 精确保持原始数值的小数位数和精度
        - **安全算法**: 采用RSA非对称加密算法，支持2048/3072/4096位密钥
        - **数值完整性**: 加密后的BigDecimal仍保持数学运算特性
        - **格式兼容**: 处理补码格式确保跨平台一致性
    - **技术实现**：
        - 加密流程：BigDecimal → 分离标度 → 无标度值加密 → 重组
        - 解密流程：解析加密值 → 解密无标度值 → 应用原始标度
        - 最大处理精度：支持BigDecimal的任意标度
    - **构造方法**：
        - 支持默认金融级安全配置快速创建实例
        - 支持自定义密钥长度和加密方案
        - 支持使用预生成的RSA密钥对
        - 支持复用已有二进制加密器配置
    - **应用场景**：
        - 金融系统金额加密（如汇率、利率计算）
        - 科学计算数据保护
        - 数据库敏感浮点字段加密
        - 需要精确小数运算的安全协议


- **RSAIntegerNumberEncryptor**: 基于RSA算法的大整数加密解密器
    - **功能**：
        - 提供精确的BigInteger类型数值加密能力
        - 保留原始数值的数学特性
        - 适用于需要精确数值运算的安全场景
    - **核心特性**：
        - **精确加密**: 保持BigInteger的数值精度和符号位
        - **安全算法**: 采用RSA非对称加密算法，支持2048/3072/4096位密钥
        - **编码规范**: 使用二进制补码格式处理数值，确保跨平台一致性
        - **长度标识**: 自动添加4字节长度头，支持变长数据解密
    - **技术实现**：
        - 加密流程：BigInteger → 补码字节数组 → RSA加密 → 添加长度头 → 新BigInteger
        - 解密流程：加密BigInteger → 提取数据 → RSA解密 → 重构原始BigInteger
        - 最大加密长度：密钥长度/8 - 填充长度 - 4字节头
    - **构造方法**：
        - 支持默认安全配置快速创建实例
        - 支持自定义密钥长度和加密方案
        - 支持使用预生成的RSA密钥对
        - 支持复用已有二进制加密器配置
    - **应用场景**：
        - 金融交易金额加密
        - 密码学协议中的大数运算
        - 数据库ID字段加密
        - 需要精确恢复的数值加密

### ✔️ pangju-commons-validation (校验模块)

校验模块，基于jakarta.validation实现。

````xml
<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-validation</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **annotation**: 校验注解
    - **基础校验**：
        - `@NotBlankElements`: 验证集合中的字符串元素非空白
        - `@RegexElements`: 验证集合元素匹配指定正则表达式
    - **格式校验**：
        - `@BankCard`: 银行卡号格式校验（支持主流银行）
        - `@ChineseName`: 中文姓名校验（2-4个汉字）
        - `@IdCard`: 身份证号码校验（支持18位+校验码）
        - `@Filename`: 文件名格式校验（支持扩展名校验）
        - `@HexColor`: 十六进制颜色值校验
        - `@IP`: IPv4/IPv6地址格式校验
        - `@Identifier`: 通用标识符校验（字母数字下划线）
        - `@Base64`: Base64编码格式验证
        - `@Md5`: MD5哈希值格式校验
    - **业务校验**：
        - `@HttpMethod`: HTTP请求方法校验（GET/POST等）
        - `@MimeType`: 媒体类型格式校验（符合IANA标准）
        - `@RequestPath`: HTTP请求路径格式校验
        - `@EnumName`: 枚举名称有效性校验
        - `@Number`: 数字格式校验（支持正负/小数）
        - `@PhoneNumber`: 电话号码格式校验（固话/手机）

### 🌏 pangju-commons-geo (地理信息模块)

地理信息模块，封装了坐标转换、解析、判断等方法。

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-geo</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **CoordinateType**: 地理坐标系类型枚举
    - **功能**：
        - 定义并管理常用的地理坐标系标准
        - 提供坐标系间的转换能力
        - 封装不同坐标系间的转换逻辑
    - **支持的坐标系**：
        - GCJ_02：国测局坐标系（火星坐标系）
            - 中国官方地图坐标系统
            - 基于WGS-84进行非线性加密
            - 高德、腾讯、百度等地图服务采用
            - 与WGS-84存在50-500米偏移
        - WGS_84：世界大地坐标系
            - 国际通用GPS坐标系统
            - GPS设备原始坐标体系
            - Google Earth等国际地图服务采用
    - **核心方法**：
        - toGCJ02(): 将当前坐标系坐标转换为GCJ-02坐标
        - toWGS84(): 将当前坐标系坐标转换为WGS-84坐标


- **GeoConstants**: GEO地理信息相关常量类
    - **功能**：
        - 提供地理信息处理中常用的常量定义
        - 包含经纬度符号、方向标识和地理边界值
    - **常量分类**：
        - **符号常量**：
            - RADIUS_CHAR: 度符号(°)
            - MINUTE_CHAR: 分符号(')
            - SECONDS_CHAR: 秒符号(")
        - **方向标识**：
            - North_DIRECTION: 北方标识符(N)
            - SOUTH_CHAR: 南方标识符(S)
            - EAST_CHAR: 东方标识符(E)
            - WEST_CHAR: 西方标识符(W)
        - **全球边界值**：
            - MIN_LATITUDE/MAX_LATITUDE: 全球最小/最大纬度值(-90°/90°)
            - MIN_LONGITUDE/MAX_LONGITUDE: 全球最小/最大经度值(-180°/180°)
        - **中国边界值**：
            - CHINA_MIN_LATITUDE: 中国最南端曾母暗沙的纬度(0.8293°)
            - CHINA_MAX_LATITUDE: 中国最北端漠河的纬度(55.8271°)
            - CHINA_MIN_LONGITUDE: 中国最西端帕米尔高原的经度(72.004°)
            - CHINA_MAX_LONGITUDE: 中国最东端黑龙江与乌苏里江交汇处的经度(137.8347°)


- **Coordinate**: 地理坐标模型类
    - **功能**：
        - 表示一个具有高精度的地理坐标点
        - 封装坐标验证、格式转换和位置判断功能
        - 使用BigDecimal保证计算精度
        - 适用于地理信息系统(GIS)、地图服务等场景
    - **核心特性**：
        - 不可变性：使用Java record类型实现线程安全
        - 精度保障：使用BigDecimal存储避免浮点误差
        - 自动验证：构造时进行经纬度范围校验
        - 格式转换：支持多种坐标格式
    - **构造方法**：
        - 主构造方法：接收BigDecimal类型的经纬度
        - 双精度构造方法：接收double类型的经纬度
        - 度分秒格式构造方法：接收DMS格式字符串
    - **核心方法**：
        - isOutOfChina(): 判断坐标是否在中国境外
        - toString(): 将坐标转换为标准度分秒表示


- **CoordinateUtils**: 地理坐标转换工具类
    - **功能**：
        - 提供高精度的地理坐标转换功能
        - 支持坐标系转换（WGS-84与GCJ-02互转）
        - 提供度分秒格式与十进制度格式互转
        - 基于BigDecimal实现高精度计算
    - **核心方法**：
        - **坐标系转换**：
            - WGS84ToGCJ02(): 世界大地坐标系转火星坐标系
            - GCJ02ToWGS84(): 火星坐标系转世界大地坐标系
        - **格式转换**：
            - toLatitudeDMS(): 十进制纬度转度分秒格式
            - toLongitudeDms(): 十进制经度转度分秒格式
            - fromDMS(): 度分秒格式转十进制度
        - **内部实现**：
            - computeGcj02Delta(): 计算GCJ-02坐标系偏移量
            - transformLongitude()/transformLatitude(): 经纬度偏移变换计算
    - **技术特点**：
        - 使用BigDecimalMath进行高精度数学运算
        - 基于WGS84椭球体参数计算
        - GCJ02偏移算法参考公开实现
        - 转换精度达±50-500米

### 📁 pangju-commons-io (IO模块)

IO模块，提供了对于文件名、文件、io流、文件类型判断等各种操作

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-io</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **FileType**: 文件类型枚举类
    - **功能**：
        - 基于MIME类型的文件分类系统
        - 支持两种匹配模式：
            - 前缀匹配：通过typePrefix匹配某一类文件（如"image/"匹配所有图片）
            - 精确匹配：通过types集合匹配特定文件类型（如压缩包的各种具体格式）
        - 预定义7种常见文件类型：
            - IMAGE：图片类型（JPEG、PNG、GIF、WEBP等）
            - TEXT：文本类型（TXT、CSV、HTML、XML等）
            - AUDIO：音频类型（MP3、WAV、AAC、OGG等）
            - MODEL：3D模型类型（STL、OBJ、FBX等）
            - VIDEO：视频类型（MP4、AVI、MKV、MOV等，含HLS流媒体）
            - COMPRESS：压缩包类型（TAR、GZIP、ZIP、RAR、7Z、CAB等）
            - DOCUMENT：办公文档类型（PDF、Excel、Word、PPT等）


- **IOConstants**: IO相关常量类
    - **功能**：
        - 集中管理IO操作相关的常量定义
        - 提供线程安全的Tika单例实例获取方法
        - 定义各种MIME类型前缀常量
    - **常量分类**：
        - **MIME类型前缀**：
            - IMAGE_MIME_TYPE_PREFIX: 图片类型前缀("image/")
            - VIDEO_MIME_TYPE_PREFIX: 视频类型前缀("video/")
            - AUDIO_MIME_TYPE_PREFIX: 音频类型前缀("audio/")
            - MODEL_MIME_TYPE_PREFIX: 3D模型类型前缀("model/")
            - TEXT_MIME_TYPE_PREFIX: 文本类型前缀("text/")
            - APPLICATION_MIME_TYPE_PREFIX: 应用类型前缀("application/")
    - **核心方法**：
        - getDefaultTika(): 获取线程安全的Apache Tika单例实例
            - 采用双重校验锁实现线程安全
            - 延迟初始化节省资源
            - 用于文件内容类型检测


- **FilenameUtils**: 文件名及路径处理工具类
    - **功能**：
        - 继承并扩展了Apache Commons IO的FilenameUtils功能
        - 提供基于文件扩展名的MIME类型检测系统
        - 支持多种文件类型判断（图片/文本/视频/音频/应用等）
        - 精确区分目录路径与文件路径
        - 支持多MIME类型集合匹配校验
        - 提供文件名重构功能（全名替换、基名替换、扩展名替换）
    - **核心方法**：
        - **MIME类型检测**：
            - getMimeType(): 获取文件MIME类型
            - isMimeType(): 精确匹配MIME类型
            - isAnyMimeType(): 批量匹配MIME类型（支持数组和集合）
        - **文件类型判断**：
            - isImageType(): 判断是否为图片类型
            - isTextType(): 判断是否为文本类型
            - isVideoType(): 判断是否为视频类型
            - isAudioType(): 判断是否为音频类型
            - isModelType(): 判断是否为3D模型类型
            - isApplicationType(): 判断是否为应用类型
        - **路径智能识别**：
            - isDirectoryPath(): 判断是否为目录路径
            - isFilePath(): 判断是否为文件路径
        - **文件名重构**：
            - rename(): 完全替换文件名（包含名称和扩展名）
            - replaceBaseName(): 替换文件基名（保留扩展名和路径）
            - replaceExtension(): 替换文件扩展名


- **FileUtils**: 增强型文件操作工具类
    - **功能**：
        - 继承并扩展了Apache Commons IO的FileUtils功能
        - 提供高性能IO流操作，支持大文件处理
        - 完整的文件加解密体系（AES/CBC和AES/CTR两种模式）
        - 基于Apache Tika的文件元数据解析和内容类型检测
        - 增强的文件删除和重命名功能
    - **核心方法**：
        - **高性能IO流**：
            - openUnsynchronizedBufferedInputStream(): 打开非同步缓冲输入流
            - openBufferedFileChannelInputStream(): 打开缓冲文件通道输入流
            - openMemoryMappedFileInputStream(): 打开内存映射文件输入流
        - **文件加解密**：
            - encryptFile()/decryptFile(): AES/CBC模式文件加解密
            - encryptFileByCtr()/decryptFileByCtr(): AES/CTR模式文件加解密
        - **文件类型检测**：
            - getMimeType(): 获取文件真实MIME类型
            - isImageType()/isTextType()/isVideoType()等: 检测文件类型
            - isMimeType()/isAnyMimeType(): 精确匹配文件MIME类型
        - **元数据解析**：
            - parseMetaData(): 解析文件内容元数据
        - **文件操作**：
            - forceDeleteIfExist(): 强制删除文件或目录
            - rename()/replaceBaseName()/replaceExtension(): 文件重命名和修改
            - exist()/notExist()/existFile()/notExistFile(): 文件存在性检查


- **IOUtils**: 增强型IO流操作工具类
    - **功能**：
        - 继承并扩展了Apache Commons IO的IOUtils功能
        - 提供基于Apache Commons Crypto的AES加解密能力
        - 支持CBC和CTR两种加密模式的流式处理
        - 提供非同步缓冲流实现，优化单线程性能
        - 智能缓冲区大小计算系统
    - **核心方法**：
        - **缓冲区优化**：
            - getBufferSize(): 根据数据大小智能计算最佳缓冲区大小
        - **非同步流操作**：
            - unsynchronizedBuffer(): 创建非同步缓冲输入流
            - toUnsynchronizedByteArrayInputStream(): 创建非同步字节数组输入流
            - toUnsynchronizedByteArrayOutputStream(): 创建非同步字节数组输出流
        - **AES/CBC加解密**：
            - encrypt()/decrypt(): 基本AES/CBC模式加解密（默认IV）
            - encrypt()/decrypt(): 扩展AES/CBC模式加解密（自定义IV）
        - **AES/CTR加解密**：
            - encryptByCtr()/decryptByCtr(): 基本AES/CTR模式加解密（默认IV）
            - encryptByCtr()/decryptByCtr(): 扩展AES/CTR模式加解密（自定义IV）
        - **通用加解密**：
            - encrypt()/decrypt(): 通用加密方法（支持自定义密钥和算法参数）

### 🖼️ pangju-commons-image (图像模块)

图像模块，提供了图像类型检测、类型获取、宽高读取、exif方向读取、缩略图生成，应用滤镜等各种操作

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-image</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **ImageConstants**: 图像处理相关常量类
    - **功能**：
        - 提供图像处理中常用的常量定义
        - 管理系统支持的图像格式信息
        - 线程安全的懒加载实现
    - **常量分类**：
        - **格式支持**：
            - NON_TRANSPARENT_IMAGE_FORMATS: 不支持透明通道的图像格式集合
        - **系统能力**：
            - SUPPORT_READ_IMAGE_TYPES: 系统支持的可读取图像MIME类型集合
            - SUPPORT_WRITE_IMAGE_TYPES: 系统支持的可写入图像MIME类型集合
    - **核心方法**：
        - getSupportReadImageTypes(): 获取系统支持的可读取图像MIME类型集合
        - getSupportWriteImageTypes(): 获取系统支持的可写入图像MIME类型集合
    - **技术特点**：
        - 使用双重检查锁实现线程安全初始化
        - 使用volatile保证可见性
        - 返回不可变集合保证数据安全


- **ImageSize**: 图像尺寸模型类
    - **功能**：
        - 表示经过方向校正后的图像实际显示尺寸
        - 封装宽度和高度两个不可变属性
        - 提供多种保持宽高比的尺寸缩放计算方法
    - **核心特性**：
        - 不可变性：使用Java record类型实现线程安全
        - 宽高比保持：所有缩放操作保持原始比例
        - 像素保护：计算结果最小为1像素
    - **核心方法**：
        - **单约束缩放**：
            - scaleByWidth(): 基于目标宽度的等比缩放
            - scaleByHeight(): 基于目标高度的等比缩放
        - **双约束缩放**：
            - scale(ImageSize): 基于目标尺寸对象的等比缩放
            - scale(int, int): 基于目标宽高值的等比缩放


- **ImageUtils**: 图像处理工具类
    - **功能**：
        - 提供全面的图像处理功能
        - 图像元数据处理（读取EXIF信息、方向校正等）
        - 图像格式检测（支持JPEG、PNG、GIF等常见格式）
        - 图像尺寸处理（自动处理EXIF方向信息）
        - MIME类型转换（图像格式与MIME类型互转）
    - **核心方法**：
        - **MIME类型处理**：
            - isSupportReadType()/isSupportWriteType(): 检查MIME类型是否支持读取/写入
            - isSameType(): 判断两个MIME类型是否属于同一图像类型
            - getMimeType(): 获取图像的MIME类型（多种重载形式）
        - **尺寸处理**：
            - getSize(): 获取图像尺寸（多种重载形式，自动处理EXIF方向）
        - **EXIF处理**：
            - getExifOrientation(): 获取图像的EXIF方向信息
    - **技术特点**：
        - 线程安全 - 无共享状态
        - 高性能 - 提供针对大文件的优化选项
        - 自动处理EXIF方向信息
        - 支持多种输入源（文件、字节数组、输入流）


- **ThumbnailUtils**: 缩略图生成工具类
    - **功能**：
        - 提供多种图像缩放策略和输出方式
        - 支持强制缩放（不考虑原始宽高比）
        - 支持等比缩放（保持原始宽高比）
        - 支持多种输出目标（文件、输出流、BufferedImage对象）
    - **核心方法**：
        - **强制缩放**：
            - forceScale(): 不考虑原始宽高比，直接缩放到指定尺寸
        - **等比缩放**：
            - scaleByWidth(): 根据宽度等比缩放图像
            - scaleByHeight(): 根据高度等比缩放图像
            - scale(): 根据目标尺寸等比缩放图像
        - **内部实现**：
            - resample(): 使用重采样算法处理图像缩放
    - **技术特点**：
        - 自动处理图像格式转换（如JPG转PNG时的透明通道处理）
        - 提供多种重采样滤波器选择（默认使用三角形滤波器）
        - 完善的参数校验和异常处理
        - 线程安全实现
    - **典型应用**：
        - 图像缩略图生成
        - 图像等比例缩放
        - 图像格式转换


- **ImageFilterUtils**: 图像滤镜处理工具类
    - **功能**：
        - 提供专业的图像滤镜处理功能
        - 支持灰度化、亮度/对比度调整等核心操作
        - 支持多种输出格式和输出方式
        - 所有操作均保持线程安全
    - **核心方法**：
        - **灰度处理**：
            - grayscale(): 将图像转换为灰度图像（多种重载形式）
        - **亮度调整**：
            - brightness(): 调整图像亮度（支持[-2.0, 2.0]范围调整）
        - **对比度调整**：
            - contrast(): 调整图像对比度（支持[-1.0, 1.0]范围调整）
        - **通用滤镜**：
            - filter(): 应用自定义图像滤镜（支持任意ImageFilter实现）
    - **技术特点**：
        - 多格式支持 - 支持PNG/JPG/BMP等常见图像格式
        - 无损处理 - 所有操作均不影响原始图像
        - 高性能 - 基于高效图像处理算法实现
        - 自动格式处理 - 根据输出格式自动选择最佳图像类型

### 🧩 pangju-commons-imageio (JAI插件模块)

JAI插件模块，提供了各种图像类型的Javax Image IO插件

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-imageio</artifactId>
    <version>1.0.0</version>
</dependency>
````

### 📦 pangju-commons-compress (压缩模块)

压缩模块，提供了zip、7z格式压缩包文件解压缩和压缩操作

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-compress</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **CompressConstants**: 文件压缩相关常量类
    - **功能**：
        - 提供压缩文件处理中常用的常量定义
        - 包含常见压缩格式的MIME类型
        - 定义压缩文件内部路径分隔符
    - **常量分类**：
        - **MIME类型**：
            - ZIP_MIME_TYPE: zip压缩文件MIME类型 ("application/zip")
            - SEVEN_Z_MIME_TYPE: 7z压缩文件MIME类型 ("application/x-7z-compressed")
        - **路径处理**：
            - PATH_SEPARATOR: 压缩文件路径分隔符 ("/")
    - **技术特点**：
        - 不可实例化设计（protected构造函数）
        - 提供标准化的常量引用


- **SevenZUtils**: 7z压缩解压工具类
    - **功能**：
        - 基于`Apache Commons Compress`实现的7z格式压缩工具
        - 支持LZMA2等高效压缩算法
        - 提供压缩、解压缩和格式检测功能
        - 支持文件、目录和集合的压缩操作
    - **核心方法**：
        - **格式检测**：
            - is7z(): 检查文件/字节数组/输入流是否为有效的7z压缩格式
        - **解压缩**：
            - unCompress(): 解压缩7z文件到指定目录
        - **压缩**：
            - compress(): 压缩文件/目录/集合到7z文件
    - **技术特点**：
        - 高效压缩 - 支持LZMA2、BZip2等7z专有压缩算法，提供高压缩率
        - 递归处理 - 自动遍历目录结构进行压缩，保持原始文件结构
        - 格式校验 - 通过文件魔数和Tika检测确保7z文件有效性
        - 大文件支持 - 采用流式处理降低内存消耗，支持GB级文件处理
        - 线程安全 - 所有方法均为静态方法，可安全用于多线程环境


- **ZipUtils**: ZIP压缩解压工具类
    - **功能**：
        - 提供基于`Apache Commons Compress`的增强功能
        - 实现ZIP格式的高效压缩与解压缩操作
        - 支持多种输入源和输出目标
        - 自动处理目录结构的递归压缩
    - **核心方法**：
        - **格式检测**：
            - isZip(): 检查文件/字节数组/输入流是否为有效的ZIP格式
        - **解压缩**：
            - unCompress(): 解压缩ZIP文件/字节数组/输入流到指定目录
        - **压缩**：
            - compress(): 压缩文件/目录/集合到ZIP文件或输出流
    - **技术特点**：
        - 多输入源支持 - 支持文件、字节数组、输入流、ZipFile对象等多种压缩源
        - 递归压缩 - 自动处理目录结构的递归压缩，保持原始文件层级关系
        - 格式安全校验 - 通过Tika检测和魔数验证确保ZIP文件格式有效性
        - 大文件优化 - 采用缓冲通道流提升大文件处理性能
        - 资源管理 - 自动管理文件资源，防止资源泄漏

### 📝 pangju-commons-compress (POI模块)

POI模块，提供了对excel、word文件各种操作

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-poi</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **PoiConstants**: POI相关常量类
    - **功能**：
        - 提供Office文档处理中常用的MIME类型常量定义
        - 支持Word、Excel、PowerPoint等主要Office格式
        - 区分新旧版本Office文件格式
    - **常量分类**：
        - **Word文档**：
            - DOCX_MIME_TYPE: Word 2007及以上版本文档MIME类型
            - DOC_MIME_TYPE: Word 97-2003版本文档MIME类型
        - **Excel工作簿**：
            - XLSX_MIME_TYPE: Excel 2007及以上版本工作簿MIME类型
            - XLS_MIME_TYPE: Excel 97-2003版本工作簿MIME类型
        - **PowerPoint演示文稿**：
            - PPTX_MIME_TYPE: PowerPoint 2007及以上版本演示文稿MIME类型
            - PPT_MIME_TYPE: PowerPoint 97-2003版本演示文稿MIME类型
    - **技术特点**：
        - 标准化的MIME类型定义
        - 便于文件类型识别和验证


- **HWPFDocumentUtils**: DOC文档工具类
    - **功能**：
        - 提供对Microsoft Word 97-2003格式(.doc)文档的操作支持
        - 实现文档格式验证和内容读取功能
        - 支持多种输入源（文件、字节数组、输入流）
    - **核心方法**：
        - **格式验证**：
            - isDoc(): 检查文件/字节数组/输入流是否为DOC格式
        - **文档加载**：
            - getDocument(): 从文件/字节数组加载DOC文档
    - **技术特点**：
        - 基于Apache POI实现DOC文档处理
        - 使用Tika进行文件类型检测
        - 线程安全设计
        - 自动资源管理，防止资源泄漏
        - 严格的参数验证
    - **注意事项**：
        - 仅支持.doc格式文档
        - 所有方法均为静态方法


- **XWPFDocumentUtils**: DOCX文档工具类
    - **功能**：
        - 提供对Microsoft Word 2007及以上格式(.docx)文档的操作支持
        - 实现文档格式验证和内容读取功能
        - 支持多种输入源（文件、字节数组、输入流）
    - **核心方法**：
        - **格式验证**：
            - isDocx(): 检查文件/字节数组/输入流是否为DOCX格式
        - **文档加载**：
            - getDocument(): 从文件/字节数组加载DOCX文档
    - **技术特点**：
        - 基于Apache POI实现DOCX文档处理
        - 使用Tika进行文件类型检测
        - 线程安全设计
        - 自动资源管理，防止资源泄漏
        - 严格的参数验证
    - **注意事项**：
        - 仅支持.docx格式文档
        - 所有方法均为静态方法


- **XWPFTemplateUtils**: DOCX模板工具类
    - **功能**：
        - 提供对DOCX模板文档的操作支持
        - 实现模板编译、标签处理和数据模型构建
        - 基于poi-tl实现模板功能
    - **核心方法**：
        - **模板编译**：
            - compile(): 编译DOCX模板文件或字节数组
        - **标签处理**：
            - getTagNames(): 获取模板中的所有标签名称
        - **数据模型构建**：
            - getDataModel(): 构建模板数据模型，将渲染数据与标签名称映射
    - **技术特点**：
        - 支持自定义模板配置
        - 自动验证DOCX格式有效性
        - 提供标签与数据的智能映射
        - 严格的参数验证和异常处理
    - **注意事项**：
        - 仅支持.docx格式模板
        - 所有方法均为静态方法
        - 需要依赖poi-tl库


- **WorkbookUtils**: Excel工作簿工具类
    - **功能**：
        - 提供对Excel工作簿(.xls和.xlsx格式)的全面操作支持
        - 实现工作簿格式验证和内容读取
        - 支持单元格数据处理和行列操作
        - 提供样式设置功能
    - **核心方法**：
        - **格式验证**：
            - isXls()/isXlsx()/isWorkbook(): 检查文件/字节数组/输入流是否为Excel格式
        - **工作簿加载**：
            - getWorkbook(): 从文件/字节数组/输入流加载Excel工作簿
        - **内容获取**：
            - getSheets()/getRows()/getCells(): 获取工作簿中的工作表/行/单元格
            - getMergedRegionCell(): 获取合并单元格区域中的单元格
        - **数据处理**：
            - getStringCellValue()/getNumericCellValue(): 获取单元格的字符串/数值型值
            - getStringFormulaCellValue()/getNumericFormulaCellValue(): 获取公式单元格的值
        - **流式处理**：
            - sheetStream()/rowStream()/cellStream(): 创建工作表/行/单元格的流
    - **技术特点**：
        - 同时支持HSSF(.xls)和XSSF(.xlsx)格式
        - 使用Tika进行文件类型检测
        - 提供流式API支持并行处理
        - 严格的参数验证和异常处理
        - 线程安全设计
    - **注意事项**：
        - 所有方法均为静态方法

### 📄 pangju-commons-compress (PDF模块)

PDF模块，提供了对PDF文件各种操作

````xml

<dependency>
    <groupId>io.github.pangju666</groupId>
    <artifactId>pangju-commons-pdf</artifactId>
    <version>1.0.0</version>
</dependency>
````

- **PdfConstants**: PDF相关常量类
    - **功能**：
        - 提供PDF文档处理中常用的常量定义
        - 包含标准MIME类型定义
    - **常量分类**：
        - **MIME类型**：
            - PDF_MIME_TYPE: PDF文档的标准MIME类型 (application/pdf)
    - **技术特点**：
        - 标准化的MIME类型定义
        - 便于PDF文件类型识别和验证


- **Bookmark**: PDF文档书签模型类
    - **功能**：
        - 表示PDF文档中的书签节点，支持层级结构
        - 实现TreeNode接口，提供树形结构操作能力
        - 关联PDF文档页码，支持页面跳转
    - **核心属性**：
        - **id**: 书签唯一标识，自动生成UUID
        - **parentId**: 父书签ID，支持层级关系
        - **name**: 书签名称，显示在PDF文档中
        - **pageNumber**: 关联的页码(1-based)
        - **children**: 子书签集合，保持层级结构
    - **技术特点**：
        - 自动生成唯一ID，使用IdUtils工具类
        - 支持父子层级关系构建
        - 页码自动转换(0-based转为1-based)
        - 实现TreeNode接口，便于树形结构操作
    - **构造方法**：
        - 支持直接创建顶层书签
        - 支持指定父书签ID创建子书签


- **PDDocumentUtils**: PDF文档高级操作工具类
    - **功能**：
        - 基于Apache PDFBox 3.x封装的高阶PDF文档处理工具
        - 提供PDF文档格式验证、内容读取和处理功能
        - 支持PDF与图像互转、页面提取、文档合并与拆分
        - 实现文档书签管理
    - **核心特性**：
        - **智能内存管理**: 根据文件大小自动选择最优处理模式（内存/混合/临时文件）
        - **全面的I/O支持**: 支持文件、字节数组、输入流等多种数据源
        - **图像处理**: 支持PDF页面转图像，可指定缩放比例或DPI
        - **书签操作**: 提供书签树结构的读取和构建功能
    - **内存管理策略**：
        - 小文件(<50MB): 纯内存模式，性能最佳
        - 中等文件(50MB-500MB): 混合模式，平衡性能与内存
        - 大文件(>500MB): 临时文件模式，内存占用最低
    - **技术特点**：
        - 线程安全设计，所有方法均为无状态静态方法
        - 严格的参数校验，使用Validate进行前置条件检查
        - 统一的异常处理机制
        - 自动保留原始文档属性和元数据
    - **注意事项**：
        - 调用方负责关闭返回的PDDocument对象
        - 大文件处理时注意磁盘空间和内存使用

## 开源协议

本项目采用 Apache License 2.0 开源协议。