package de.gotovoid.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.TypeConverter;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Object representing a recording.
 * A {@link Recording} contains the name.
 * <p>
 * Created by DJ on 22/12/17.
 */
@Entity(tableName = "recording")
public class Recording extends GenericEntity<Recording> {
    /**
     * The name of the {@link Recording}.
     */
    @ColumnInfo(name = "name")
    private String mName;
    /**
     * The {@link Type} of the track.
     */
    @ColumnInfo(name = "type")
    @TypeConverters({Type.Converter.class})
    private Type mType;
    /**
     * Time the recording was started.
     */
    @ColumnInfo(name = "time_stamp")
    private long mTimeStamp;
    /**
     * The recording state. True if the {@link Recording} is currently being recorded.
     */
    @ColumnInfo(name = "recording")
    private boolean mIsRecording;

    /**
     * Constructor for the database. Takes the unique id, name, and recording state of the
     * {@link Recording}.
     *
     * @param name        name
     * @param isRecording recording state
     */
    public Recording(final String name,
                     final Type type,
                     final boolean isRecording,
                     final long timeStamp) {
        mName = name;
        mType = type;
        mIsRecording = isRecording;
        mTimeStamp = timeStamp;
    }

    /**
     * Returns the name of the {@link Recording}.
     *
     * @return the name
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns the {@link Type} of the recording.
     *
     * @return the type
     */
    public Type getType() {
        return mType;
    }

    /**
     * Returns the time stamp when the {@link Recording} was started.
     *
     * @return the time stamp
     */
    public long getTimeStamp() {
        return mTimeStamp;
    }

    /**
     * Returns true if the {@link Recording} is currently being recorded.
     *
     * @return true if recording.
     */
    public boolean isRecording() {
        return mIsRecording;
    }

    /**
     * Sets the recording state of the {@link Recording}.
     * True if currently recording.
     *
     * @param recording true if recording
     */
    public void setRecording(final boolean recording) {
        mIsRecording = recording;
    }

    @Override
    public boolean isContentEqual(@NonNull final Recording entity) {
        return getId() == entity.getId()
                && getType() == entity.getType()
                && isRecording() == entity.isRecording()
                && getTimeStamp() == entity.getTimeStamp()
                && getName().equals(entity.getName());
    }

    /**
     * The type of the {@link Recording}.
     * The {@link Recording} can be of several types listed in this {@link Enum}.
     */
    public enum Type {
        HIKE,
        FLIGHT;

        /**
         * Converter for the {@link Type} to be used by the
         * {@link android.arch.persistence.room.RoomDatabase}.
         * Converts the {@link Type} to a {@link String} value that can be stored in the
         * {@link android.arch.persistence.room.RoomDatabase}.
         * <p>
         * Created by DJ on 13/01/18.
         */
        public static class Converter {
            /**
             * Converts a {@link Type} object to a {@link String} object.
             *
             * @param type {@link Type} to be converted
             * @return {@link String}
             */
            @TypeConverter
            @NonNull
            public static String toString(@NonNull final Recording.Type type) {
                return type.toString();
            }

            /**
             * Converts a {@link String} value back to a {@link Type} object.
             * Returns null if invalid{@link String} is passed.
             *
             * @param string {@link String} to be converted
             * @return {@link Type}
             */
            @Nullable
            @TypeConverter
            public static Recording.Type toType(@NonNull final String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                try {
                    return Recording.Type.valueOf(string);
                } catch (final IllegalArgumentException exception) {
                    return null;
                }
            }
        }
    }
}
