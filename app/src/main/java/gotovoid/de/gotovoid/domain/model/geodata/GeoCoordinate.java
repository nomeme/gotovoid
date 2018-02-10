package gotovoid.de.gotovoid.domain.model.geodata;

import android.support.annotation.NonNull;

/**
 * Defines a {@link GeoCoordinate} consisting of longitude and latitude.
 * <p>
 * Created by DJ on 04/01/18.
 */

public class GeoCoordinate {
    /**
     * Radius of the earth in meters.
     */
    public static final double EARTH_RADIUS = 6372800; // In meters
    /**
     * Minimal latitudinal value.
     */
    public static final double LAT_MIN = -90;
    /**
     * Maximal latitudinal value.
     */
    public static final double LAT_MAX = 90;
    /**
     * Minimal longitudinal value.
     */
    public static final double LNG_MIN = -180;
    /**
     * Maximal longitudinal value.
     */
    public static final double LNG_MAX = 180;

    /**
     * Latitude.
     */
    private final double mLatitude;
    /**
     * Longitude.
     */
    private final double mLongitude;

    /**
     * Constructor taking longitude and latitude.
     *
     * @param latitude  latitude
     * @param longitude longitude
     */
    public GeoCoordinate(final double latitude, final double longitude) {
        if (latitude > LAT_MAX || latitude < LAT_MIN) {
            throw new IllegalArgumentException("Latitude value of [" + latitude
                    + "] not in range [" + LAT_MIN + "," + LAT_MAX + "]");
        }
        if (longitude > LNG_MAX || longitude < LNG_MIN) {
            throw new IllegalArgumentException("Longitude value of [" + longitude
                    + "] not in range[" + LNG_MIN + "," + LNG_MAX + "]");
        }
        mLatitude = latitude;
        mLongitude = longitude;
    }

    /**
     * Returns the latitude.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Returns the longitude.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Returns the Haversine distance between two {@link GeoCoordinate} instances in meters.
     * https://rosettacode.org/wiki/Haversine_formula
     *
     * @param other the other {@link GeoCoordinate}
     * @return Haversine distance in meters
     */
    public double getHaversineDistanceTo(@NonNull final GeoCoordinate other) {
        if (other == null) {
            return 0;
        }
        double distanceLat = Math.toRadians(other.mLatitude - mLatitude);
        double distanceLng = Math.toRadians(other.mLongitude - mLongitude);
        double lat1 = Math.toRadians(mLatitude);
        double lat2 = Math.toRadians(other.mLatitude);

        double a = Math.pow(Math.sin(distanceLat / 2), 2)
                + Math.pow(Math.sin(distanceLng / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS * c;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("{lat: ");
        builder.append(mLatitude);
        builder.append(", lng: ");
        builder.append(mLongitude);
        builder.append("}");
        return builder.toString();

    }
}
