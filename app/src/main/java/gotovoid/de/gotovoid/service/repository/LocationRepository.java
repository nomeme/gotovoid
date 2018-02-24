package gotovoid.de.gotovoid.service.repository;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import gotovoid.de.gotovoid.repository.IRepositoryProvider;
import gotovoid.de.gotovoid.service.communication.CallbackRegistration;
import gotovoid.de.gotovoid.service.communication.ISensorServiceCallback;
import gotovoid.de.gotovoid.service.communication.Response;
import gotovoid.de.gotovoid.service.communication.SensorServiceMessenger;
import gotovoid.de.gotovoid.service.sensors.SensorType;

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
     * Manages the {@link CallbackRegistration} sorted by {@link SensorType}
     * and {@link ServiceObserver}, so they can be set and removed as listeners on the
     * {@link SensorServiceMessenger}.
     */
    private final Map<SensorType, Map<ServiceObserver, CallbackRegistration>> mObservers;

    /**
     * Constructor taking the {@link SensorServiceMessenger} instance to use for service
     * communication..
     *
     * @param messenger the {@link SensorServiceMessenger to use}
     */
    public LocationRepository(final SensorServiceMessenger messenger) {
        mServiceMessenger = messenger;
        mObservers = new HashMap<>();
    }

    /**
     * Returns the {@link LocationRepository} for the given {@link Activity}.
     * The {@link Activity} must implement {@link IRepositoryProvider}.
     *
     * @param activity the{@link Activity} to get the {@link LocationRepository} from
     * @return the {@link LocationRepository} instance.
     */
    public static LocationRepository getRepository(final Activity activity) {
        Log.d(TAG, "getRepository() called with: activity = [" + activity + "]");
        if (activity instanceof IRepositoryProvider) {
            // TODO: this is a code smell. we need to provide a generic interface for the specific activity
            return ((IRepositoryProvider) activity).getLocationRepository();
        }
        Log.e(TAG, "getRepository: null");
        return null;
    }

    /**
     * Returns the {@link LiveData} for the {@link ExtendedGeoCoordinate} representing the
     * current location.
     *
     * @param updateFrequency the updateFrequency for the {@link LiveData}
     * @return the {@link LiveData}
     */
    public LiveData<ExtendedGeoCoordinate> getLocation(final long updateFrequency) {
        // TODO: maybe we should not always return a new LiveData object.
        return new RepositoryLiveData<ExtendedGeoCoordinate>(this,
                updateFrequency,
                SensorType.LOCATION);
    }

    /**
     * Returns the {@link LiveData} for the current pressure.
     *
     * @param updateFrequency the update frequency for the {@link LiveData}
     * @return the {@link LiveData}
     */
    public LiveData<Float> getPressure(final long updateFrequency) {
        // TODO: maybe we should not always return a new LiveData object.
        return new RepositoryLiveData<Float>(this,
                updateFrequency,
                SensorType.PRESSURE);
    }

    /**
     * Add a new {@link ServiceObserver}.
     *
     * @param observer the {@link ServiceObserver} to be added
     */
    public void addObserver(@NonNull final ServiceObserver observer) {
        Log.d(TAG, "addObserver() called with: observer = [" + observer + "]");
        if (!mObservers.containsKey(observer.getType())) {
            mObservers.put(observer.getType(), new HashMap<ServiceObserver, CallbackRegistration>());
        }
        Map<ServiceObserver, CallbackRegistration> observers = mObservers.get(observer.getType());

        if (observers.containsKey(observer)) {
            return;
        }
        CallbackRegistration registration = new CallbackRegistration(observer.getType(),
                observer,
                observer.getUpdateFrequency());
        observers.put(observer, registration);
        mServiceMessenger.start(registration, observer);
    }

    /**
     * Remove the given {@link ServiceObserver}.
     *
     * @param observer the {@link ServiceObserver} to be removed
     */
    public void removeObserver(@NonNull final ServiceObserver observer) {
        Log.d(TAG, "removeObserver() called with: observer = [" + observer + "]");
        if (!mObservers.containsKey(observer.getType())) {
            return;
        }
        Map<ServiceObserver, CallbackRegistration> observers = mObservers.get(observer.getType());
        if (!observers.containsKey(observer)) {
            return;
        }
        CallbackRegistration registration = observers.remove(observer);
        mServiceMessenger.stop(registration);
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

    /**
     * Observer for the sensor service.
     * Extends the {@link ISensorServiceCallback} so that it can be used for communication with
     * the {@link gotovoid.de.gotovoid.service.LocationService}.
     * TODO: possibly move this to the SensorServiceMessenger
     *
     * @param <T> the type the sensor provides
     */
    public static abstract class ServiceObserver<T extends Serializable> extends ISensorServiceCallback.Stub {
        private final long mUpdateFrequency;
        private final SensorType mType;

        /**
         * Constructor taking the update frequency and {@link SensorType}, so that a
         * {@link CallbackRegistration} for the {@link SensorServiceMessenger}
         * can be generated.
         *
         * @param updateFrequency the update frequency
         * @param type            {@link SensorType}
         */
        public ServiceObserver(final long updateFrequency, final SensorType type) {
            mUpdateFrequency = updateFrequency;
            mType = type;
        }

        @Override
        public void onSensorValueChanged(final Response response) throws RemoteException {
            onChange((T) response.getValue());
        }

        /**
         * Returns the update frequency.
         *
         * @return the update frequency
         */
        protected long getUpdateFrequency() {
            return mUpdateFrequency;
        }

        /**
         * Returns the {@link SensorType}.
         *
         * @return the {@link SensorType}
         */
        protected SensorType getType() {
            return mType;
        }

        /**
         * Called when the sensor value changed.
         *
         * @param value value that changed
         */
        public abstract void onChange(final T value);

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(getClass().getSimpleName());
            builder.append("{type: ");
            builder.append(getType());
            builder.append(", updateFrequency: ");
            builder.append(getUpdateFrequency());
            builder.append("}");
            return builder.toString();
        }
    }

    /**
     * Observer for data from the {@link LocationRepository}. Automatically updates a
     * {@link LiveData} object.
     *
     * @param <T> {@link Serializable} data to be observed
     */
    private static class RepositoryObserver<T extends Serializable> extends ServiceObserver<T> {
        private MutableLiveData<T> mLiveData;

        /**
         * Create a new instance of the {@link RepositoryObserver} with update frequency and
         * {@link SensorType}.
         *
         * @param updateFrequency the update frequency of the sensor
         * @param type            the {@link SensorType}
         */
        public RepositoryObserver(long updateFrequency, SensorType type) {
            super(updateFrequency, type);
        }

        /**
         * Set the {@link MutableLiveData} to be updated when the observed value changes.
         *
         * @param liveData the {@link MutableLiveData} to be updated
         */
        public void setLiveData(final MutableLiveData<T> liveData) {
            mLiveData = liveData;
        }

        @Override
        public void onChange(final T value) {
            Log.d(TAG, "onChange() called with: value = [" + value + "]");
            if (mLiveData != null) {
                mLiveData.postValue(value);
            }
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(getClass().getSimpleName());
            builder.append("{type: ");
            builder.append(getType());
            builder.append(", updateFrequency: ");
            builder.append(getUpdateFrequency());
            builder.append(", liveData: ");
            builder.append(mLiveData);
            builder.append("}");
            return builder.toString();
        }
    }

    /**
     * {@link MutableLiveData} that supports automatically registering as an Observer through
     * an {@link RegistrationHandler}.
     * TODO: move this to a dedicated class
     *
     * @param <T> type of the data to be updated
     */
    public static class ObserverLiveData<T> extends MutableLiveData<T> {
        private static final String TAG = ObserverLiveData.class.getSimpleName();
        private final RegistrationHandler mRegistrationHandler;

        public ObserverLiveData(final RegistrationHandler handler) {
            mRegistrationHandler = handler;
        }

        @Override
        protected void onActive() {
            super.onActive();
            Log.d(TAG, "onActive: ");
            if (mRegistrationHandler != null) {
                mRegistrationHandler.onRegister();
            }
        }

        @Override
        protected void onInactive() {
            super.onInactive();
            Log.d(TAG, "onInactive: ");
            if (mRegistrationHandler != null) {
                mRegistrationHandler.onUnregister();
            }
        }

        /**
         * Interface for the registration on an {@link java.util.Observable}.
         */
        public interface RegistrationHandler {
            /**
             * Called when there are active observers registered at the {@link LiveData} object,
             * so registration on the {@link java.util.Observable} is required.
             */
            void onRegister();

            /**
             * Called when there are no active observers registered anymore at the {@link LiveData}.
             * So the registration on the {@link java.util.Observable} needs to be cancelled.
             */
            void onUnregister();
        }
    }

    /**
     * {@link MutableLiveData} using the {@link java.util.Observer} pattern to listen to changes
     * at an {@link java.util.Observable} when it has active listeners itself.
     * TODO: add an additional type for the observer so we can make it more generic
     *
     * @param <T> the type of the observed value
     */
    public static class RepositoryLiveData<T extends Serializable> extends ObserverLiveData<T> {

        /**
         * Constructor taking the {@link LocationRepository}, update frequency and
         * {@link SensorType} on oder to create a {@link RepositoryObserver} that is used to
         * observe sensor updates.
         *
         * @param repository     the {@link LocationRepository} to register for updates
         * @param updateFreqency the update frequency in milliseconds
         * @param type           the {@link SensorType}
         */
        public RepositoryLiveData(final LocationRepository repository,
                                  final long updateFreqency,
                                  final SensorType type) {
            this(repository, new RepositoryObserver(updateFreqency, type));
        }

        /**
         * Constructor taking the {@link LocationRepository} and {@link ServiceObserver} used
         * to observe sensor updates.
         *
         * @param repository the {@link LocationRepository} to register for updates
         * @param observer   observer to register for updates
         */
        public RepositoryLiveData(final LocationRepository repository,
                                  final RepositoryObserver<T> observer) {
            super(getRegistrationHandler(repository, observer));
            observer.setLiveData(this);
        }

        /**
         * Returns the {@link ObserverLiveData.RegistrationHandler} for the
         * {@link RepositoryLiveData}.
         *
         * @param repository the {@link RepositoryLiveData} to register at for updates
         * @param observer   the {@link ServiceObserver} to register for updates
         * @return the {@link ObserverLiveData.RegistrationHandler}
         */
        private static RegistrationHandler getRegistrationHandler(
                final LocationRepository repository,
                final ServiceObserver observer) {
            return new RegistrationHandler(repository, observer);
        }

        /**
         * Implementation of the {@link ObserverLiveData.RegistrationHandler} that automatically
         * registers a given observer at a {@link LocationRepository}
         * TODO add additional type for the observer so we can make it really generic
         *
         * @param <T> type of observed data
         */
        private static class RegistrationHandler<T extends Serializable>
                implements ObserverLiveData.RegistrationHandler {
            private final LocationRepository mRepository;
            private final ServiceObserver<T> mObserver;

            /**
             * Constructor taking the {@link LocationRepository} to register at and
             * the {@link ServiceObserver} to r5egister.
             *
             * @param repository the {@link LocationRepository}
             * @param observer
             */
            private RegistrationHandler(final LocationRepository repository,
                                        final ServiceObserver observer) {
                mRepository = repository;
                mObserver = observer;
            }

            @Override
            public void onRegister() {
                mRepository.addObserver(mObserver);
            }

            @Override
            public void onUnregister() {
                mRepository.removeObserver(mObserver);
            }
        }
    }
}
