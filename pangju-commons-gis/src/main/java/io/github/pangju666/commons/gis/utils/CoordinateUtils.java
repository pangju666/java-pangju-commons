package io.github.pangju666.commons.gis.utils;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.pangju666.commons.gis.model.Coordinate;

import java.math.BigDecimal;
import java.math.MathContext;

public class CoordinateUtils {
	protected static final char RADIUS_CHAR = '°';
	protected static final char MINUTE_CHAR = '′';
	protected static final char SECONDS_CHAR = '″';

	protected static final BigDecimal CHINA_MIN_LATITUDE = BigDecimal.valueOf(3.86);
	protected static final BigDecimal CHINA_MAX_LATITUDE = BigDecimal.valueOf(53.55);
	protected static final BigDecimal CHINA_MIN_LONGITUDE = BigDecimal.valueOf(73.66);
	protected static final BigDecimal CHINA_MAX_LONGITUDE = BigDecimal.valueOf(135.05);

	protected static final BigDecimal PI = BigDecimal.valueOf(3.1415926535897932384626);
	protected static final BigDecimal EE = BigDecimal.valueOf(0.00669342162296594323);
	protected static final BigDecimal RADIUS = BigDecimal.valueOf(6378245.0);

	protected static final BigDecimal ZERO_ONE = BigDecimal.valueOf(0.1);
	protected static final BigDecimal ZERO_TWO = BigDecimal.valueOf(0.2);
	protected static final BigDecimal TWO = BigDecimal.valueOf(2);
	protected static final BigDecimal THREE = BigDecimal.valueOf(3);
	protected static final BigDecimal SIX = BigDecimal.valueOf(6);
	protected static final BigDecimal TWELVE = BigDecimal.valueOf(12);
	protected static final BigDecimal TWENTY = BigDecimal.valueOf(20);
	protected static final BigDecimal THIRTY = BigDecimal.valueOf(30);
	protected static final BigDecimal THIRTY_FIVE = BigDecimal.valueOf(35);
	protected static final BigDecimal FORTY = BigDecimal.valueOf(40);
	protected static final BigDecimal SIXTY = BigDecimal.valueOf(60);
	protected static final BigDecimal NEGATE_ONE_HUNDRED = BigDecimal.valueOf(-100);
	protected static final BigDecimal ONE_HUNDRED_AND_FIVE = BigDecimal.valueOf(105);
	protected static final BigDecimal ONE_HUNDRED_AND_FIFTY = BigDecimal.valueOf(150);
	protected static final BigDecimal ONE_HUNDRED_AND_SIXTY = BigDecimal.valueOf(160);
	protected static final BigDecimal ONE_HUNDRED_AND_EIGHTY = BigDecimal.valueOf(180);
	protected static final BigDecimal THREE_HUNDRED = BigDecimal.valueOf(300);
	protected static final BigDecimal THREE_HUNDRED_AND_TWENTY = BigDecimal.valueOf(320);
	protected static final BigDecimal THREE_THOUSAND_AND_SIX_HUNDRED = BigDecimal.valueOf(3600);

	protected CoordinateUtils() {
	}

	public static BigDecimal parseCoordinate(String coordinate) {
		int degreeIndex = coordinate.indexOf(RADIUS_CHAR);
		BigDecimal degreeNumber = new BigDecimal(coordinate.substring(0, degreeIndex));

		int minuteIndex = coordinate.indexOf(MINUTE_CHAR, degreeIndex);
		BigDecimal minuteNumber = new BigDecimal(coordinate.substring(degreeIndex + 1, minuteIndex));

		int secondsIndex = coordinate.indexOf(SECONDS_CHAR, minuteIndex);
		BigDecimal secondsNumber = new BigDecimal(coordinate.substring(minuteIndex + 1, secondsIndex));

		return degreeNumber.add(minuteNumber.divide(SIXTY, MathContext.DECIMAL32))
			.add(secondsNumber.divide(THREE_THOUSAND_AND_SIX_HUNDRED, MathContext.DECIMAL32));
	}

	public static Coordinate WGS84ToGCJ02(BigDecimal longitude, BigDecimal latitude) {
		if (isOutOfChina(longitude, latitude)) {
			return new Coordinate(longitude, latitude);
		} else {
			Coordinate delta = deltaCoordinate(longitude, latitude);
			return new Coordinate(longitude.add(delta.longitude()), latitude.add(delta.latitude()));
		}
	}

	public static Coordinate GCJ02ToWGS84(BigDecimal longitude, BigDecimal latitude) {
		if (isOutOfChina(longitude, latitude)) {
			return new Coordinate(longitude, latitude);
		} else {
			Coordinate delta = deltaCoordinate(longitude, latitude);
			BigDecimal magicLongitude = longitude.add(delta.longitude());
			BigDecimal magicLatitude = latitude.add(delta.latitude());
			return new Coordinate(longitude.multiply(TWO).subtract(magicLongitude), latitude.multiply(TWO).subtract(magicLatitude));
		}
	}

	public static boolean isOutOfChina(BigDecimal longitude, BigDecimal latitude) {
		latitude = latitude.abs();
		longitude = longitude.abs();
		return !(longitude.compareTo(CHINA_MIN_LONGITUDE) > 0 &&
			longitude.compareTo(CHINA_MAX_LONGITUDE) < 0 &&
			latitude.compareTo(CHINA_MIN_LATITUDE) > 0 &&
			latitude.compareTo(CHINA_MAX_LATITUDE) < 0);
	}

	protected static Coordinate deltaCoordinate(BigDecimal longitude, BigDecimal latitude) {
		BigDecimal radiusLatitude = latitude.divide(ONE_HUNDRED_AND_EIGHTY, MathContext.DECIMAL32).multiply(PI); // lat / 180 * PI
		BigDecimal magic = BigDecimalMath.sin(radiusLatitude, MathContext.DECIMAL32); // Math.sin(radiusLat)
		magic = BigDecimal.ONE.subtract(magic.multiply(magic).multiply(EE)); // 1 - EE * magic * magic
		BigDecimal sqrtMagic = magic.sqrt(MathContext.DECIMAL32); // Math.sqrt(magic)

		BigDecimal deltaLongitude = transformLongitude(longitude.subtract(ONE_HUNDRED_AND_FIVE), latitude.subtract(THIRTY_FIVE));
		deltaLongitude = deltaLongitude.multiply(ONE_HUNDRED_AND_EIGHTY) // newLng * 180
			.divide(RADIUS.divide(sqrtMagic, MathContext.DECIMAL32) // / ((RADIUS / sqrtMagic)
				.multiply(BigDecimalMath.cos(radiusLatitude, MathContext.DECIMAL32).multiply(PI)), MathContext.DECIMAL32); // * Math.cos(radiusLat) * PI)

		BigDecimal deltaLatitude = transformLatitude(longitude.subtract(ONE_HUNDRED_AND_FIVE), latitude.subtract(THIRTY_FIVE));
		deltaLatitude = deltaLatitude.multiply(ONE_HUNDRED_AND_EIGHTY) // newLat * 180
			.divide(RADIUS.multiply(BigDecimal.ONE.subtract(EE)) // / (((RADIUS * (1 - EE))
				.divide(magic.multiply(sqrtMagic), MathContext.DECIMAL32) // / (magic * sqrtMagic))
				.multiply(PI), MathContext.DECIMAL32); // * PI)

		return new Coordinate(deltaLongitude, deltaLatitude);
	}

	protected static BigDecimal transformLongitude(BigDecimal longitude, BigDecimal latitude) {
		return longitude.add(THREE_HUNDRED) // 300.0 + lng
			.add(latitude.multiply(TWO)) // 2.0 * lat
			.add(longitude.multiply(ZERO_ONE).multiply(longitude)) // 0.1 * lng * lng
			.add(longitude.multiply(ZERO_ONE).multiply(latitude)) // 0.1 * lng * lat
			.add(longitude.abs().sqrt(MathContext.DECIMAL32).multiply(ZERO_ONE)) // 0.1 * Math.sqrt(Math.abs(lng))
			.add(BigDecimalMath.sin(longitude.multiply(SIX).multiply(PI), MathContext.DECIMAL32).multiply(TWENTY) // 20.0 * Math.sin(6.0 * lng * PI)
				.add(BigDecimalMath.sin(longitude.multiply(TWO).multiply(PI), MathContext.DECIMAL32).multiply(TWENTY)) // 20.0 * Math.sin(2.0 * lng * PI)
				.multiply(TWO) // * 2.0
				.divide(THREE, MathContext.DECIMAL32)) // / 3.0
			.add(BigDecimalMath.sin(longitude.multiply(PI), MathContext.DECIMAL32).multiply(TWENTY) // 20.0 * Math.sin(lng * PI)
				.add(BigDecimalMath.sin(longitude.divide(THREE, MathContext.DECIMAL32).multiply(PI), MathContext.DECIMAL32).multiply(FORTY)) // 40.0 * Math.sin((lng / 3.0) * PI)
				.multiply(TWO) // * 2.0
				.divide(THREE, MathContext.DECIMAL32)) // / 3.0
			.add(BigDecimalMath.sin(longitude.divide(TWELVE, MathContext.DECIMAL32).multiply(PI), MathContext.DECIMAL32).multiply(ONE_HUNDRED_AND_FIFTY) // 150.0 * Math.sin((lng / 12.0) * PI)
				.add(BigDecimalMath.sin(longitude.divide(THIRTY, MathContext.DECIMAL32).multiply(PI), MathContext.DECIMAL32).multiply(THREE_HUNDRED)) // 300.0 * Math.sin((lng / 30.0) * PI)
				.multiply(TWO) // * 2.0
				.divide(THREE, MathContext.DECIMAL32)); // / 3.0
	}

	protected static BigDecimal transformLatitude(BigDecimal longitude, BigDecimal latitude) {
		return longitude.multiply(TWO) // lng * 2.0
			.add(NEGATE_ONE_HUNDRED) // -100
			.add(latitude.multiply(THREE)) // lag * 3.0
			.add(latitude.multiply(ZERO_TWO).multiply(latitude)) // 0.2 * lat * lat
			.add(longitude.multiply(ZERO_ONE).multiply(latitude)) // 0.1 * lng * lat
			.add(longitude.abs().sqrt(MathContext.DECIMAL32).multiply(ZERO_TWO)) // 0.2 * Math.sqrt(Math.abs(lng))
			.add(BigDecimalMath.sin(longitude.multiply(SIX).multiply(PI), MathContext.DECIMAL32).multiply(TWENTY) // 20.0 * Math.sin(6.0 * lng * PI)
				.add(BigDecimalMath.sin(longitude.multiply(TWO).multiply(PI), MathContext.DECIMAL32).multiply(TWENTY)) // 20.0 * Math.sin(2.0 * lng * PI)
				.multiply(TWO) // * 2.0
				.divide(THREE, MathContext.DECIMAL32)) // / 3.0
			.add(BigDecimalMath.sin(latitude.multiply(PI), MathContext.DECIMAL32).multiply(TWENTY) // 20.0 * Math.sin(lat * PI)
				.add(BigDecimalMath.sin(latitude.divide(THREE, MathContext.DECIMAL32).multiply(PI), MathContext.DECIMAL32).multiply(FORTY)) // 40.0 * Math.sin((lat / 3.0) * PI))
				.multiply(TWO) // * 2.0
				.divide(THREE, MathContext.DECIMAL32)) // / 3.0
			.add(BigDecimalMath.sin(latitude.divide(TWELVE, MathContext.DECIMAL32).multiply(PI), MathContext.DECIMAL32).multiply(ONE_HUNDRED_AND_SIXTY) // 160.0 * Math.sin((lat / 12.0) * PI)
				.add(BigDecimalMath.sin(latitude.multiply(PI).divide(THIRTY, MathContext.DECIMAL32), MathContext.DECIMAL32).multiply(THREE_HUNDRED_AND_TWENTY)) // 320 * Math.sin((lat * PI) / 30.0)
				.multiply(TWO) // * 2.0
				.divide(THREE, MathContext.DECIMAL32)); // / 3.0
	}
}
