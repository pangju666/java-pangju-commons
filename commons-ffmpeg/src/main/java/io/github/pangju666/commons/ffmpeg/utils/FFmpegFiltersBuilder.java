package io.github.pangju666.commons.ffmpeg.utils;

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import io.github.pangju666.commons.io.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.IOException;
import java.util.*;

public abstract class FFmpegFiltersBuilder {
	protected final List<Alias> aliasList = new ArrayList<>();
	protected final Set<String> aliasSet = new HashSet<>();
	protected final Map<String, List<String>> aliasFiltersMap = new HashMap<>();
	protected final List<String> globalFilterList = new ArrayList<>();
	protected int indexInputs;

	public static FFmpegFiltersBuilder video() {
		return new Video();
	}

	public static FFmpegFiltersBuilder audio() {
		return new Audio();
	}

	public FFmpegFiltersBuilder addInput() {
		aliasList.add(null);
		++indexInputs;

		return this;
	}

	public FFmpegFiltersBuilder addInput(String alias, String filter) {
		Validate.notBlank(alias, "alias 不可为空");
		Validate.notBlank(filter, "filter 不可为空");

		if (!aliasSet.contains(alias)) {
			aliasSet.add(alias);
			aliasList.add(new Alias(alias, true));
			++indexInputs;

			List<String> aliasFilters = aliasFiltersMap.computeIfAbsent(alias, k -> new ArrayList<>());
			aliasFilters.add(filter);
		}

		return this;
	}

	public FFmpegFiltersBuilder addInput(String alias, String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");
		Validate.notBlank(alias, "alias 不可为空");

		return addInput(alias, filterName + "=" + StringUtils.join(args,
			FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	public FFmpegFiltersBuilder addFileSource(String alias, File file) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");

		return addSource(alias, String.format("movie='%s'", FFmpegUtils.getSafeFilePath(file)));
	}

	public FFmpegFiltersBuilder addFileSource(String alias, File file, String filter) throws IOException {
		FileUtils.checkFile(file, "file 不可为 null");
		Validate.notBlank(filter, "filter 不可为空");

		return addSource(alias, String.format("movie='%s'", FFmpegUtils.getSafeFilePath(file) +
			FFmpegConstants.FILTER_CONCAT_SEPARATOR + filter));
	}

	public FFmpegFiltersBuilder addFileSource(String alias, File file, String filterName, String... args) throws IOException {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");
		Validate.notBlank(alias, "alias 不可为空");

		return addFileSource(alias, file, filterName + "=" + StringUtils.join(args,
			FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	public FFmpegFiltersBuilder addSource(String alias, String filter) {
		Validate.notBlank(alias, "alias 不可为空");
		Validate.notBlank(filter, "filter 不可为空");

		if (!aliasSet.contains(alias)) {
			aliasSet.add(alias);
			aliasList.add(new Alias(alias, false));

			List<String> aliasFilters = aliasFiltersMap.computeIfAbsent(alias, k -> new ArrayList<>());
			aliasFilters.add(filter);
		}

		return this;
	}

	public FFmpegFiltersBuilder addSource(String alias, String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");
		Validate.notBlank(alias, "alias 不可为空");

		return addSource(alias, filterName + "=" + StringUtils.join(args,
			FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	public FFmpegFiltersBuilder appendAliasFilter(String alias, String filter) {
		Validate.notBlank(alias, "alias 不可为空");
		Validate.notBlank(filter, "filter 不可为空");

		if (aliasSet.contains(alias)) {
			aliasFiltersMap.get(alias).add(filter);
		}

		return this;
	}

	public FFmpegFiltersBuilder appendAliasFilter(String alias, String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");

		return appendAliasFilter(alias, filterName + "=" + StringUtils.join(args,
			FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	public FFmpegFiltersBuilder addGlobalFilter(String filter) {
		if (StringUtils.isNotBlank(filter)) {
			globalFilterList.add(filter);
		}

		return this;
	}

	public FFmpegFiltersBuilder addGlobalFilter(String filterName, String... args) {
		Validate.notBlank(filterName, "filterName 不可为空");
		Validate.notEmpty(args, "args 不可为空");

		return addGlobalFilter(filterName + "=" + StringUtils.join(args, FFmpegConstants.FILTER_ARG_SEPARATOR));
	}

	public String build() {
		if (globalFilterList.isEmpty()) {
			return null;
		}
		if (aliasList.isEmpty() && globalFilterList.size() == 1) {
			return globalFilterList.get(0);
		}

		String filterChain = StringUtils.join(globalFilterList, FFmpegConstants.FILTER_CONCAT_SEPARATOR);

		if (aliasList.isEmpty()) {
			return FFmpegConstants.SINGLE_INPUT_TAG + filterChain + FFmpegConstants.SINGLE_OUTPUT_TAG;
		}

		StringBuilder inputTagsBuilder = new StringBuilder();
		StringBuilder inputTagFFmpegFiltersBuilder = new StringBuilder();
		int inputIndex = 0;
		for (Alias alias : aliasList) {
			String inputTag = getIndexInputTag(inputIndex);
			if (Objects.nonNull(alias)) {
				if (alias.isInput()) {
					inputTagFFmpegFiltersBuilder.append("[").append(inputTag).append("]");
					++inputIndex;
				}
				List<String> inputFilters = aliasFiltersMap.get(alias.alias());
				inputTagFFmpegFiltersBuilder.append(StringUtils.join(inputFilters, FFmpegConstants.FILTER_CONCAT_SEPARATOR))
					.append("[").append(alias.alias()).append("]").append(FFmpegConstants.FILTER_BRANCH_SEPARATOR);
			}

			if (Objects.nonNull(alias)) {
				inputTagsBuilder.append("[").append(alias.alias()).append("]");
			} else {
				inputTagsBuilder.append("[").append(inputTag).append("]");
			}
		}

		if (indexInputs == 0) {
			return inputTagFFmpegFiltersBuilder + FFmpegConstants.SINGLE_INPUT_TAG + inputTagsBuilder + filterChain +
				FFmpegConstants.SINGLE_OUTPUT_TAG;
		} else {
			return inputTagFFmpegFiltersBuilder.toString() + inputTagsBuilder + filterChain + getOutputTag();
		}
	}

	protected abstract String getIndexInputTag(int n);

	protected abstract String getOutputTag();

	public static class Video extends FFmpegFiltersBuilder {
		@Override
		protected String getIndexInputTag(int n) {
			return n + ":v";
		}

		@Override
		protected String getOutputTag() {
			return FFmpegConstants.OUTPUT_VIDEO_TAG;
		}
	}

	public static class Audio extends FFmpegFiltersBuilder {
		@Override
		protected String getIndexInputTag(int n) {
			return n + ":a";
		}

		@Override
		protected String getOutputTag() {
			return FFmpegConstants.OUTPUT_AUDIO_TAG;
		}
	}

	protected record Alias(String alias, Boolean isInput) {
	}
}
