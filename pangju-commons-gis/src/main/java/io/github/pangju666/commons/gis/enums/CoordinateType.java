package io.github.pangju666.commons.gis.enums;

import io.github.pangju666.commons.gis.model.Coordinate;
import io.github.pangju666.commons.gis.utils.CoordinateUtils;

import java.math.BigDecimal;

public enum CoordinateType {
	GCJ_02,
	WGS_84;

	public Coordinate toGCJ02(final Double longitude, final Double latitude) {
		return toGCJ02(new Coordinate(
			BigDecimal.valueOf(longitude),
			BigDecimal.valueOf(latitude)
		));
	}

	public Coordinate toGCJ02(final BigDecimal longitude, final BigDecimal latitude) {
		return toGCJ02(new Coordinate(longitude, latitude));
	}

	public Coordinate toGCJ02(final Coordinate location) {
		return switch (this) {
			case GCJ_02 -> location;
			case WGS_84 -> CoordinateUtils.WGS84ToGCJ02(location.longitude(), location.latitude());
		};
	}

	public Coordinate toWGS84(final Double longitude, final Double latitude) {
		return toWGS84(new Coordinate(
			BigDecimal.valueOf(longitude),
			BigDecimal.valueOf(latitude)
		));
	}

	public Coordinate toWGS84(final BigDecimal longitude, final BigDecimal latitude) {
		return toWGS84(new Coordinate(longitude, latitude));
	}

	public Coordinate toWGS84(final Coordinate location) {
		return switch (this) {
			case GCJ_02 -> CoordinateUtils.GCJ02ToWGS84(location.longitude(), location.latitude());
			case WGS_84 -> location;
		};
	}
}
