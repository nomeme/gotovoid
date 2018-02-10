package gotovoid.de.gotovoid.database.access;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;

import java.util.List;

import gotovoid.de.gotovoid.database.model.RecordingEntry;

/**
 * {@link Dao} for the {@link RecordingEntry}.
 * Defines access methods to the {@link RecordingEntry} table of a
 * {@link android.arch.persistence.room.RoomDatabase}.
 * <p>
 * Created by DJ on 22/12/17.
 */
@Dao
public interface RecordingEntryDao extends GenericDao<RecordingEntry> {

    @Query("SELECT * FROM recording_entry")
    LiveData<List<RecordingEntry>> observeAll();

    @Query("SELECT * FROM recording_entry")
    List<RecordingEntry> getAll();

    @Query("SELECT * FROM recording_entry WHERE recording_id = :recordingId")
    List<RecordingEntry> getTrackEntries(final long recordingId);

    @Query("SELECT * FROM recording_entry WHERE recording_id = :recordingId")
    LiveData<List<RecordingEntry>> observeTrackEntries(final long recordingId);
}
