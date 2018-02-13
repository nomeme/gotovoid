package gotovoid.de.gotovoid.service.sensors;

import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;


import gotovoid.de.gotovoid.database.model.CalibratedAltitude;
import gotovoid.de.gotovoid.database.model.RecordingEntry;

/**
 * Created by DJ on 20/01/18.
 */

class RecordingSensor extends AbstractSensor<Long> {
    private static final String TAG = RecordingSensor.class.getSimpleName();
    private final AbstractSensor.Observer<Float> mPressureObserver;
    private final AbstractSensor.Observer<Location> mLocationObserver;
    private final PressureSensor mPressureSensor;
    private final LocationSensor mLocationSensor;
    private final RecordingSensor.Observer<RecordingEntry> mRecordingEntryObserver;

    private CalibratedAltitude mCalibratedAltitude;
    private Integer mAltitude;

    private Long mRecordingId;

    RecordingSensor(final PressureSensor pressureSensor,
                    final LocationSensor locationSensor,
                    final Observer<RecordingEntry> observer) {
        mRecordingEntryObserver = observer;
        mLocationObserver = new AbstractSensor.Observer<Location>() {
            @Override
            public void onChange(@NonNull final Location location) {
                Log.d(TAG, "onChange() called with: location = [" + location + "]");
                // TODO: use pressure
                Log.d(TAG, "onChange: altitude: " + mAltitude);
                Log.d(TAG, "onChange: observer: " + mRecordingEntryObserver);
                if (mAltitude != null
                        && mRecordingEntryObserver != null
                        && mRecordingId != null) {
                    Log.d(TAG, "onChange: 1");
                    final RecordingEntry entry =
                            new RecordingEntry(
                                    mRecordingId,
                                    System.currentTimeMillis(),
                                    location.getLongitude(),
                                    location.getLatitude(),
                                    mAltitude);
                    Log.d(TAG, "onChange: 2");
                    mRecordingEntryObserver.onChange(entry);
                    notifyObserver(mRecordingId);
                }
            }
        };
        mPressureObserver = new AbstractSensor.Observer<Float>() {
            @Override
            public void onChange(@NonNull final Float pressure) {
                Log.d(TAG, "onChange() called with: aFloat = [" + pressure + "]");
                Log.d(TAG, "onChange: calibrated: " + mCalibratedAltitude);
                if (mCalibratedAltitude == null || pressure == null) {
                    mAltitude = null;
                    return;
                }
                mAltitude = mCalibratedAltitude.calculateHeight(pressure);
            }
        };
        mPressureSensor = pressureSensor;
        mLocationSensor = locationSensor;
    }

    public void setCalibratedAltitude(final CalibratedAltitude calibratedAltitude) {
        mCalibratedAltitude = calibratedAltitude;
    }

    public void startRecording(final long recordingId) {
        Log.d(TAG, "startRecording() called with: recordingId = [" + recordingId + "]");
        mRecordingId = recordingId;
        mPressureSensor.addObserver(mPressureObserver);
        mLocationSensor.addObserver(mLocationObserver);
    }

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
        stopSensor();
        startSensor();
    }
}
