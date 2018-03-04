package de.gotovoid.service.sensors;

/**
 * Created by DJ on 02/03/18.
 */

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import de.gotovoid.BuildConfig;
import de.gotovoid.service.LocationService;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Verify the functionality of the {@link SensorHandler}.
 * TODO: {@link SensorHandler.RecordingEntryObserver} that observer is called
 * TODO: verify start and stop recording
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SensorHandlerTest {
    private LocationService mService;
    private SensorHandler mSensorHandler;

    /**
     * Prepare the tests.
     *
     * @throws Exception
     */
    @Before
    public void before() throws Exception {
        mService = Robolectric.buildService(LocationService.class).get();
        mSensorHandler = new SensorHandler(mService.getApplication());
    }

    /**
     * Verify adding a {@link LocationSensor.Observer} works as expected.
     */
    @Test
    public void testAddLocationObserver() {
        LocationSensor.Observer observer = Mockito.mock(LocationSensor.Observer.class);
        assertThat(mSensorHandler.getLocationSensor().isStarted(), is(false));
        mSensorHandler.addObserver(observer);
        assertThat(mSensorHandler.getLocationSensor().isStarted(), is(true));
    }

    /**
     * Verify removing a {@link LocationSensor.Observer} works as expected.
     */
    @Test
    public void testRemoveLocationObserver() {
        LocationSensor.Observer observer = Mockito.mock(LocationSensor.Observer.class);
        mSensorHandler.addObserver(observer);
        assertThat(mSensorHandler.getLocationSensor().isStarted(), is(true));
        mSensorHandler.removeObserver(observer);
        assertThat(mSensorHandler.getLocationSensor().isStarted(), is(false));
    }

    /**
     * Verify adding a {@link PressureSensor.Observer} works as expected.
     */
    @Test
    public void testAddPressureObserver() {
        PressureSensor.Observer observer = Mockito.mock(PressureSensor.Observer.class);
        assertThat(mSensorHandler.getPressureSensor().isStarted(), is(false));
        mSensorHandler.addObserver(observer);
        assertThat(mSensorHandler.getPressureSensor().isStarted(), is(true));
    }

    /**
     * Verify removing a {@link PressureSensor.Observer} works as expected.
     */
    @Test
    public void testRemovePressureObserver() {
        PressureSensor.Observer observer = Mockito.mock(PressureSensor.Observer.class);
        mSensorHandler.addObserver(observer);
        assertThat(mSensorHandler.getPressureSensor().isStarted(), is(true));
        mSensorHandler.removeObserver(observer);
        assertThat(mSensorHandler.getPressureSensor().isStarted(), is(false));
    }

    /**
     * Verify adding a {@link RecordingSensor.Observer} works as expected.
     */
    @Test
    public void testAddRecordingObserver() {
        RecordingSensor.Observer observer = Mockito.mock(RecordingSensor.Observer.class);
        assertThat(mSensorHandler.getRecordingSensor().isStarted(), is(false));
        mSensorHandler.addObserver(observer);
        assertThat(mSensorHandler.getRecordingSensor().isStarted(), is(true));
    }

    /**
     * Verify removing a {@link RecordingSensor.Observer} works as expected.
     */
    @Test
    public void testRemoveRecordingObserver() {
        RecordingSensor.Observer observer = Mockito.mock(RecordingSensor.Observer.class);
        mSensorHandler.addObserver(observer);
        assertThat(mSensorHandler.getRecordingSensor().isStarted(), is(true));
        mSensorHandler.removeObserver(observer);
        assertThat(mSensorHandler.getRecordingSensor().isStarted(), is(false));
    }
}
