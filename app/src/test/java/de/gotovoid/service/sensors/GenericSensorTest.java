package de.gotovoid.service.sensors;

import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Created by DJ on 25/02/18.
 */

/**
 * Verify the basic functionality of the {@link AbstractSensor}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public abstract class GenericSensorTest {

    /**
     * Initialize the test.
     */
    @Before
    public void before() {
        PowerMockito.mockStatic(Log.class);
    }

    /**
     * Return the {@link AbstractSensor} instance under test.
     *
     * @return the {@link AbstractSensor} instance under test
     */
    protected abstract AbstractSensor getSensor();

    /**
     * Verify that adding an {@link AbstractSensor.Observer} works.
     */
    @Test
    public void testAddObserver() {
        assertThat(getSensor().getObservers().isEmpty(), equalTo(true));
        final AbstractSensor.Observer observer = Mockito.mock(AbstractSensor.Observer.class);
        getSensor().addObserver(observer);
        assertThat(getSensor().getObservers().isEmpty(), equalTo(false));
    }

    /**
     * Verify that adding more than one {@link AbstractSensor.Observer} works.
     */
    @Test
    public void testAddSeveralObservers() {
        final int observerCount = 10;
        assertThat(getSensor().getObservers().size(), equalTo(0));
        for (int i = 0; i < observerCount; i++) {
            final AbstractSensor.Observer observer = Mockito.mock(AbstractSensor.Observer.class);
            getSensor().addObserver(observer);
        }
        assertThat(getSensor().getObservers().size(), equalTo(observerCount));
    }

    /**
     * Verify that adding the same {@link AbstractSensor.Observer} several times just adds one
     * instance.
     */
    @Test
    public void testAddSameObserver() {
        final int observerCount = 10;
        assertThat(getSensor().getObservers().size(), equalTo(0));
        final AbstractSensor.Observer observer = Mockito.mock(AbstractSensor.Observer.class);
        for (int i = 0; i < observerCount; i++) {
            getSensor().addObserver(observer);
        }
        assertThat(getSensor().getObservers().size(), equalTo(1));
    }

    /**
     * Verify that adding an {@link AbstractSensor.Observer} starts the sensor and removing the
     * {@link AbstractSensor.Observer} stops it.
     */
    @Test
    public void testIsStarted() {
        assertThat(getSensor().isStarted(), equalTo(false));
        final AbstractSensor.Observer observer = Mockito.mock(AbstractSensor.Observer.class);
        getSensor().addObserver(observer);
        assertThat(getSensor().isStarted(), equalTo(true));
        getSensor().removeObserver(observer);
        assertThat(getSensor().isStarted(), equalTo(false));
    }

    /**
     * Verfy that removing an observer works.
     */
    @Test
    public void testRemoveObserver() {
        final AbstractSensor.Observer observer = Mockito.mock(AbstractSensor.Observer.class);
        getSensor().addObserver(observer);
        getSensor().removeObserver(observer);
        assertThat(getSensor().getObservers().isEmpty(), equalTo(true));
    }

    /**
     * Verifies that the update frequency is set correctly.
     */
    @Test
    public void testGetUpdateFrequency() {
        final long updateFrequency = 200000;
        final AbstractSensor.Observer observer = Mockito.mock(AbstractSensor.Observer.class);
        Mockito.when(observer.getUpdateFrequency()).thenReturn(updateFrequency);
        getSensor().addObserver(observer);
        assertThat(getSensor().getUpdateFrequency(), equalTo(updateFrequency));
    }

    /**
     * Verifies that the update frequency is updated when an {@link AbstractSensor.Observer} with a
     * faster update frequency is added.
     */
    @Test
    public void testGetUpdateFrequencyAdd() {
        final long minUpdateFreq = 2000;
        final long maxUpdateFreq = 4000;
        final AbstractSensor.Observer observer1 = Mockito.mock(AbstractSensor.Observer.class);
        final AbstractSensor.Observer observer2 = Mockito.mock(AbstractSensor.Observer.class);
        Mockito.when(observer1.getUpdateFrequency()).thenReturn(maxUpdateFreq);
        Mockito.when(observer2.getUpdateFrequency()).thenReturn(minUpdateFreq);
        getSensor().addObserver(observer1);
        getSensor().addObserver(observer2);
        assertThat(getSensor().getUpdateFrequency(), equalTo(minUpdateFreq));
    }

    /**
     * Verifies that the update frequency is adapted when the {@link AbstractSensor.Observer} is
     * removed.
     */
    @Test
    public void testGetUpdateFrequencyRemove() {
        final long minUpdateFreq = 2000;
        final long maxUpdateFreq = 4000;
        final AbstractSensor.Observer observer1 = Mockito.mock(AbstractSensor.Observer.class);
        final AbstractSensor.Observer observer2 = Mockito.mock(AbstractSensor.Observer.class);
        Mockito.when(observer1.getUpdateFrequency()).thenReturn(maxUpdateFreq);
        Mockito.when(observer2.getUpdateFrequency()).thenReturn(minUpdateFreq);
        getSensor().addObserver(observer1);
        getSensor().addObserver(observer2);
        getSensor().removeObserver(observer2);
        assertThat(getSensor().getUpdateFrequency(), equalTo(maxUpdateFreq));
    }

    /**
     * Verifies that the {@link AbstractSensor.Observer} are notified appropriately.
     */
    @Test
    public void testNotifyObserver() {
        final AbstractSensor.Observer observer1 = Mockito.mock(AbstractSensor.Observer.class);
        final AbstractSensor.Observer observer2 = Mockito.mock(AbstractSensor.Observer.class);
        final Object data = new Object();
        getSensor().addObserver(observer1);
        getSensor().addObserver(observer2);
        getSensor().notifyObserver(data);
        Mockito.verify(observer1, Mockito.times(1)).onChange(data);
        Mockito.verify(observer2, Mockito.times(1)).onChange(data);
    }

    /**
     * Verifies that the update frequency of the {@link AbstractSensor.Observer} is regarded
     * when the {@link AbstractSensor} notifies it's observers.
     */
    @Test
    public void testNotifyTiming() {
        final int count = 10;
        final long minFreq = 50;
        final long maxFreq = minFreq * 2;
        final AbstractSensor.Observer observer1 = Mockito.mock(AbstractSensor.Observer.class);
        final AbstractSensor.Observer observer2 = Mockito.mock(AbstractSensor.Observer.class);
        final Object data = new Object();
        Mockito.when(observer1.getUpdateFrequency()).thenReturn(minFreq);
        Mockito.when(observer2.getUpdateFrequency()).thenReturn(maxFreq);
        getSensor().addObserver(observer1);
        getSensor().addObserver(observer2);
        for (int i = 0; i < count; i++) {
            getSensor().notifyObserver(data);
            try {
                Thread.sleep(minFreq);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Mockito.verify(observer1, Mockito.times(count)).onChange(Mockito.any());
        Mockito.verify(observer2, Mockito.times(count / 2)).onChange(Mockito.any());
    }
}