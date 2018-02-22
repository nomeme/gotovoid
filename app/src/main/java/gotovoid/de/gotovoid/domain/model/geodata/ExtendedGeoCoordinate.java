package gotovoid.de.gotovoid.domain.model.geodata;

import java.io.Serializable;

/**
 * Created by DJ on 17/02/18.
 */

/**
 * This class serves as data holder object for location information.
 */
public class ExtendedGeoCoordinate implements Serializable {
    /**
     * The latitude.
     */
    private final double mLatitude;
    /**
     * The longitude.
     */
    private final double mLongitude;
    /**
     * The altitude.
     */
    private final double mAltitude;
    /**
     * The accuracy of the location information.
     */
    private final float mAccuracy;

    /**
     * Constructor taking longitude, latitude, altitude and accuracy of the location information.
     *
     * @param latitude  latitude
     * @param longitude longitude
     * @param altitude  the altitude
     * @param accuracy  the accuracy
     */
    public ExtendedGeoCoordinate(final double latitude,
                                 final double longitude,
                                 final double altitude,
                                 final float accuracy) {
        mLatitude = latitude;
        mLongitude = longitude;
        mAltitude = altitude;
        mAccuracy = accuracy;
    }

    /**
     * Returns the latitude.
     *
     * @return the latitude.
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Returns the altitude.
     *
     * @return the altitude
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Returns the accuracy.
     *
     * @return the accuracy
     */
    public float getAccuracy() {
        return mAccuracy;
    }

    /**
     * Returns the altitude.
     *
     * @return the altitude
     */
    public double getAltitude() {
        return mAltitude;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("{latitude: ");
        builder.append(getLatitude());
        builder.append(", longitude: ");
        builder.append(getLongitude());
        builder.append(", altitude: ");
        builder.append(getAltitude());
        builder.append(", accuracy: ");
        builder.append(getAccuracy());
        builder.append('}');
        return builder.toString();
    }
}
