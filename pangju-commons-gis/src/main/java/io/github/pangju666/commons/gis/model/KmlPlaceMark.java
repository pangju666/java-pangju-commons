package io.github.pangju666.commons.gis.model;

import org.locationtech.jts.geom.Geometry;

public record KmlPlaceMark(String name, String description, Geometry geometry, boolean open) {
}
