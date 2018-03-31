package de.gotovoid.service.repository;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import de.gotovoid.repository.IRepositoryProvider;
import de.gotovoid.service.communication.SensorServiceMessenger;
import de.gotovoid.database.model.Recording;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.SensorType;

/**
 * Created by DJ on 04/01/18.
 */

/**
 * Repository for the sensor data.
 */
public class LocationRepository {
    private static final String TAG = LocationRepository.class.getSimpleName();

    /**
     * Provides communication to the sensor service.
     */
    private final SensorServiceMessenger mServiceMessenger;

    /**
     * Manages the registered {@link RepositoryObserver}s.
     */
    private RepositoryObservable mRepositoryObservable;

    /**
     * Constructor taking the {@link SensorServiceMessenger} instance to use for service
     * communication.
     *
     * @param messenger the {@link SensorServiceMessenger to use}
     */
    public LocationRepository(final SensorServiceMessenger messenger) {
        mServiceMessenger = messenger;
        mRepositoryObservable = new RepositoryObservable(messenger);
    }

    /**
     * Returns the {@link LocationRepository} for the given {@link Activity}.
     * The {@link Activity} must implement {@link IRepositoryProvider}.
     *
     * @param activity the{@link Activity} to get the {@link LocationRepository} from
     * @return the {@link LocationRepository} instance.
     */
    @Nullable
    public static LocationRepository getRepository(final Activity activity) {
        if (activity instanceof IRepositoryProvider) {
            // TODO: this is a code smell. we need to provide a generic interface for the specific activity
            return ((IRepositoryProvider) activity).getLocationRepository();
        }
        return null;
    }

    /**
     * Returns the {@link LiveData} for the {@link ExtendedGeoCoordinate} representing the
     * current location.
     *
     * @param updateFrequency the updateFrequency for the {@link LiveData}
     * @return the {@link LiveData}
     */
    @NonNull
    public LiveData<AbstractSensor.Result<ExtendedGeoCoordinate>>
    getLocation(final long updateFrequency) {
        // TODO: maybe we should not always return a new LiveData object.
        Log.d(TAG, "getLocation() called with: updateFrequency = [" + updateFrequency + "]");
        return new RepositoryLiveData<>(mRepositoryObservable,
                updateFrequency,
                SensorType.LOCATION);
    }

    /**
     * Returns the {@link LiveData} for the current pressure.
     *
     * @param updateFrequency the update frequency for the {@link LiveData}
     * @return the {@link LiveData}
     */
    @NonNull
    public LiveData<AbstractSensor.Result<Float>> getPressure(final long updateFrequency) {
        // TODO: maybe we should not always return a new LiveData object.
        return new RepositoryLiveData<>(mRepositoryObservable,
                updateFrequency,
                SensorType.PRESSURE);
    }

    /**
     * Adds a new {@link RepositoryObserver}.
     *
     * @param observer the {@link RepositoryObserver} to be registered
     */
    public void addObserver(@NonNull final RepositoryObserver observer) {
        mRepositoryObservable.addObserver(observer);
    }

    /**
     * Removes the given {@link RepositoryObserver}.
     *
     * @param observer the {@link RepositoryObserver} to be removed
     */
    public void removeObserver(@NonNull final RepositoryObserver observer) {
        mRepositoryObservable.removeObserver(observer);
    }

    /**
     * Start recording.
     * TODO: Does it really make sense to send this id or should the service generate
     *
     * @param recording the recording id to start recording for.
     */
    public void startRecording(final Recording recording) {
        mServiceMessenger.startRecording(recording.getId());
    }

    /**
     * Stop recording.
     */
    public void stopRecording() {
        mServiceMessenger.stopRecording();
    }

}
