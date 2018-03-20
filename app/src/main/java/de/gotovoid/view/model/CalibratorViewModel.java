package de.gotovoid.view.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.LocationSensor;
import de.gotovoid.database.AppDatabase;
import de.gotovoid.database.model.CalibratedAltitude;
import de.gotovoid.service.repository.LocationRepository;
import de.gotovoid.service.sensors.PressureSensor;
import de.gotovoid.service.sensors.SensorType;
import de.gotovoid.view.CalibratorFragment;

/**
 * Created by DJ on 23/12/17.
 */

/**
 * This {@link AndroidViewModel} implementation handles all the data displayed and modified by
 * the {@link CalibratorFragment}.
 * <p>
 */
public class CalibratorViewModel extends AndroidViewModel {
    private static final String TAG = CalibratorViewModel.class.getSimpleName();
    private static final long UPDATE_FREQUENCY = 1000;

    /**
     * Database to store the data.
     */
    private final AppDatabase mDatabase;

    /**
     * Observable for pressure sensor updates.
     */
    private final LocationRepository.ServiceObserver<AbstractSensor.Result<Float>> mPressureObserver;

    /**
     * Thread to handle background tasks, like modifying database entries.
     */
    private final HandlerThread mHandlerThread;

    /**
     * Handler to add {@link Runnable}s to the background thread.
     */
    private final Handler mHandler;

    /**
     * The repository for location data.
     */
    private LocationRepository mRepository;

    /**
     * Value holder for the current pressure.
     */
    // TODO: make this handle calibrating state
    final private MutableLiveData<Float> mCurrentPressure;
    /**
     * Value holder for the current altitude.
     */
    final private MutableLiveData<Integer> mCurrentAltitude;

    /**
     * Stores the calibrated altitude.
     */
    private CalibratedAltitude mCalibratedAltitude;

    /**
     * Returns true if the altitude changed due to user interaction.
     */
    private boolean mIsAltitudeChanged = false;

    /**
     * Constructor taking the {@link Application}.
     *
     * @param application the {@link Application}
     */
    public CalibratorViewModel(@NonNull final Application application) {
        super(application);
        // Create the background thread.
        mHandlerThread = new HandlerThread("Looper");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        // Get the database instance.
        mDatabase = AppDatabase.getDatabaseInstance(application);

        // Create the pressure sensor observer to be notified by the repository.
        mPressureObserver = new LocationRepository.ServiceObserver<AbstractSensor.Result<Float>>(
                UPDATE_FREQUENCY,
                SensorType.PRESSURE) {
            @Override
            public void onChange(final AbstractSensor.Result<Float> value) {
                // Notify the pressure LiveData.
                // TODO: handle calibrating state
                mCurrentPressure.postValue(value.getValue());
                // Notify the altitude LiveData
                mCurrentAltitude.postValue(calculateAltitude(value.getValue()));
            }
        };
        // Create the LiveData object for the current pressure
        mCurrentPressure = new LocationRepository.ObserverLiveData<>(new RegistrationHandler());
        // Create the LiveData object for the altitude
        mCurrentAltitude = new LocationRepository.ObserverLiveData<>(new RegistrationHandler());

        // We need to extract the data using a background thread
        // TODO: maybe use LiveData instead
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCalibratedAltitude = mDatabase.getCalibratedPressureDao().getCalibratedPressure();
            }
        });
    }

    /**
     * Initialize the {@link CalibratorViewModel} with the {@link LocationRepository} needed to
     * receive data.
     *
     * @param repository the {@link LocationRepository} to receive data from
     */
    public void init(@NonNull final LocationRepository repository) {
        // Get the repository
        Log.d(TAG, "init() called with: repository = [" + repository + "]");
        mRepository = repository;
    }

    /**
     * Returns the altitude as {@link LiveData}.
     *
     * @return the altitude
     */
    public LiveData<Integer> getAltitude() {
        return mCurrentAltitude;
    }

    /**
     * Returns the pressure as {@link LiveData}.
     *
     * @return the pressure
     */
    public LiveData<Float> getPressure() {
        return mCurrentPressure;
    }

    /**
     * Set the newly changed altitude modified by ui side.
     *
     * @param altitude altitude to be set
     */
    public void setAltitude(final Integer altitude) {
        mIsAltitudeChanged = true;
        mCalibratedAltitude = new CalibratedAltitude(0,
                mCurrentPressure.getValue(),
                altitude);
        mCurrentAltitude.postValue(altitude);
    }

    /**
     * True if the altitude has been changed by the ui.
     *
     * @return true if altitude has been changed
     */
    public boolean isAltitudeChanged() {
        return mIsAltitudeChanged;
    }

    /**
     * Called when the current calibrated altitude value should be persisted.
     */
    public void persistCalibratedAltitude() {
        Log.d(TAG, "persistCalibratedAltitude() called");
        // Fire up a background thread so we can modify the database.
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCalibratedAltitude != null) {
                    mDatabase.getCalibratedPressureDao()
                            .setCalibratedPressure(mCalibratedAltitude);
                }
                mIsAltitudeChanged = false;
            }
        });
    }

    /**
     * Calculates the altitude using the pressure value provided.
     *
     * @param currentPressure the current pressure
     * @return the new altitude
     */
    @Nullable
    private Integer calculateAltitude(@Nullable final Float currentPressure) {
        Log.d(TAG, "calculateAltitude() called with: currentPressure = ["
                + currentPressure + "]");
        if (mCalibratedAltitude != null
                && currentPressure != null) {
            // Use the calibrated altitude to calculate the new altitude using the current pressure.
            return mCalibratedAltitude.calculateHeight(currentPressure);
        }
        return null;
    }

    @Override
    protected void onCleared() {
        Log.d(TAG, "onCleared() called");
        super.onCleared();
        mHandlerThread.quitSafely();
    }

    /**
     * This class handles the registration of the
     * {@link LocationSensor.Observer} when the
     * {@link LiveData}'s {@link LiveData#hasActiveObservers()} changes.
     */
    private class RegistrationHandler
            implements LocationRepository.ObserverLiveData.RegistrationHandler {
        @Override
        public void onRegister() {
            Log.d(TAG, "onRegister() called");
            Log.d(TAG, "onRegister: " + mRepository);
            if (mRepository != null) {
                mRepository.addObserver(mPressureObserver);
            }
        }

        @Override
        public void onUnregister() {
            Log.d(TAG, "onUnregister() called");
            Log.d(TAG, "onUnregister: " + mRepository);
            if (mRepository != null) {
                if (!mCurrentAltitude.hasActiveObservers()
                        && !mCurrentPressure.hasActiveObservers())
                    mRepository.removeObserver(mPressureObserver);
            }
        }
    }

}
