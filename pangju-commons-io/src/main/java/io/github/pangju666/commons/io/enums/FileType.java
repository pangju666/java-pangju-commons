package io.github.pangju666.commons.io.enums;

import java.util.Collections;
import java.util.Set;

public enum FileType {
	IMAGE("图片", Collections.emptySet(), "image/"),
	TEXT("文本", Collections.emptySet(), "text/"),
	AUDIO("音频", Collections.emptySet(), "audio/"),
	VIDEO("视频", Collections.emptySet(), "video/"),
	COMPRESS("压缩包", Set.of(
		"application/x-tar", "application/gzip", "application/x-bzip", "application/x-bzip2",
		"application/x-zip-compressed", "application/x-uc2-compressed", "application/x-rar-compressed",
		"application/x-ace-compressed", "application/x-7z-compressed"
	), null),
	DOCUMENT("文档", Set.of("application/pdf",
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.ms-excel",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/msword",
		"application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/vnd.ms-powerpoint"
	), null);

	private final String label;
	private final Set<String> types;
	private final String typePrefix;

	FileType(String label, Set<String> types, String typePrefix) {
		this.label = label;
		this.types = types;
		this.typePrefix = typePrefix;
	}

	public String getLabel() {
		return label;
	}

	public Set<String> getTypes() {
		return types;
	}

	public String getTypePrefix() {
		return typePrefix;
	}
}
