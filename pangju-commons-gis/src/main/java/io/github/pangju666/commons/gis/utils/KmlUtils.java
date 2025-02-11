package io.github.pangju666.commons.gis.utils;

import io.github.pangju666.commons.gis.model.KmlPlaceMark;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.kml.v22.KMLConfiguration;
import org.geotools.xsd.PullParser;
import org.locationtech.jts.geom.Geometry;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class KmlUtils {
	protected KmlUtils() {
	}

	public static List<KmlPlaceMark> parseKml(final String content) throws XMLStreamException, IOException,
		SAXException {
		List<KmlPlaceMark> kmlPlaceMarks = new ArrayList<>();
		parseKml(new ByteArrayInputStream(content.getBytes()), kmlPlaceMarks::add);
		return kmlPlaceMarks;
	}

	public static List<KmlPlaceMark> parseKml(final byte[] bytes) throws XMLStreamException, IOException, SAXException {
		List<KmlPlaceMark> kmlPlaceMarks = new ArrayList<>();
		parseKml(new ByteArrayInputStream(bytes), kmlPlaceMarks::add);
		return kmlPlaceMarks;
	}

	public static List<KmlPlaceMark> parseKml(final InputStream inputStream) throws XMLStreamException, IOException,
		SAXException {
		List<KmlPlaceMark> kmlPlaceMarks = new ArrayList<>();
		parseKml(inputStream, kmlPlaceMarks::add);
		return kmlPlaceMarks;
	}

	public static void parseKml(final String content, final Consumer<KmlPlaceMark> consumer) throws IOException,
		XMLStreamException, SAXException {
		parseKml(new ByteArrayInputStream(content.getBytes()), consumer);
	}

	public static void parseKml(final byte[] bytes, final Consumer<KmlPlaceMark> consumer) throws IOException,
		XMLStreamException, SAXException {
		parseKml(new ByteArrayInputStream(bytes), consumer);
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
}
