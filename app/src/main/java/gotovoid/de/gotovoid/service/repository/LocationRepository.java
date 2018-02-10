package gotovoid.de.gotovoid.service.repository;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.repository.IRepositoryProvider;
import gotovoid.de.gotovoid.service.communication.LocationServiceMessenger;

/**
 * Class defining the access to location data.
 * Created by DJ on 04/01/18.
 */
public class LocationRepository {
    private static final String TAG = LocationRepository.class.getSimpleName();

    private final Set<RecordingObserver> mRecordingObservers = new HashSet<>();
    private final LocationServiceMessenger.RecordingObserver mRecordingObserver
            = new LocationServiceMessenger.RecordingObserver() {
        @Override
        public void onChange(@NonNull final Long entryId) {
            for (RecordingObserver observer : mRecordingObservers) {
                observer.onRecordingUpdate(entryId);
            }
        }
    };

    private LocationServiceMessenger mMessenger;
    private RegisterableLiveData<Float> mPressure;
    private RegisterableLiveData<Location> mLocation;

    public LocationRepository(final LocationServiceMessenger messenger) {
        mMessenger = messenger;
    }

    public static LocationRepository getRepository(final Activity activity) {
        if (activity instanceof IRepositoryProvider) {
            // TODO: this is a code smell. we need to provide a generic interface for the specific activity
            return ((IRepositoryProvider) activity).getLocationRepository();
        }
        return null;
    }

    public LiveData<Float> getPressure() {
        if (mPressure == null) {
            mPressure = new RegisterableLiveData<>();
            mPressure.setRegistrationHandler(
                    new RegisterableLiveData.RegistrationHandler() {
                        private final LocationServiceMessenger.PressureObserver mObserver =
                                new LocationServiceMessenger.PressureObserver() {
                                    @Override
                                    public void onChange(@NonNull final Float pressure) {
                                        Log.d(TAG, "onPerssureChanged() called with: pressure = ["
                                                + pressure + "]");
                                        mPressure.postValue(pressure);
                                    }
                                };

                        @Override
                        public void onRegister() {
                            Log.d(TAG, "onRegister: ");
                            mMessenger.addObserver(mObserver);
                        }

                        @Override
                        public void onUnregister() {
                            Log.d(TAG, "onUnregister: ");
                            mMessenger.removeObserver(mObserver);
                        }
                    });

        }
        return mPressure;
    }

    public LiveData<Location> getLocation() {
        if (mLocation == null) {
            mLocation = new RegisterableLiveData<>();

            mLocation.setRegistrationHandler(new RegisterableLiveData.RegistrationHandler() {
                private final LocationServiceMessenger.LocationObserver mObserver =
                        new LocationServiceMessenger.LocationObserver() {
                            @Override
                            public void onChange(@NonNull final Location location) {
                                Log.d(TAG, "onLocationChanged() called with: location = ["
                                        + location + "]");
                                mLocation.postValue(location);
                            }
                        };

                @Override
                public void onRegister() {
                    Log.d(TAG, "onRegister: ");
                    mMessenger.addObserver(mObserver);
                }

                @Override
                public void onUnregister() {
                    mMessenger.removeObserver(mObserver);
                }
            });
        }
        return mLocation;
    }

    public void startRecording(final Recording recording) {
        mMessenger.startRecording(recording);
        mMessenger.addObserver(mRecordingObserver);
    }

    public void stopRecording() {
        mMessenger.removeObserver(mRecordingObserver);
        mMessenger.stopRecording();
    }

    public void addRecordingObserver(final RecordingObserver observer) {
        synchronized (mRecordingObservers) {
            mRecordingObservers.add(observer);
        }
    }

    public void removeRecordingObserver(final RecordingObserver observer) {
        synchronized (mRecordingObservers) {
            mRecordingObservers.remove(observer);
        }
    }

    public interface RecordingObserver {
        void onRecordingUpdate(final long recordingId);
    }

    private static class RegisterableLiveData<T> extends MutableLiveData<T> {
        private RegistrationHandler mRegistrationHandler;

        public void setRegistrationHandler(final RegistrationHandler handler) {
            mRegistrationHandler = handler;
        }

        @Override
        protected void onActive() {
            super.onActive();
            Log.d(TAG, "onActive() called");
            if (mRegistrationHandler != null) {
                mRegistrationHandler.onRegister();
            }
        }

        @Override
        protected void onInactive() {
            super.onInactive();
            Log.d(TAG, "onInactive() called");
            if (mRegistrationHandler != null) {
                mRegistrationHandler.onUnregister();
            }
        }

        public interface RegistrationHandler {
            void onRegister();

            void onUnregister();
        }
    }
}
