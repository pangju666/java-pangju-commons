/*
 *   Copyright 2026 pangju666
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

package io.github.pangju666.commons.media.lang;

import java.util.Set;

/**
 * 媒体相关常量类，定义FFmpeg支持的音频、视频、字幕文件格式集合
 * <p>
 * 该类包含音频、视频、字幕三类媒体的完整格式支持，涵盖读取和写入两个维度，
 * 同时提供两种格式集合：FFmpeg格式标识和文件扩展名后缀。
 * <p>
 * <h3>FFmpeg格式标识集合</h3>
 * <ul>
 *   <li><b>音频格式</b>：{@link #SUPPORTED_READ_AUDIO_FORMATS}、{@link #SUPPORTED_WRITE_AUDIO_FORMATS}</li>
 *   <li><b>视频格式</b>：{@link #SUPPORTED_READ_VIDEO_FORMATS}、{@link #SUPPORTED_WRITE_VIDEO_FORMATS}</li>
 *   <li><b>字幕格式</b>：{@link #SUPPORTED_READ_SUBTITLE_FORMATS}、{@link #SUPPORTED_WRITE_SUBTITLE_FORMATS}</li>
 * </ul>
 * <p>
 * <h3>文件扩展名后缀集合</h3>
 * <ul>
 *   <li><b>音频后缀</b>：{@link #SUPPORTED_READ_AUDIO_FILE_FORMATS}、{@link #SUPPORTED_WRITE_AUDIO_FILE_FORMATS}</li>
 *   <li><b>视频后缀</b>：{@link #SUPPORTED_READ_VIDEO_FILE_FORMATS}、{@link #SUPPORTED_WRITE_VIDEO_FILE_FORMATS}</li>
 *   <li><b>字幕后缀</b>：{@link #SUPPORTED_READ_SUBTITLE_FILE_FORMATS}、{@link #SUPPORTED_WRITE_SUBTITLE_FILE_FORMATS}</li>
 * </ul>
 * <p>
 * <h3>格式命名规范</h3>
 * <ul>
 *   <li>FFmpeg格式标识采用原生短名称（如"mp3"、"mp4"、"srt"）</li>
 *   <li>文件扩展名后缀用于匹配文件名，均为小写</li>
 *   <li>裸码流格式区分字节序（大端be/小端le）、位深、编码类型</li>
 *   <li>游戏/专业格式针对特定场景设计，名称与FFmpeg保持一致</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 检查是否支持读取某FFmpeg音频格式
 * if (MediaConstants.SUPPORTED_READ_AUDIO_FORMATS.contains("mp3")) {
 *     // 可以读取
 * }
 *
 * // 检查文件扩展名是否为支持的视频格式
 * if (MediaConstants.SUPPORTED_READ_VIDEO_FILE_FORMATS.contains("mp4")) {
 *     // 可以读取
 * }
 *
 * // 检查是否支持写入某字幕格式
 * if (MediaConstants.SUPPORTED_WRITE_SUBTITLE_FILE_FORMATS.contains("srt")) {
 *     // 可以写入
 * }
 * }</pre>
 *
 * @author pangju666
 * @see <a href="https://ffmpeg.org/ffmpeg-formats.html">FFmpeg Formats Official Doc</a>
 * @since 1.1.0
 */
public class MediaConstants {
	/**
	 * FFmpeg支持的可读取音频文件格式集合
	 * <p>
	 * 包含以下类型的音频格式：
	 * <ul>
	 *   <li>通用音频格式（MP3、FLAC、WAV、AAC等）</li>
	 *   <li>专业音频格式（CAF、IRCAM、SoX等）</li>
	 *   <li>游戏专属音频格式（ADX、HCA、FSB等）</li>
	 *   <li>语音编码格式（G.729、AMR、codec2等）</li>
	 *   <li>裸PCM格式（不同位深、字节序、浮点/整型）</li>
	 *   <li>老式/小众音频格式（AU、VOC、SHN等）</li>
	 * </ul>
	 * 格式标识均为小写，与FFmpeg命令行使用的格式名完全一致
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_READ_AUDIO_FORMATS = Set.of(
		"aa",         // Audible AA 有声书加密音频
		"aac",        // ADTS AAC 裸音频码流
		"aax",        // Audible AAX 加密有声书音频
		"ace",        // tri-Ace 游戏音频容器
		"acm",        // Interplay ACM 老式游戏音频
		"act",        // ACT 录音笔语音格式
		"adf",        // Artworx Data Format 游戏音频
		"adp",        // ADP 低码率语音格式
		"ads",        // Sony PS2 ADS 游戏音频流
		"adx",        // CRI ADX 主流游戏有损音频
		"aea",        // MD STUDIO 专业录音棚音频
		"afc",        // AFC 通用音频容器
		"aix",        // CRI AIX 高压缩游戏音频
		"alp",        // 乐高 ALP 游戏背景音乐
		"amrnb",      // AMR-NB 手机窄带语音裸流
		"amrwb",      // AMR-WB 手机宽带语音裸流
		"apac",       // APAC 专用裸音频码流
		"apc",        // CRYO APC 复古游戏音频
		"ape",        // Monkey's Audio APE 无损压缩音频
		"apm",        // 育碧 APM 雷曼系列游戏音频
		"aptx",       // aptX 蓝牙基础有损音频编码
		"aptx_hd",    // aptX HD 蓝牙高清无损音频编码
		"apv",        // APV 裸音频码流格式
		"ast",        // AST 游戏音频流容器
		"au",         // Sun AU Unix 老式未压缩PCM音频
		"bfstm",      // BFSTM 任天堂二进制音频流
		"binka",      // Bink Audio 跨平台游戏音轨
		"boa",        // 黑色行动 BOA 游戏音频
		"bonk",       // Bonk 轻量化无损音频格式
		"brstm",      // BRSTM Wii 游戏音频流
		"c93",        // Interplay C93 复古游戏语音
		"caf",        // Apple CAF 苹果核心专业音频容器
		"codec2raw",  // codec2 低带宽语音原始裸流
		"daud",       // D-Cinema 数字影院专业音频
		"dfpwm",      // DFPWM 低比特率简易音频
		"dss",        // DSS 数码录音笔语音格式
		"dts",        // DTS 影院环绕声裸码流
		"dtshd",      // DTS-HD 高清无损环绕声裸流
		"eac3",       // E-AC3 增强型杜比全景声裸流
		"epaf",       // Ensoniq 专业录音工作站音频
		"fsb",        // FMOD FSB 游戏音效/音乐打包格式
		"fwse",       // Capcom MT Framework 游戏音频
		"g728",       // G.728 窄带电话语音编码
		"g729",       // G.729 网络电话语音裸流
		"genh",       // GENhdr 科研级专业多通道音频
		"hca",        // CRI HCA 日系游戏高压缩音频
		"hcom",       // Mac HCOM 老式苹果压缩语音
		"iamf",       // IAMF 沉浸式空间音频裸流
		"ilbc",       // iLBC 网络通话低延迟语音
		"ircam",      // IRCAM 声学研究专用音频格式
		"kvag",       // Sony PS VAG 主机游戏音频
		"laf",        // LAF Limitless 极限音频格式
		"lc3",        // LC3 蓝牙低功耗语音编码
		"loas",       // LOAS AAC 同步传输裸音频流
		"mlp",        // MLP 杜比无损多声道音频
		"mmf",        // MMF 雅马哈老式手机铃声格式
		"mods",       // MobiClip MODS 复古Tracker音轨
		"moflex",     // MobiClip MOFLEX 移动端音轨格式
		"mpc",        // Musepack 高音质有损音频
		"mpc8",       // Musepack SV8 新版高效有损音频
		"mulaw",      // mu-law 北美电话8位未压缩PCM
		"musx",       // Eurocom MUSX 主机游戏音频
		"nistsphere", // NIST 语音识别标准测试音频
		"nsp",        // CSL实验室NSP 语音采集格式
		"oma",        // Sony OpenMG 索尼加密数字音频
		"osq",        // OSQ 低码率语音存储格式
		"pvf",        // PVF 便携设备原始语音格式
		"qcp",        // QCP 高通功能机语音录音
		"qoa",        // QOA 高速轻量无损音频
		"rka",        // RK Audio 复古游戏音频
		"rso",        // 乐高RSO 机器人设备音频
		"sdx",        // Sample Dump eXchange 采样交换音频
		"sds",        // MIDI Sample Dump 硬件采样音频
		"shn",        // Shorten 早期无损压缩音频
		"siff",       // SIFF 复古PC游戏音频容器
		"sln",        // Asterisk SLN 电话裸PCM音频
		"sol",        // Sierra SOL 冒险游戏音频
		"tak",        // TAK 高压缩率无损音频格式
		"tta",        // TTA True Audio 无损多通道音频
		"voc",        // Creative VOC 创新声卡老式波形音频
		"w64",        // Wave64 超大采样位宽WAV扩展格式
		"wav",        // WAV 标准PCM无损波形音频容器
		"wavarc",     // WAV归档压缩无损音频格式
		"wsaud",      // Westwood 西木游戏音频格式
		"wsd",        // WSD 单比特无损音频格式
		"wv",         // WavPack 混合模式无损音频
		"wve",        // Psion 3 老式掌上设备语音
		"xa",         // Maxis XA 模拟人生系列游戏音频
		"xmd",        // Konami XMD 科乐美主机游戏音频
		"xvag",       // PS3 XVAG 索尼主机打包音频
		"xwma",       // XWMA 微软无损WMA音频编码
		"ac3",        // AC3 标准杜比5.1环绕声裸流
		"ac4",        // AC4 新一代杜比沉浸式音频裸流
		"aiff",       // AIFF 苹果标准未压缩波形音频
		"alaw",       // A-law 欧洲电话8位未压缩PCM
		"amr",        // 3GPP AMR 通用手机语音封装
		"codec2",     // codec2 开源低带宽语音封装格式
		"f32be",      // PCM 32位浮点 大端序原始音频
		"f32le",      // PCM 32位浮点 小端序原始音频
		"f64be",      // PCM 64位浮点 大端序高精度音频
		"f64le",      // PCM 64位浮点 小端序高精度音频
		"g722",       // G.722 宽带高清电话语音编码
		"g723_1",     // G.723.1 低速率网络语音编码
		"g726",       // G.726 ADPCM 大端序自适应语音
		"g726le",     // G.726 ADPCM 小端序自适应语音
		"gsm",        // GSM 移动电话标准有损语音
		"spdif",      // SPDIF 光纤/同轴数字音频封装
		"sox",        // SoX Sound eXchange 专业音频工具原生格式
		"s16be",      // PCM 16位有符号 大端序原始音频
		"s16le",      // PCM 16位有符号 小端序原始音频
		"s24be",      // PCM 24位有符号 大端专业录音音频
		"s24le",      // PCM 24位有符号 小端专业录音音频
		"s32be",      // PCM 32位有符号 大端高精度音频
		"s32le",      // PCM 32位有符号 小端高精度音频
		"s8",         // PCM 8位有符号 原始音频采样
		"u8",         // PCM 8位无符号 原始音频采样
		"u16be",      // PCM 16位无符号 大端序原始音频
		"u16le",      // PCM 16位无符号 小端序原始音频
		"u24be",      // PCM 24位无符号 大端序原始音频
		"u24le",      // PCM 24位无符号 小端序原始音频
		"u32be",      // PCM 32位无符号 大端序原始音频
		"u32le"       // PCM 32位无符号 小端序原始音频
	);

	/**
	 * FFmpeg支持的可写入音频文件格式集合
	 * <p>
	 * 相较于读取格式，写入格式做了以下筛选：
	 * <ul>
	 *   <li>移除仅支持解码的老旧/小众格式</li>
	 *   <li>保留主流可编码格式（MP3、FLAC、AAC、WAV等）</li>
	 *   <li>保留专业广电/游戏音频格式</li>
	 *   <li>保留全类型裸PCM格式（支持高精度录音/混音）</li>
	 *   <li>新增Web端音频格式（Opus、WebM音频等）</li>
	 * </ul>
	 * 可直接用于FFmpeg编码输出的格式参数（-f 参数）
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_WRITE_AUDIO_FORMATS = Set.of(
		"ac3",        // AC3 标准杜比5.1环绕声裸音频码流
		"ac4",        // AC4 新一代杜比沉浸式全景声裸流
		"adts",       // ADTS 封装格式的AAC音频流
		"adx",        // CRI ADX 游戏通用有损音频编码
		"aea",        // MD STUDIO 专业录音棚音频格式
		"aiff",       // AIFF 苹果无损未压缩波形音频容器
		"alaw",       // A-law 欧洲电信8位PCM语音编码
		"amr",        // 3GPP AMR 手机通用语音封装格式
		"aptx",       // aptX 蓝牙基础有损音频编码
		"aptx_hd",    // aptX HD 蓝牙高清无损音频编码
		"apv",        // APV 专用裸音频码流格式
		"ast",        // AST 游戏音频流封装容器
		"au",         // Sun AU Unix 老式未压缩PCM音频
		"caf",        // Apple CAF 苹果专业核心音频容器
		"codec2",     // codec2 开源低带宽语音封装格式
		"codec2raw",  // codec2 低带宽语音原始裸码流
		"daud",       // D-Cinema 数字影院专业标准音频
		"dfpwm",      // DFPWM 低比特率轻量化音频编码
		"dts",        // DTS 影院多声道环绕声裸码流
		"eac3",       // E-AC3 增强型杜比全景声裸音频流
		"f32be",      // PCM 32位浮点 大端序原始音频采样
		"f32le",      // PCM 32位浮点 小端序原始音频采样
		"f64be",      // PCM 64位浮点 大端序高精度音频
		"f64le",      // PCM 64位浮点 小端序高精度音频
		"flac",       // FLAC 通用开源无损压缩音频
		"g722",       // G.722 宽带高清电话语音编码
		"g723_1",     // G.723.1 低码率网络通话语音编码
		"g726",       // G.726 ADPCM 大端序自适应语音编码
		"g726le",     // G.726 ADPCM 小端序自适应语音编码
		"gsm",        // GSM 移动电话标准有损语音编码
		"iamf",       // IAMF 沉浸式空间音频标准裸流
		"ilbc",       // iLBC 低延迟网络通话语音编码
		"ircam",      // IRCAM 声学实验室专业音频格式
		"lc3",        // LC3 蓝牙低功耗高清语音编码
		"mlp",        // MLP 杜比无损多声道音频编码
		"mulaw",      // mu-law 北美电信8位PCM语音编码
		"qoa",        // QOA 高速轻量级无损音频格式
		"shn",        // Shorten 早期无损压缩音频格式
		"sox",        // SoX Sound eXchange 专业音频工具原生格式
		"spdif",      // SPDIF 光纤/同轴数字音频传输封装
		"tak",        // TAK 高压缩率无损音频格式
		"tta",        // TTA True Audio 无损多通道音频编码
		"u8",         // PCM 8位无符号原始音频采样
		"u16be",      // PCM 16位无符号 大端序原始音频
		"u16le",      // PCM 16位无符号 小端序原始音频
		"u24be",      // PCM 24位无符号 大端序专业音频
		"u24le",      // PCM 24位无符号 小端序专业音频
		"u32be",      // PCM 32位无符号 大端序高精度音频
		"u32le",      // PCM 32位无符号 小端序高精度音频
		"voc",        // Creative VOC 创新声卡老式波形音频
		"w64",        // Wave64 超大容量扩展WAV音频格式
		"wav",        // WAV 标准无损PCM波形音频容器
		"wavarc",     // WAV归档压缩型无损音频格式
		"wv",         // WavPack 支持纯无损/混合有损音频格式
		"xwma",       // XWMA 微软无损WMA音频编码
		"s16be",      // PCM 16位有符号 大端序原始音频
		"s16le",      // PCM 16位有符号 小端序原始音频
		"s24be",      // PCM 24位有符号 大端序录音级音频
		"s24le",      // PCM 24位有符号 小端序录音级音频
		"s32be",      // PCM 32位有符号 大端序高精度音频
		"s32le",      // PCM 32位有符号 小端序高精度音频
		"s8",         // PCM 8位有符号原始音频采样
		"opus",       // Opus 开源高音质低延迟通用有损音频
		"oga",        // Oga Ogg容器纯音频后缀封装
		"ogg",        // Ogg 通用开源多媒体音视频容器
		"spx",        // Speex Ogg封装低码率网络语音
		"truehd"      // TrueHD 杜比无损高清影院多声道音频
	);

	/**
	 * FFmpeg支持的可读取视频文件格式集合
	 * <p>
	 * 包含以下类型的视频格式：
	 * <ul>
	 *   <li>通用容器格式（MP4、MKV、AVI、MOV、FLV等）</li>
	 *   <li>裸视频码流（H.264、H.265、AV1、AVS系列等）</li>
	 *   <li>游戏专属视频格式（Bink、SMK、THP、XMV等）</li>
	 *   <li>广电/专业格式（MXF、GXF、DNxHD、R3D等）</li>
	 *   <li>监控/安防格式（DAV、IFV、IV8等）</li>
	 *   <li>老式/小众格式（3DO STR、4XM、FLI/FLC等）</li>
	 *   <li>原始视频格式（rawvideo、YUV4MPEG等）</li>
	 * </ul>
	 * 格式标识与FFmpeg探测格式的名称完全一致，可用于格式识别场景
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_READ_VIDEO_FORMATS = Set.of(
		"3dostr",        // 3DO STR 老式游戏机专属视频格式
		"4xm",           // 4X Technologies 游戏视频封装格式
		"a64",           // Commodore 64 复古电脑简易视频
		"amv",           // AMV 便携MP4播放器专用低清视频
		"anm",           // Deluxe Paint 复古点阵动画格式
		"argo_asf",      // Argonaut 游戏自研ASF视频容器
		"argo_cvg",      // Argonaut 游戏CVG动画视频格式
		"asf_o",         // 旧版标准ASF流媒体视频容器
		"bethsoftvid",   // 贝塞斯达游戏专用VID视频格式
		"bink",          // RAD Game Tools Bink 跨平台游戏视频
		"bmv",           // Discworld II BMV 老式PC游戏动画
		"cdxl",          // Commodore CDXL CD-ROM多媒体视频
		"cine",          // Phantom 高速摄像机原始视频素材
		"dhav",          // DAV 监控录像机专属视频格式
		"dirac",         // Dirac 开源无损/近无损裸视频码流
		"dnxhd",         // DNxHD SMPTE广电专业编辑裸视频流
		"dv",            // DV 磁带摄像机标准数字视频
		"dxa",           // DXA 复古游戏逐帧动画格式
		"ea",            // EA 艺电游戏自研视频封装
		"film_cpk",      // Sega FILM / CPK 世嘉主机游戏视频
		"flic",          // FLI/FLC 经典2D逐帧动画格式
		"gdv",           // Gremlin 复古电脑游戏视频格式
		"idcin",         // id Software 老游戏电影动画格式
		"iff",           // IFF 通用交换文件动画图像格式
		"ifv",           // IFV 民用监控DVR录像视频
		"ingenient",     // Ingenient MJPEG 安防MJPEG视频流
		"ipmovie",       // Interplay MVE 90年代PC游戏影片
		"ipu",           // IPU 专用硬件裸视频码流
		"iv8",           // IndigoVision 工业监控摄像机视频
		"ivf",           // IVF VP8/VP9 裸视频码流封装容器
		"ivr",           // IVR 互联网录制流媒体视频
		"kux",           // 优酷KUX 平台加密专属视频格式
		"lxf",           // LXF 广电专业磁带素材流格式
		"mlv",           // Magic Lantern MLV 单反魔灯RAW视频
		"mjpeg_2000",    // JPEG2000 专业影视无损/有损裸视频
		"nsv",           // Nullsoft NSV 早期Winamp流媒体视频
		"nuv",           // NuppelVideo Linux桌面老式录屏视频
		"paf",           // PAF 复古游戏打包动画文件
		"pmp",           // PSP PMP 索尼掌机第三方视频格式
		"r3d",           // REDCODE R3D 电影摄影机RAW素材
		"rawvideo",      // rawvideo 未压缩YUV/RGB原始裸视频流
		"roq",           // id RoQ idTech引擎游戏动画视频
		"rpl",           // RPL / ARMovie 老式DOS游戏影片
		"smk",           // Smacker 经典复古游戏高压缩动画
		"smush",         // LucasArts SMUSH 卢卡斯冒险游戏动画
		"thp",           // THP 任天堂主机标准游戏视频
		"tiertexseq",    // Tiertex SEQ 8位机复古序列动画
		"tmv",           // 8088flex TMV 极早期PC低分辨率视频
		"usm",           // CRI USM 日系3A游戏通用视频格式
		"vividas",       // Vividas VIV 老式互联网高清视频
		"vivo",          // Vivo 早期功能机自带录像视频格式
		"vmd",           // Sierra VMD 雪乐山冒险游戏动画
		"wtv",           // WTV Windows电视节目录制封装
		"xmv",           // XMV 微软Xbox主机游戏视频格式
		"yop",           // Psygnosis YOP 复古主机动画格式
		"mpegtsraw",     // 原始未处理MPEG-TS传输裸流
		"mpegvideo",     // MPEG1/MPEG2 标准裸视频码流
		"vob",           // DVD VOB 光盘标准视频封装文件
		"av1",           // AV1 Annex B 新一代高效裸视频码流
		"avs2",          // AVS2 国产第二代数字视频裸码流
		"avs3",          // AVS3 国产第三代高清视频标准裸码流
		"cavsvideo",     // AVS1 初代国产音视频标准裸流
		"h261",          // H.261 早期视频会议裸码流
		"h263",          // H.263 早期手机/视频通话裸视频流
		"h264",          // H.264/AVC 通用主流高清裸视频码流
		"hevc",          // HEVC/H.265 高压缩比4K裸视频码流
		"m4v",           // M4V 苹果专用MPEG4裸视频码流
		"mjpeg",         // MJPEG 单帧JPEG串联裸视频流
		"mpeg",          // MPEG-PS MPEG1/2节目流(VCD/SVCD)
		"mpegts",        // MPEG-TS 直播/广播电视传输流容器
		"vc1",           // VC1 WMV衍生高清裸视频码流
		"vc1test",       // VC1 标准测试裸视频码流
		"vvc",           // VVC/H.266 下一代超高清裸视频码流
		"obu",           // AV1 OBU 轻量化AV1裸码流分片格式
		"matroska",      // MKV Matroska 通用多媒体开源容器
		"mov",           // MOV QuickTime 苹果专业媒体容器
		"mp4",           // MP4 MPEG-4 Part14 通用网页/设备容器
		"avi",           // AVI Windows经典音视频交错容器
		"flv",           // FLV Flash 早期网页流媒体视频容器
		"live_flv",      // live_flv FLV实时直播流媒体封装
		"webm",          // WebM 网页开源VP9/AV1专用容器
		"nut",           // NUT FFmpeg开源轻量多媒体容器
		"filmstrip",     // Adobe Filmstrip 胶片序列无损帧存储
		"yuv4mpegpipe"   // YUV4MPEG 原始YUV未压缩视频管道序列
	);

	/**
	 * FFmpeg支持的可写入视频文件格式集合
	 * <p>
	 * 相较于读取格式，写入格式做了以下优化：
	 * <ul>
	 *   <li>移除仅支持解码的游戏/老旧格式</li>
	 *   <li>保留主流通用容器（MP4、MKV、MOV、AVI等）</li>
	 *   <li>保留新一代视频编码裸流（H.265、AV1、VVC等）</li>
	 *   <li>新增Web端格式（WebM）</li>
	 *   <li>保留广电专业格式（MXF系列、GXF、DNxHD等）</li>
	 *   <li>支持光盘封装格式（DVD、VCD、SVCD）</li>
	 * </ul>
	 * 可直接作为FFmpeg编码输出的格式参数（-f 参数），适配不同应用场景
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_WRITE_VIDEO_FORMATS = Set.of(
		"3g2",           // 3GPP2 CDMA手机专用视频封装容器
		"3gp",           // 3GPP GSM手机通用轻量化视频容器
		"amv",           // AMV 低价随身播放器专用低分辨率视频格式
		"asf",           // ASF Windows早年流媒体多媒体容器
		"avi",           // AVI Windows经典音视频交错存储容器
		"av1",           // AV1 Annex B 新一代高效开源裸视频码流
		"avs2",          // AVS2 国产第二代高清视频编码裸流
		"avs3",          // AVS3 国产第三代超高清视频编码裸流
		"dirac",         // Dirac 开源无损/近无损专业裸视频码流
		"dnxhd",         // DNxHD SMPTE广电剪辑专用无损/近无损裸视频
		"dv",            // DV 磁带数码摄像机标准视频格式
		"filmstrip",     // Adobe Filmstrip 无损逐帧胶片序列存储格式
		"flv",           // FLV Flash网页直播/点播流媒体容器
		"gxf",           // GXF 广电专业素材交换存储容器
		"h261",          // H.261 早期视频会议低码率裸视频码流
		"h263",          // H.263 老式手机视频通话裸视频码流
		"h264",          // H.264/AVC 全平台通用高清裸视频码流
		"hevc",          // HEVC/H.265 高压缩比4K/8K裸视频码流
		"ivf",           // IVF VP8/VP9/AV1裸码流简易封装容器
		"lxf",           // LXF 广电专业磁带播出素材流格式
		"m4v",           // M4V 苹果iTunes专用MPEG-4裸视频码流
		"matroska",      // Matroska(MKV) 开源万能多媒体封装容器
		"mjpeg",         // MJPEG 逐帧JPEG串联无损/有损裸视频流
		"mjpeg_2000",    // JPEG2000 影视后期无损专业裸视频编码
		"mov",           // MOV QuickTime 苹果专业影视编辑容器
		"mp4",           // MP4 MPEG-4 Part14 通用终端/网页标准容器
		"mpeg",          // MPEG-PS MPEG1/2节目流（VCD/SVCD标准）
		"mpegts",        // MPEG-TS 直播、IPTV、广电传输流容器
		"nut",           // NUT FFmpeg自研开源轻量化多媒体容器
		"obu",           // AV1 OBU 无封装轻量化AV1裸码流分片格式
		"rawvideo",      // rawvideo 未压缩原始YUV/RGB像素裸视频流
		"vc1",           // VC1 WMV衍生高清影视裸视频编码码流
		"vvc",           // VVC/H.266 下一代超高清高效视频裸码流
		"webm",          // WebM 网页开源免专利VP9/AV1专用容器
		"yuv4mpegpipe"   // YUV4MPEG 原始未压缩YUV视频管道序列格式
	);

	/**
	 * FFmpeg支持的可读取字幕文件格式集合
	 * <p>
	 * 包含以下类型的字幕格式：
	 * <ul>
	 *   <li>通用文本字幕（SRT、SubViewer、MicroDVD等）</li>
	 *   <li>图形字幕（VobSub、PGS/SUP、DVB字幕等）</li>
	 *   <li>专业字幕（SCC、STL、DVB Teletext等）</li>
	 *   <li>歌词字幕（LRC）</li>
	 *   <li>老式/小众字幕（JacoSub、MPL2、RealText等）</li>
	 * </ul>
	 * 支持从视频文件中提取内嵌字幕，或读取独立字幕文件
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_READ_SUBTITLE_FORMATS = Set.of(
		"aqtitle",       // AQTitle 影视剪辑专用字幕格式
		"dvbsub",        // DVB Subtitle 数字电视内嵌图形字幕流
		"jacosub",       // JacoSub 老式文本字幕格式
		"lrc",           // LRC 音乐播放器歌词同步字幕
		"mcc",           // MCC MacCaption 广电专业闭路字幕格式
		"microdvd",      // MicroDVD 基于帧号索引的简易文本字幕
		"mpl2",          // MPL2 MPlayer二代文本字幕格式
		"mpsub",         // MPSub MPlayer老式时间轴字幕
		"pjs",           // PJS Phoenix Japanimation Society 动画字幕
		"realtext",      // RealText RealMedia RM配套字幕格式
		"sami",          // SAMI Windows媒体同步多语言字幕
		"scc",           // SCC Scenarist 广电标准闭路字幕文件
		"stl",           // STL Spruce 广电专业播出字幕文件
		"subviewer",     // SubViewer v2 通用影音文本字幕
		"subviewer1",    // SubViewer v1 初代简易字幕格式
		"sup",           // SUP HDMV 蓝光图形位图字幕
		"tedcaptions",   // TED Talks 演讲专用字幕格式
		"vobsub",        // VobSub DVD光盘图形字幕（idx+sup）
		"vplayer",       // VPlayer 移动端影音播放器字幕格式
		"ass",           // ASS SSA高级特效字幕，支持样式/动画
		"srt",           // SRT SubRip 全网最通用简易文本字幕
		"ttml",          // TTML W3C标准化XML专业字幕规范
		"webvtt"         // WebVTT 网页浏览器原生支持字幕格式
	);

	/**
	 * FFmpeg支持的可写入字幕文件格式集合
	 * <p>
	 * 相较于读取格式，写入格式做了以下筛选：
	 * <ul>
	 *   <li>移除仅支持解析的老旧文本格式</li>
	 *   <li>保留通用文本字幕（SRT、WebVTT、MicroDVD等）</li>
	 *   <li>新增特效字幕（ASS/SSA）</li>
	 *   <li>保留广电专业字幕（SCC、MCC、TTML）</li>
	 *   <li>保留图形字幕（SUP/PGS）</li>
	 * </ul>
	 * 可用于将内嵌字幕提取为独立文件，或转换字幕格式
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_WRITE_SUBTITLE_FORMATS = Set.of(
		"ass",        // ASS 高级字幕格式，支持字体、颜色、动画、特效样式
		"lrc",        // LRC 歌词同步字幕，多用于音乐播放器
		"mcc",        // MCC MacCaption 广电专业闭路字幕制作格式
		"microdvd",   // MicroDVD 基于视频帧序号定位的简易文本字幕
		"rcwt",       // RCWT Raw Captions With Time 原始带时间码字幕流
		"scc",        // SCC Scenarist 广播电视标准闭路字幕文件
		"srt",        // SRT SubRip 通用极简文本字幕，兼容性最强
		"ttml",       // TTML W3C标准化XML专业字幕，广电/流媒体通用
		"webvtt"      // WebVTT 网页原生字幕，浏览器、短视频平台标准
	);

	/**
	 * 支持的可读取音频文件后缀集合
	 * <p>
	 * 包含通用音频格式、专业音频格式、游戏音频格式、语音编码格式等，用于文件扩展名检查和识别。
	 * <p>
	 * 包含的格式类型：
	 * <ul>
	 *   <li>通用音频格式（mp3, flac, wav, aac, m4a, wma等）</li>
	 *   <li>专业音频格式（caf, aiff, sox等）</li>
	 *   <li>游戏音频格式（adx, hca, fsb, bfstm, brstm等）</li>
	 *   <li>语音编码格式（amr, g729, ilbc等）</li>
	 *   <li>老式/小众音频格式（au, voc, shn等）</li>
	 * </ul>
	 * <p>
	 * 注意：后缀名均为小写，用于文件扩展名匹配，与FFmpeg格式标识可能有细微差异
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_READ_AUDIO_FILE_FORMATS = Set.of(
		"aa", "aac", "aax", "ace", "acm", "act", "adf", "adp", "ads", "adx", "aea", "afc",
		"aix", "alp", "amr", "apac", "apc", "ape", "apm", "aptx", "ast", "au", "bfstm",
		"binka", "boa", "bonk", "brstm", "c93", "caf", "daud", "dfpwm", "dss", "dts",
		"dtshd", "eac3", "epaf", "fsb", "fwse", "g728", "g729", "genh", "hca", "hcom",
		"iamf", "ilbc", "ircam", "kvag", "laf", "lc3", "loas", "mlp", "mmf", "mods",
		"moflex", "mpc", "mpc8", "musx", "nistsphere", "nsp", "oma", "osq", "pvf",
		"qcp", "qoa", "rka", "rso", "sdx", "sds", "shn", "siff", "sln", "sol", "tak",
		"tta", "voc", "w64", "wav", "wavarc", "wsaud", "wsd", "wv", "wve", "xa",
		"xmd", "xvag", "xwma", "ac3", "ac4", "aiff", "alaw", "gsm", "spdif", "sox",
		"mp3", "m4a", "wma"
	);

	/**
	 * 支持的可写入音频文件后缀集合
	 * <p>
	 * 筛选了主流可编码的音频格式和专业广电/游戏音频格式，用于输出文件扩展名选择。
	 * <p>
	 * 包含的格式类型：
	 * <ul>
	 *   <li>通用音频格式（mp3, flac, aac, wav, m4a, wma等）</li>
	 *   <li>专业音频格式（caf, aiff, sox等）</li>
	 *   <li>Web端音频格式（opus, ogg, oga, spx等）</li>
	 *   <li>无损高清格式（truehd, mlp等）</li>
	 * </ul>
	 * <p>
	 * 注意：后缀名均为小写，用于文件扩展名匹配
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_WRITE_AUDIO_FILE_FORMATS = Set.of(
		"ac3", "ac4", "aac", "adx", "aea", "aiff", "alaw", "amr", "aptx", "ast", "au",
		"caf", "daud", "dfpwm", "dts", "eac3", "flac", "g722", "g723", "g726", "gsm",
		"iamf", "ilbc", "ircam", "lc3", "mlp", "qoa", "shn", "sox", "spdif", "tak",
		"tta", "voc", "w64", "wav", "wavarc", "wv", "xwma", "opus", "oga", "ogg",
		"spx", "truehd", "mp3", "m4a", "wma"
	);

	/**
	 * 支持的可读取视频文件后缀集合
	 * <p>
	 * 包含通用容器格式、游戏专属格式、广电专业格式、监控安防格式等，用于文件扩展名检查和识别。
	 * <p>
	 * 包含的格式类型：
	 * <ul>
	 *   <li>通用容器格式（mp4, mkv, avi, mov, flv, webm等）</li>
	 *   <li>游戏专属格式（bink, smk, thp, xmv, usm等）</li>
	 *   <li>广电专业格式（ts, m2ts, lxf, gxf等）</li>
	 *   <li>监控/安防格式（dav, ifv, iv8等）</li>
	 *   <li>老式/小众格式（3do, 4xm, flc, fli等）</li>
	 * </ul>
	 * <p>
	 * 注意：后缀名均为小写，用于文件扩展名匹配
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_READ_VIDEO_FILE_FORMATS = Set.of(
		"3do", "4xm", "a64", "amv", "anm", "asf", "avi", "bethsoftvid", "bink", "bmv",
		"cdxl", "cine", "dav", "dv", "dxa", "ea", "flc", "fli", "gdv", "idcin", "iff",
		"ifv", "iv8", "ivf", "ivr", "kux", "lxf", "mlv", "mov", "mp4", "m4v", "mpg",
		"mpeg", "nsv", "nuv", "paf", "pmp", "r3d", "roq", "rpl", "smk", "smush", "thp",
		"tmv", "ts", "usm", "viv", "vivo", "vmd", "vob", "wtv", "xmv", "yop", "flv",
		"webm", "mkv", "nut", "m2ts"
	);

	/**
	 * 支持的可写入视频文件后缀集合
	 * <p>
	 * 筛选了主流通用容器和广电专业格式，用于输出文件扩展名选择。
	 * <p>
	 * 包含的格式类型：
	 * <ul>
	 *   <li>通用容器格式（mp4, mkv, mov, avi, flv, webm等）</li>
	 *   <li>移动设备格式（3gp, 3g2等）</li>
	 *   <li>广电专业格式（ts, gxf, lxf等）</li>
	 *   <li>原始视频格式（yuv等）</li>
	 * </ul>
	 * <p>
	 * 注意：后缀名均为小写，用于文件扩展名匹配
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_WRITE_VIDEO_FILE_FORMATS = Set.of(
		"3g2", "3gp", "amv", "asf", "avi", "dv", "flv", "gxf", "ivf", "lxf", "m4v",
		"mkv", "mov", "mp4", "mpg", "mpeg", "ts", "nut", "webm", "yuv"
	);

	/**
	 * 支持的可读取字幕文件后缀集合
	 * <p>
	 * 包含通用文本字幕、图形字幕、专业字幕、歌词字幕等，用于文件扩展名检查和识别。
	 * <p>
	 * 包含的格式类型：
	 * <ul>
	 *   <li>通用文本字幕（srt, ass, ssa, sub, vtt, ttml等）</li>
	 *   <li>图形字幕（sup, idx等）</li>
	 *   <li>专业字幕（scc, stl, mcc等）</li>
	 *   <li>歌词字幕（lrc等）</li>
	 *   <li>老式/小众字幕（aqt, jss, mpl, mps, pjs, rt, smi, ted等）</li>
	 * </ul>
	 * <p>
	 * 注意：后缀名均为小写，用于文件扩展名匹配
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_READ_SUBTITLE_FILE_FORMATS = Set.of(
		"aqt", "dvbsub", "jss", "lrc", "mcc", "sub", "mpl", "mps", "pjs", "rt",
		"smi", "scc", "stl", "sup", "ted", "idx", "ass", "ssa", "srt", "ttml", "vtt"
	);

	/**
	 * 支持的可写入字幕文件后缀集合
	 * <p>
	 * 筛选了通用文本字幕、特效字幕和专业广电格式，用于输出文件扩展名选择。
	 * <p>
	 * 包含的格式类型：
	 * <ul>
	 *   <li>通用文本字幕（srt, vtt, ttml, sub等）</li>
	 *   <li>特效字幕（ass, ssa等）</li>
	 *   <li>专业字幕（scc, mcc等）</li>
	 *   <li>歌词字幕（lrc等）</li>
	 * </ul>
	 * <p>
	 * 注意：后缀名均为小写，用于文件扩展名匹配
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> SUPPORTED_WRITE_SUBTITLE_FILE_FORMATS = Set.of(
		"ass", "ssa", "lrc", "mcc", "sub", "scc", "srt", "ttml", "vtt"
	);
}
