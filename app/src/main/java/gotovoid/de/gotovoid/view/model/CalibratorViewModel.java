package gotovoid.de.gotovoid.view.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import gotovoid.de.gotovoid.database.AppDatabase;
import gotovoid.de.gotovoid.database.model.CalibratedAltitude;
import gotovoid.de.gotovoid.service.repository.LocationRepository;

/**
 * This {@link AndroidViewModel} implementation handles all the data displayed and modified by
 * the {@link gotovoid.de.gotovoid.view.CalibratorFragment}.
 * <p>
 * Created by DJ on 23/12/17.
 */

public class CalibratorViewModel extends AndroidViewModel {
    private static final String TAG = CalibratorViewModel.class.getSimpleName();

    /**
     * Database to store the data.
     */
    private final AppDatabase mDatabase;
    /**
     * The repository for location data.
     */
    private LocationRepository mRepository;

    /**
     * Value holder for the current pressure.
     */
    private LiveData<Float> mCurrentPressure;
    /**
     * Value holder for the current altitude.
     */
    private MutableLiveData<Integer> mCurrentAltitude;

    /**
     * Stores the calibrated altitude.
     */
    private CalibratedAltitude mCalibratedAltitude;

    /**
     * Returns true if the altitude changed due to user interaction.
     */
    private boolean mIsAltitudeChanged = false;

    /**
     * Thread to handle background tasks, like modifying database entries.
     */
    private HandlerThread mHandlerThread;
    /**
     * Handler to add {@link Runnable}s to the background thread.
     */
    private Handler mHandler;


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
        // We need to extract the data using a background thread
        // TODO: maybe use LiveData instead
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCalibratedAltitude = mDatabase.getCalibratedPressureDao().getCalibratedPressure();
            }
        });
    }

    public void init(final LocationRepository repository) {
        // Get the repository
        mRepository = repository;
        mCurrentPressure = mRepository.getPressure();
        mCurrentAltitude =
                new ObserverMutableLiveData<Integer, Float>(mCurrentPressure) {
                    @Override
                    public void onChanged(@Nullable final Float pressure) {
                        postValue(calculateAltitude(pressure));
                    }
                };
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
     * Called when the calibrated altitude value should be persisted.
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
     * This class defines a {@link LiveData} implementation that itself observes a {@link LiveData}
     * instance and notifies it's listeners about changes.
     *
     * @param <Type>  Type of the {@link LiveData}
     * @param <Value> Type of the monitored {@link LiveData}
     */
    private abstract class ObserverMutableLiveData<Type, Value>
            extends MutableLiveData<Type>
            implements Observer<Value> {
        private final LiveData<Value> mObservedLiveData;

        private ObserverMutableLiveData(@NonNull final LiveData<Value> observedLiveData) {
            mObservedLiveData = observedLiveData;
        }

        @Override
        protected void onActive() {
            mObservedLiveData.observeForever(this);
        }

        @Override
        protected void onInactive() {
            mObservedLiveData.removeObserver(this);
        }

        @Override
        public abstract void onChanged(@Nullable final Value value);
    }
}
