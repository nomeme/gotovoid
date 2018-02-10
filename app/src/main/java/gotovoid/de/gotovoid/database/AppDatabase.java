package gotovoid.de.gotovoid.database;

import android.app.Application;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import gotovoid.de.gotovoid.database.access.CalibratedPressureDao;
import gotovoid.de.gotovoid.database.access.RecordingDao;
import gotovoid.de.gotovoid.database.access.RecordingEntryDao;
import gotovoid.de.gotovoid.database.model.CalibratedAltitude;
import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.database.model.RecordingEntry;
import gotovoid.de.gotovoid.database.model.RecordingWithEntries;
import gotovoid.de.gotovoid.domain.model.GPXParser;

/**
 * {@link RoomDatabase} for the {@link Application}.
 * <p>
 * Created by DJ on 24/12/17.
 */
@Database(entities = {CalibratedAltitude.class, Recording.class, RecordingEntry.class},
        version = 1,
        exportSchema = false)
@TypeConverters({Recording.Type.Converter.class})
public abstract class AppDatabase extends RoomDatabase {
    private static final String TAG = AppDatabase.class.getSimpleName();
    private static AppDatabase sInstance;

    /**
     * Return the {@link AppDatabase} bound to the {@link Application#getApplicationContext()}.
     *
     * @param application the {@link Application}
     * @return the {@link AppDatabase}
     */
    public static AppDatabase getDatabaseInstance(final Application application) {
        if (sInstance == null) {
            sInstance = build(application);
        }
        return sInstance;
    }

    /**
     * Create the database instance and pre fill with some data.
     *
     * @param application the {@link Application}.
     * @return the {@link AppDatabase} instance
     */
    private static AppDatabase build(final Application application) {
        return Room.databaseBuilder(application.getApplicationContext(),
                AppDatabase.class, "app_database")
                // prepopulate the database
                .addCallback(new Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        super.onCreate(db);
                        List<String> files = new ArrayList<>();
                        AssetManager manager = application.getAssets();
                        try {
                            for (String file : manager.list("")) {
                                if (file.endsWith(".gpx")) {
                                    files.add(file);
                                    Log.d(TAG, "onCreate: " + file);
                                }
                            }
                        } catch (final IOException exception) {
                            Log.e(TAG, "onCreate: ", exception);
                        }
                        final List<RecordingWithEntries> recordings =
                                new ArrayList<>(files.size());
                        for (String file : files) {
                            try {
                                final InputStream inputStream = manager.open(file);
                                final RecordingWithEntries recording =
                                        GPXParser.parseRecording(inputStream);
                                if (recording != null) {
                                    Log.d(TAG, "onCreate: add: " + recording.getRecording().getName());
                                    recordings.add(recording);
                                }
                            } catch (final XmlPullParserException | IOException exception) {
                                Log.e(TAG, "run: ", exception);
                            }
                        }
                        final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                            @Override
                            protected Void doInBackground(Void... voids) {
                                for (RecordingWithEntries recording : recordings) {
                                    if (recording == null
                                            || recording.getRecording() == null
                                            || recording.getEntries() == null
                                            || recording.getEntries().isEmpty()) {
                                        continue;
                                    }
                                    long id = getDatabaseInstance(application).getRecordingDao()
                                            .add(recording.getRecording());
                                    for (RecordingEntry entry : recording.getEntries()) {
                                        entry.setRecordingId(id);
                                    }
                                    getDatabaseInstance(application).getRecordingEntryDao()
                                            .addAll(recording.getEntries());
                                }
                                return null;
                            }
                        };
                        task.execute();
                    }
                })
                .build();
    }

    /**
     * Returns the {@link CalibratedPressureDao} to access the {@link CalibratedAltitude} table.
     *
     * @return the {@link CalibratedPressureDao}
     */
    public abstract CalibratedPressureDao getCalibratedPressureDao();

    /**
     * Returns the {@link RecordingDao} to access the {@link Recording} table.
     *
     * @return the {@link RecordingDao}
     */
    public abstract RecordingDao getRecordingDao();

    /**
     * Returns the {@link RecordingEntryDao} to access the {@link RecordingEntry} table.
     *
     * @return the {@link RecordingEntryDao}
     */
    public abstract RecordingEntryDao getRecordingEntryDao();

}
