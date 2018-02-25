package de.gotovoid.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/**
 * Object defining the common properties for an {@link android.arch.persistence.room.Entity}.
 * <p>
 * Created by DJ on 13/01/18.
 */
public abstract class GenericEntity<Type extends GenericEntity> {
    /**
     * The unique id of the {@link Recording}.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long mId;

    /**
     * Set the id of the {@link Type}.
     *
     * @param primaryKey the id
     */
    public void setId(final long primaryKey) {
        mId = primaryKey;
    }

    /**
     * Returns the id of the {@link Type}.
     *
     * @return the id
     */
    public long getId() {
        return mId;
    }

    /**
     * Compares the stored values and returns true if they are all equal.
     *
     * @param entity {@link Type} to compare to
     * @return true if equal
     */
    public abstract boolean isContentEqual(@NonNull final Type entity);
}
