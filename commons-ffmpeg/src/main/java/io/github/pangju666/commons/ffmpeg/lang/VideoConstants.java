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
 * 视频相关常量类，定义FFmpeg支持的视频文件格式集合
 * <p>
 * 该类包含两个核心常量：
 * <ul>
 *   <li>支持读取的视频格式集合：覆盖通用视频容器、裸视频码流、游戏视频、广电专业格式、监控视频等</li>
 *   <li>支持写入的视频格式集合：筛选FFmpeg可编码输出的主流视频格式，包含通用容器、专业广电格式、Web端格式等</li>
 * </ul>
 * <p>
 * 格式分类说明：
 * <ul>
 *   <li>容器格式：包含音视频的封装格式（如MP4、MKV、AVI）</li>
 *   <li>裸码流格式：仅视频数据的裸流（如H.264、H.265、AV1）</li>
 *   <li>专业格式：广电/安防/摄像机专用格式（如MXF、R3D、DAV）</li>
 *   <li>游戏视频：游戏引擎/平台专属视频格式（如Bink、SMK、THP）</li>
 * </ul>
 *
 * @author pangju666
 * @see <a href="https://ffmpeg.org/ffmpeg-formats.html#Video-Formats">FFmpeg Video Formats Official Doc</a>
 * @since 1.1.0
 */
public class VideoConstants {
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
	public static final Set<String> READ_FILE_FORMATS = Set.of(
		"3dostr",          // 3DO STR 游戏视频
		"4xm",             // 4X Technologies 游戏视频
		"amv",             // AMV 播放器视频
		"anm",             // Deluxe Paint 动画
		"argo_asf",        // Argonaut 游戏ASF视频
		"argo_cvg",        // Argonaut CVG视频
		"asf",             // ASF 多媒体容器
		"asf_o",           // 旧版ASF
		"avi",             // AVI 通用视频容器
		"av1",             // AV1 裸视频码流
		"avs2",            // AVS2 国产裸视频
		"avs3",            // AVS3 国产裸视频
		"bethsoftvid",     // 贝塞斯达游戏视频
		"bink",            // Bink 游戏视频
		"bmv",             // Discworld II BMV动画
		"cavsvideo",       // AVS1 裸视频码流
		"cdxl",            // Commodore CDXL视频
		"cine",            // Phantom高速摄像机视频
		"dhav",            // DAV监控视频
		"dirac",           // Dirac无损裸视频
		"dnxhd",           // DNxHD广播裸视频
		"dv",              // DV摄像机视频
		"dvdvideo",        // DVD光盘视频
		"dxa",             // DXA游戏动画
		"ea",              // EA游戏视频
		"film_cpk",        // Sega FILM游戏视频
		"filmstrip",       // Adobe胶片序列
		"flv",             // FLV Flash视频
		"flic",            // FLI/FLC动画
		"gdv",             // Gremlin游戏视频
		"gxf",             // GXF广电交换容器
		"h261",            // H.261裸视频
		"h263",            // H.263裸视频
		"h264",            // H.264裸视频
		"hevc",            // H.265裸视频
		"idcin",           // id Software电影
		"iff",             // IFF动画格式
		"ifv",             // IFV监控视频
		"ingenient",       // Ingenient MJPEG
		"ipmovie",         // Interplay MVE
		"ipu",             // IPU裸视频
		"iv8",             // IndigoVision监控
		"ivf",             // IVF VP8/VP9封装
		"ivr",             // IVR网络录制视频
		"kux",             // 优酷KUX加密视频
		"live_flv",        // 直播FLV
		"lxf",             // LXF广电流
		"m4v",             // M4V裸MPEG4视频
		"matroska",        // MKV容器
		"mjpeg",           // MJPEG裸视频
		"mjpeg_2000",      // MJPEG2000裸视频
		"mlv",             // 魔灯单反MLV视频
		"mov",             // MOV QuickTime容器
		"mp4",             // MP4通用容器
		"mpeg",            // MPEG-PS节目流
		"mpegts",          // MPEG-TS传输流
		"mpegtsraw",       // 原始TS裸流
		"mpegvideo",       // MPEG1/2裸视频
		"mxf",             // MXF广电素材容器
		"nsv",             // Nullsoft流媒体NSV
		"nut",             // NUT开源容器
		"nuv",             // NuppelVideo
		"obu",             // AV1 OBU裸码流
		"paf",             // PAF游戏动画
		"pmp",             // PSP PMP视频
		"r3d",             // RED RAW摄像机R3D
		"rawvideo",        // 原始YUV裸视频
		"rm",              // RealMedia RM视频
		"roq",             // id RoQ游戏动画
		"rpl",             // RPL游戏视频
		"smk",             // Smacker游戏视频
		"smush",           // LucasArts SMUSH动画
		"thp",             // 任天堂THP游戏视频
		"tiertexseq",      // Tiertex SEQ动画
		"tmv",             // 8088flex复古TMV
		"usm",             // CRI USM游戏视频
		"vc1",             // VC1裸视频码流
		"vc1test",         // VC1测试码流
		"vividas",         // Vividas VIV视频
		"vivo",            // Vivo老式手机视频
		"vmd",             // Sierra VMD游戏视频
		"vob",             // DVD VOB视频
		"vvc",             // H.266/VVC裸视频
		"webm",            // WebM网页视频
		"wtv",             // Windows电视录像WTV
		"xmv",             // XMV微软游戏视频
		"yop",             // Psygnosis YOP动画
		"yuv4mpegpipe"     // YUV4MPEG原始视频序列
	);

	/**
	 * FFmpeg支持的可写入视频文件格式集合
	 * <p>
	 * 相较于读取格式，写入格式做了以下优化：
	 * <ul>
	 *   <li>移除仅支持解码的游戏/老旧格式</li>
	 *   <li>保留主流通用容器（MP4、MKV、MOV、AVI等）</li>
	 *   <li>保留新一代视频编码裸流（H.265、AV1、VVC等）</li>
	 *   <li>新增Web端格式（WebM、WebP、AVIF等）</li>
	 *   <li>保留广电专业格式（MXF系列、GXF、DNxHD等）</li>
	 *   <li>支持光盘封装格式（DVD、VCD、SVCD）</li>
	 * </ul>
	 * 可直接作为FFmpeg编码输出的格式参数（-f 参数），适配不同应用场景
	 *
	 * @since 1.1.0
	 */
	public static final Set<String> WRITE_FILE_FORMATS = Set.of(
		"3g2",              // 3GP2 手机视频容器
		"3gp",              // 3GP 移动端视频
		"a64",              // Commodore64简易视频
		"amv",              // AMV播放器专用视频
		"asf",              // ASF Windows流媒体容器
		"avi",              // AVI通用音视频容器
		"avif",             // AVIF动图/短视频
		"avm2",             // AVM2新版SWF
		"avs2",             // AVS2裸视频码流
		"avs3",             // AVS3裸视频码流
		"cavsvideo",        // AVS1标清裸视频
		"dirac",            // Dirac开源无损裸流
		"dnxhd",            // DNxHD广播专业码流
		"dv",               // DV摄像机标清视频
		"dvd",              // DVD VOB光盘封装
		"evc",              // EVC轻量化裸视频
		"f4v",              // F4V Adobe Flash高清视频
		"film_cpk",         // Sega游戏视频容器
		"filmstrip",        // Adobe胶片序列
		"flv",              // FLV传统Flash视频
		"gxf",              // GXF广电交换容器
		"h261",             // H.261老式裸视频
		"h263",             // H.263早期手机裸流
		"h264",             // H.264通用裸视频码流
		"hevc",             // H.265高清裸视频码流
		"ipod",             // iPod专用优化MP4
		"ismv",             // Smooth Streaming流媒体文件
		"ivf",              // IVF VP8/VP9封装
		"matroska",         // MKV万能多媒体容器
		"mjpeg",            // MJPEG运动JPEG裸流
		"mov",              // QuickTime MOV苹果容器
		"mp4",              // MP4全球通用视频容器
		"mpeg",             // MPEG-PS标准节目流
		"mpeg1video",       // MPEG1裸视频码流
		"mpeg2video",       // MPEG2裸视频码流
		"mpegts",           // MPEG-TS直播传输流
		"mxf",              // MXF广电专业素材容器
		"mxf_d10",          // MXF D10广播子格式
		"mxf_opatom",       // MXF OP-Atom剪辑专用
		"nut",              // NUT开源轻量容器
		"obu",              // AV1 OBU分段裸码流
		"ogv",              // Ogg-Video开源视频容器
		"rawvideo",         // 原始YUV未压缩裸视频
		"rm",               // RealMedia RM老式流媒体
		"roq",              // id Software RoQ游戏动画
		"smjpeg",           // SDL MJPEG游戏视频
		"svcd",             // SVCD高清VCD封装
		"swf",              // SWF Flash动画视频
		"vc1",              // VC1 WMV裸视频码流
		"vc1test",          // VC1测试裸码流
		"vcd",              // VCD标清光盘视频
		"vob",              // DVD VOB文件格式
		"vvc",              // H.266/VVC新一代裸码流
		"webm",             // WebM谷歌开源网页视频
		"webp",             // WebP高效动态图片
		"wtv",              // Windows TV录像文件
		"yuv4mpegpipe"      // YUV4MPEG原始视频文件
	);
}
