package gotovoid.de.gotovoid.database.access;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import gotovoid.de.gotovoid.database.model.CalibratedAltitude;

/**
 * {@link Dao} for the {@link CalibratedAltitude}.
 * Defines access methods to the {@link CalibratedAltitude} table of a
 * {@link android.arch.persistence.room.RoomDatabase}.
 * <p>
 * Created by DJ on 24/12/17.
 */
@Dao
public interface CalibratedPressureDao {

    /**
     * Returns the {@link CalibratedAltitude}.
     *
     * @return the {@link CalibratedAltitude}
     */
    @Query("SELECT * FROM calibrated_altitude WHERE id = 0")
    CalibratedAltitude getCalibratedPressure();
/*
    @Query("INSERT OR REPLACE INTO calibrated_pressures (id, pressure, altitude) values (0,
    :pressure.pressure, :pressure.getAltitude())")
    void setCalibratedPressure(final CalibratedAltitude pressure);

    @Query("UPDATE OR REPLACE calibrated_pressure SET pressure = :pressure, altitude = :altitude
    WHERE id = 0")
    void setCalibratedPressure(final double pressure, final int altitude);*/

    /**
     * Sets the {@link CalibratedAltitude}.
     *
     * @param calibratedAltitude thr {@link CalibratedAltitude} to set
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void setCalibratedPressure(final CalibratedAltitude calibratedAltitude);
}
