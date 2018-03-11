package de.gotovoid.service.sensors;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.LocationServices;

import de.gotovoid.database.model.Recording;
import de.gotovoid.database.AppDatabase;
import de.gotovoid.database.model.RecordingEntry;

/**
 * Created by DJ on 24/12/17.
 */

/**
 * Handler for {@link AbstractSensor} instances.
 * Manages the available {@link AbstractSensor} instances and provides methods to
 * register and unregister {@link AbstractSensor.Observer}s.
 */
public class SensorHandler {
    private static final String TAG = SensorHandler.class.getSimpleName();

    /**
     * Database instance to save recording data.
     */
    private final AppDatabase mDatabase;

    /**
     * Sensor for location data.
     */
    private final LocationSensor mLocationSensor;
    /**
     * Sensor for pressure data.
     */
    private final PressureSensor mPressureSensor;
    /**
     * Sensor for recording data.
     */
    private final RecordingSensor mRecordingSensor;
    /**
     * Observer to write recording data to database.
     */
    private final RecordingEntryObserver mRecordingEntryObserver;
    /**
     * {@link HandlerThread} for database interaction.
     */
    private final HandlerThread mHandlerThread;
    /**
     * {@link Handler} for database interaction.
     */
    private final Handler mHandler;

    /**
     * Constructor taking the {@link Application} as context.
     *
     * @param application the {@link Application}
     */
    public SensorHandler(final Application application) {
        mLocationSensor = new LocationSensor(LocationServices
                .getFusedLocationProviderClient(application));
        mPressureSensor = new PressureSensor(application);
        mRecordingEntryObserver = new RecordingEntryObserver();
        mRecordingSensor = new RecordingSensor(mPressureSensor,
                mLocationSensor,
                mRecordingEntryObserver);

        mHandlerThread = new HandlerThread("Looper");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mDatabase = AppDatabase.getDatabaseInstance(application);
    }

    /**
     * Returns the {@link LocationSensor}.
     *
     * @return the {@link LocationSensor}
     */
    protected LocationSensor getLocationSensor() {
        return mLocationSensor;
    }

    /**
     * Returns the {@link PressureSensor}.
     *
     * @return the {@link PressureSensor}
     */
    protected PressureSensor getPressureSensor() {
        return mPressureSensor;
    }

    /**
     * Returns the {@link RecordingSensor}.
     *
     * @return the {@link RecordingSensor}
     */
    protected RecordingSensor getRecordingSensor() {
        return mRecordingSensor;
    }

    /**
     * Start recording data for the {@link Recording} with
     * the given id.
     *
     * @param recordingId id of the {@link Recording}
     */
    public void startRecording(final long recordingId) {
        Log.d(TAG, "startRecording() called with: recordingId = [" + recordingId + "]");
        // TODO: remove
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: start recording");
                try {
                    // TODO: make updateable
                    mRecordingSensor.setCalibratedAltitude(
                            mDatabase.getCalibratedPressureDao().getCalibratedPressure());
                    mRecordingSensor.startRecording(recordingId);
                } catch (final IllegalStateException exception) {
                    Log.e(TAG, "run: start recording failed: ", exception);
                }
            }
        });
    }

    /**
     * Stop recording.
     */
    public void stopRecording() {
        mRecordingSensor.stopRecording();
    }

    /**
     * Add the given {@link AbstractSensor.Observer} for an {@link AbstractSensor}.
     *
     * @param observer the {@link AbstractSensor.Observer}
     */
    public void addObserver(@NonNull final AbstractSensor.Observer observer) {
        Log.d(TAG, "addObserver() called with: observer = [" + observer + "]");
        // TODO: maybe we should not use references to concrete implementations here
        switch (observer.getType()) {
            case PRESSURE:
                mPressureSensor.addObserver(observer);
                break;
            case LOCATION:
                mLocationSensor.addObserver(observer);
                break;
            case RECORDING:
                mRecordingSensor.addObserver(observer);
                break;
        }
    }

    /**
     * Remove the given {@link AbstractSensor.Observer}.
     *
     * @param observer {@link AbstractSensor.Observer} to be removed
     */
    public void removeObserver(@NonNull final AbstractSensor.Observer observer) {
        Log.d(TAG, "removeObserver() called with: observer = [" + observer + "]");
        // TODO: maybe we should not use references to concrete implementations here
        switch (observer.getType()) {
            case PRESSURE:
                mPressureSensor.removeObserver(observer);
                break;
            case LOCATION:
                mLocationSensor.removeObserver(observer);
                break;
            case RECORDING:
                mRecordingSensor.removeObserver(observer);
                break;
        }
    }

    /**
     * Returns true if currently recording.
     *
     * @return true if recording
     */
    public boolean isRecording() {
        return mRecordingSensor.isStarted();
    }

    /**
     * Stop all sensors.
     */
    public void stopSensors() {
        mHandlerThread.quitSafely();
    }

    /**
     * This {@link AbstractSensor.Observer} implementation takes care of storing
     * {@link Recording} data in the database.
     * TODO: maybe we can simplify this if we move it to the sensor
     */
    private class RecordingEntryObserver extends AbstractSensor.Observer<RecordingEntry> {
        /**
         * Constructor.
         */
        public RecordingEntryObserver() {
            super(0, SensorType.RECORDING);
        }

        @Override
        public void onChange(@NonNull final RecordingEntry recordingEntry) {
            Log.d(TAG, "onChange() called with: recordingEntry = [" + recordingEntry + "]");
            if (recordingEntry == null) {
                Log.e(TAG, "onChange: recordingEntry is null");
                return;
            } else {
                Log.d(TAG, "onChange: write data: " + recordingEntry.getRecordingId());
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: write data: ");
                    try {
                        mDatabase.getRecordingEntryDao().add(recordingEntry);
                    } catch (final IllegalStateException exception) {
                        Log.e(TAG, "run: save recording entry failed: ", exception);
                    }
                }
            });
        }
    }
}
