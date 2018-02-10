package gotovoid.de.gotovoid.view.model;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gotovoid.de.gotovoid.database.AppDatabase;
import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.database.model.RecordingEntry;
import gotovoid.de.gotovoid.service.repository.LocationRepository;

/**
 * Created by DJ on 20/01/18.
 */

public class RecorderViewModel extends AndroidViewModel {
    private static final String TAG = RecorderViewModel.class.getSimpleName();
    private final RecordingObserver mRecordingObserver;

    private final AppDatabase mDatabase;
    private final HandlerThread mHandlerThread;
    private final Handler mHandler;

    private final MutableLiveData<List<RecordingEntry>> mEntries;

    private LocationRepository mLocationRepository;

    public RecorderViewModel(@NonNull final Application application) {
        super(application);
        mRecordingObserver = new RecordingObserver();
        mDatabase = AppDatabase.getDatabaseInstance(application);

        mHandlerThread = new HandlerThread("Looper");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mEntries = new MutableLiveData<>();
    }

    public void init(final LocationRepository repository) {
        mLocationRepository = repository;
    }

    public LiveData<List<RecordingEntry>> getEntries() {
        return mEntries;
    }

    public void startRecording(final Context context, final Recording.Type type) {
        Log.d(TAG, "startRecording() called with: context = [" + context + "]");
        mLocationRepository.addRecordingObserver(mRecordingObserver);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final long timeStamp = System.currentTimeMillis();
                SimpleDateFormat format = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault());
                Recording recording = new Recording(
                        format.format(new Date(timeStamp)),
                        type,
                        true,
                        timeStamp);
                final long recordingId = mDatabase.getRecordingDao().add(recording);
                Log.d(TAG, "run: start recording id: "+recordingId);
                recording = mDatabase.getRecordingDao().getRecording(recordingId);
                mLocationRepository.startRecording(recording);
            }
        });
    }

    public void stopRecording(final Context context) {
        Log.d(TAG, "stopRecording() called with: context = [" + context + "]");
        mLocationRepository.stopRecording();
        mLocationRepository.removeRecordingObserver(mRecordingObserver);
    }

    public void requestUpdate() {
        mRecordingObserver.onRecordingUpdate(mRecordingObserver.mRecordingId);
    }

    @Override
    protected void onCleared() {
        Log.d(TAG, "onCleared: ");
        super.onCleared();
        mHandlerThread.quitSafely();
    }

    private class RecordingObserver implements LocationRepository.RecordingObserver {
        private long mRecordingId;

        @Override
        public void onRecordingUpdate(final long recordingId) {
            mRecordingId = recordingId;
            Log.d(TAG, "onRecordingUpdate() called with: recordingId = [" + recordingId + "]");
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEntries.postValue(mDatabase
                            .getRecordingEntryDao()
                            .getTrackEntries(recordingId));
                }
            });
        }
    }
}
