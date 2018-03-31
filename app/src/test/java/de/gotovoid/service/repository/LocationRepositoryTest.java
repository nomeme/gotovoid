package de.gotovoid.service.repository;

import android.app.Activity;
import android.arch.lifecycle.LiveData;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import de.gotovoid.database.model.Recording;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.repository.IRepositoryProvider;
import de.gotovoid.service.communication.SensorServiceMessenger;
import de.gotovoid.service.sensors.AbstractSensor;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 26/03/18.
 */

/**
 * Test cases for the {@link LocationRepository}.
 */
public class LocationRepositoryTest {
    private static final long UPDATE_FREQUENCY = 1000;
    private static final long RECORDING_ID = 123;

    private SensorServiceMessenger mMessenger;
    private LocationRepository mRepository;

    /**
     * Prepare the test case.
     */
    @Before
    public void before() {
        mMessenger = Mockito.mock(SensorServiceMessenger.class);
        mRepository = new LocationRepository(mMessenger);
    }

    /**
     * Verify that the repository is returned correctly.
     */
    @Test
    public void testGetRepository() {
        IRepositoryProvider provider = Mockito.mock(ProviderActivity.class);
        LocationRepository.getRepository((Activity) provider);
        Mockito.verify(provider, Mockito.times(1)).getLocationRepository();
    }

    /**
     * Verifies that the {@link LiveData} for location is created correctly.
     */
    // TODO: verify that the update frequency is set correctly!
    @Test
    public void testGetLocation() {
        final LiveData<AbstractSensor.Result<ExtendedGeoCoordinate>> location =
                mRepository.getLocation(UPDATE_FREQUENCY);
        assertThat(location, notNullValue());
    }

    /**
     * Verifies that the {@link LiveData} for pressure is created correctly.
     */
    // TODO: verify that the update frequency is set correctly!
    @Test
    public void testGetPressure() {
        final LiveData<AbstractSensor.Result<Float>> pressure =
                mRepository.getPressure(UPDATE_FREQUENCY);
        assertThat(pressure, notNullValue());
    }

    /**
     * Verify that the recording was started correctly.
     */
    @Test
    public void testStartRecording() {
        final Recording recording = Mockito.mock(Recording.class);
        Mockito.when(recording.getId()).thenReturn(RECORDING_ID);
        mRepository.startRecording(recording);
        Mockito.verify(mMessenger, Mockito.times(1))
                .startRecording(RECORDING_ID);
    }

    /**
     * Verify that the recording was stopped correctly.
     */
    @Test
    public void testStopRecording() {
        mRepository.stopRecording();
        Mockito.verify(mMessenger, Mockito.times(1)).stopRecording();
    }

    /**
     * Mock class to extend and implement the necessary classes and interfaces.
     */
    private abstract class ProviderActivity extends Activity implements IRepositoryProvider {
    }
}
