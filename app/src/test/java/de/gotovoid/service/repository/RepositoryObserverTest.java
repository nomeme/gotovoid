package de.gotovoid.service.repository;

import android.arch.persistence.room.Update;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.List;

import de.gotovoid.service.communication.CallbackRegistration;
import de.gotovoid.service.sensors.SensorType;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 26/03/18.
 */

/**
 * Test cases for the {@link RepositoryObserver}.
 */
@RunWith(Parameterized.class)
public class RepositoryObserverTest {
    private static final long UPDATE_FREQUENCY = 1000;
    @Parameterized.Parameter
    public SensorType mType;

    /**
     * Prepare the parameters.
     *
     * @return the parameters
     */
    @Parameterized.Parameters
    public static List<Object[]> initializeParameters() {
        final List<Object[]> list = new ArrayList<>();
        for (SensorType type : SensorType.values()) {
            list.add(new Object[]{type});
        }
        return list;
    }

    /**
     * Verifies that the constructor works as expected.
     */
    @Test
    public void testConstructor() {
        final RepositoryObserver observer = new TestRepositoryObserver(UPDATE_FREQUENCY, mType);
        assertThat(observer.getUpdateFrequency(), is(UPDATE_FREQUENCY));
        assertThat(observer.getSensorType(), is(mType));
    }

    /**
     * Verify that the {@link CallbackRegistration} is created correctly.
     */
    @Test
    public void testCallbackRegistration() {
        final RepositoryObserver observer = new TestRepositoryObserver(UPDATE_FREQUENCY, mType);
        CallbackRegistration registration = observer.getCallbackRegistration();
        assertThat(registration.getUpdateFrequency(), is(UPDATE_FREQUENCY));
        assertThat(registration.getType(), is(mType));
    }

    /**
     * Verifies that the {@link CallbackRegistration} is always the same.
     */
    @Test
    public void testCallBackRegistrationSame() {
        final RepositoryObserver observer = new TestRepositoryObserver(UPDATE_FREQUENCY, mType);
        CallbackRegistration registration = observer.getCallbackRegistration();
        assertThat(observer.getCallbackRegistration(), is(registration));
    }

    /**
     * Implementation of the {@link RepositoryObserver} class for test purposes only.
     */
    private class TestRepositoryObserver extends RepositoryObserver {

        /**
         * Constructor taking the update frequency and {@link SensorType} needed for the callback
         * registration.
         *
         * @param updateFrequency frequency in ms to receive updates
         * @param sensorType      {@link SensorType} to receive updates for
         */
        public TestRepositoryObserver(long updateFrequency, SensorType sensorType) {
            super(updateFrequency, sensorType);
        }

        @Override
        public void onChange(final Object data) {
            // Do nothing
        }
    }
}
