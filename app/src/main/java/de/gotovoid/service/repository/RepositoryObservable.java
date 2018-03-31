package de.gotovoid.service.repository;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.gotovoid.components.arcitecture.IObservable;
import de.gotovoid.service.communication.CallbackRegistration;
import de.gotovoid.service.communication.SensorServiceMessenger;
import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.SensorType;

/**
 * Created by DJ on 20/03/18.
 */

/**
 * Manager for {@link RepositoryObserver}s.
 * Manages the {@link RepositoryObserver}s currently registered at the
 * {@link SensorServiceMessenger}.
 *
 * @param <T> type of the observer data
 */
class RepositoryObservable<T extends Serializable>
        implements IObservable<AbstractSensor.Result<T>> {
    private static final String TAG = RepositoryObserver.class.getSimpleName();
    /**
     * Provides communication to the sensor service.
     */
    private final SensorServiceMessenger mServiceMessenger;
    /**
     * Manages the {@link CallbackRegistration} sorted by {@link SensorType}
     * and {@link RepositoryObserver}, so they can be set and removed as listeners on the
     * {@link SensorServiceMessenger}.
     */
    private final Map<SensorType, Map<RepositoryObserver<T>, CallbackRegistration>> mObservers;

    /**
     * Constructor taking the {@link SensorServiceMessenger} to register the
     * {@link RepositoryObserver}s vor updates
     *
     * @param serviceMessenger the {@link SensorServiceMessenger}
     */
    public RepositoryObservable(final @NonNull SensorServiceMessenger serviceMessenger) {
        mServiceMessenger = serviceMessenger;
        mObservers = new HashMap<>();
    }

    /**
     * Returns the observers for test purposes only.
     *
     * @return the observers
     */
    @NonNull
    protected Map<SensorType, Map<RepositoryObserver<T>, CallbackRegistration>> getObservers() {
        return mObservers;
    }

    /**
     * Add an {@link Observer} to be managed.
     * The {@link Observer} will then be registered at the {@link SensorServiceMessenger} and
     * receive updates from tat point on.
     *
     * @param observer the {@link Observer} to be added
     */
    @Override
    public void addObserver(final @NonNull Observer<AbstractSensor.Result<T>> observer) {
        Log.d(TAG, "addObserver() called with: observer = [" + observer + "]");
        if (observer instanceof RepositoryObserver) {
            final RepositoryObserver obs = (RepositoryObserver) observer;
            if (!mObservers.containsKey(obs.getSensorType())) {
                mObservers.put(obs.getSensorType(), new HashMap<>());
            }
            Map<RepositoryObserver<T>, CallbackRegistration> observers
                    = mObservers.get(obs.getSensorType());

            if (observers.containsKey(obs)) {
                return;
            }
            observers.put(obs, obs.getCallbackRegistration());
            mServiceMessenger.start(obs.getCallbackRegistration(), obs);
        }
    }

    /**
     * Remove an {@link Observer} from the managed observers.
     * The {@link Observer} will no longer receive updates.
     *
     * @param observer {@link Observer} to be removed
     */
    @Override
    public void removeObserver(final @NonNull Observer<AbstractSensor.Result<T>> observer) {
        Log.d(TAG, "removeObserver() called with: observer = [" + observer + "]");
        if (observer instanceof RepositoryObserver) {
            final RepositoryObserver obs = (RepositoryObserver) observer;
            if (!mObservers.containsKey(obs.getSensorType())) {
                return;
            }
            Map<RepositoryObserver<T>, CallbackRegistration> observers
                    = mObservers.get(obs.getSensorType());
            if (!observers.containsKey(obs)) {
                return;
            }
            CallbackRegistration registration = observers.remove(obs);
            mServiceMessenger.stop(registration);
        }
    }
}
