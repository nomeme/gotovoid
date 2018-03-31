package de.gotovoid.service.sensors;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;


import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 25/02/18.
 */

/**
 * Verify the basic functionality of the {@link AbstractSensor}.
 */
public abstract class AbstractSensorTest extends GenericSensorTest {
    private Sensor mSensor;

    @Before
    public void before() {
        mSensor = new Sensor(Mockito.mock(AbstractSensor.StateEvaluator.class));
    }

    /**
     * Return the {@link AbstractSensor} instance under test.
     *
     * @return the {@link AbstractSensor} instance under test
     */
    protected AbstractSensor getSensor() {
        return mSensor;
    }

    /**
     * Verify that the start method is called when the first observer is added.
     */
    @Test
    public void testStart() {
        assertThat(mSensor.mIsStarted, equalTo(false));
        final AbstractSensor.Observer observer = Mockito.mock(AbstractSensor.Observer.class);
        mSensor.addObserver(observer);
        assertThat(mSensor.mIsStarted, equalTo(true));
    }

    /**
     * Verify that the sensor stop method is called when the last observer is removed.
     */
    @Test
    public void testStop() {
        final AbstractSensor.Observer observer1 = Mockito.mock(AbstractSensor.Observer.class);
        final AbstractSensor.Observer observer2 = Mockito.mock(AbstractSensor.Observer.class);
        mSensor.addObserver(observer1);
        mSensor.addObserver(observer2);
        mSensor.removeObserver(observer1);
        assertThat(mSensor.mIsStopped, equalTo(false));
        mSensor.removeObserver(observer2);
        assertThat(mSensor.mIsStopped, equalTo(true));
    }

    /**
     * Verify that the sensor is restarted when an observer with a shorter update frequency
     * is added.
     */
    @Test
    public void testRestartWhenAdded() {
        final long minUpdateFreq = 2000;
        final long maxUpdateFreq = 4000;
        final AbstractSensor.Observer observer1 = Mockito.mock(AbstractSensor.Observer.class);
        Mockito.when(observer1.getUpdateFrequency()).thenReturn(maxUpdateFreq);
        final AbstractSensor.Observer observer2 = Mockito.mock(AbstractSensor.Observer.class);
        Mockito.when(observer2.getUpdateFrequency()).thenReturn(minUpdateFreq);
        mSensor.addObserver(observer1);
        mSensor.addObserver(observer2);
        assertThat(mSensor.mIsRestarted, equalTo(true));
    }

    /**
     * Verify that the sensor is restarted when the observer with the shortest update
     * frequency is removed.
     */
    @Test
    public void testRestartWhenRemoved() {
        final long minUpdateFreq = 2000;
        final long maxUpdateFreq = 4000;
        final AbstractSensor.Observer observer1 = Mockito.mock(AbstractSensor.Observer.class);
        Mockito.when(observer1.getUpdateFrequency()).thenReturn(minUpdateFreq);
        final AbstractSensor.Observer observer2 = Mockito.mock(AbstractSensor.Observer.class);
        Mockito.when(observer2.getUpdateFrequency()).thenReturn(maxUpdateFreq);
        mSensor.addObserver(observer1);
        mSensor.addObserver(observer2);
        assertThat(mSensor.mIsRestarted, equalTo(false));
        getSensor().removeObserver(observer1);
        assertThat(mSensor.mIsRestarted, equalTo(true));
    }

    /**
     * {@link AbstractSensor} implementation for test purposes.
     */
    private class Sensor extends AbstractSensor {
        private boolean mIsStarted;
        private boolean mIsStopped;
        private boolean mIsRestarted;

        public Sensor(final StateEvaluator stateEvaluator) {
            super(stateEvaluator);
        }

        @Override
        protected void startSensor() {
            mIsStarted = true;
        }

        @Override
        protected void stopSensor() {
            mIsStopped = true;
        }

        @Override
        protected void restartSensor() {
            mIsRestarted = true;
        }
    }
}