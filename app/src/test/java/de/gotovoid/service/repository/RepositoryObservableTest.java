package de.gotovoid.service.repository;

import android.telecom.Call;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.gotovoid.components.arcitecture.IObservable;
import de.gotovoid.service.communication.CallbackRegistration;
import de.gotovoid.service.communication.SensorServiceMessenger;
import de.gotovoid.service.sensors.SensorType;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by DJ on 26/03/18.
 */
@RunWith(Parameterized.class)
public class RepositoryObservableTest {
    private static final int TIMES = 10;

    private SensorServiceMessenger mMessenger;
    private RepositoryObservable mObservable;
    private CallbackRegistration mRegistration;

    @Parameterized.Parameter
    public SensorType mType;

    @Parameterized.Parameters
    public static Collection<Object[]> initiazizeParameters() {
        final List<Object[]> list = new ArrayList<>();
        for (SensorType type : SensorType.values()) {
            list.add(new Object[]{type});
        }
        return list;
    }

    @Before
    public void before() {
        mMessenger = Mockito.mock(SensorServiceMessenger.class);
        mObservable = new RepositoryObservable(mMessenger);
        mRegistration = Mockito.mock(CallbackRegistration.class);
    }

    @Test
    public void testAddObserver() {
        final RepositoryObserver observer = Mockito.mock(RepositoryObserver.class);
        Mockito.when(observer.getSensorType()).thenReturn(mType);
        Mockito.when(observer.getCallbackRegistration()).thenReturn(mRegistration);
        mObservable.addObserver(observer);
        Mockito.verify(mMessenger, Mockito.times(1))
                .start(mRegistration, observer);
    }

    @Test
    public void testAddObserverSeveral() {
        final List<RepositoryObserver> observers = new ArrayList<>();
        for (int i = 0; i < TIMES; i++) {
            final RepositoryObserver observer = Mockito.mock(RepositoryObserver.class);
            Mockito.when(observer.getSensorType()).thenReturn(mType);
            Mockito.when(observer.getCallbackRegistration()).thenReturn(mRegistration);
            observers.add(observer);
        }
        for (RepositoryObserver observer : observers) {
            mObservable.addObserver(observer);
        }
        Mockito.verify(mMessenger, Mockito.times(TIMES))
                .start(Mockito.eq(mRegistration), Mockito.any(RepositoryObserver.class));
    }

    @Test
    public void testAddWrongObserver() {
        final IObservable.Observer observer = Mockito.mock(IObservable.Observer.class);
        mObservable.addObserver(observer);
        Mockito.verify(mMessenger, Mockito.times(0))
                .start(Mockito.any(CallbackRegistration.class),
                        Mockito.any(RepositoryObserver.class));
    }

    @Test
    public void testRemoveObserver() {
        final RepositoryObserver observer = Mockito.mock(RepositoryObserver.class);
        Mockito.when(observer.getSensorType()).thenReturn(mType);
        Mockito.when(observer.getCallbackRegistration()).thenReturn(mRegistration);
        mObservable.addObserver(observer);
        mObservable.removeObserver(observer);
        Mockito.verify(mMessenger, Mockito.times(1))
                .stop(mRegistration);
    }

    @Test
    public void testRemoveObserverSeveral() {
        final List<RepositoryObserver> observers = new ArrayList<>();
        for (int i = 0; i < TIMES; i++) {
            final RepositoryObserver observer = Mockito.mock(RepositoryObserver.class);
            Mockito.when(observer.getSensorType()).thenReturn(mType);
            Mockito.when(observer.getCallbackRegistration()).thenReturn(mRegistration);
            observers.add(observer);
        }
        for (RepositoryObserver observer : observers) {
            mObservable.addObserver(observer);
        }
        for (RepositoryObserver observer : observers) {
            mObservable.removeObserver(observer);
        }
        Mockito.verify(mMessenger, Mockito.times(TIMES))
                .stop(mRegistration);
    }
}
