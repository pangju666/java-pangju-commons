package io.github.pangju666.commons.gis.model;

import org.locationtech.jts.geom.Geometry;

import java.util.Map;

public record GeoJsonFeature(String type, Geometry geometry, Map<String, Object> properties) {
}
