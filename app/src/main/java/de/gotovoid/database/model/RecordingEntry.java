package de.gotovoid.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Object representing an entry of a {@link Recording}.
 * This contains the id of the {@link Recording} as well as the latitude, longitude and altitude.
 * The time stamp can be used to compute the speed between two {@link RecordingEntry}s.
 * <p>
 * Created by DJ on 22/12/17.
 */
@Entity(tableName = "recording_entry",
        indices = {@Index(value = "recording_id", name = "recording")},
        foreignKeys = @ForeignKey(entity = Recording.class,
                parentColumns = "id",
                childColumns = "recording_id",
                onDelete = ForeignKey.CASCADE))
public class RecordingEntry {
    @ColumnInfo(name = "id")
    @PrimaryKey(autoGenerate = true)
    private long mId;
    @ColumnInfo(name = "recording_id")
    private long mRecordingId;
    @ColumnInfo(name = "time_stamp")
    private long mTimeStamp;
    @ColumnInfo(name = "longitude")
    private double mLongitude;
    @ColumnInfo(name = "latitude")
    private double mLatitude;
    @ColumnInfo(name = "altitude")
    private int mAltitude;

    /**
     * Default Constructor.
     *
     * @param recordingId id of the recording
     * @param timeStamp   time stamp
     * @param longitude   longitude
     * @param latitude    latitude
     * @param altitude    altitude
     */
    public RecordingEntry(final long recordingId,
                          final long timeStamp,
                          final double longitude,
                          final double latitude,
                          final int altitude) {
        mRecordingId = recordingId;
        mTimeStamp = timeStamp;
        mLongitude = longitude;
        mLatitude = latitude;
        mAltitude = altitude;
    }

    /**
     * Constructor for minimal initialization.
     *
     * @param longitude the longitude
     * @param latitude  the latitude
     * @param altitude  the altitude
     */
    @Ignore
    public RecordingEntry(
            final double longitude,
            final double latitude,
            final int altitude) {
        mLongitude = longitude;
        mLatitude = latitude;
        mAltitude = altitude;
    }

    /**
     * Set the primary key for the {@link RecordingEntry}.
     *
     * @param primaryKey the primary key
     */
    public void setId(final long primaryKey) {
        mId = primaryKey;
    }

    /**
     * Returns the unique id of this {@link RecordingEntry}.
     *
     * @return the unique id
     */
    public long getId() {
        return mId;
    }

    /**
     * Set the uid of the recording.
     *
     * @param recordingId the id to set
     */
    public void setRecordingId(final long recordingId) {
        mRecordingId = recordingId;
    }

    /**
     * Returns the {@link Recording} id.
     *
     * @return the {@link Recording} id
     */
    public long getRecordingId() {
        return mRecordingId;
    }

    /**
     * Returns the time stamp.
     *
     * @return the time stamp
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Returns the longitude.
     *
     * @return the longitude
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * Returns the latitude.
     *
     * @return the latitude
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * Returns the altitude in meters.
     *
     * @return altitude in meters
     */
    public int getAltitude() {
        return mAltitude;
    }

}
