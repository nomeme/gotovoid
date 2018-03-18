package de.gotovoid.service.sensors;

import android.support.annotation.NonNull;
import android.util.Log;


import de.gotovoid.database.model.CalibratedAltitude;
import de.gotovoid.database.model.Recording;
import de.gotovoid.database.model.RecordingEntry;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;

/**
 * Created by DJ on 20/01/18.
 */

/**
 * {@link AbstractSensor} implementation for recording data.
 * This is a wrapper to combine both, the {@link LocationSensor} and {@link PressureSensor}
 * data into one result.
 */
public class RecordingSensor extends AbstractSensor<Long> {
    private static final String TAG = RecordingSensor.class.getSimpleName();
    /**
     * The {@link AbstractSensor.Observer} for the {@link PressureSensor}.
     */
    private PressureObserver mPressureObserver;
    /**
     * The {@link AbstractSensor.Observer} for the {@link LocationSensor}.
     */
    private LocationObserver mLocationObserver;
    /**
     * The {@link PressureSensor} providing altitude data.
     */
    private final PressureSensor mPressureSensor;
    /**
     * The {@link LocationSensor} providing the location data.
     */
    private final LocationSensor mLocationSensor;
    /**
     * Observer for {@link RecordingEntry} updates.
     */
    private final AbstractSensor.Observer<RecordingEntry> mRecordingEntryObserver;

    /**
     * The {@link CalibratedAltitude} data to calculate the altitude from air pressure.
     */
    private CalibratedAltitude mCalibratedAltitude;
    /**
     * The current altitude.
     */
    private Integer mAltitude;

    /**
     * The id of the {@link Recording} the sensor data is for.
     */
    private Long mRecordingId;

    /**
     * Constructor taking the {@link PressureSensor} and {@link LocationSensor} instance,
     * as well as the {@link AbstractSensor.Observer} for {@link RecordingEntry} changes.
     * TODO: handle RecordingEntry database input locally and not via listener!
     *
     * @param pressureSensor the {@link PressureSensor}
     * @param locationSensor the {@link LocationSensor}
     * @param observer       the {@link AbstractSensor.Observer} for recording data
     */
    RecordingSensor(final PressureSensor pressureSensor,
                    final LocationSensor locationSensor,
                    final AbstractSensor.Observer<RecordingEntry> observer) {
        super(new StateEvaluator(5, 5));
        mRecordingEntryObserver = observer;
        mPressureSensor = pressureSensor;
        mLocationSensor = locationSensor;
    }

    /**
     * Set the {@link CalibratedAltitude} so the altitude can be calculated from
     * {@link PressureSensor} data.
     *
     * @param calibratedAltitude the {@link CalibratedAltitude}
     */
    public void setCalibratedAltitude(final CalibratedAltitude calibratedAltitude) {
        mCalibratedAltitude = calibratedAltitude;
    }

    /**
     * Start recording data for the {@link Recording}
     * with the given id.
     *
     * @param recordingId the id of the recording
     */
    public void startRecording(final long recordingId) {
        Log.d(TAG, "startRecording() called with: recordingId = [" + recordingId + "]");
        mRecordingId = recordingId;
        mPressureObserver = new PressureObserver(getUpdateFrequency());
        mLocationObserver = new LocationObserver(getUpdateFrequency());
        mPressureSensor.addObserver(mPressureObserver);
        mLocationSensor.addObserver(mLocationObserver);
    }

    /**
     * Stop the recording.
     */
    public void stopRecording() {
        Log.d(TAG, "stopRecording() called");
        mRecordingId = null;
        mPressureSensor.removeObserver(mPressureObserver);
        mLocationSensor.removeObserver(mLocationObserver);
    }

    @Override
    protected void startSensor() {
        // Do nothing
    }

    @Override
    protected void stopSensor() {
        // Do nothing
    }

    @Override
    protected void restartSensor() {
        // Do nothing
    }

    protected PressureObserver getPressureObserver() {
        return mPressureObserver;
    }

    protected LocationObserver getLocationObserver() {
        return mLocationObserver;
    }

    /**
     * Observer for location data updates.
     * Will notify the {@link RecordingSensor} observer.
     */
    protected class LocationObserver extends AbstractSensor.Observer<ExtendedGeoCoordinate> {
        /**
         * Constructor taking the update frequency.
         *
         * @param updateFrequency the update frequency
         */
        public LocationObserver(long updateFrequency) {
            super(updateFrequency, SensorType.LOCATION);
        }

        @Override
        public void onChange(@NonNull final Result<ExtendedGeoCoordinate> extendedGeoCoordinate) {
            Log.d(TAG, "onChange() called with: extendedGeoCoordinate = [" + extendedGeoCoordinate + "]");
            if (extendedGeoCoordinate != null
                    && mAltitude != null
                    && mRecordingEntryObserver != null
                    && mRecordingId != null) {
                // Create a new RecordingEntry
                final RecordingEntry entry =
                        new RecordingEntry(
                                mRecordingId,
                                System.currentTimeMillis(),
                                extendedGeoCoordinate.getValue().getLongitude(),
                                extendedGeoCoordinate.getValue().getLatitude(),
                                mAltitude);
                // Notify the RecordingEntry observer to store the data in the database
                mRecordingEntryObserver.onChange(
                        new Result<>(SensorState.RUNNING, entry));
                // Notify the registered observers with the recording id
                notifyObserver(mRecordingId);
            }
        }
    }

    /**
     * Observer for pressure data.
     * Will store the current pressure in a member variable
     */
    protected class PressureObserver extends AbstractSensor.Observer<Float> {
        /**
         * Constructor taking the update frequency.
         *
         * @param updateFrequency the update frequency
         */
        public PressureObserver(long updateFrequency) {
            super(updateFrequency, SensorType.PRESSURE);
        }

        @Override
        public void onChange(@NonNull final Result<Float> result) {
            Log.d(TAG, "onChange() called with: pressure = [" + result + "]");
            if (mCalibratedAltitude == null || result == null) {
                mAltitude = null;
                return;
            }
            mAltitude = mCalibratedAltitude.calculateHeight(result.getValue());
        }
    }

    private static class StateEvaluator
            extends AbstractSensor.StateEvaluator<Long> {

        public StateEvaluator(final int bufferSize, final double tolerance) {
            super(bufferSize, tolerance);
        }

        @Override
        protected double computeDifference(final Long first,
                                           final Long second) {
            return 0;
        }
    }
}
