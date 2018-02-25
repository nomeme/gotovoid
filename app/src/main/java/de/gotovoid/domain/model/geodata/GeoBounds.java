package de.gotovoid.domain.model.geodata;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import de.gotovoid.domain.model.units.DistanceUnit;
import de.gotovoid.domain.model.units.UnitValue;

/**
 * Class containing bounds of the {@link GeoCoordinate}s.
 * Takes a {@link List} of {@link GeoCoordinate}s and computes the bounds of the given
 * {@link GeoCoordinate}s.
 * <p>
 * Created by DJ on 04/01/18.
 */

public class GeoBounds {
    private static final String TAG = GeoBounds.class.getSimpleName();
    /**
     * The southmost latitudinal value.
     */
    private final double mLatMin;
    /**
     * The northmost latitudinal value.
     */
    private final double mLatMax;
    /**
     * The westmost longitudinal value.
     */
    private final double mLngMin;
    /**
     * The eastmost latitudinal value.
     */
    private final double mLngMax;

    /**
     * Constructor taking a {@link List} of {@link GeoCoordinate}s to determine their bounds.
     *
     * @param coordinates {@link List} of {@link GeoCoordinate}s
     */
    public GeoBounds(@NonNull final List<GeoCoordinate> coordinates) {
        double latMin = GeoCoordinate.LAT_MAX;
        double latMax = GeoCoordinate.LAT_MIN;
        double lngMin = GeoCoordinate.LNG_MAX;
        double lngMax = GeoCoordinate.LAT_MIN;
        if (coordinates != null) {
            for (final GeoCoordinate coordinate : coordinates) {
                final double latitude = coordinate.getLatitude();
                final double longitude = coordinate.getLongitude();
                if (latitude > latMax) {
                    latMax = latitude;
                }
                if (latitude < latMin) {
                    latMin = latitude;
                }
                if (longitude > lngMax) {
                    lngMax = longitude;
                }
                if (longitude < lngMin) {
                    lngMin = longitude;
                }
            }
        }
        mLatMin = latMin;
        mLatMax = latMax;
        mLngMin = lngMin;
        mLngMax = lngMax;
        Log.d(TAG, "GeoBounds: latMin: " + latMin + ", latMax: " + latMax
                + ", lngMin: " + lngMin + ", lngMax: " + lngMax);
    }

    /**
     * Returns the north west {@link GeoCoordinate} of the {@link GeoBounds}.
     *
     * @return the north west {@link GeoCoordinate}
     */
    @NonNull
    public GeoCoordinate getNorthWest() {
        return new GeoCoordinate(mLatMax, mLngMin);
    }

    /**
     * Returns the north east {@link GeoCoordinate} of the {@link GeoBounds}.
     *
     * @return the north east {@link GeoCoordinate}
     */
    @NonNull
    public GeoCoordinate getNorthEast() {
        return new GeoCoordinate(mLatMax, mLngMax);
    }

    /**
     * Returns the south west {@link GeoCoordinate} of the {@link GeoBounds}.
     *
     * @return the south west {@link GeoCoordinate}
     */
    @NonNull
    public GeoCoordinate getSouthWest() {
        return new GeoCoordinate(mLatMin, mLngMin);
    }

    /**
     * Returns the south east {@link GeoCoordinate} of the {@link GeoBounds}.
     *
     * @return the south east {@link GeoCoordinate}
     */
    @NonNull
    public GeoCoordinate getSouthEast() {
        return new GeoCoordinate(mLatMin, mLngMax);
    }

    /**
     * Returns the latitudinal distance in degrees.
     *
     * @return the latitudinal distance
     */
    public double getLatDegreesDistance() {
        return mLatMax - mLatMin;
    }

    /**
     * Returns the longitudinal distance in degrees.
     *
     * @return the longitudinal distance
     */
    public double getLngDegreesDistance() {
        return mLngMax - mLngMin;
    }

    /**
     * Returns the latitudinal distance in meters computed using the Haversine function.
     *
     * @return latitudinal distance in meters
     * @see GeoCoordinate#getHaversineDistanceTo(GeoCoordinate)
     */
    @NonNull
    public UnitValue<DistanceUnit> getLatHaversineDistance() {
        return new UnitValue<>(getSouthEast().getHaversineDistanceTo(getNorthEast()),
                DistanceUnit.METERS);
    }

    /**
     * Returns the longitudinal distance in meters computed using the Haversine function.
     *
     * @return longitudinal distance in meters
     * @see GeoCoordinate#getHaversineDistanceTo(GeoCoordinate)
     */
    @NonNull
    public UnitValue<DistanceUnit> getLngHaversineDistance() {
        return new UnitValue<>(getSouthEast().getHaversineDistanceTo(getSouthWest()),
                DistanceUnit.METERS);
    }

    /**
     * Returns true if the given {@link GeoCoordinate} is within the {@link GeoBounds}.
     *
     * @param coordinate {@link GeoCoordinate} to check
     * @return true if within bounds
     */
    public boolean isInBounds(@NonNull final GeoCoordinate coordinate) {
        final double longitude = coordinate.getLongitude();
        final double latitude = coordinate.getLatitude();
        return latitude > mLatMin && latitude < mLatMax
                && longitude > mLngMin && longitude < mLngMax;
    }

    /**
     * Returns true if the given {@link GeoBounds} are of equal dimension regarding the given
     * tolerance in degrees.
     *
     * @param bounds           {@link GeoBounds} to compare to
     * @param toleranceDegrees tolerance in degrees
     * @return true if equal
     */
    public boolean isEqualDimension(@NonNull final GeoBounds bounds,
                                    final double toleranceDegrees) {
        final double latDiff = bounds.getLatDegreesDistance() - bounds.getLatDegreesDistance();
        final double lngDiff = bounds.getLngDegreesDistance() - bounds.getLngDegreesDistance();
        return latDiff < toleranceDegrees && lngDiff < toleranceDegrees;
    }

    /**
     * Returns true if the given {@link GeoCoordinate} is within the bounds of this
     * {@link GeoBounds} object.
     *
     * @param coordinate {@link GeoCoordinate} to check bounds for
     * @return true if within bounds
     */
    @NonNull
    public RelativePosition getPositionInBounds(@NonNull final GeoCoordinate coordinate) {
        return new RelativePosition(coordinate);
    }

    /**
     * Defines the relative position of a {@link GeoCoordinate} within the {@link GeoBounds}.
     */
    public class RelativePosition {
        final double mLatitude;
        final double mLongitude;

        /**
         * Constructor taking the {@link GeoCoordinate}.
         *
         * @param coordinate {@link GeoCoordinate}
         */
        private RelativePosition(final GeoCoordinate coordinate) {
            this(coordinate.getLatitude(), coordinate.getLongitude());
        }

        /**
         * Constructor taking the longitude and latitude.
         *
         * @param latitude  the latitude
         * @param longitude the longitude
         */
        private RelativePosition(final double latitude, final double longitude) {
            mLatitude = (latitude - mLatMin) / getLatDegreesDistance();
            mLongitude = (longitude - mLngMin) / getLngDegreesDistance();
        }

        /**
         * Returns the relative longitude.
         *
         * @return the relative longitude
         */
        public double getLongitude() {
            return mLongitude;
        }

        /**
         * Returns the relative latitude.
         *
         * @return the relative latitude
         */
        public double getLatitude() {
            return mLatitude;
        }
    }
}
