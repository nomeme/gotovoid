package gotovoid.de.gotovoid.database.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

/**
 * This class defines a table to store the calibrated pressure value.
 * This is used to calculate the the current altitude using the calibrated pressure value.
 * <p>
 * Created by DJ on 24/12/17.
 */
@Entity(tableName = "calibrated_altitude")
public class CalibratedAltitude {
    @PrimaryKey
    @ColumnInfo(name = "id")
    private long mId;
    @ColumnInfo(name = "pressure")
    private float mPressure;
    @ColumnInfo(name = "altitude")
    private int mAltitude;

    /**
     * Constructor.
     *
     * @param id       id of the {@link CalibratedAltitude}
     * @param pressure the pressure
     * @param altitude the altitude
     */
    public CalibratedAltitude(final long id, final float pressure, final int altitude) {
        mId = id;
        mPressure = pressure;
        mAltitude = altitude;
    }

    /**
     * Returns the id of the {@link CalibratedAltitude}.
     *
     * @return the id
     */
    public long getId() {
        return mId;
    }

    /**
     * Returns the pressure in hPa.
     *
     * @return the pressure in hPa
     */
    public float getPressure() {
        return mPressure;
    }

    /**
     * Returns the altitude in meters.
     *
     * @return the altitude in meters.
     */
    public int getAltitude() {
        return mAltitude;
    }

    /**
     * Calculates the altitude in meters using the barometric height formula. Uses the stored
     * pressure and altitude values as basis to compute the height difference to
     * the pressure value provided in hPa.
     *
     * @param pressure pressure
     * @return altitude
     */
    @Ignore
    public int calculateHeight(final float pressure) {
        // Calculation using barometric height formula disregarding temperature influence.
        // As we do not have access to the current pressure at sea level we use differential height
        return (int) (44330 * (1 - Math.pow((pressure / mPressure), 1 / 5.255))) + mAltitude;
    }

}
