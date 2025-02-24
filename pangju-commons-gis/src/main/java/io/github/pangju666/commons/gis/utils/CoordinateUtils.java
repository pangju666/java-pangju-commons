package io.github.pangju666.commons.gis.utils;

import ch.obermuhlner.math.big.BigDecimalMath;
import io.github.pangju666.commons.gis.lang.Constants;
import io.github.pangju666.commons.gis.model.Coordinate;

import java.math.BigDecimal;
import java.math.MathContext;

public class CoordinateUtils {
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
		int degreeIndex = coordinate.indexOf(Constants.RADIUS_CHAR);
		BigDecimal degreeNumber = new BigDecimal(coordinate.substring(0, degreeIndex));

		int minuteIndex = coordinate.indexOf(Constants.MINUTE_CHAR, degreeIndex);
		BigDecimal minuteNumber = new BigDecimal(coordinate.substring(degreeIndex + 1, minuteIndex));

		int secondsIndex = coordinate.indexOf(Constants.SECONDS_CHAR, minuteIndex);
		BigDecimal secondsNumber = new BigDecimal(coordinate.substring(minuteIndex + 1, secondsIndex));

		return degreeNumber.add(minuteNumber.divide(SIXTY, MathContext.DECIMAL32))
			.add(secondsNumber.divide(THREE_THOUSAND_AND_SIX_HUNDRED, MathContext.DECIMAL32));
	}

	// 误差：约在 50-500 米
	public static Coordinate GCJ02ToWGS84(BigDecimal longitude, BigDecimal latitude) {
		if (isOutOfChina(longitude, latitude)) {
			return new Coordinate(longitude, latitude);
		} else {
			Coordinate deltaCoordinate = computeGcj02Delta(longitude, latitude);
			return new Coordinate(longitude.subtract(deltaCoordinate.longitude()),
				latitude.subtract(deltaCoordinate.latitude()));
		}
	}

	public static Coordinate WGS84ToGCJ02(BigDecimal longitude, BigDecimal latitude) {
		if (isOutOfChina(longitude, latitude)) {
			return new Coordinate(longitude, latitude);
		} else {
			Coordinate deltaCoordinate = computeGcj02Delta(longitude, latitude);
			return new Coordinate(longitude.add(deltaCoordinate.longitude()),
				latitude.add(deltaCoordinate.latitude()));
		}
	}

	public static boolean isOutOfChina(BigDecimal longitude, BigDecimal latitude) {
		return !(longitude.compareTo(Constants.CHINA_MIN_LONGITUDE) > 0 &&
			longitude.compareTo(Constants.CHINA_MAX_LONGITUDE) < 0 &&
			latitude.compareTo(Constants.CHINA_MIN_LATITUDE) > 0 &&
			latitude.compareTo(Constants.CHINA_MAX_LATITUDE) < 0);
	}

	// 计算偏移量
	protected static Coordinate computeGcj02Delta(BigDecimal longitude, BigDecimal latitude) {
		// latitude / 180.0 * PI
		BigDecimal radiusLatitude = latitude.divide(ONE_HUNDRED_AND_EIGHTY, MathContext.DECIMAL32)
			.multiply(Constants.PI);
		// 1 - EE * (sin(radiusLatitude))²
		BigDecimal magic = BigDecimal.ONE.subtract(Constants.EE.multiply(BigDecimalMath.sin(
			radiusLatitude, MathContext.DECIMAL32).pow(2)));
		// sqrt(magic)
		BigDecimal sqrtMagic = magic.sqrt(MathContext.DECIMAL32);

		// (transformLat(longitude - 105.0, latitude - 35.0) * 180.0) / ((A * (1 - EE)) / (magic * sqrtMagic) * PI)
		BigDecimal deltaLatitude = transformLatitude(longitude.subtract(ONE_HUNDRED_AND_FIVE),
			latitude.subtract(THIRTY_FIVE)).multiply(ONE_HUNDRED_AND_EIGHTY)
			.divide(Constants.A.multiply(BigDecimal.ONE.subtract(Constants.EE))
				.divide(magic.multiply(sqrtMagic), MathContext.DECIMAL32)
				.multiply(Constants.PI), MathContext.DECIMAL32);

		// (transformLat(longitude - 105.0, latitude - 35.0) * 180.0) / (A / sqrtMagic * Math.cos(radLat) * PI)
		BigDecimal deltaLongitude = transformLongitude(longitude.subtract(ONE_HUNDRED_AND_FIVE),
			latitude.subtract(THIRTY_FIVE)).multiply(ONE_HUNDRED_AND_EIGHTY)
			.divide(Constants.A.divide(sqrtMagic, MathContext.DECIMAL32)
				.multiply(BigDecimalMath.cos(radiusLatitude, MathContext.DECIMAL32))
				.multiply(Constants.PI), MathContext.DECIMAL32);

		return new Coordinate(deltaLongitude, deltaLatitude);
	}

	protected static BigDecimal transformLongitude(BigDecimal longitude, BigDecimal latitude) {
		return longitude
			// + 300
			.add(THREE_HUNDRED)
			// + (2.0 * latitude)
			.add(latitude.multiply(TWO))
			// + (0.1 * longitude * longitude)
			.add(longitude.pow(2).multiply(ZERO_ONE))
			// + (0.1 * longitude * latitude)
			.add(longitude.multiply(ZERO_ONE).multiply(latitude))
			// + (0.1 * Math.sqrt(Math.abs(longitude)))
			.add(longitude.abs().sqrt(MathContext.DECIMAL32).multiply(ZERO_ONE))
			// ((20.0 * Math.sin(6.0 * longitude * PI) + 20.0 * Math.sin(2.0 * longitude * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath
				.sin(longitude.multiply(SIX).multiply(Constants.PI),
					MathContext.DECIMAL32)
				.multiply(TWENTY)
				.add(BigDecimalMath.sin(longitude.multiply(TWO).multiply(Constants.PI),
					MathContext.DECIMAL32).multiply(TWENTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32))
			// ((20.0 * Math.sin(longitude * PI) + 40.0 * Math.sin((lng / 3.0) * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath
				.sin(longitude.multiply(Constants.PI), MathContext.DECIMAL32)
				.multiply(TWENTY)
				.add(BigDecimalMath
					.sin(longitude.divide(THREE, MathContext.DECIMAL32)
						.multiply(Constants.PI), MathContext.DECIMAL32)
					.multiply(FORTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32))
			// ((150.0 * Math.sin((lng / 12.0) * PI) + // 300.0 * Math.sin((lng / 30.0) * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath
				.sin(longitude.divide(TWELVE, MathContext.DECIMAL32).multiply(Constants.PI),
					MathContext.DECIMAL32)
				.multiply(ONE_HUNDRED_AND_FIFTY)
				.add(BigDecimalMath
					.sin(longitude.divide(THIRTY, MathContext.DECIMAL32).multiply(Constants.PI),
						MathContext.DECIMAL32)
					.multiply(THREE_HUNDRED))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32));
	}

	protected static BigDecimal transformLatitude(BigDecimal longitude, BigDecimal latitude) {
		return longitude
			// * 2.0
			.multiply(TWO)
			// -100
			.add(NEGATE_ONE_HUNDRED)
			// + (latitude * 3.0)
			.add(latitude.multiply(THREE))
			// + (0.2 * latitude * latitude)
			.add(latitude.pow(2).multiply(ZERO_TWO))
			// + (0.1 * longitude * latitude)
			.add(longitude.multiply(ZERO_ONE).multiply(latitude))
			// + (0.2 * Math.sqrt(Math.abs(longitude)))
			.add(longitude.abs().sqrt(MathContext.DECIMAL32).multiply(ZERO_TWO))
			// + ((20.0 * Math.sin(6.0 * longitude * PI) + 20.0 * Math.sin(2.0 * longitude * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath
				.sin(longitude.multiply(SIX).multiply(Constants.PI),
					MathContext.DECIMAL32)
				.multiply(TWENTY)
				.add(BigDecimalMath.sin(longitude.multiply(TWO).multiply(Constants.PI),
						MathContext.DECIMAL32)
					.multiply(TWENTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32))
			// + ((20.0 * Math.sin(latitude * PI) + 40.0 * Math.sin(latitude / 3.0) * PI)) * 2.0 / 3.0)
			.add(BigDecimalMath.sin(latitude.multiply(Constants.PI), MathContext.DECIMAL32)
				.multiply(TWENTY)
				.add(BigDecimalMath
					.sin(latitude.divide(THREE, MathContext.DECIMAL32)
						.multiply(Constants.PI), MathContext.DECIMAL32)
					.multiply(FORTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32))
			// ((160.0 * Math.sin((latitude / 12.0 * PI) + 320 * Math.sin(latitude * PI / 30.0)) * 2.0 / 3.0)
			.add(BigDecimalMath.sin(latitude.divide(TWELVE, MathContext.DECIMAL32)
						.multiply(Constants.PI),
					MathContext.DECIMAL32).multiply(ONE_HUNDRED_AND_SIXTY)
				.add(BigDecimalMath.sin(latitude.multiply(Constants.PI)
						.divide(THIRTY, MathContext.DECIMAL32),
					MathContext.DECIMAL32).multiply(THREE_HUNDRED_AND_TWENTY))
				.multiply(TWO)
				.divide(THREE, MathContext.DECIMAL32));
	}
}
