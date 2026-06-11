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
 * 字幕相关常量类，定义FFmpeg支持的字幕文件格式集合
 * <p>
 * 该类包含两个核心常量：
 * <ul>
 *   <li>支持读取的字幕格式：覆盖文本字幕、图形字幕、广电专业字幕、歌词字幕等</li>
 *   <li>支持写入的字幕格式：筛选FFmpeg可编码输出的主流字幕格式，包含特效字幕、网页字幕、广电字幕等</li>
 * </ul>
 * <p>
 * 字幕格式分类：
 * <ul>
 *   <li>文本字幕：纯文本+时间戳（SRT、WebVTT、LRC等）</li>
 *   <li>图形字幕：位图/矢量图形字幕（SUP、VobSub等）</li>
 *   <li>特效字幕：支持样式/动画的字幕（ASS/SSA）</li>
 *   <li>专业字幕：广电/闭路电视专用格式（SCC、STL、MCC等）</li>
 * </ul>
 *
 * @author pangju666
 * @see <a href="https://ffmpeg.org/ffmpeg-formats.html#Subtitle-Formats">FFmpeg Subtitle Formats Official Doc</a>
 * @since 1.1.0
 */
public class SubtitleConstants {
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
	public static final Set<String> READ_SUBTITLE_FILE_FORMATS = Set.of(
		"aqtitle",         // AQTitle字幕
		"dvbsub",          // DVB数字电视字幕裸流
		"dvbtxt",          // DVB文本字幕
		"jacosub",         // JacoSub老式字幕
		"lrc",             // LRC歌词字幕
		"microdvd",        // MicroDVD简易字幕
		"mpl2",            // MPL2字幕
		"mpsub",           // MPlayer MPSub字幕
		"pjs",             // PJS动画字幕
		"realtext",        // RealText RM播放器字幕
		"sami",            // SAMI微软同步字幕
		"scc",             // SCC闭路隐藏字幕
		"stl",             // STL广电专业字幕
		"srt",             // SubRip通用字幕
		"subviewer",       // SubViewer字幕
		"subviewer1",      // SubViewer v1旧版
		"sup",             // PGS蓝光图形字幕
		"tedcaptions",     // TED演讲字幕
		"vobsub",          // DVD VOB图形字幕
		"webvtt"           // WebVTT网页字幕
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
	public static final Set<String> WRITE_SUBTITLE_FILE_FORMATS = Set.of(
		"ass",             // ASS/SSA 带特效高级字幕
		"jacosub",         // JacoSub老式字幕格式
		"lrc",             // LRC 歌词同步字幕
		"mcc",             // MacCaption 广电专业字幕
		"microdvd",        // MicroDVD 通用简易字幕
		"rcwt",            // RCWT 原始带时间戳字幕流
		"scc",             // SCC 闭路电视隐藏字幕
		"srt",             // SubRip 全网通用基础字幕
		"sup",             // HDMV PGS 蓝光图形字幕
		"ttml",            // TTML W3C标准结构化字幕
		"webvtt"           // WebVTT HTML5网页视频字幕
	);
}
