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

package io.github.pangju666.commons.ffmpeg.lang;

import java.util.Set;

/**
 * 音频相关常量类，定义FFmpeg支持的音频文件格式集合
 * <p>
 * 该类包含两个核心常量：
 * <ul>
 *   <li>支持读取的音频格式集合：覆盖常见音频格式、专业音频格式、游戏音频格式、裸PCM格式等</li>
 *   <li>支持写入的音频格式集合：筛选FFmpeg可编码输出的音频格式，包含主流压缩格式和裸PCM格式</li>
 * </ul>
 * <p>
 * 格式说明：
 * <ul>
 *   <li>格式标识采用FFmpeg原生短名称（如"mp3"、"flac"）</li>
 *   <li>裸PCM格式区分字节序（大端be/小端le）、位深、符号类型</li>
 *   <li>游戏音频格式针对主流游戏引擎/平台的专属音频格式</li>
 * </ul>
 *
 * @author pangju666
 * @see <a href="https://ffmpeg.org/ffmpeg-formats.html#Audio-Formats">FFmpeg Audio Formats Official Doc</a>
 * @since 1.1.0
 */
public class AudioConstants {
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
	public static final Set<String> READ_AUDIO_FILE_FORMATS = Set.of(
		"aa",               // Audible AA有声书
		"aac",              // ADTS AAC裸音频
		"aax",              // CRI加密AAX音频
		"ace",              // tri-Ace游戏音频
		"acm",              // Interplay ACM游戏音频
		"act",              // ACT录音语音
		"adf",              // Artworx音频格式
		"adp",              // ADP语音格式
		"ads",              // PS2 ADS游戏音频
		"adx",              // CRI ADX游戏音频
		"aea",              // MD STUDIO录音棚音频
		"afc",              // AFC音频
		"aiff",             // AIFF苹果波形音频
		"aix",              // CRI AIX游戏音频
		"alaw",             // A-law PCM电话语音
		"alp",              // 乐高ALP游戏音频
		"amr",              // AMR手机语音
		"amrnb",            // AMR-NB窄带裸流
		"amrwb",            // AMR-WB宽带裸流
		"apac",             // APAC裸音频码流
		"apc",              // CRYO APC游戏音频
		"ape",              // Monkey's APE无损音频
		"apm",              // 育碧APM游戏音频
		"aptx",             // aptX蓝牙裸音频
		"aptx_hd",          // aptX HD无损蓝牙音频
		"apv",              // APV裸音频码流
		"ast",              // AST游戏音频流
		"au",               // Sun AU老式Unix音频
		"bfstm",            // BFSTM游戏音频
		"binka",            // Bink游戏音频
		"boa",              // Black Ops游戏音频
		"bonk",             // Bonk无损音频
		"brstm",            // BRSTM游戏音频
		"c93",              // Interplay C93音频
		"caf",              // CAF苹果核心音频
		"codec2",           // codec2低码率语音
		"codec2raw",        // codec2原始裸流
		"daud",             // 影院D-Cinema音频
		"dfpwm",            // DFPWM低比特音频
		"dss",              // DSS数字语音
		"dts",              // DTS环绕声裸流
		"dtshd",            // DTS-HD高清环绕声
		"eac3",             // E-AC3增强杜比
		"epaf",             // Ensoniq录音音频
		"flac",             // FLAC无损音频
		"fsb",              // FMOD游戏音频FSB
		"fwse",             // MT Framework游戏音频
		"g722",             // G.722宽带语音
		"g723_1",           // G.723.1窄带语音
		"g726",             // G.726 ADPCM大端
		"g726le",           // G.726 ADPCM小端
		"g728",             // G.728语音编码
		"g729",             // G.729裸语音文件
		"genh",             // GENhdr专业音频
		"gsm",              // GSM移动电话语音
		"hca",              // CRI HCA游戏音频
		"hcom",             // Mac HCOM压缩音频
		"iamf",             // IAMF沉浸式空间音频
		"ilbc",             // iLBC网络通话语音
		"ircam",            // IRCAM科研专业音频
		"kvag",             // VAG索尼PS游戏音频
		"laf",              // LAF极限音频格式
		"lc3",              // LC3蓝牙低功耗语音
		"libgme",           // 模拟器游戏音乐
		"libmodplug",       // ModPlug音轨
		"libopenmpt",       // Tracker游戏音轨
		"loas",             // LOAS AAC同步裸流
		"mlp",              // MLP无损多声道音频
		"mmf",              // MMF雅马哈手机铃声
		"mods",             // MobiClip MODS音轨
		"moflex",           // MobiClip MOFLEX音频
		"mp2",              // MP2广播音频
		"mp3",              // MP3通用有损音频
		"mpc",              // Musepack有损音频
		"mpc8",             // Musepack SV8新版
		"mulaw",            // mu-law北美电话语音
		"musx",             // Eurocom MUSX游戏音频
		"nistsphere",       // NIST语音音频
		"nsp",              // CSL实验室NSP语音
		"oma",              // Sony OpenMG加密音频
		"osq",              // OSQ语音格式
		"pvf",              // PVF便携语音格式
		"qcp",              // QCP高通手机语音
		"qoa",              // QOA高效无损音频
		"rka",              // RK Audio游戏音频
		"rso",              // 乐高RSO设备音频
		"sbc",              // SBC蓝牙基础音频
		"sdx",              // Sample Dump音频交换
		"sds",              // MIDI采样音频
		"shn",              // Shorten早期无损音频
		"siff",             // SIFF游戏音频
		"sln",              // Asterisk SLN裸PCM
		"sol",              // Sierra SOL游戏音频
		"sox",              // SoX专业音频格式
		"spdif",            // SPDIF光纤同轴封装
		"tak",              // TAK高压缩无损音频
		"tta",              // TTA True Audio无损
		"voc",              // Creative创新声卡VOC
		"w64",              // Wave64超大WAV
		"wav",              // WAV标准PCM波形
		"wavarc",           // WAV归档压缩格式
		"wsaud",            // Westwood游戏音频
		"wsd",              // WSD单比特音频
		"wv",               // WavPack混合无损音频
		"wve",              // Psion 3语音音频
		"xa",               // Maxis XA游戏音频
		"xmd",              // Konami XMD游戏音频
		"xvag",             // PS3 XVAG游戏音频
		"xwma",             // XWMA微软无损音频
		"f32be",            // 32位浮点大端PCM
		"f32le",            // 32位浮点小端PCM
		"f64be",            // 64位浮点大端PCM
		"f64le",            // 64位浮点小端PCM
		"s8",               // 8位有符号PCM
		"u8",               // 8位无符号PCM
		"s16be",            // 16位大端PCM
		"s16le",            // 16位小端PCM
		"s24be",            // 24位大端专业PCM
		"s24le",            // 24位小端专业PCM
		"s32be",            // 32位大端高精度PCM
		"s32le"             // 32位小端高精度PCM
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
	public static final Set<String> WRITE_AUDIO_FILE_FORMATS = Set.of(
		"ac3",             // raw AC-3 杜比数字裸音频码流
		"ac4",             // raw AC-4 新一代杜比全景声裸码流
		"adts",            // ADTS AAC 带ADTS头AAC裸流
		"adx",             // CRI ADX 游戏音频格式
		"aea",             // MD STUDIO 录音棚音频格式
		"aiff",            // AIFF 苹果无损波形音频
		"alaw",            // PCM A-law 欧美8K电话语音
		"alp",             // LEGO Racers ALP 乐高游戏音频
		"amr",             // 3GPP AMR 手机语音音频
		"aptx",            // raw aptX 蓝牙高清音频裸流
		"aptx_hd",         // raw aptX HD 无损蓝牙音频
		"ast",             // AST 游戏音频流容器
		"au",              // Sun AU Unix老式音频格式
		"caf",             // CAF Apple核心无损音频
		"codec2",          // codec2 低码率语音音频
		"codec2raw",       // codec2 原始裸码流
		"daud",            // D-Cinema 影院专用音频
		"dfpwm",           // DFPWM 低比特音频格式
		"dts",             // raw DTS 影院环绕声裸流
		"eac3",            // raw E-AC3 增强杜比数字
		"flac",            // FLAC 开源无损压缩音频
		"g722",            // G.722 宽带电话语音
		"g723_1",          // G.723.1 窄带语音编码
		"g726",            // G.726 大端ADPCM语音
		"g726le",          // G.726 小端ADPCM语音
		"gsm",             // GSM 移动电话语音
		"iamf",            // IAMF 沉浸式空间音频
		"ilbc",            // iLBC 网络通话语音
		"ircam",           // IRCAM 专业科研音频格式
		"kvag",            // VAG 索尼PS游戏音频
		"latm",            // LOAS/LATM AAC低延迟裸流
		"lc3",             // LC3 蓝牙低功耗语音音频
		"mlp",             // MLP 无损多声道影院音频
		"mmf",             // MMF 雅马哈手机铃声音频
		"mp2",             // MP2 MPEG Layer2广播音频
		"mp3",             // MP3 通用有损音频
		"mulaw",           // PCM mu-law 北美8K电话语音
		"oga",             // OGA Ogg纯音频容器
		"ogg",             // Ogg 通用多媒体容器（可存纯音频）
		"oma",             // Sony OpenMG 索尼加密音频
		"opus",            // Opus Ogg封装超低延迟音频
		"rso",             // 乐高RSO设备音频
		"s16be",           // 16位有符号大端PCM裸音频
		"s16le",           // 16位有符号小端PCM裸音频
		"s24be",           // 24位有符号大端专业PCM
		"s24le",           // 24位有符号小端专业PCM
		"s32be",           // 32位有符号大端高精度PCM
		"s32le",           // 32位有符号小端高精度PCM
		"s8",              // 8位有符号PCM原始波形
		"sbc",             // SBC 蓝牙基础音频编码
		"sox",             // SoX 专业音频工具原生格式
		"spdif",           // SPDIF 光纤同轴音频封装
		"spx",             // Speex Ogg语音编码
		"truehd",          // TrueHD 无损杜比全景声裸流
		"tta",             // TTA 高压缩无损音频
		"voc",             // Creative VOC 创新声卡音频
		"w64",             // Wave64 64位超大WAV音频
		"wav",             // WAV 标准无损PCM波形音频
		"wsaud",           // Westwood 游戏音频格式
		"wv",              // WavPack 混合无损/有损音频
		"f32be",           // 32位浮点大端专业录音PCM
		"f32le",           // 32位浮点小端专业录音PCM
		"f64be",           // 64位浮点大端高精度混音PCM
		"f64le"            // 64位浮点小端高精度混音PCM
	);
}
