package io.github.pangju666.commons.poi.lang;

import java.util.Set;

public class PoiConstants {
	public static final String DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
	public static final String DOC_MIME_TYPE = "application/msword";

	public static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public static final String XLS_MIME_TYPE = "application/vnd.ms-excel";

	public static final String PPTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
	public static final String PPT_MIME_TYPE = "application/vnd.ms-powerpoint";

	public static final Set<String> POI_TL_PICTURE_TYPES = Set.of(
		"image/svg+xml",
		"image/svg",
		"image/bmp",
		"image/x-bmp",
		"image/tiff",
		"image/x-tiff",
		"image/gif",
		"image/png",
		"image/jpeg",
		"image/x-pict",
		"image/x-wmf",
		"application/x-emf"
	);
}
