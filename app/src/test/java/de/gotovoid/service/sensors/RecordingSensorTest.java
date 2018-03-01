package de.gotovoid.service.sensors;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import de.gotovoid.database.model.CalibratedAltitude;
import de.gotovoid.database.model.RecordingEntry;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;

/**
 * Created by DJ on 27/02/18.
 */

/**
 * Verify the functionality of the {@link RecordingSensor}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class RecordingSensorTest extends GenericSensorTest {
    private RecordingSensor mRecordingSensor;
    private LocationSensor mLocationSensor;
    private PressureSensor mPressureSensor;
    private AbstractSensor.Observer<RecordingEntry> mObserver;

    /**
     * Prepare the test.
     */
    @Before
    public void before() {
        PowerMockito.mockStatic(Log.class);
        mLocationSensor = Mockito.mock(LocationSensor.class);
        mPressureSensor = Mockito.mock(PressureSensor.class);
        mObserver = Mockito.mock(AbstractSensor.Observer.class);
        mRecordingSensor = new RecordingSensor(mPressureSensor, mLocationSensor, mObserver);
    }

    @Override
    protected RecordingSensor getSensor() {
        return mRecordingSensor;
    }

    /**
     * Verify the method {@link RecordingSensor#startSensor()} works as expected.
     */
    @Test
    public void testStartRecording() {
        getSensor().startRecording(0);
        Mockito.verify(mLocationSensor, Mockito.times(1))
                .addObserver(Mockito.any(AbstractSensor.Observer.class));
        Mockito.verify(mPressureSensor, Mockito.times(1))
                .addObserver(Mockito.any(AbstractSensor.Observer.class));
    }

    /**
     * Verify the method {@link RecordingSensor#stopSensor()} works as expected.
     */
    @Test
    public void stopRecording() {
        getSensor().stopRecording();
        Mockito.verify(mLocationSensor, Mockito.times(1))
                .removeObserver(Mockito.any(AbstractSensor.Observer.class));
        Mockito.verify(mPressureSensor, Mockito.times(1))
                .removeObserver(Mockito.any(AbstractSensor.Observer.class));
    }

    /**
     * Verifies that notifying the observers works.
     */
    @Test
    public void testNotify() {
        final RecordingSensor.Observer observer = Mockito.mock(RecordingSensor.Observer.class);
        final ExtendedGeoCoordinate coordinate = Mockito.mock(ExtendedGeoCoordinate.class);
        final CalibratedAltitude altitude = Mockito.mock(CalibratedAltitude.class);
        final long recordingId = 1337;
        getSensor().setCalibratedAltitude(altitude);
        getSensor().addObserver(observer);
        getSensor().startRecording(recordingId);
        getSensor().getPressureObserver().onChange(0f);
        Mockito.verify(observer, Mockito.times(0))
                .onChange(Mockito.anyLong());
        getSensor().getLocationObserver().onChange(coordinate);
        Mockito.verify(observer, Mockito.times(1))
                .onChange(recordingId);
    }

    /**
     * Verify that there are no updates when no altitude value is available.
     */
    @Test
    public void testNotifyNoAltitude() {
        final RecordingSensor.Observer observer = Mockito.mock(RecordingSensor.Observer.class);
        final ExtendedGeoCoordinate coordinate = Mockito.mock(ExtendedGeoCoordinate.class);
        getSensor().addObserver(observer);
        getSensor().startRecording(0);
        getSensor().getPressureObserver().onChange(0f);
        getSensor().getLocationObserver().onChange(coordinate);
        Mockito.verify(observer, Mockito.times(0))
                .onChange(Mockito.anyLong());
    }

    /**
     * Verify that there is no update when there is a null value provided.
     */
    @Test
    public void testNotifyNull() {
        final RecordingSensor.Observer observer = Mockito.mock(RecordingSensor.Observer.class);
        final CalibratedAltitude altitude = Mockito.mock(CalibratedAltitude.class);
        getSensor().setCalibratedAltitude(altitude);
        getSensor().addObserver(observer);
        getSensor().startRecording(0);
        getSensor().getPressureObserver().onChange(0f);
        getSensor().getLocationObserver().onChange(null);
        Mockito.verify(observer, Mockito.times(0))
                .onChange(Mockito.anyLong());
    }

}
