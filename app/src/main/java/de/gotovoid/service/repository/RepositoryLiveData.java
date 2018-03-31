package de.gotovoid.service.repository;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import de.gotovoid.components.arcitecture.ObserverLiveData;
import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.SensorType;

/**
 * Created by DJ on 20/03/18.
 */

/**
 * Implementation of the {@link ObserverLiveData} for the {@link LocationRepository}.
 *
 * @param <T> type of the observed data
 */
public class RepositoryLiveData<T extends Serializable>
        extends ObserverLiveData<AbstractSensor.Result<T>> {

    /**
     * Constructor taking the {@link  RepositoryObservable} to be observed,
     * the update frequency and {@link SensorType} in order to observe sensor data.
     *
     * @param observable      the {@link RepositoryObservable}
     * @param updateFrequency the update frequency in milliseconds
     * @param sensorType      the {@link SensorType}
     */
    public RepositoryLiveData(final @NonNull RepositoryObservable<T> observable,
                              final long updateFrequency,
                              final @NonNull SensorType sensorType) {
        this(new Observer<>(updateFrequency, sensorType), observable);
    }

    /**
     * Private constructor taking the {@link Observer} instance for observing the data.
     * The {@link Observer} will take care of updating the {@link RepositoryLiveData} when
     * changes are observed.
     * Therefor the {@link RepositoryLiveData} will be registered to be updated via
     * {@link #postValue(Object)}.
     *
     * @param observer   the {@link Observer} to observe data changes
     * @param observable the {@link RepositoryObservable} to be observed
     */
    private RepositoryLiveData(final @NonNull Observer<T> observer,
                               final @NonNull RepositoryObservable<T> observable) {
        super(observable, observer);
        observer.setLiveData(this);
    }

    /**
     * Creates a new {@link RepositoryLiveData} instance with the given {@link RepositoryObserver}
     * and the {@link RepositoryObservable}. The {@link RepositoryObservable} needs to take care
     * of updating the {@link RepositoryLiveData} when the observed data changes, by implementing
     * {@link RepositoryObserver#onChange(Object)}.
     *
     * @param observer   the {@link Observer}
     * @param observable the {@link RepositoryObservable}
     */
    public RepositoryLiveData(final @NonNull RepositoryObserver<T> observer,
                              final @NonNull RepositoryObservable<T> observable) {
        super(observable, observer);
    }

    /**
     * Creates a {@link RepositoryLiveData} instance with the given
     * {@link ObserverLiveData.RegistrationHandler}.
     *
     * @param handler the {@link ObserverLiveData.RegistrationHandler}
     */
    public RepositoryLiveData(final @NonNull RegistrationHandler handler) {
        super(handler);
    }

    /**
     * Hidden implementation of the {@link RepositoryObserver} to automatically update the
     * {@link RepositoryLiveData}.
     *
     * @param <T> type of the observed data
     */
    private static class Observer<T extends Serializable> extends RepositoryObserver<T> {
        private RepositoryLiveData<T> mLiveData;

        /**
         * Constructor taking the update frequency and {@link SensorType} needed for the callback
         * registration.
         *
         * @param updateFrequency frequency in ms to receive updates
         * @param sensorType      {@link SensorType} to receive updates for
         */
        private Observer(final long updateFrequency, final SensorType sensorType) {
            super(updateFrequency, sensorType);
        }

        @Override
        public void onChange(final @Nullable AbstractSensor.Result<T> data) {
            if (mLiveData != null) {
                mLiveData.postValue(data);
            }
        }

        /**
         * Set the {@link RepositoryLiveData} to be updated when the observed value changes.
         *
         * @param liveData the {@link RepositoryLiveData}
         */
        private void setLiveData(final @NonNull RepositoryLiveData<T> liveData) {
            mLiveData = liveData;
        }
    }
}
