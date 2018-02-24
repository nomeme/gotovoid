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
import gotovoid.de.gotovoid.service.sensors.SensorType;

/**
 * Created by DJ on 20/01/18.
 */

/**
 * This {@link android.arch.lifecycle.ViewModel} handles all the data needed to show the current
 * state of recording.
 */
public class RecorderViewModel extends AndroidViewModel {
    private static final String TAG = RecorderViewModel.class.getSimpleName();
    private static final long UPDATE_FREQUENCY = 1000;

    /**
     * Database containing the {@link Recording} data.
     */
    private final AppDatabase mDatabase;
    /**
     * {@link HandlerThread} for database communication.
     */
    private final HandlerThread mHandlerThread;
    /**
     * {@link Handler} running on the {@link HandlerThread}'s {@link android.os.Looper}.
     */
    private final Handler mHandler;
    /**
     * Observer for the repository.
     */
    private final LocationRepository.ServiceObserver<Long> mObserver;

    /**
     * The {@link List} of {@link RecordingEntry}s to be displayed.
     */
    private final MutableLiveData<List<RecordingEntry>> mEntries;
    /**
     * Repository providing sensor data.
     */
    private LocationRepository mLocationRepository;

    /**
     * Constructor taking the {@link Application}.
     *
     * @param application the {@link Application}
     */
    public RecorderViewModel(@NonNull final Application application) {
        super(application);
        mDatabase = AppDatabase.getDatabaseInstance(application);

        // Start the handler.
        mHandlerThread = new HandlerThread("Looper");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        /*
         Create the LiveData object for the RecordingEntries.
         This has to be posted new data manually on feedback from the repository, because
         Room not yet provides events through inter process communication.
          */
        mEntries = new MutableLiveData<>();
        mObserver = new LocationRepository.ServiceObserver<Long>(UPDATE_FREQUENCY,
                SensorType.RECORDING) {

            @Override
            public void onChange(final Long recordingId) {
                Log.d(TAG, "onRecordingUpdate() called with: recordingId = ["
                        + recordingId + "]");
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mEntries.postValue(mDatabase
                                .getRecordingEntryDao()
                                .getTrackEntries(recordingId));
                    }
                });
            }
        };
    }

    /**
     * Initialize the {@link RecorderViewModel} with the appropriate {@link LocationRepository}.
     *
     * @param repository the {@link LocationRepository}
     */
    public void init(final LocationRepository repository) {
        mLocationRepository = repository;
    }

    /**
     * Return the {@link RecordingEntry} objects to be displayed.
     *
     * @return the {@link RecordingEntry}
     */
    public LiveData<List<RecordingEntry>> getEntries() {
        return mEntries;
    }

    /**
     * Start a new recording given the {@link Recording.Type}.
     *
     * @param type the {@link Recording.Type}
     */
    public void startRecording(final Recording.Type type) {
        Log.d(TAG, "startRecording() called with: type = [" + type + "]");
        /*
         Use the Handler to add a new Recording to the database and tell the repository to
         start a new recording.
          */
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                final long timeStamp = System.currentTimeMillis();
                // Create the date format for the name.
                SimpleDateFormat format = new SimpleDateFormat(
                        // TODO: store this somewhere centralized.
                        "yyyy-MM-dd'T'HH:mm:ss",
                        Locale.getDefault());
                // Create the new recording.
                Recording recording = new Recording(
                        format.format(new Date(timeStamp)),
                        type,
                        true,
                        timeStamp);
                // Retreive the id of the newly added recording.
                final long recordingId = mDatabase.getRecordingDao().add(recording);

                Log.d(TAG, "run: start recording id: " + recordingId);
                // Tell the repository to start a new recording.
                recording = mDatabase.getRecordingDao().getRecording(recordingId);
                mLocationRepository.startRecording(recording);
                mLocationRepository.addObserver(mObserver);
            }
        });
    }

    /**
     * Stop the recording.
     */
    public void stopRecording() {
        mLocationRepository.removeObserver(mObserver);
        mLocationRepository.stopRecording();
    }

    // TODO: When is this used?
    public void requestUpdate() {
        // mRecordingObserver.onRecordingUpdate(mRecordingObserver.mRecordingId);
    }

    @Override
    protected void onCleared() {
        Log.d(TAG, "onCleared: ");
        super.onCleared();
        mHandlerThread.quitSafely();
    }
}
