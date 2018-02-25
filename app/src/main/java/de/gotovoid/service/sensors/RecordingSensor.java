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
    private AbstractSensor.Observer<Float> mPressureObserver;
    /**
     * The {@link AbstractSensor.Observer} for the {@link LocationSensor}.
     */
    private AbstractSensor.Observer<ExtendedGeoCoordinate> mLocationObserver;
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
        mPressureObserver = new PressureObserver(1000);
        mLocationObserver = new LocationObserver(1000);
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
        mPressureObserver = null;
        mLocationObserver = null;
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

    /**
     * Observer for location data updates.
     * Will notify the {@link RecordingSensor} observer.
     */
    private class LocationObserver extends LocationSensor.Observer {
        /**
         * Constructor taking the update frequency.
         *
         * @param updateFrequency the update frequency
         */
        public LocationObserver(long updateFrequency) {
            super(updateFrequency);
        }

        @Override
        public void onChange(@NonNull final ExtendedGeoCoordinate extendedGeoCoordinate) {
            Log.d(TAG, "onChange() called with: extendedGeoCoordinate = [" + extendedGeoCoordinate + "]");
            if (mAltitude != null
                    && mRecordingEntryObserver != null
                    && mRecordingId != null) {
                // Create a new RecordingEntry
                final RecordingEntry entry =
                        new RecordingEntry(
                                mRecordingId,
                                System.currentTimeMillis(),
                                extendedGeoCoordinate.getLongitude(),
                                extendedGeoCoordinate.getLatitude(),
                                mAltitude);
                // Notify the RecordingEntry observer to store the data in the database
                mRecordingEntryObserver.onChange(entry);
                // Notify the registered observers with the recording id
                notifyObserver(mRecordingId);
            }
        }
    }

    /**
     * Observer for pressure data.
     * Will store the current pressure in a member variable
     */
    private class PressureObserver extends PressureSensor.Observer {
        /**
         * Constructor taking the update frequency.
         *
         * @param updateFrequency the update frequency
         */
        public PressureObserver(long updateFrequency) {
            super(updateFrequency);
        }

        @Override
        public void onChange(@NonNull final Float pressure) {
            Log.d(TAG, "onChange() called with: pressure = [" + pressure + "]");
            if (mCalibratedAltitude == null || pressure == null) {
                mAltitude = null;
                return;
            }
            mAltitude = mCalibratedAltitude.calculateHeight(pressure);
        }
    }

    /**
     * Observer for the {@link RecordingSensor}.
     * TODO: consider removing this in order to make it more generic
     */
    public abstract static class Observer extends AbstractSensor.Observer<Long> {
        /**
         * Constructor taking the update frequency.
         *
         * @param updateFrequency the update frequency
         */
        public Observer(final long updateFrequency) {
            super(updateFrequency, SensorType.RECORDING);
        }
    }
}
