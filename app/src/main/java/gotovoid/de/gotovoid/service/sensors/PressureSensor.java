package gotovoid.de.gotovoid.service.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventCallback;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Created by DJ on 07/01/18.
 */

public class PressureSensor extends AbstractSensor<Float> {
    private static final String TAG = PressureSensor.class.getSimpleName();
    private final SensorManager mSensorManager;
    private final Sensor mPressureSensor;
    private final SensorEventCallback mSensorCallback = new SensorEventCallback() {
        @Override
        public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
            super.onAccuracyChanged(sensor, accuracy);
            Log.d(TAG, "onAccuracyChanged() called with: sensor = ["
                    + sensor + "], accuracy = [" + accuracy + "]");
        }

        @Override
        public void onSensorChanged(final SensorEvent event) {
            final float val = event.values[0];
            long millis = System.currentTimeMillis();
            mPressure = mPressure * 4 + val;
            mPressure = mPressure / 5;
            if (millis - mTimeStamp > getUpdateFrequency()) {
                Log.d(TAG, "onSensorChanged: update: " + mPressure);
                notifyObserver(mPressure);
                mTimeStamp = millis;
            }
        }
    };

    private long mTimeStamp;
    private float mPressure;

    PressureSensor(@NonNull final Context context) {
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
}
