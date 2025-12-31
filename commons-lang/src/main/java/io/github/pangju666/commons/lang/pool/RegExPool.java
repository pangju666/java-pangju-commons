/*
 *   Copyright 2025 pangju666
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.github.pangju666.commons.lang.pool;

/**
 * 正则表达式大全（大部分来自社区与参考<a href="https://github.com/chinabugotech/hutool/blob/5.8.36/hutool-core/src/main/java/cn/hutool/core/lang/RegexPool.java">HutTool</a>）
 *
 * <p>说明：默认用于整串匹配（{@link String#matches}）；部分遵循 RFC/ISO/Unicode 等规范或权威实现</p>
 *
 * <p>
 * 分类与常量导航：
 * <ul>
 *   <li>数字：{@link #NUMBER}、{@link #POSITIVE_NUMBER}、{@link #FLOAT_NUMBER}、{@link #POSITIVE_FLOAT_NUMBER}</li>
 *   <li>格式：{@link #MIME_TYPE}、{@link #IDENTIFIER}、{@link #MD5}、{@link #BANK_CARD}、{@link #FILENAME}、{@link #FILENAME_WITHOUT_EXTENSION}</li>
 *   <li>UUID：{@link #UUID}、{@link #UUID_SIMPLE}、{@link #JAVA_UUID}、{@link #JAVA_UUID_SIMPLE}</li>
 *   <li>进制：{@link #HEX}</li>
 *   <li>日期时间：{@link #DATE}、{@link #TIME_12}、{@link #TIME_24}</li>
 *   <li>数字/金额：{@link #MONEY}</li>
 *   <li>车辆/组织：{@link #VEHICLE_PLATE_NUMBER}、{@link #VEHICLE_FRAME_NUMBER}、{@link #VEHICLE_DRIVING_NUMBER}、{@link #CREDIT_CODE}</li>
 *   <li>姓名/设备：{@link #CHINESE_NAME}、{@link #PHONE_IMEI}</li>
 *   <li>路径/版本/媒体：{@link #LINUX_DIR_PATH}、{@link #LINUX_FILE_PATH}、{@link #WINDOWS_DIR_PATH}、{@link #WINDOWS_FILE_PATH}、{@link #IMAGE_URL}、{@link #VIDEO_URL}</li>
 *   <li>护照：{@link #PASSPORT}</li>
 *   <li>基础字符：{@link #HEX_COLOR}、{@link #ENGLISH_CHARACTER}、{@link #ENGLISH_CHARACTERS}、{@link #CHINESE_CHARACTER}、{@link #CHINESE_CHARACTERS}、{@link #SYMBOLS_CHARACTER}、{@link #SYMBOLS_CHARACTERS}</li>
 *   <li>网络地址：{@link #IPV4}、{@link #IPV6}、{@link #MAC}、{@link #NET_MASK}、{@link #DOMAIN}</li>
 *   <li>通讯与身份：{@link #EMAIL}、{@link #MOBILE_PHONE_STRONG}、{@link #MOBILE_PHONE_WEAK}、{@link #TEL_PHONE}、{@link #ID_CARD}、{@link #ZIP_CODE}</li>
 *   <li>链接与资源：{@link #URI}、{@link #URL}、{@link #HTTP_URL}、{@link #FTP_URL}、{@link #FILE_URL}</li>
 * </ul>
 *
 * @author pangju666
 * @since 1.0.0
 */
public class RegExPool {
	/**
	 * 数字
	 *
	 * @since 1.0.0
	 */
	public static final String NUMBER = "-?\\d+";
	/**
	 * 正数
	 *
	 * @since 1.0.0
	 */
	public static final String POSITIVE_NUMBER = "\\d+";
	/**
	 * 浮点数
	 *
	 * @since 1.0.0
	 */
	public static final String FLOAT_NUMBER = "(-?[1-9]\\d*\\.\\d+|-?0\\.\\d*[1-9])";
	/**
	 * 正浮点数
	 *
	 * @since 1.0.0
	 */
	public static final String POSITIVE_FLOAT_NUMBER = "([1-9]\\d*\\.\\d+|0\\.\\d*[1-9])";
	/**
	 * 文件Mime Type（符合 RFC 2045 / RFC 6838）
	 *
	 * <p>示例：text/plain、application/json; charset=utf-8</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc2045">RFC 2045, Section 5.1</a>
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc6838">RFC 6838, Section 4.2</a>
	 * @since 1.0.0
	 */
	public static final String MIME_TYPE = "[A-Za-z0-9!#$&+-.^_`{|}~]+/[A-Za-z0-9!#$&+-.^_`{|}~]+" +
		"(?:\\s*;\\s*[A-Za-z0-9!#$&+-.^_`{|}~]+=(?:[A-Za-z0-9!#$&+-.^_`{|}~]+|\"(?:[^\"\\\\]|\\\\.)*\"))*";
	/**
	 * 标识符（首字符为字母或下划线，其后为字母/数字/下划线）
	 *
	 * <p>示例：var_1、_name</p>
	 *
	 * @since 1.0.0
	 */
	public static final String IDENTIFIER = "[a-zA-Z_][a-zA-Z0-9_]*";
	/**
	 * MD5（32位十六进制）
	 *
	 * <p>示例：d41d8cd98f00b204e9800998ecf8427e</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc1321">RFC 1321</a>
	 * @since 1.0.0
	 */
	public static final String MD5 = "[a-fA-F0-9]{32}";
	/**
	 * 银行卡号（10到30位, 覆盖对公/私账户）
	 *
	 * <p>示例：6222021234567890123</p>
	 *
	 * @see <a href="https://pay.weixin.qq.com/wiki/doc/api/xiaowei.php?chapter=22_1">微信支付</a>
	 * @since 1.0.0
	 */
	public static final String BANK_CARD = "[1-9]\\d{9,29}";
	/**
	 * 文件名称（不含保留字符）
	 *
	 * <p>示例：readme.txt、image.png</p>
	 *
	 * @since 1.0.0
	 */
	public static final String FILENAME = "[^\\\\<>:\"/|?*.]+(\\.[^\\\\<>:\"/|?*]+)?";
	/**
	 * 文件名称（无拓展名，不含保留字符）
	 *
	 * <p>示例：readme、file</p>
	 *
	 * @since 1.0.0
	 */
	public static final String FILENAME_WITHOUT_EXTENSION = "[^\\\\<>:\"/|?*.]+";
	/**
	 * UUID（兼容大小写）
	 *
	 * <p>示例：123e4567-e89b-12d3-a456-426614174000</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc4122">RFC 4122</a>
	 * @since 1.0.0
	 */
	public static final String UUID = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
	/**
	 * 不带横线的 UUID（兼容大小写）
	 *
	 * <p>示例：123e4567e89b12d3a456426614174000</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc4122">RFC 4122</a>
	 * @since 1.0.0
	 */
	public static final String UUID_SIMPLE = "[0-9a-fA-F]{8}[0-9a-fA-F]{4}[0-9a-fA-F]{4}[0-9a-fA-F]{4}[0-9a-fA-F]{12}";
	/**
	 * Java版本的UUID（仅小写）
	 *
	 * <p>示例：123e4567-e89b-12d3-a456-426614174000</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc4122">RFC 4122</a>
	 * @since 1.0.0
	 */
	public static final String JAVA_UUID = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
	/**
	 * Java版本的不带横线的UUID（仅小写）
	 *
	 * <p>示例：123e4567e89b12d3a456426614174000</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc4122">RFC 4122</a>
	 * @since 1.0.0
	 */
	public static final String JAVA_UUID_SIMPLE = "[0-9a-f]{8}[0-9a-f]{4}[0-9a-f]{4}[0-9a-f]{4}[0-9a-f]{12}";
	/**
	 * 16进制（大小写均可）
	 *
	 * <p>示例：DEADBEEF、deadbeef</p>
	 *
	 * @since 1.0.0
	 */
	public static final String HEX = "[a-fA-F0-9]+";
	/**
	 * 日期，YYYY-MM-DD格式（闰年与月份天数校验）
	 *
	 * <p>示例：2024-02-29、2023-12-31</p>
	 *
	 * @since 1.0.0
	 */
	public static final String DATE = "(?:(?!0000)[0-9]{4}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1[0-9]|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[0-9]{2}(?:0[48]|[2468][048]|[13579][26])|(?:0[48]|[2468][048]|[13579][26])00)-02-29)";
	/**
	 * 十二小时制时间（含 01–12）
	 *
	 * <p>示例：12:34:56、01:02:03</p>
	 *
	 * @since 1.0.0
	 */
	public static final String TIME_12 = "(?:1[0-2]|0?[1-9]):[0-5]\\d:[0-5]\\d";
	/**
	 * 二十四小时制时间（含 00–23）
	 *
	 * <p>示例：00:00:00、23:59:59</p>
	 *
	 * @since 1.0.0
	 */
	public static final String TIME_24 = "(?:[01]\\d|2[0-3]):[0-5]\\d:[0-5]\\d";
	/**
	 * 货币金额（支持负数；支持纯数字或千分位分隔；小数精度 1-2）
	 *
	 * <p>示例：123、1,234、1,234.56、-100.5、0.99</p>
	 *
	 * @see <a href="https://cldr.unicode.org/">Unicode CLDR</a>
	 * @since 1.0.0
	 */
	public static final String MONEY = "-?(?:\\d+(?:\\.\\d{1,2})?|\\d{1,3}(?:,\\d{3})+(?:\\.\\d{1,2})?)";
	/**
	 * 车牌号码（含新能源简化规则）
	 *
	 * <p>示例：粤B12345、京A1234学</p>
	 *
	 * @since 1.0.0
	 */
	public static final String VEHICLE_PLATE_NUMBER = "[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z][A-HJ-NP-Z0-9]{4,5}[A-HJ-NP-Z0-9挂学警港澳]|[京津沪渝冀豫云辽黑湘皖鲁新苏浙赣鄂桂甘晋蒙陕吉闽贵粤青藏川宁琼使领][A-HJ-NP-Z](?:((\\d{5}[DF])|([DF]([A-HJ-NP-Z0-9])[0-9]{4})))";
	/**
	 * 车架号（车辆识别代号 车辆识别码）
	 *
	 * <p>说明：十七位码；车辆的唯一标示</p>
	 *
	 * <p>示例：LDC613P23A1305189、LSJA24U62JG269225</p>

	 * @since 1.0.0
	 */
	public static final String VEHICLE_FRAME_NUMBER = "[A-HJ-NPR-Z0-9]{8}[0-9X][A-HJ-NPR-Z0-9]{2}\\d{6}";
	/**
	 * 驾驶证（驾驶证档案编号、行驶证编号）
	 *
	 * <p>说明：12位数字字符串（仅限：中国驾驶证档案编号）</p>
	 *
	 * <p>示例：430101758218</p>
	 *
	 * @since 1.0.0
	 */
	public static final String VEHICLE_DRIVING_NUMBER = "[0-9]{12}";
	/**
	 * 统一社会信用代码
	 *
	 * <p>示例：91350211ABCD123456</p>
	 *
	 * @since 1.0.0
	 */
	public static final String CREDIT_CODE = "[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}";
	/**
	 * 中文姓名
	 *
	 * <p>说明：2-60位；放宽汉字范围：如生僻姓名：刘欣䶮（yǎn）</p>
	 *
	 * <p>示例：张三、阿卜杜尼亚孜·毛力尼亚孜</p>
	 *
	 * <p>维吾尔族姓名说明</p>
	 * 维吾尔族姓名里面的点是 · 输入法中文状态下，键盘左上角数字1前面的那个符号；<br>
	 * 错误字符：{@code ．.。．.}<br>
	 * 正确维吾尔族姓名：
	 * <pre>
	 * 霍加阿卜杜拉·麦提喀斯木
	 * 玛合萨提别克·哈斯木别克
	 * 阿布都热依木江·艾斯卡尔
	 * 阿卜杜尼亚孜·毛力尼亚孜
	 * </pre>
	 * <pre>
	 * ----------
	 * 错误示例：孟  伟                reason: 有空格
	 * 错误示例：连逍遥0               reason: 数字
	 * 错误示例：依帕古丽-艾则孜        reason: 特殊符号
	 * 错误示例：牙力空.买提萨力        reason: 新疆人的点不对
	 * 错误示例：王建鹏2002-3-2        reason: 有数字、特殊符号
	 * 错误示例：雷金默(雷皓添）        reason: 有括号
	 * 错误示例：翟冬:亮               reason: 有特殊符号
	 * 错误示例：李                   reason: 少于2位
	 * ----------
	 * </pre>
	 *
	 * @since 1.0.0
	 */
	public static final String CHINESE_NAME = "[\u4e00-\u9fa5·]{2,30}";
	/**
	 * 手机IMEI码（支持 15 位 IMEI 与 16 位 IMEISV）
	 *
	 * <p>示例：490154203237518、4901542032375187</p>
	 *
	 * @see <a href="https://portal.3gpp.org/desktopmodules/Specifications/SpecificationDetails.aspx?specificationId=453">3GPP TS 23.003</a>
	 * @since 1.0.0
	 */
	public static final String PHONE_IMEI = "\\d{15}|\\d{16}";
	/**
	 * Linux 目录路径
	 *
	 * <p>示例：/usr/local/</p>
	 *
	 * @since 1.0.0
	 */
	public static final String LINUX_DIR_PATH = "\\/(?:[^\\/]+\\/)*";
	/**
	 * Linux 文件路径
	 *
	 * <p>示例：/usr/local/bin/bash</p>
	 *
	 * @since 1.0.0
	 */
	public static final String LINUX_FILE_PATH = "\\/(?:[^\\/]+\\/)*[^\\/]+";
	/**
	 * Windows目录路径（支持 \ 或 / 作为分隔符，排除保留字符：&lt; &gt; : " | ? * / \）
	 *
	 * <p>示例：C:\Windows\、C:/Program Files/、D:\\Data\\Project、E:/mixed/allowed</p>
	 *
	 * @see <a href="https://learn.microsoft.com/windows/win32/fileio/naming-a-file">Windows 文件命名约定</a>
	 * @since 1.0.0
	 */
	public static final String WINDOWS_DIR_PATH = "[a-zA-Z]:[/\\\\](?:[^/:*?\"<>|\\x00-\\x1F\\\\]*[/\\\\]?)*";
	/**
	 * Windows文件路径（支持 \ 或 / 作为分隔符，可选扩展名）
	 *
	 * <p>示例：C:\Windows\System32\cmd.exe、C:/Program Files/app/config、D:\\Data\\.gitignore</p>
	 *
	 * @see <a href="https://learn.microsoft.com/windows/win32/fileio/naming-a-file">Windows 文件命名约定</a>
	 * @since 1.0.0
	 */
	public static final String WINDOWS_FILE_PATH = "[a-zA-Z]:[/\\\\](?:(?:[^/:*?\"<>|\\x00-\\x1F\\\\]+[/\\\\])*)" +
		"(?!(?i:CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\.[^/:*?\"<>|\\x00-\\x1F\\\\]*)?$)[^/:*?\"<>|\\x00-\\x1F\\\\]+(?<=[^\\s.])";
	/**
	 * 图片链接（常见扩展名）
	 *
	 * <p>示例：https://example.com/a/b.jpg</p>
	 *
	 * @since 1.0.0
	 */
	public static final String IMAGE_URL = "https?:\\/\\/(.+\\/)+.+(\\.(gif|png|jpg|jpeg|webp|svg|psd|bmp|tif))";
	/**
	 * 视频链接（常见扩展名）
	 *
	 * <p>示例：https://example.com/a/b.mp4</p>
	 *
	 * @since 1.0.0
	 */
	public static final String VIDEO_URL = "https?:\\/\\/(.+\\/)+.+(\\.(swf|avi|flv|mpg|rm|mov|wav|asf|3gp|mkv|rmvb|mp4))";
	/**
	 * 护照
	 *
	 * <p>示例：E12345678、G12345678</p>
	 *
	 * @since 1.0.0
	 */
	public static final String PASSPORT = "([EeKkGgDdSsPpHh]\\d{8}|([Ee][a-fA-F]|[DdSsPp][Ee]|[Kk][Jj]|[Mm][Aa]|1[45])\\d{7})";
	/**
	 * 十六进制颜色（3/4/6/8 位，含可选 #）
	 *
	 * <p>示例：#FFFFFF、FFF、#FFFFFFFF、FFFF</p>
	 *
	 * @since 1.0.0
	 */
	public static final String HEX_COLOR = "#?([a-fA-F0-9]{6}|[a-fA-F0-9]{3}|[a-fA-F0-9]{8}|[a-fA-F0-9]{4})";
	/**
	 * 字母（英文 A–Z、a–z）
	 *
	 * <p>示例：A、b</p>
	 *
	 * @since 1.0.0
	 */
	public static final String ENGLISH_CHARACTER = "[a-zA-Z]";
	/**
	 * 多个字母（英文 A–Z、a–z）
	 *
	 * <p>示例：ab、ABC</p>
	 *
	 * @since 1.0.0
	 */
	public static final String ENGLISH_CHARACTERS = ENGLISH_CHARACTER + "+";
	/**
	 * 单个中文汉字
	 *
	 * <p>示例：中、汉</p>
	 *
	 * @see <a href="https://www.unicode.org/charts/unihan.html">Unicode Unihan</a>
	 * @since 1.0.0
	 */
	public static final String CHINESE_CHARACTER = "(?:[\u3400-\u4DB5\u4E00-\u9FEA\uFA0E\uFA0F\uFA11\uFA13\uFA14\uFA1F\uFA21\uFA23\uFA24\uFA27-\uFA29]|[\uD840-\uD868\uD86A-\uD86C\uD86F-\uD872\uD874-\uD879][\uDC00-\uDFFF]|\uD869[\uDC00-\uDED6\uDF00-\uDFFF]|\uD86D[\uDC00-\uDF34\uDF40-\uDFFF]|\uD86E[\uDC00-\uDC1D\uDC20-\uDFFF]|\uD873[\uDC00-\uDEA1\uDEB0-\uDFFF]|\uD87A[\uDC00-\uDFE0])";
	/**
	 * 符号（常见中文/英文符号集）
	 *
	 * <p>示例：!、《》</p>
	 *
	 * @since 1.0.0
	 */
	public static final String SYMBOLS_CHARACTER = "[_.!+-=—,$%^，。？、~@#￥…&*《》<>「」{}【】()（）/”“\"～！·]";
	/**
	 * 多个符号（常见中文/英文符号集）
	 *
	 * <p>示例：!!、《》</p>
	 *
	 * @since 1.0.0
	 */
	public static final String SYMBOLS_CHARACTERS = SYMBOLS_CHARACTER + "+";
	/**
	 * 多个中文汉字
	 *
	 * <p>示例：中文、测试</p>
	 *
	 * @since 1.0.0
	 */
	public static final String CHINESE_CHARACTERS = CHINESE_CHARACTER + "+";
	/**
	 * IPv4（严格 0–255 每段）
	 *
	 * <p>示例：127.0.0.1、255.255.255.255</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc791">RFC 791</a>
	 * @since 1.0.0
	 */
	public static final String IPV4 = "(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)(?:\\.(?:25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}";
	/**
	 * IPv6（全/压缩表示）
	 *
	 * <p>示例：2001:0db8:85a3:0000:0000:8a2e:0370:7334、::1</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc4291">RFC 4291</a>
	 * @since 1.0.0
	 */
	public static final String IPV6 = "(([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|(([0-9a-fA-F]{1,4}:){1,7}:)|(([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4})|(([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2})|(([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3})|(([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4})|(([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5})|([0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6}))|(:((:[0-9a-fA-F]{1,4}){1,7}|:)))";
	/**
	 * MAC地址（支持 : 或 - 分隔，分隔符需一致，大小写均可）
	 *
	 * <p>示例：a0:b1:c2:d3:e4:f5、A0-B1-C2-D3-E4-F5</p>
	 *
	 * @since 1.0.0
	 */
	public static final String MAC = "(?:[0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}|(?:[0-9A-Fa-f]{2}-){5}[0-9A-Fa-f]{2}";
	/**
	 * 子网掩码（IPv4 常见掩码）
	 *
	 * <p>示例：255.255.255.0</p>
	 *
	 * @since 1.0.0
	 */
	public static final String NET_MASK = "(254|252|248|240|224|192|128)\\.0\\.0\\.0|255\\.(254|252|248|240|224|192|128|0)\\.0\\.0|255\\.255\\.(254|252|248|240|224|192|128|0)\\.0|255\\.255\\.255\\.(255|254|252|248|240|224|192|128|0)";
	/**
	 * 域名（多级域名）
	 *
	 * <p>示例：example.com、sub.example.co.uk</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc1035">RFC 1035</a>、<a href="https://www.rfc-editor.org/rfc/rfc1123">RFC 1123</a>
	 * @since 1.0.0
	 */
	public static final String DOMAIN = "(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,}";
	/**
	 * 电子邮箱
	 *
	 * <p>示例：user@example.com、user.name+tag@example.co.uk</p>
	 *
	 * @see <a href="https://www.rfc-editor.org/rfc/rfc5322">RFC 5322</a>、<a href="http://emailregex.com/">http://emailregex.com/</a>
	 * @since 1.0.0
	 */
	public static final String EMAIL = "(?:[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+\\/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])";
	/**
	 * 电话号码（强），按中国号段收敛匹配
	 *
	 * <p>示例：13812345678、19912345678</p>
	 *
	 * @since 1.0.0
	 */
	public static final String MOBILE_PHONE_STRONG = "1(?:(?:3[\\d])|(?:4[5-79])|(?:5[0-35-9])|(?:6[5-7])|(?:7[0-8])|(?:8[\\d])|(?:9[189]))\\d{8}";
	/**
	 * 电话号码（弱），仅校验 1 开头的 11 位数字
	 *
	 * <p>示例：11123456789</p>
	 *
	 * @since 1.0.0
	 */
	public static final String MOBILE_PHONE_WEAK = "1\\d{10}";
	/**
	 * 座机号码（支持区号与分机）
	 *
	 * <p>示例：010-88888888、0512-12345678-123</p>
	 *
	 * @since 1.0.0
	 */
	public static final String TEL_PHONE = "(?:(?:\\d{3}-)?\\d{8}|(?:\\d{4}-)?\\d{7,8})(?:-\\d+)?";
	/**
	 * 身份证号码（支持 15/18 位，含闰年日期校验）
	 *
	 * <p>示例：11010519491231002X、130503670401001</p>
	 *
	 * @since 1.0.0
	 */
	public static final String ID_CARD = "\\d{6}((((((19|20)\\d{2})(0[13-9]|1[012])(0[1-9]|[12]\\d|30))|(((19|20)\\d{2})(0[13578]|1[02])31)|((19|20)\\d{2})02(0[1-9]|1\\d|2[0-8])|((((19|20)([13579][26]|[2468][048]|0[48]))|(2000))0229))\\d{3})|((((\\d{2})(0[13-9]|1[012])(0[1-9]|[12]\\d|30))|((\\d{2})(0[13578]|1[02])31)|((\\d{2})02(0[1-9]|1\\d|2[0-8]))|(([13579][26]|[2468][048]|0[048])0229))\\d{2}))(\\d|X|x)";
	/**
	 * 邮编（兼容港澳台）
	 *
	 * <p>示例：100000、999077</p>
	 *
	 * @since 1.0.0
	 */
	public static final String ZIP_CODE = "(0[1-7]|1[0-356]|2[0-7]|3[0-6]|4[0-7]|5[1-7]|6[1-7]|7[0-5]|8[013-6])\\d{4}";
	/**
	 * URI
	 *
	 * <p>示例：scheme://host/path?query#frag、mailto:user@example.com</p>
	 *
	 * <p>参考：<a href="https://www.ietf.org/rfc/rfc3986.html#appendix-B">RFC 3986</a></p>
	 *
	 * @since 1.0.0
	 */
	public static final String URI = "(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?";
	/**
	 * 链接（通用 URL 校验）
	 * <p>示例：https://example.com/path?q=1#frag</p>
	 *
	 * @see <a href="http://stackoverflow.com/questions/161738/what-is-the-best-regular-expression-to-check-if-a-string-is-a-valid-url">what-is-the-best-regular-expression-to-check-if-a-string-is-a-valid-url</a>
	 * @since 1.0.0
	 */
	public static final String URL = "(?i)^([a-z](?:[-a-z0-9\\+\\.])*)" + // protocol
		// auth
		":(?:\\/\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:])*@)?" +
		// host/ip
		"((?:\\[(?:(?:(?:[0-9a-f]{1,4}:){6}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|::(?:[0-9a-f]{1,4}:){5}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4}:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|v[0-9a-f]+[-a-z0-9\\._~!\\$&'\\(\\)\\*\\+,;=:]+)\\]|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}|(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=@])*))" +
		// port
		"(?::([0-9]*))?" +
		"(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|\\/(?:(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*)?|(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|(?!(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])))(?:\\?(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\x{E000}-\\x{F8FF}\\x{F0000}-\\x{FFFFD}|\\x{100000}-\\x{10FFFD}\\/\\?])*)?(?:\\#(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\/\\?])*)?";
	/**
	 * Http、Https链接
	 *
	 * <p>示例：https://example.com、http://example.com:8080/a/b</p>
	 *
	 * @since 1.0.0
	 */
	public static final String HTTP_URL = "http(s?)" +
		// auth
		":(?:\\/\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:])*@)?" +
		// host/ip
		"((?:\\[(?:(?:(?:[0-9a-f]{1,4}:){6}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|::(?:[0-9a-f]{1,4}:){5}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4}:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|v[0-9a-f]+[-a-z0-9\\._~!\\$&'\\(\\)\\*\\+,;=:]+)\\]|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}|(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=@])*))" +
		// port
		"(?::([0-9]*))?" +
		"(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|\\/(?:(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*)?|(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|(?!(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])))(?:\\?(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\x{E000}-\\x{F8FF}\\x{F0000}-\\x{FFFFD}|\\x{100000}-\\x{10FFFD}\\/\\?])*)?(?:\\#(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\/\\?])*)?";
	/**
	 * Ftp、Ftps链接
	 *
	 * <p>示例：ftp://example.com/resource、ftps://example.com/a/b</p>
	 *
	 * @since 1.0.0
	 */
	public static final String FTP_URL = "ftp(s?)" +
		// auth
		":(?:\\/\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:])*@)?" +
		// host/ip
		"((?:\\[(?:(?:(?:[0-9a-f]{1,4}:){6}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|::(?:[0-9a-f]{1,4}:){5}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){4}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:[0-9a-f]{1,4}:[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){3}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,2}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:){2}(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,3}[0-9a-f]{1,4})?::[0-9a-f]{1,4}:(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,4}[0-9a-f]{1,4})?::(?:[0-9a-f]{1,4}:[0-9a-f]{1,4}|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3})|(?:(?:[0-9a-f]{1,4}:){0,5}[0-9a-f]{1,4})?::[0-9a-f]{1,4}|(?:(?:[0-9a-f]{1,4}:){0,6}[0-9a-f]{1,4})?::)|v[0-9a-f]+[-a-z0-9\\._~!\\$&'\\(\\)\\*\\+,;=:]+)\\]|(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(?:\\.(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}|(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=@])*))" +
		// port
		"(?::([0-9]*))?" +
		"(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|\\/(?:(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*)?|(?:(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))+)(?:\\/(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@]))*)*|(?!(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])))(?:\\?(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\x{E000}-\\x{F8FF}\\x{F0000}-\\x{FFFFD}|\\x{100000}-\\x{10FFFD}\\/\\?])*)?(?:\\#(?:(?:%[0-9a-f][0-9a-f]|[-a-z0-9\\._~\\x{A0}-\\x{D7FF}\\x{F900}-\\x{FDCF}\\x{FDF0}-\\x{FFEF}\\x{10000}-\\x{1FFFD}\\x{20000}-\\x{2FFFD}\\x{30000}-\\x{3FFFD}\\x{40000}-\\x{4FFFD}\\x{50000}-\\x{5FFFD}\\x{60000}-\\x{6FFFD}\\x{70000}-\\x{7FFFD}\\x{80000}-\\x{8FFFD}\\x{90000}-\\x{9FFFD}\\x{A0000}-\\x{AFFFD}\\x{B0000}-\\x{BFFFD}\\x{C0000}-\\x{CFFFD}\\x{D0000}-\\x{DFFFD}\\x{E1000}-\\x{EFFFD}!\\$&'\\(\\)\\*\\+,;=:@])|[\\/\\?])*)?";
	/**
	 * 文件链接（本地文件协议，兼容 Windows 盘符和 Unix 路径）
	 *
	 * <p>支持格式：</p>
	 * <ul>
	 *   <li>{@code file:///C:/path/to/file.txt} （Windows 本地）</li>
	 *   <li>{@code file:///usr/local/bin/bash} （Unix 本地）</li>
	 *   <li>{@code file://localhost/C:/test.txt} （显式 localhost）</li>
	 *   <li>{@code file://host.example.com/path} （远程主机，语法合法但运行时可能不可用）</li>
	 * </ul>
	 *
	 * @since 1.0.0
	 */
	public static final String FILE_URL = "file://(?:localhost|(?:[a-zA-Z0-9.-]+(?:\\:[0-9]+)?)|)?/(?:[A-Za-z]:)?(?:%[0-9a-fA-F]{2}|[-._~!$&'()*+,;=:@/\\\\\\w])*";

	protected RegExPool() {
	}
}
