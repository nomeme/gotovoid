package de.gotovoid.view.binding;

import android.support.annotation.NonNull;

import de.gotovoid.database.model.RecordingEntry;
import de.gotovoid.domain.model.geodata.GeoCoordinate;

/**
 * Created by DJ on 01/04/18.
 */

public class FlightInfoData {
    private static final long SECONDS_IN_HOUR = 3600;
    private static final long MILLIS_IN_SECOND = 1000, KILOMETERS_IN_METERS = 1000;
    final int mAscendingSpeed;
    final int mAltitude;
    final int mGroundSpeed;

    /**
     * Ascending speed is provided in meters per second, whereas the Speed
     *
     * @param ascendingSpeed the current ascending speed
     * @param groundSpeed    the current ground speed
     * @param altitude       the current altitude
     */
    public FlightInfoData(final int ascendingSpeed,
                          final int groundSpeed,
                          final int altitude) {
        mAscendingSpeed = ascendingSpeed;
        mGroundSpeed = groundSpeed;
        mAltitude = altitude;
    }

    /**
     * Creates a new {@link FlightInfoData} taking two {@link RecordingEntry}s.
     *
     * @param first  the first {@link RecordingEntry}
     * @param second the second {@link RecordingEntry}
     */
    public FlightInfoData(final @NonNull RecordingEntry first,
                          final @NonNull RecordingEntry second) {
        this((int) getVerticalSpeed(first, second),
                (int) getGroundSpeed(first, second),
                (int) second.getAltitude());
    }

    /**
     * Computes the ground speed from the two provided {@link RecordingEntry} objects.
     *
     * @param first  the first {@link RecordingEntry}
     * @param second the second {@link RecordingEntry}
     * @return the ground speed
     */
    private static float getGroundSpeed(final @NonNull RecordingEntry first,
                                        final @NonNull RecordingEntry second) {
        // TODO: change this to store m/s and use unit conversion.
        // We provide this in kilometers per hour.
        final long timeDiff = (second.getTimeStamp() - first.getTimeStamp()) / MILLIS_IN_SECOND;
        final double distance = getCoordinate(first)
                .getHaversineDistanceTo(getCoordinate(second));
        return (float) ((distance / timeDiff) * SECONDS_IN_HOUR / KILOMETERS_IN_METERS);

    }

    /**
     * Computes the vertical speed from two provided {@link RecordingEntry} objects.
     *
     * @param first  the first {@link RecordingEntry}
     * @param second the second {@link RecordingEntry}
     * @return the vertical speed
     */
    private static float getVerticalSpeed(final @NonNull RecordingEntry first,
                                          final @NonNull RecordingEntry second) {
        // We provide this in meters per second
        final long timeDiff = (second.getTimeStamp() - first.getTimeStamp()) / MILLIS_IN_SECOND;
        final double heightDiff = second.getAltitude() - first.getAltitude();
        return (float) (heightDiff / timeDiff);

    }

    /**
     * Returns a {@link GeoCoordinate} for the given {@link RecordingEntry}.
     *
     * @param entry the {@link RecordingEntry}
     * @return the {@link GeoCoordinate}
     */
    private static GeoCoordinate getCoordinate(final @NonNull RecordingEntry entry) {
        return new GeoCoordinate(entry.getLatitude(), entry.getLongitude());
    }

    /**
     * Returns the ascending speed.
     *
     * @return the ascending speed
     */
    public int getAscendingSpeed() {
        return mAscendingSpeed;
    }

    /**
     * Returns the altitude.
     *
     * @return the altitude
     */
    public int getAltitude() {
        return mAltitude;
    }

    /**
     * Returns the ground speed.
     *
     * @return the ground speed
     */
    public int getGroundSpeed() {
        return mGroundSpeed;
    }
}
