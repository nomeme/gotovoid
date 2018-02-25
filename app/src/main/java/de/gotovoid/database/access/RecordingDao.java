package de.gotovoid.database.access;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Transaction;

import java.util.List;

import de.gotovoid.database.model.RecordingWithEntries;
import de.gotovoid.database.model.Recording;

/**
 * {@link Dao} for the {@link Recording}.
 * Defines access methods to the {@link Recording} table of a
 * {@link android.arch.persistence.room.RoomDatabase}.
 * <p>
 * Created by DJ on 22/12/17.
 */
@Dao
public interface RecordingDao extends GenericDao<Recording> {
    /**
     * Get all {@link Recording}s in an observable {@link LiveData}.
     *
     * @return all {@link Recording}s
     */
    @Query("SELECT * FROM recording")
    LiveData<List<Recording>> observeAll();

    /**
     * Returns the {@link Recording} with the given {@link Recording#getId()}.
     *
     * @param id {@link Recording#getId()}
     * @return the {@link Recording}
     */
    @Query("SELECT * FROM recording where id = :id")
    Recording getRecording(final long id);

    /**
     * Returns the {@link Recording} with the given {@link Recording#getId()}.
     *
     * @param id {@link Recording#getId()}
     * @return the {@link Recording}
     */
    @Query("SELECT * FROM recording where id = :id")
    LiveData<Recording> observeRecording(final long id);

    /**
     * Returns the {@link RecordingWithEntries} with the given
     * {@link Recording#getId()}.
     *
     * @param id id of the {@link Recording}
     * @return {@link RecordingWithEntries}
     */
    @Query("SELECT * FROM recording where id = :id")
    @Transaction
    RecordingWithEntries getRecordingWithEntries(final long id);

    @Query("SELECT * FROM recording WHERE id = :id")
    @Transaction
    LiveData<RecordingWithEntries> observeRecordingWithEntries(final long id);
}
