package de.gotovoid.service.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventCallback;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;

/**
 * Created by DJ on 07/01/18.
 */

/**
 * Concrete implementation of the {@link AbstractSensor} for air pressure.
 * Serves as wrapper for the actual {@link Sensor} provided by the system.
 */
public class PressureSensor extends AbstractSensor<Float> {
    private static final String TAG = PressureSensor.class.getSimpleName();
    /**
     * {@link SensorManager} of the system to addObserver for pressure sensor updates.
     */
    private final SensorManager mSensorManager;
    /**
     * The actual system {@link Sensor}.
     */
    private final Sensor mPressureSensor;
    /**
     * The time stamp of the last update.
     */
    @Deprecated
    private long mTimeStamp;
    /**
     * Observer for {@link Sensor} events.
     */
    private final SensorEventCallback mSensorCallback = new SensorEventCallback() {
        @Override
        public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
            super.onAccuracyChanged(sensor, accuracy);
            Log.d(TAG, "onAccuracyChanged() called with: sensor = ["
                    + sensor + "], accuracy = [" + accuracy + "]");
        }

        @Override
        public void onSensorChanged(final SensorEvent event) {
            if (event == null || event.values == null || event.values.length == 0) {
                return;
            }
            final float val = event.values[0];
            long millis = System.currentTimeMillis();
            if (millis - mTimeStamp > getUpdateFrequency()) {
                Log.d(TAG, "onSensorChanged: update: " + val);
                notifyObserver(val);
                mTimeStamp = millis;
            }
        }
    };

    /**
     * Constructor taking the {@link Context} to get the {@link Sensor}s from.
     *
     * @param context the {@link Context}
     */
    PressureSensor(@NonNull final Context context) {
        super(new StateEvaluator(4, .1));
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mPressureSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
    }

    @Override
    protected void startSensor() {
        if (mPressureSensor != null && mSensorManager != null) {
            mSensorManager.registerListener(mSensorCallback,
                    mPressureSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void stopSensor() {
        if (mPressureSensor != null && mSensorManager != null) {
            mSensorManager.unregisterListener(mSensorCallback);
        }
    }

    @Override
    protected void restartSensor() {
        stopSensor();
        startSensor();
    }

    /**
     * Return the {@link SensorEventCallback} instance.
     *
     * @return the {@link SensorEventCallback}
     */
    protected SensorEventCallback getSensorCallback() {
        return mSensorCallback;
    }

    private static class StateEvaluator extends AbstractSensor.StateEvaluator<Float> {

        public StateEvaluator(int bufferSize, double tolerance) {
            super(bufferSize, tolerance);
        }

        @Override
        protected double computeDifference(final Float first, final Float second) {
            return Math.abs(first - second);
        }
    }
}
