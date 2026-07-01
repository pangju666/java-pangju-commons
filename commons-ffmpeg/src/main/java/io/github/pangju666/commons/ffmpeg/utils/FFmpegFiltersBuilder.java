package io.github.pangju666.commons.ffmpeg.utils;

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * FFmpeg 滤镜链构建器
 * <p>
 * 提供流式 API 用于构建复杂的 FFmpeg 滤镜链，支持多个输入源、别名、分支滤镜和全局滤镜。
 * 支持视频和音频两种过滤模式。
 * </p>
 * <h3>核心特性</h3>
 * <ul>
 *     <li>支持添加输入源、文件源</li>
 *     <li>支持别名管理</li>
 *     <li>支持分支滤镜</li>
 *     <li>支持全局滤镜链</li>
 *     <li>流畅的链式调用 API</li>
 * </ul>
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 示例1：视频添加图片水印
 * String watermarkFilter = FFmpegFiltersBuilder.video()
 * 	.addInput()
 *     .addFileSource("wm", new File("watermark.png"))
 *     .appendAliasFilter("wm", "scale=iw*0.2:-1")
 *     .appendAliasFilter("wm", "format=rgba")
 *     .appendAliasFilter("wm", "colorchannelmixer=aa=0.8")
 *     .addGlobalFilter("overlay", "W-w-20", "H-h-20")
 *     .build();
 * // 输出结果：movie='E\:/Roaming/output/watermark.jpg',scale=iw*0.2:-1,format=rgba,colorchannelmixer=aa=0.8[wm];[in][wm]overlay=W-w-20:H-h-20[out]
 *
 * // 示例2：视频添加文字水印
 * String textWatermarkFilter = FFmpegFiltersBuilder.video()
 *     .addInput()
 *     .addGlobalFilter("drawtext", "text='Copyright 2024'", "fontsize=48", "fontcolor=white", "x=W-tw-20", "y=H-th-20")
 *     .build();
 * // 输出结果：drawtext=text='Copyright 2024':fontsize=48:fontcolor=white:x=W-tw-20:y=H-th-20
 *
 * // 示例3：音频混音
 * String textWatermarkFilter = FFmpegFiltersBuilder.video()
 *     .addInput()
 *     .addInput("bgm", "aresample=44100")
 *     .addGlobalFilter("amix", "inputs=2", "dropout_transition=0", "duration=first", "weights=1 1")
 *     .build();
 * // 输出结果：[1:v]aresample=44100[bgm];[0:v][bgm]amix=inputs=2:dropout_transition=0:duration=first:weights=1 1[v]
 *
 * // 示例4：视频裁剪和缩放
 * String cropScaleFilter = FFmpegFiltersBuilder.video()
 *     .addGlobalFilter("crop", "1000:1000:100:100")
 *     .addGlobalFilter("scale", "720:-1")
 *     .build();
 * // 输出结果：[in]crop=1000:1000:100:100,scale=720:-1[out]
 *
 * // 示例5：音频处理（音量调整+重采样）
 * String audioFilter = FFmpegFiltersBuilder.audio()
 *     .addGlobalFilter("volume", "2.0")
 *     .addGlobalFilter("aresample", "44100")
 *     .build();
 * // 输出结果：[in]volume=2.0,aresample=44100[out]
 *
 * // 示例6：视频变速处理
 * String speedFilter = FFmpegFiltersBuilder.video()
 *     .addGlobalFilter("setpts", "PTS*0.5")
 *     .addGlobalFilter("atempo", "2.0")
 *     .build();
 * // 输出结果：[in]setpts=PTS*0.5,atempo=2.0[out]
 * }</pre>
 *
 * @author pangju666
 * @since 1.1.0
 */
public abstract class FFmpegFiltersBuilder {
	/**
	 * 别名列表，用于构建输入标签顺序
	 *
	 * @since 1.1.0
	 */
	protected final List<Pair<String, Boolean>> aliasList = new ArrayList<>();

	/**
	 * 已存在的别名集合，用于去重
	 *
	 * @since 1.1.0
	 */
	protected final Set<String> aliasSet = new HashSet<>();

	/**
	 * 别名与对应的滤镜链的映射关系
	 *
	 * @since 1.1.0
	 */
	protected final Map<String, List<String>> aliasFiltersMap = new HashMap<>();

	/**
	 * 全局滤镜列表
	 *
	 * @since 1.1.0
	 */
	protected final List<String> globalFilterList = new ArrayList<>();

	/**
	 * 输入源的索引计数器
	 *
	 * @since 1.1.0
	 */
	protected int indexInputs;

	/**
	 * 创建视频滤镜构建器
	 *
	 * @return 视频滤镜构建器实例
	 * @since 1.1.0
	 */
	public static FFmpegFiltersBuilder video() {
		return new Video();
	}

	/**
	 * 创建音频滤镜构建器
	 *
	 * @return 音频滤镜构建器实例
	 * @since 1.1.0
	 */
	public static FFmpegFiltersBuilder audio() {
		return new Audio();
	}

	/**
	 * 添加输入源
	 *
	 * @return 当前构建器实例
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addInput() {
		aliasList.add(null);
		++indexInputs;

		return this;
	}

	/**
	 * 添加带滤镜的输入源
	 *
	 * @param alias  输入源别名
	 * @param filter 输入源滤镜字符串
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当参数为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addInput(String alias, String filter) {
		Validate.notBlank(alias, "alias 不可为空");
		Validate.notBlank(filter, "filter 不可为空");

		if (!aliasSet.contains(alias)) {
			aliasSet.add(alias);
			aliasList.add(Pair.of(alias, true));
			++indexInputs;

			List<String> aliasFilters = aliasFiltersMap.computeIfAbsent(alias, k -> new ArrayList<>());
			aliasFilters.add(filter);
		}

		return this;
	}

	/**
	 * 添加带滤镜的输入源
	 *
	 * @param alias      输入源别名
	 * @param filterName 滤镜名称
	 * @param args       滤镜参数
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当参数为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addInput(String alias, String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");
		Validate.notBlank(alias, "alias 不可为空");

		return addInput(alias, filterName + "=" + StringUtils.join(args,
			FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	/**
	 * 添加文件源（如图片、视频等）
	 *
	 * @param alias 文件源别名
	 * @param file  文件对象
	 * @return 当前构建器实例
	 * @throws IOException              文件操作失败时抛出
	 * @throws IllegalArgumentException 当文件无效时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addFileSource(String alias, File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		return addFileSource(alias, file.getAbsolutePath());
	}

	/**
	 * 添加文件源（如图片、视频等）并应用滤镜
	 *
	 * @param alias  文件源别名
	 * @param file   文件对象
	 * @param filter 滤镜字符串
	 * @return 当前构建器实例
	 * @throws IOException              文件操作失败时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addFileSource(String alias, File file, String filter) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		return addFileSource(alias, file.getAbsolutePath(), filter);
	}

	/**
	 * 添加文件源（如图片、视频等）并应用滤镜
	 *
	 * @param alias      文件源别名
	 * @param file       文件对象
	 * @param filterName 滤镜名称
	 * @param args       滤镜参数
	 * @return 当前构建器实例
	 * @throws IOException              文件操作失败时抛出
	 * @throws IllegalArgumentException 当参数无效时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addFileSource(String alias, File file, String filterName, String... args) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		return addFileSource(alias, file.getAbsolutePath(), filterName, args);
	}

	/**
	 * 添加文件源（如图片、视频等）
	 *
	 * @param alias    文件源别名
	 * @param filePath 文件路径
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当文件路径为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addFileSource(String alias, String filePath) {
		return addSource(alias, String.format("movie='%s'", FFmpegUtils.getSafeFileSourcePath(filePath)));
	}

	/**
	 * 添加文件源（如图片、视频等）并应用滤镜
	 *
	 * @param alias    文件源别名
	 * @param filePath 文件路径
	 * @param filter   滤镜字符串
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当文件路径为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addFileSource(String alias, String filePath, String filter) {
		Validate.notBlank(filter, "filter 不可为空");

		return addSource(alias, String.format("movie='%s'", FFmpegUtils.getSafeFileSourcePath(filePath) +
			FFmpegConstants.FILTER_CONCAT_SEPARATOR + filter));
	}

	/**
	 * 添加文件源（如图片、视频等）并应用滤镜
	 *
	 * @param alias      文件源别名
	 * @param filePath   文件路径
	 * @param filterName 滤镜名称
	 * @param args       滤镜参数
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当文件路径为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addFileSource(String alias, String filePath, String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");

		return addFileSource(alias, filePath, filterName + "=" + StringUtils.join(args,
			FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	/**
	 * 添加自定义源
	 *
	 * @param alias  源别名
	 * @param filter 滤镜字符串
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当参数为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addSource(String alias, String filter) {
		Validate.notBlank(alias, "alias 不可为空");
		Validate.notBlank(filter, "filter 不可为空");

		if (!aliasSet.contains(alias)) {
			aliasSet.add(alias);
			aliasList.add(Pair.of(alias, false));

			List<String> aliasFilters = aliasFiltersMap.computeIfAbsent(alias, k -> new ArrayList<>());
			aliasFilters.add(filter);
		}

		return this;
	}

	/**
	 * 添加自定义源
	 *
	 * @param alias      源别名
	 * @param filterName 滤镜名称
	 * @param args       滤镜参数
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当参数为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addSource(String alias, String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");
		Validate.notBlank(alias, "alias 不可为空");

		return addSource(alias, filterName + "=" + StringUtils.join(args,
			FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	/**
	 * 为别名追加滤镜
	 *
	 * @param alias  源别名
	 * @param filter 滤镜字符串
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当参数为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder appendAliasFilter(String alias, String filter) {
		Validate.notBlank(alias, "alias 不可为空");
		Validate.notBlank(filter, "filter 不可为空");

		if (aliasSet.contains(alias)) {
			aliasFiltersMap.get(alias).add(filter);
		}

		return this;
	}

	/**
	 * 为别名追加滤镜
	 *
	 * @param alias      源别名
	 * @param filterName 滤镜名称
	 * @param args       滤镜参数
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当参数为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder appendAliasFilter(String alias, String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");

		return appendAliasFilter(alias, filterName + "=" + StringUtils.join(args,
			FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	/**
	 * 添加全局滤镜
	 *
	 * @param filter 滤镜字符串
	 * @return 当前构建器实例
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addGlobalFilter(String filter) {
		if (StringUtils.isNotBlank(filter)) {
			globalFilterList.add(filter);
		}

		return this;
	}

	/**
	 * 添加全局滤镜
	 *
	 * @param filterName 滤镜名称
	 * @param args       滤镜参数
	 * @return 当前构建器实例
	 * @throws IllegalArgumentException 当参数为空时抛出
	 * @since 1.1.0
	 */
	public FFmpegFiltersBuilder addGlobalFilter(String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");

		return addGlobalFilter(filterName + "=" + StringUtils.join(args, FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	/**
	 * 构建最终的 FFmpeg 滤镜链字符串
	 *
	 * @return FFmpeg 滤镜链字符串，没有全局滤镜时返回 null
	 * @since 1.1.0
	 */
	public String build() {
		if (globalFilterList.isEmpty()) {
			return null;
		}

		boolean isSingleInput = indexInputs <= 1;

		if (isSingleInput && aliasFiltersMap.isEmpty() && globalFilterList.size() == 1) {
			return globalFilterList.get(0);
		}

		String filterChain = StringUtils.join(globalFilterList, FFmpegConstants.FILTER_CONCAT_SEPARATOR);

		if (isSingleInput && aliasFiltersMap.isEmpty()) {
			return FFmpegConstants.FILTER_SINGLE_INPUT_TAG + filterChain + FFmpegConstants.FILTER_SINGLE_OUTPUT_TAG;
		}

		StringBuilder inputTagsBuilder = new StringBuilder();
		StringBuilder inputTagFFmpegFiltersBuilder = new StringBuilder();
		int inputIndex = 0;
		for (Pair<String, Boolean> alias : aliasList) {
			String inputTag = getIndexInputTag(inputIndex);
			if (Objects.nonNull(alias)) {
				if (alias.getRight()) {
					inputTagFFmpegFiltersBuilder.append("[").append(inputTag).append("]");
					++inputIndex;
				}
				List<String> inputFilters = aliasFiltersMap.get(alias.getLeft());
				inputTagFFmpegFiltersBuilder.append(StringUtils.join(inputFilters, FFmpegConstants.FILTER_CONCAT_SEPARATOR))
					.append("[").append(alias.getLeft()).append("]").append(FFmpegConstants.FILTER_BRANCH_SEPARATOR);
			} else {
				++inputIndex;
			}

			if (Objects.nonNull(alias)) {
				inputTagsBuilder.append("[").append(alias.getLeft()).append("]");
			} else if (!isSingleInput) {
				inputTagsBuilder.append("[").append(inputTag).append("]");
			}
		}

		if (isSingleInput) {
			return inputTagFFmpegFiltersBuilder + FFmpegConstants.FILTER_SINGLE_INPUT_TAG + inputTagsBuilder +
				filterChain + FFmpegConstants.FILTER_SINGLE_OUTPUT_TAG;
		} else {
			return inputTagFFmpegFiltersBuilder.toString() + inputTagsBuilder + filterChain + getOutputTag();
		}
	}

	/**
	 * 获取索引输入标签
	 *
	 * @param n 输入索引
	 * @return 输入标签字符串
	 * @since 1.1.0
	 */
	protected abstract String getIndexInputTag(int n);

	/**
	 * 获取输出标签
	 *
	 * @return 输出标签字符串
	 * @since 1.1.0
	 */
	protected abstract String getOutputTag();

	/**
	 * 视频滤镜构建器实现
	 *
	 * @since 1.1.0
	 */
	public static class Video extends FFmpegFiltersBuilder {
		@Override
		protected String getIndexInputTag(int n) {
			return n + ":v";
		}

		@Override
		protected String getOutputTag() {
			return FFmpegConstants.FILTER_OUTPUT_VIDEO_TAG;
		}
	}

	/**
	 * 音频滤镜构建器实现
	 *
	 * @since 1.1.0
	 */
	public static class Audio extends FFmpegFiltersBuilder {
		@Override
		protected String getIndexInputTag(int n) {
			return n + ":a";
		}

		@Override
		protected String getOutputTag() {
			return FFmpegConstants.FILTER_OUTPUT_AUDIO_TAG;
		}
	}
}
