package gotovoid.de.gotovoid.view.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.location.Location;
import android.support.annotation.NonNull;

import java.util.List;

import gotovoid.de.gotovoid.database.AppDatabase;
import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.database.model.RecordingEntry;
import gotovoid.de.gotovoid.database.model.RecordingWithEntries;
import gotovoid.de.gotovoid.service.repository.LocationRepository;

/**
 * {@link RecordingDisplayViewModel} is the {@link AndroidViewModel} implementation
 * for the recording display.
 * <p>
 * Created by DJ on 05/01/18.
 */
public class RecordingDisplayViewModel extends AndroidViewModel {
    private static final String TAG = RecordingDisplayViewModel.class.getSimpleName();
    private final AppDatabase mDatabase;

    private long mRecordingId;
    private LocationRepository mRepository;
    private LiveData<Recording> mRecording;
    private LiveData<List<RecordingEntry>> mRecordingEntries;
    private LiveData<RecordingWithEntries> mRecordingWithEntries;

    private LiveData<Location> mLocation;

    public RecordingDisplayViewModel(@NonNull final Application application) {
        super(application);
        mDatabase = AppDatabase.getDatabaseInstance(application);

    }

    public void init(final LocationRepository repository) {
        mRepository = repository;
        mLocation = mRepository.getLocation();
    }

    public void setRecordingId(final long recordingId) {
        mRecordingId = recordingId;
        mRecording = mDatabase.getRecordingDao()
                .observeRecording(mRecordingId);
        mRecordingEntries = mDatabase.getRecordingEntryDao()
                .observeTrackEntries(mRecordingId);
        mRecordingWithEntries = mDatabase.getRecordingDao()
                .observeRecordingWithEntries(mRecordingId);
    }

    public long getRecordingId() {
        return mRecordingId;
    }

    public LiveData<List<RecordingEntry>> getRecordingEntries() {
        return mRecordingEntries;
    }

    public LiveData<Recording> getRecording() {
        return mRecording;
    }

    public LiveData<Location> getLocation() {
        return mLocation;
    }

    public LiveData<RecordingWithEntries> getRecordingWithEntries() {
        return mRecordingWithEntries;
    }
}
