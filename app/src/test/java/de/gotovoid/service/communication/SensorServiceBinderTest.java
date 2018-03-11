package de.gotovoid.service.communication;

import android.app.Application;
import android.os.RemoteException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import de.gotovoid.BuildConfig;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
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

/**
 * Test that verifies the correct functionality of the {@link SensorServiceBinder}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SensorServiceBinderTest {
    private static final long RECORDING_ID = 12345;
    private SensorServiceBinder mBinder;
    private SensorHandler mSensorHandler;

    /**
     * Prepare the test cases.
     */
    @Before
    public void before() {
        mSensorHandler = Mockito.mock(SensorHandler.class);
        mBinder = new SensorServiceBinder(mSensorHandler);
    }

    /**
     * Verify that setting the paused state works.
     *
     * @throws RemoteException
     */
    @Test
    public void testSetUpdatePaused() throws RemoteException {
        assertThat(mBinder.isUpdatePaused(), is(false));
        mBinder.setUpdatePaused(true);
        assertThat(mBinder.isUpdatePaused(), is(true));
        mBinder.setUpdatePaused(false);
        assertThat(mBinder.isUpdatePaused(), is(false));
    }

    /**
     * Verify that starting a recording works.
     *
     * @throws RemoteException
     */
    @Test
    public void testStartRecording() throws RemoteException {
        mBinder.startRecording(RECORDING_ID);
        Mockito.verify(mSensorHandler, Mockito.times(1))
                .startRecording(RECORDING_ID);
    }

    /**
     * Verify that stopping a recording works
     *
     * @throws RemoteException
     */
    @Test
    public void testStopRecording() throws RemoteException {
        mBinder.stopRecording();
        Mockito.verify(mSensorHandler, Mockito.times(1))
                .stopRecording();
    }

    /**
     * Verify that starting a recording several times does not work.
     *
     * @throws RemoteException
     */
    @Test
    public void testStartRecordingSeveralTimes() throws RemoteException {
        mBinder.startRecording(RECORDING_ID);
        mBinder.startRecording(RECORDING_ID);
        mBinder.startRecording(RECORDING_ID);
        Mockito.verify(mSensorHandler, Mockito.times(1))
                .startRecording(RECORDING_ID);
    }

    /**
     * Internal parameterized test to verify the functionality for different {@link SensorType}s.
     */
    @RunWith(ParameterizedRobolectricTestRunner.class)
    @Config(constants = BuildConfig.class)
    public static class StartStopSensorTest {
        private static final long UPDATE_FREQUENCY = 1000;
        private SensorHandler mSensorHandler;
        private CallbackRegistration mCallbackRegistration;
        private ISensorServiceCallback mCallback;
        private SensorServiceBinder mBinder;

        private SensorType mSensorType;
        public Class mClazz;
        public Serializable mValue;

        /**
         * Constructor for the parameterized test.
         *
         * @param type  {@link SensorType}
         * @param clazz {@link Class} of the Sensor
         * @param value sensor value
         */
        public StartStopSensorTest(final SensorType type,
                                   final Class clazz,
                                   final Serializable value) {
            mSensorType = type;
            mClazz = clazz;
            mValue = value;
        }

        /**
         * Create the parameters for the test.
         *
         * @return the parameters
         */
        @ParameterizedRobolectricTestRunner.Parameters(name = "SensorType = {0}")
        public static Collection<Object[]> data() {
            final List<Object[]> data = new ArrayList<>();
            for (final SensorType type : SensorType.values()) {
                final Object[] parameters = new Object[3];
                parameters[0] = type;
                switch (type) {
                    case LOCATION:
                        parameters[1] = LocationSensor.Observer.class;
                        parameters[2] = new ExtendedGeoCoordinate(123d,
                                456d,
                                2f,
                                10f);
                        break;
                    case PRESSURE:
                        parameters[1] = PressureSensor.Observer.class;
                        parameters[2] = 1234f;
                        break;
                    case RECORDING:
                        parameters[1] = RecordingSensor.Observer.class;
                        parameters[2] = 1234l;
                        break;
                }
                data.add(parameters);
            }
            return data;
        }

        /**
         * Prepare the test cases.
         */
        @Before
        public void before() {
            mSensorHandler = Mockito.mock(SensorHandler.class);
            mBinder = new SensorServiceBinder(mSensorHandler);
            mCallback = Mockito.mock(ISensorServiceCallback.class);
            mCallbackRegistration = new CallbackRegistration(mSensorType,
                    mCallback, UPDATE_FREQUENCY);
        }

        /**
         * Verify that starting the sensor creates a new {@link SensorServiceBinder.Callback}
         *
         * @throws RemoteException
         */
        @Test
        public void testStartSensor() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            final SensorServiceBinder.Callback callback =
                    mBinder.getCallback(mCallbackRegistration);
            assertThat(callback.getCallback(), is(mCallback));
            assertThat(callback.getObserver(), IsInstanceOf.instanceOf(mClazz));
        }

        /**
         * Verify that starting the sensor registers an {@link AbstractSensor.Observer} at the
         * {@link SensorHandler}.
         *
         * @throws RemoteException
         */
        @Test
        public void testStartSensorOnHandler() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            Mockito.verify(mSensorHandler, Mockito.times(1))
                    .addObserver(Mockito.any(AbstractSensor.Observer.class));
        }

        /**
         * Verify that stopping the sensor removes the {@link SensorServiceBinder.Callback}.
         *
         * @throws RemoteException
         */
        @Test
        public void testStopSensor() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            mBinder.stopSensor(mCallbackRegistration);
            assertThat(mBinder.getCallback(mCallbackRegistration), is(nullValue()));
        }

        /**
         * Verify that stopping the sensor removes the {@link AbstractSensor.Observer} from the
         * {@link SensorHandler}.
         *
         * @throws RemoteException
         */
        @Test
        public void testStopSensorOnHandler() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            mBinder.stopSensor(mCallbackRegistration);
            Mockito.verify(mSensorHandler, Mockito.times(1))
                    .removeObserver(Mockito.any(AbstractSensor.Observer.class));
        }

        /**
         * Verify that notifying the {@link ISensorServiceCallback}s works as expected.
         *
         * @throws RemoteException
         */
        @Test
        public void testNotifyObservers() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            final SensorServiceBinder.Callback callback =
                    mBinder.getCallback(mCallbackRegistration);
            callback.getObserver().onChange(mValue);
            Mockito.verify(mCallback,
                    Mockito.times(1))
                    .onSensorValueChanged(Mockito.any(Response.class));
        }

        /**
         * Verify that {@link ISensorServiceCallback}s are not notified when
         * {@link SensorServiceBinder#isUpdatePaused()} was called with true.
         *
         * @throws RemoteException
         */
        @Test
        public void testSetUpdatePausedNoNotify() throws RemoteException {
            mBinder.startSensor(mCallbackRegistration, mCallback);
            mBinder.setUpdatePaused(true);
            final SensorServiceBinder.Callback callback =
                    mBinder.getCallback(mCallbackRegistration);
            callback.getObserver().onChange(mValue);
            Mockito.verify(mCallback,
                    Mockito.times(0))
                    .onSensorValueChanged(Mockito.any(Response.class));
        }

    }
}
