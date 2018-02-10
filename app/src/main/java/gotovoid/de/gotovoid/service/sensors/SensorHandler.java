package gotovoid.de.gotovoid.service.sensors;

import android.app.Application;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import gotovoid.de.gotovoid.database.AppDatabase;
import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.database.model.RecordingEntry;
import gotovoid.de.gotovoid.service.communication.ServiceMessageHandler;

/**
 * Created by DJ on 24/12/17.
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

    private final RecordingSensor mRecordingSensor;
    private final RecordingEntryObserver mRecordingEntryObserver;

    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    private long mUpdateInterval = 5000;

    /**
     * Constructor taking the {@link Application} as context.
     *
     * @param application the {@link Application}
     */
    public SensorHandler(final Application application) {
        mLocationSensor = new LocationSensor(application);
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

    public void startRecording(final long recordingId) {
        Log.d(TAG, "startRecording() called with: recordingId = [" + recordingId + "]");
        // TODO: remove
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                // TODO: make updateable
                Log.d(TAG, "run: start recording");
                try {
                    mRecordingSensor.setCalibratedAltitude(
                            mDatabase.getCalibratedPressureDao().getCalibratedPressure());
                    mRecordingSensor.startRecording(recordingId);
                } catch (final IllegalStateException exception) {
                    Log.e(TAG, "run: start recording failed: ", exception);
                }

            }
        });
    }

    public void stopRecording() {
        // TODO: remove
        mRecordingSensor.stopRecording();
    }

    public void addObserver(@NonNull final AbstractSensor.Observer observer) {
        if (observer instanceof PressureObserver) {
            mPressureSensor.addObserver(observer);
        }
        if (observer instanceof LocationObserver) {
            mLocationSensor.addObserver(observer);
        }
        if (observer instanceof RecordingObserver) {
            mRecordingSensor.addObserver(observer);
        }
    }

    public void removeObserver(@NonNull final AbstractSensor.Observer observer) {
        if (observer instanceof PressureObserver) {
            mPressureSensor.removeObserver(observer);
        }
        if (observer instanceof LocationObserver) {
            mLocationSensor.addObserver(observer);
        }
        if (observer instanceof RecordingObserver) {
            mRecordingSensor.removeObserver(observer);
        }
    }

    public void setUpdateInterval(final long updateInterval) {
        // TODO: restart sensor listeners
        mUpdateInterval = updateInterval;
    }

    public boolean isRecording() {
        return mRecordingSensor.isStarted();
    }

    public void stopSensors() {
        mHandlerThread.quitSafely();
    }

    public interface LocationObserver extends AbstractSensor.Observer<Location> {
    }

    public interface PressureObserver extends AbstractSensor.Observer<Float> {
    }

    public interface RecordingObserver extends AbstractSensor.Observer<Long> {
    }

    private class RecordingEntryObserver implements AbstractSensor.Observer<RecordingEntry> {
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
