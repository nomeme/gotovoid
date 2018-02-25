package de.gotovoid.database.model;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Relation;

import java.util.List;

/**
 * This class is a value holder class to hold a {@link Recording} and it's respective
 * {@link RecordingEntry}s.
 * <p>
 * Created by DJ on 14/01/18.
 */
public class RecordingWithEntries {
    @Embedded
    public Recording mRecording;
    @Relation(parentColumn = "id",
            entityColumn = "recording_id")
    public List<RecordingEntry> mEntries;

    public RecordingWithEntries() {
    }

    /**
     * Constructor setting the {@link Recording} and {@link RecordingEntry}s.
     *
     * @param recording the {@link Recording}
     * @param entries   the {@link RecordingEntry}
     */
    public RecordingWithEntries(final Recording recording,
                                final List<RecordingEntry> entries) {
        this.mRecording = recording;
        this.mEntries = entries;
    }

    public void setRecording(final Recording recording) {
        mRecording = recording;
    }

    /**
     * Returns the {@link Recording}.
     *
     * @return the {@link Recording}
     */
    public Recording getRecording() {
        return mRecording;
    }

    public void setEntries(final List<RecordingEntry> entries) {
        mEntries = entries;
    }

    /**
     * Returns the {@link RecordingEntry}s.
     *
     * @return the {@link RecordingEntry}s
     */
    public List<RecordingEntry> getEntries() {
        return mEntries;
    }
}
