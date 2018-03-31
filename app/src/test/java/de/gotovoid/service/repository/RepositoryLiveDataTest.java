package de.gotovoid.service.repository;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.os.RemoteException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.gotovoid.service.communication.CallbackRegistration;
import de.gotovoid.service.communication.Response;
import de.gotovoid.service.communication.SensorServiceMessenger;
import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.SensorType;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 26/03/18.
 */

@RunWith(Parameterized.class)
public class RepositoryLiveDataTest {
    private static final long UPDATE_FREQUENCY = 100;
    // Needed for lifecycle components, so that postValue is performed immediately
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    @Parameterized.Parameter
    public SensorType mType;

    @Parameterized.Parameters
    public static Collection<Object[]> initParameters() {
        final List<Object[]> list = new ArrayList<>();
        for (SensorType type : SensorType.values()) {
            list.add(new Object[]{type});
        }
        return list;
    }

    /**
     * Verifies that the {@link RepositoryLiveData} adds itself correctly as an observer.
     */
    @Test
    public void testObserve() {
        final RepositoryObservable observable = Mockito.mock(RepositoryObservable.class);
        final RepositoryLiveData liveData = new RepositoryLiveData(observable,
                UPDATE_FREQUENCY, mType);
        final Observer observer = Mockito.mock(Observer.class);
        liveData.observeForever(observer);

        Mockito.verify(observable, Mockito.times(1))
                .addObserver(Mockito.any(RepositoryObserver.class));
    }

    /**
     * Verifies that the {@link RepositoryLiveData} removes itself correctly as an observer.
     */
    @Test
    public void testUnObserve() {
        final RepositoryObservable observable = Mockito.mock(RepositoryObservable.class);
        final RepositoryLiveData liveData = new RepositoryLiveData(observable,
                UPDATE_FREQUENCY, mType);
        final Observer observer = Mockito.mock(Observer.class);
        liveData.observeForever(observer);
        liveData.removeObserver(observer);
        Mockito.verify(observable, Mockito.times(1))
                .removeObserver(Mockito.any(RepositoryObserver.class));
    }

    /**
     * Verifies that updates are received.
     */
    @Test
    public void testReceiveUpdate() {
        final MockObservable observable =
                new MockObservable(Mockito.mock(SensorServiceMessenger.class));
        final RepositoryLiveData liveData = new RepositoryLiveData(observable,
                UPDATE_FREQUENCY, mType);
        final Observer observer = Mockito.mock(Observer.class);
        liveData.observeForever(observer);
        final AbstractSensor.Result result = Mockito.mock(AbstractSensor.Result.class);
        observable.notifyObservers(mType, result);
        Mockito.verify(observer, Mockito.times(1))
                .onChanged(result);
    }

    /**
     * Verifies that updates are not received when the {@link RepositoryLiveData} is no longer
     * observing.
     */
    @Test
    public void testUpdateNotObserving() {
        final MockObservable observable =
                new MockObservable(Mockito.mock(SensorServiceMessenger.class));
        final RepositoryLiveData liveData = new RepositoryLiveData(observable,
                UPDATE_FREQUENCY, mType);
        final Observer observer = Mockito.mock(Observer.class);
        liveData.observeForever(observer);
        liveData.removeObserver(observer);
        final AbstractSensor.Result result = Mockito.mock(AbstractSensor.Result.class);
        observable.notifyObservers(mType, result);
        Mockito.verify(observer, Mockito.times(0))
                .onChanged(result);
    }

    /**
     * Verifies that the correct sensor type updates are received.
     */
    @Test
    public void testUpdateWrongSensor() {
        final MockObservable observable =
                new MockObservable(Mockito.mock(SensorServiceMessenger.class));
        final RepositoryLiveData liveData = new RepositoryLiveData(observable,
                UPDATE_FREQUENCY, mType);
        final Observer observer = Mockito.mock(Observer.class);
        liveData.observeForever(observer);
        final AbstractSensor.Result result = Mockito.mock(AbstractSensor.Result.class);
        for (SensorType type : SensorType.values()) {
            observable.notifyObservers(type, result);
        }
        Mockito.verify(observer, Mockito.times(1))
                .onChanged(result);
    }

    /**
     * Observable for testing only.
     * Provides the ability to call the observers.
     */
    private class MockObservable extends RepositoryObservable {

        /**
         * Constructor taking the {@link SensorServiceMessenger}
         *
         * @param serviceMessenger
         */
        public MockObservable(final SensorServiceMessenger serviceMessenger) {
            super(serviceMessenger);
        }

        /**
         * Notify the observers
         *
         * @param type   the {@link SensorType}
         * @param result the {@link de.gotovoid.service.sensors.AbstractSensor.Result}
         */
        protected void notifyObservers(final SensorType type,
                                       final AbstractSensor.Result result) {
            Map<SensorType, Map<RepositoryObserver, CallbackRegistration>> observers
                    = getObservers();
            if (observers == null) {
                return;
            }
            Map<RepositoryObserver, CallbackRegistration> obs = observers.get(type);
            if (obs == null) {
                return;
            }
            for (RepositoryObserver observer : obs.keySet()) {
                try {
                    Response response = Mockito.mock(Response.class);
                    Mockito.when(response.getValue()).thenReturn(result);
                    observer.onSensorValueChanged(response);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
