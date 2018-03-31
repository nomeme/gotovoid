package de.gotovoid.service.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventCallback;
import android.hardware.SensorManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by DJ on 25/02/18.
 */

/**
 * Verify the functionality of the {@link PressureSensor}.
 */
public class PressureSensorTest extends GenericSensorTest {
    private PressureSensor mPressureSensor;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    /**
     * Prepare the test run.
     */
    @Before
    public void before() {
        final Context context = Mockito.mock(Context.class);
        mSensorManager = Mockito.mock(SensorManager.class);
        mSensor = Mockito.mock(Sensor.class);
        Mockito.when(context.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mSensorManager);
        Mockito.when(mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)).thenReturn(mSensor);
        mPressureSensor = new PressureSensor(context);
    }

    /**
     * Returns the {@link PressureSensor} under test.
     *
     * @return the {@link PressureSensor} under test.
     */
    @Override
    protected PressureSensor getSensor() {
        return mPressureSensor;
    }

    @Override
    protected Serializable getData() {
        return 0f;
    }

    /**
     * Verifies that starting the {@link PressureSensor} causes the {@link PressureSensor} to
     * actually addObserver for hardware {@link Sensor} updates.
     */
    @Test
    public void startSensor() {
        getSensor().startSensor();
        Mockito.verify(mSensorManager, Mockito.times(1))
                .registerListener(Mockito.any(SensorEventCallback.class),
                        Mockito.eq(mSensor),
                        Mockito.eq(SensorManager.SENSOR_DELAY_NORMAL));
    }

    /**
     * Verifies that stopping the {@link PressureSensor} causes the {@link PressureSensor} to
     * actually unregister for hardware {@link Sensor} updates.
     */
    @Test
    public void stopSensor() {
        getSensor().stopSensor();
        Mockito.verify(mSensorManager, Mockito.times(1))
                .unregisterListener(Mockito.any(SensorEventCallback.class));
    }

    /**
     * Return a {@link SensorEvent} with the given value.
     *
     * @param value value to set
     * @return the {@link SensorEvent}
     */
    private static SensorEvent createSensorEvent(final Float value) {
        final SensorEvent sensorEvent = Mockito.mock(SensorEvent.class);
        try {
            final Field valuesField = SensorEvent.class.getField("values");
            valuesField.setAccessible(true);
            float[] sensorValue;
            if (value == null) {
                sensorValue = new float[]{};
            } else {
                sensorValue = new float[]{value};
            }
            try {
                valuesField.set(sensorEvent, sensorValue);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return sensorEvent;
    }

    /**
     * Verifies that notifying the {@link PressureSensor} works as expected.
     */
    @Test
    public void testNotify() {
        final PressureSensor.Observer observer = Mockito.mock(PressureSensor.Observer.class);
        final SensorEvent event = createSensorEvent(0f);
        getSensor().addObserver(observer);
        getSensor().getSensorCallback().onSensorChanged(event);
        Mockito.verify(observer, Mockito.times(1))
                .onChange(Mockito.any(AbstractSensor.Result.class));
    }

    /**
     * Verifies that notifying the {@link PressureSensor} with null does not cause
     * an update or {@link NullPointerException}.
     */
    @Test
    public void testNotfiyNull() {
        final PressureSensor.Observer observer = Mockito.mock(PressureSensor.Observer.class);
        getSensor().addObserver(observer);
        getSensor().getSensorCallback().onSensorChanged(null);
        Mockito.verify(observer, Mockito.times(0))
                .onChange(Mockito.any(AbstractSensor.Result.class));
    }

    /**
     * Verifies that notifying the {@link PressureSensor} with an empty {@link SensorEvent}
     * does not cause an update or {@link NullPointerException}.
     */
    @Test
    public void testNotifyEmpty() {
        final PressureSensor.Observer observer = Mockito.mock(PressureSensor.Observer.class);
        final SensorEvent event = createSensorEvent(null);
        getSensor().addObserver(observer);
        getSensor().getSensorCallback().onSensorChanged(event);
        Mockito.verify(observer, Mockito.times(0))
                .onChange(Mockito.any(AbstractSensor.Result.class));
    }
}
