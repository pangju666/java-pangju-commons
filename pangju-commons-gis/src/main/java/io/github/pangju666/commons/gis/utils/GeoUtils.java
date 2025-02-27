package io.github.pangju666.commons.gis.utils;

import io.github.pangju666.commons.gis.model.GeoJsonFeature;
import io.github.pangju666.commons.gis.model.KmlPlaceMark;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.UnsynchronizedByteArrayInputStream;
import org.geotools.api.feature.Property;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.feature.FeatureIterator;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.xsd.PullParser;
import org.locationtech.jts.geom.Geometry;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GeoUtils {
	protected GeoUtils() {
	}

	public static List<GeoJsonFeature> parseGeoJson(final String content) throws IOException {
		List<GeoJsonFeature> geoJsonFeatures = new ArrayList<>();
		parseGeoJson(buildByteArrayInputStream(content.getBytes()), geoJsonFeatures::add);
		return geoJsonFeatures;
	}

	public static List<GeoJsonFeature> parseGeoJson(final byte[] bytes) throws IOException {
		List<GeoJsonFeature> geoJsonFeatures = new ArrayList<>();
		parseGeoJson(buildByteArrayInputStream(bytes), geoJsonFeatures::add);
		return geoJsonFeatures;
	}

	public static List<GeoJsonFeature> parseGeoJson(final File file) throws IOException {
		try (InputStream inputStream = FileUtils.openInputStream(file)) {
			return parseGeoJson(inputStream);
		}
	}

	public static List<GeoJsonFeature> parseGeoJson(final InputStream inputStream) throws IOException {
		List<GeoJsonFeature> geoJsonFeatures = new ArrayList<>();
		parseGeoJson(inputStream, geoJsonFeatures::add);
		return geoJsonFeatures;
	}

	public static void parseGeoJson(final String content, final Consumer<GeoJsonFeature> consumer) throws IOException {
		parseGeoJson(buildByteArrayInputStream(content.getBytes()), consumer);
	}

	public static void parseGeoJson(final byte[] bytes, final Consumer<GeoJsonFeature> consumer) throws IOException {
		parseGeoJson(buildByteArrayInputStream(bytes), consumer);
	}

	public static void parseGeoJson(final File file, final Consumer<GeoJsonFeature> consumer) throws IOException {
		try (InputStream inputStream = FileUtils.openInputStream(file)) {
			parseGeoJson(inputStream, consumer);
		}
	}

	public static void parseGeoJson(final InputStream inputStream, final Consumer<GeoJsonFeature> consumer) throws IOException {
		FeatureJSON json = new FeatureJSON();
		try (FeatureIterator<SimpleFeature> iterator = json.streamFeatureCollection(inputStream)) {
			while (iterator.hasNext()) {
				SimpleFeature simpleFeature = iterator.next();
				Geometry geometry = (Geometry) simpleFeature.getDefaultGeometry();
				if (Objects.nonNull(geometry)) {
					Map<String, Object> properties = simpleFeature.getProperties()
						.stream()
						.filter(property -> !(property.getValue() instanceof Geometry))
						.collect(Collectors.toMap(property -> property.getName().toString(),
							Property::getValue));
					GeoJsonFeature jsonFeature = new GeoJsonFeature(simpleFeature.getType().getTypeName(),
						geometry, properties);
					consumer.accept(jsonFeature);
				}
			}
		}
	}

	public static List<KmlPlaceMark> parseKml(final String content) throws XMLStreamException, IOException,
		SAXException {
		List<KmlPlaceMark> kmlPlaceMarks = new ArrayList<>();
		parseKml(buildByteArrayInputStream(content.getBytes()), kmlPlaceMarks::add);
		return kmlPlaceMarks;
	}

	public static List<KmlPlaceMark> parseKml(final byte[] bytes) throws XMLStreamException, IOException, SAXException {
		List<KmlPlaceMark> kmlPlaceMarks = new ArrayList<>();
		parseKml(buildByteArrayInputStream(bytes), kmlPlaceMarks::add);
		return kmlPlaceMarks;
	}

	public static List<KmlPlaceMark> parseKml(final File file) throws IOException, XMLStreamException, SAXException {
		try (InputStream inputStream = FileUtils.openInputStream(file)) {
			return parseKml(inputStream);
		}
	}

	public static List<KmlPlaceMark> parseKml(final InputStream inputStream) throws XMLStreamException, IOException,
		SAXException {
		List<KmlPlaceMark> kmlPlaceMarks = new ArrayList<>();
		parseKml(inputStream, kmlPlaceMarks::add);
		return kmlPlaceMarks;
	}

	public static void parseKml(final String content, final Consumer<KmlPlaceMark> consumer) throws IOException,
		XMLStreamException, SAXException {
		parseKml(buildByteArrayInputStream(content.getBytes()), consumer);
	}

	public static void parseKml(final byte[] bytes, final Consumer<KmlPlaceMark> consumer) throws IOException,
		XMLStreamException, SAXException {
		parseKml(buildByteArrayInputStream(bytes), consumer);
	}

	public static void parseKml(final File file, final Consumer<KmlPlaceMark> consumer) throws IOException,
		XMLStreamException, SAXException {
		try (InputStream inputStream = FileUtils.openInputStream(file)) {
			parseKml(inputStream, consumer);
		}
	}

	public static void parseKml(final InputStream inputStream, final Consumer<KmlPlaceMark> consumer) throws IOException,
		XMLStreamException, SAXException {
		PullParser parser = new PullParser(new KMLConfiguration(), inputStream, SimpleFeature.class);
		SimpleFeature simpleFeature = (SimpleFeature) parser.parse();
		while (Objects.nonNull(simpleFeature)) {
			Geometry geometry = (Geometry) simpleFeature.getDefaultGeometry();
			if (Objects.nonNull(geometry)) {
				KmlPlaceMark kmlPlaceMark = new KmlPlaceMark(
					(String) simpleFeature.getProperty("name").getValue(),
					(String) simpleFeature.getProperty("description").getValue(),
					geometry,
					(boolean) simpleFeature.getProperty("open").getValue()
				);
				consumer.accept(kmlPlaceMark);
			}
			simpleFeature = (SimpleFeature) parser.parse();
		}
	}

	protected static UnsynchronizedByteArrayInputStream buildByteArrayInputStream(byte[] bytes) throws IOException {
		return UnsynchronizedByteArrayInputStream.builder()
			.setByteArray(bytes)
			.setOffset(0)
			.setLength(bytes.length)
			.get();
	}
}
