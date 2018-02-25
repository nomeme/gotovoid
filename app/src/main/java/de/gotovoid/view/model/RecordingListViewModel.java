package de.gotovoid.view.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import de.gotovoid.database.AppDatabase;
import de.gotovoid.database.model.Recording;

/**
 * {@link AndroidViewModel} implementation for the recording list.
 * <p>
 * Created by DJ on 15/01/18.
 */
public class RecordingListViewModel extends AndroidViewModel {
    private static final String TAG = RecordingListViewModel.class.getSimpleName();
    private final AppDatabase mDatabase;
    private LiveData<List<Recording>> mRecordings;

    /**
     * Constructor taking the {@link Application}.
     *
     * @param application the {@link Application}
     */
    public RecordingListViewModel(@NonNull final Application application) {
        super(application);
        mDatabase = AppDatabase.getDatabaseInstance(application);
        mRecordings = mDatabase.getRecordingDao().observeAll();
    }

    /**
     * Returns the recordings.
     *
     * @return the recordings
     */
    public LiveData<List<Recording>> getRecordings() {
        return mRecordings;
    }

    public void deleteRecording(final Recording recording) {
        Log.d(TAG, "deleteRecording: recording: " + recording.getId());
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                mDatabase.getRecordingDao().remove(recording);
                return null;
            }
        };
        task.execute();
    }
}
