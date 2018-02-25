package de.gotovoid.database.access;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Update;

import java.util.Collection;

/**
 * {@link Dao} defining the common basic interactions with a
 * {@link android.arch.persistence.room.RoomDatabase} table.
 * <p>
 * Created by DJ on 12/01/18.
 */
@Dao
public interface GenericDao<Data> {
    /**
     * Adds the given {@link Data} to the table.
     *
     * @param data {@link Data} to be added
     */
    @Insert
    long add(final Data data);

    /**
     * Adds the given {@link Data}s to the table.
     *
     * @param datas {@link Data} to be added.
     */
    @Insert
    long[] addAll(final Data... datas);

    /**
     * Adds the given {@link Collection} of {@link Data} to the table.
     *
     * @param datas {@link Data} to be added
     */
    @Insert
    long[] addAll(final Collection<Data> datas);

    /**
     * Updates the given {@link Data} in the table.
     *
     * @param data {@link Data} to be updated
     */
    @Update
    void update(final Data data);

    /**
     * Removes the given {@link Data} from the table.
     *
     * @param data {@link Data} to be removed
     */
    @Delete
    void remove(final Data data);

    /**
     * Removes the given {@link Data}s from the table.
     *
     * @param datas {@link Data}s to be removed
     */
    @Delete
    void removeAll(final Data... datas);

    /**
     * Adds the given {@link Collection} of {@link Data} to the table.
     *
     * @param datas {@link Data}s to be removed
     */
    @Delete
    void removeAll(final Collection<Data> datas);
}
