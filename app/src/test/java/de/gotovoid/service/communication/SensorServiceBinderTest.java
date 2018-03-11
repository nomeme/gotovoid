package de.gotovoid.service.communication;

import android.os.RemoteException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import de.gotovoid.BuildConfig;
import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.LocationSensor;
import de.gotovoid.service.sensors.PressureSensor;
import de.gotovoid.service.sensors.RecordingSensor;
import de.gotovoid.service.sensors.SensorHandler;
import de.gotovoid.service.sensors.SensorType;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import org.hamcrest.core.IsInstanceOf;

/**
 * Created by DJ on 04/03/18.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SensorServiceBinderTest {
    private static final long RECORDING_ID = 12345;
    private SensorServiceBinder mBinder;
    private SensorHandler mSensorHandler;

    @Before
    public void before() {
        mSensorHandler = Mockito.mock(SensorHandler.class);
        mBinder = new SensorServiceBinder(mSensorHandler);
    }

    @Test
    public void testSetUpdatePaused() throws RemoteException {
        assertThat(mBinder.isUpdatePaused(), is(false));
        mBinder.setUpdatePaused(true);
        assertThat(mBinder.isUpdatePaused(), is(true));
        mBinder.setUpdatePaused(false);
        assertThat(mBinder.isUpdatePaused(), is(false));
    }

    @Test
    public void testSetUpdatePausedNoNotify() {
        // TODO: implement!
    }

    @Test
    public void testStartRecording() throws RemoteException {
        mBinder.startRecording(RECORDING_ID);
        Mockito.verify(mSensorHandler, Mockito.times(1))
                .startRecording(RECORDING_ID);
    }

    @Test
    public void testStopRecording() throws RemoteException {
        mBinder.stopRecording();
        Mockito.verify(mSensorHandler, Mockito.times(1))
                .stopRecording();
    }

    @RunWith(ParameterizedRobolectricTestRunner.class)
    @Config(constants = BuildConfig.class)
    public static class StartStopSensorTest {
        private static final long UPDATE_FREQUENCY = 1000;
        private static final int CALLBACK_ID = 12345;
        private SensorHandler mSensorHandler;
        private CallbackRegistration mCallbackRegistration;
        private ISensorServiceCallback mCallback;
        private SensorServiceBinder mBinder;

        private SensorType mSensorType;
        public Class mClazz;

        public StartStopSensorTest(final SensorType type, final Class clazz) {
            mSensorType = type;
            mClazz = clazz;
        }

        @ParameterizedRobolectricTestRunner.Parameters(name = "SensorType = {0}")
        public static Collection<Object[]> data() {
            final List<Object[]> data = new ArrayList<>();
            for (final SensorType type : SensorType.values()) {
                final Object[] parameters = new Object[2];
                parameters[0] = type;
                switch (type) {
                    case LOCATION:
                        parameters[1] = LocationSensor.Observer.class;
                        break;
                    case PRESSURE:
                        parameters[1] = PressureSensor.Observer.class;
                        break;
                    case RECORDING:
                        parameters[1] = RecordingSensor.Observer.class;
                        break;
                }
                data.add(parameters);
            }
            return data;
        }

        @Before
        public void before() {
            mSensorHandler = Mockito.mock(SensorHandler.class);
            mBinder = new SensorServiceBinder(mSensorHandler);
            mCallback = Mockito.mock(ISensorServiceCallback.class);
            mCallbackRegistration = new CallbackRegistration(mSensorType,
                    mCallback, UPDATE_FREQUENCY);
        }

        @Test
        public void testStartSensor() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            final SensorServiceBinder.Callback callback =
                    mBinder.getCallback(mCallbackRegistration);
            assertThat(callback.getCallback(), is(mCallback));
            assertThat(callback.getObserver(), IsInstanceOf.instanceOf(mClazz));
        }

        @Test
        public void testStartSensorOnHandler() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            Mockito.verify(mSensorHandler, Mockito.times(1))
                    .addObserver(Mockito.any(AbstractSensor.Observer.class));
        }

        @Test
        public void testStopSensor() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            mBinder.stopSensor(mCallbackRegistration);
            assertThat(mBinder.getCallback(mCallbackRegistration), is(nullValue()));
        }

        @Test
        public void testStopSensorOnHandler() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            mBinder.stopSensor(mCallbackRegistration);
            Mockito.verify(mSensorHandler, Mockito.times(1))
                    .removeObserver(Mockito.any(AbstractSensor.Observer.class));
        }

    }
}
