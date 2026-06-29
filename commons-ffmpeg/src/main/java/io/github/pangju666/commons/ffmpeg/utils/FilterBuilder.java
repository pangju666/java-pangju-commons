package io.github.pangju666.commons.ffmpeg.utils;

import io.github.pangju666.commons.ffmpeg.lang.FFmpegConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.List;

public abstract class FilterBuilder {
	protected final List<String> inputFilters = new ArrayList<>();

	protected final StringBuilder inputTagsBuilder = new StringBuilder();
	protected final List<String> filters = new ArrayList<>();
	protected int inputs = 0;

	protected FilterBuilder() {
	}

	public static FilterBuilder video() {
		return new Video();
	}

	public static FilterBuilder audio() {
		return new Audio();
	}

	public static FilterBuilder single() {
		return new Single();
	}

	public FilterBuilder addInput() {
		inputTagsBuilder.append(getInputTag(inputs));
		++inputs;

		return this;
	}

	public FilterBuilder addInput(String alias, String filter) {
		Validate.notBlank(alias, "alias 不可为空");
		Validate.notBlank(filter, "filter 不可为空");

		String aliasTag = String.format("[%s]", alias);
		inputFilters.add(getInputTag(inputs) + filter + aliasTag);
		inputTagsBuilder.append(aliasTag);
		++inputs;

		return this;
	}

	public FilterBuilder addFilter(String filter) {
		Validate.notBlank(filter, "filter 不可为空");

		filters.add(filter);

		return this;
	}

	public String build() {
		if (filters.isEmpty()) {
			return null;
		}
		return StringUtils.join(inputFilters, FFmpegConstants.FILTER_BRANCH_SEPARATOR) +
			FFmpegConstants.FILTER_BRANCH_SEPARATOR + inputTagsBuilder + StringUtils.join(filters,
			FFmpegConstants.FILTER_CONCAT_SEPARATOR) + getOutputTag();
	}

	protected abstract String getInputTag(int n);

	protected abstract String getInputTags();

	protected abstract String getOutputTag();

	public static class Video extends FilterBuilder {
		public Video() {
		}

		@Override
		protected String getInputTag(int n) {
			return String.format("[%d:v]", n);
		}

		@Override
		protected String getInputTags() {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < inputs; i++) {
				builder.append(getInputTag(i));
			}
			return builder.toString();
		}

		@Override
		protected String getOutputTag() {
			return FFmpegConstants.OUTPUT_VIDEO_TAG;
		}
	}

	public static class Audio extends FilterBuilder {
		public Audio() {
		}

		@Override
		protected String getInputTag(int n) {
			return String.format("[%d:a]", n);
		}

		@Override
		protected String getInputTags() {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < inputs; i++) {
				builder.append(getInputTag(i));
			}
			return builder.toString();
		}

		@Override
		protected String getOutputTag() {
			return FFmpegConstants.OUTPUT_AUDIO_TAG;
		}
	}

	public static class Single extends FilterBuilder {
		public Single() {
		}

		@Override
		public FilterBuilder addInput() {
			return this;
		}

		@Override
		public FilterBuilder addInput(String alias, String filter) {
			return this;
		}

		@Override
		public String build() {
			if (filters.isEmpty()) {
				return null;
			}
			if (filters.size() == 1) {
				return filters.get(0);
			}
			return FFmpegConstants.SINGLE_INPUT_TAG + StringUtils.join(filters,
				FFmpegConstants.FILTER_CONCAT_SEPARATOR) + FFmpegConstants.SINGLE_OUTPUT_TAG;
		}

		@Override
		protected String getInputTag(int n) {
			return "";
		}

		@Override
		protected String getInputTags() {
			return "";
		}

		@Override
		protected String getOutputTag() {
			return "";
		}
	}
}
