package de.gotovoid.view.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import de.gotovoid.database.model.RecordingWithEntries;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.service.repository.LocationRepository;
import de.gotovoid.database.AppDatabase;
import de.gotovoid.database.model.Recording;
import de.gotovoid.database.model.RecordingEntry;
import de.gotovoid.service.sensors.AbstractSensor;

/**
 * Created by DJ on 05/01/18.
 */

/**
 * {@link RecordingDisplayViewModel} is the {@link AndroidViewModel} implementation
 * for the recording display.
 */
public class RecordingDisplayViewModel extends AndroidViewModel {
    private static final String TAG = RecordingDisplayViewModel.class.getSimpleName();
    private static final long UPDATE_FREQUENCY = 1000;
    /**
     * Database containing the {@link Recording} data.
     */
    private final AppDatabase mDatabase;

    /**
     * Repository for sensor information.
     */
    private LocationRepository mRepository;

    /**
     * Id of the {@link Recording} to be displayed.
     */
    private long mRecordingId;

    /**
     * The {@link Recording} to be displayed.
     */
    private LiveData<Recording> mRecording;

    /**
     * The {@link List} of {@link RecordingEntry}s to be displayed.
     */
    private LiveData<List<RecordingEntry>> mRecordingEntries;

    /**
     * The {@link RecordingWithEntries} contains both {@link Recording} and {@link RecordingEntry}.
     */
    private LiveData<RecordingWithEntries> mRecordingWithEntries;

    /**
     * The current location.
     */
    private LiveData<AbstractSensor.Result<ExtendedGeoCoordinate>> mLocation;

    /**
     * Constructor taking the {@link Application}.
     *
     * @param application the {@link Application}
     */
    public RecordingDisplayViewModel(@NonNull final Application application) {
        super(application);
        mDatabase = AppDatabase.getDatabaseInstance(application);
    }

    /**
     * Initialize the {@link RecordingDisplayViewModel} with the {@link LocationRepository}.
     *
     * @param repository the {@link LocationRepository}
     */
    public void init(final LocationRepository repository) {
        Log.d(TAG, "init() called with: repository = [" + repository + "]");
        mRepository = repository;
        mLocation = mRepository.getLocation(UPDATE_FREQUENCY);
    }

    /**
     * Set the id of the {@link Recording} to be displayed.
     *
     * @param recordingId id of the {@link Recording}
     */
    public void setRecordingId(final long recordingId) {
        mRecordingId = recordingId;
        mRecording = mDatabase.getRecordingDao()
                .observeRecording(mRecordingId);
        mRecordingEntries = mDatabase.getRecordingEntryDao()
                .observeTrackEntries(mRecordingId);
        mRecordingWithEntries = mDatabase.getRecordingDao()
                .observeRecordingWithEntries(mRecordingId);
    }

    /**
     * Returns the id of the {@link Recording} to be displayed.
     *
     * @return the id of the {@link Recording}
     */
    public long getRecordingId() {
        return mRecordingId;
    }

    /**
     * Returns whether the GPS is currently active.
     *
     * @return true if GPS is active
     */
    public boolean isGPSActive() {
        return mLocation.hasActiveObservers();
    }

    /**
     * Returns the {@link List} of {@link RecordingEntry}s to be displayed as observable.
     *
     * @return the {@link List} of {@link RecordingEntry}s
     */
    public LiveData<List<RecordingEntry>> getRecordingEntries() {
        return mRecordingEntries;
    }

    /**
     * Returns the {@link Recording} to be displayed as observable.
     *
     * @return the {@link Recording}
     */
    public LiveData<Recording> getRecording() {
        return mRecording;
    }

    /**
     * Returns the current location as {@link ExtendedGeoCoordinate} as observable.
     *
     * @return the current location
     */
    public LiveData<AbstractSensor.Result<ExtendedGeoCoordinate>> getLocation() {
        return mLocation;
    }

    /**
     * Returns the {@link RecordingWithEntries} containing {@link Recording} and
     * {@link RecordingEntry}s.
     *
     * @return the {@link RecordingWithEntries}
     */
    // TODO: check if needed
    public LiveData<RecordingWithEntries> getRecordingWithEntries() {
        return mRecordingWithEntries;
    }
}
