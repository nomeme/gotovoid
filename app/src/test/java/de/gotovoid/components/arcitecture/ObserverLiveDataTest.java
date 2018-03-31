package de.gotovoid.components.arcitecture;

/**
 * Created by DJ on 25/03/18.
 */

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * Test for the {@link ObserverLiveData}
 */

/**
 * Test for the {@link ObserverLiveData}.
 */
public class ObserverLiveDataTest {
    // Needed for lifecycle components, so that postValue is performed immediately
    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();
    private ObserverLiveData.RegistrationHandler mRegistrationHandler;

    /**
     * Prepare the test cases.
     */
    @Before
    public void before() {
        mRegistrationHandler = Mockito.mock(ObserverLiveData.RegistrationHandler.class);
    }

    /**
     * Verify registering an {@link Observer} works as expected.
     */
    @Test
    public void testRegister() {
        final ObserverLiveData liveData = new ObserverLiveData(mRegistrationHandler);
        liveData.observeForever(Mockito.mock(Observer.class));
        verify(mRegistrationHandler, Mockito.times(1))
                .onRegister();
    }

    /**
     * Verify that registering several {@link Observer}s works as expected.
     */
    @Test
    public void testRegisterSeveral() {
        final ObserverLiveData liveData = new ObserverLiveData(mRegistrationHandler);
        final List<Observer> observers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            observers.add(Mockito.mock(Observer.class));
        }
        for (Observer observer : observers) {
            liveData.observeForever(observer);
        }
        verify(mRegistrationHandler, Mockito.times(1))
                .onRegister();
    }

    /**
     * Verify that removing an {@link Observer} instance worls as expected.
     */
    @Test
    public void testUnregister() {
        final ObserverLiveData liveData = new ObserverLiveData(mRegistrationHandler);
        final Observer observer = Mockito.mock(Observer.class);
        liveData.observeForever(observer);
        liveData.removeObserver(observer);
        verify(mRegistrationHandler, Mockito.times(1))
                .onUnregister();
    }

    /**
     * Verify that removing several {@link Observer} instances works as expected.
     */
    @Test
    public void testUnregisterSeveral() {
        final ObserverLiveData liveData = new ObserverLiveData(mRegistrationHandler);
        final List<Observer> observers = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            observers.add(Mockito.mock(Observer.class));
        }
        for (Observer observer : observers) {
            liveData.observeForever(observer);
        }
        for (Observer observer : observers) {
            liveData.removeObserver(observer);
        }
        verify(mRegistrationHandler, Mockito.times(1))
                .onUnregister();
    }

    /**
     * Verifies that receiving updates works as expected.
     */
    @Test
    public void testGetUpdate() {
        MockObservable observable = new MockObservable();
        final ObserverLiveData liveData = new ObserverLiveData(observable);
        final Object data = new Object();
        final Observer observer = Mockito.mock(Observer.class);
        liveData.observeForever(observer);
        observable.setValue(data);
        verify(observer, Mockito.times(1)).onChanged(data);
    }

    /**
     * {@link IObservable} implementation for test purposes only.
     *
     * @param <T> type of the observed data
     */
    private class MockObservable<T> implements IObservable<T> {
        private Observer mObserver;

        @Override
        public void addObserver(final Observer<T> observer) {
            mObserver = observer;
        }

        @Override
        public void removeObserver(final Observer<T> observer) {
            mObserver = null;
        }

        public void setValue(final T data) {
            System.out.print(data);
            mObserver.onChange(data);
        }
    }
}
