package gotovoid.de.gotovoid.service.communication;

import android.os.Parcel;
import android.os.Parcelable;

import gotovoid.de.gotovoid.service.sensors.SensorType;

/**
 * Created by DJ on 17/02/18.
 */

/**
 * This class represents a callback registration for asynchronous AIDL communication.
 * The class is {@link Parcelable} in order to be transmitted via AIDL.
 * It contains the necessary data for the service to identify the callback and
 * send sensor updates of the given {@link SensorType} to the {@link ISensorServiceCallback}
 * in the requested update frequency.
 */
public class CallbackRegistration implements Parcelable {
    /**
     * Creator for the {@link Parcelable}.
     */
    public static final Parcelable.Creator<CallbackRegistration> CREATOR =
            new Parcelable.Creator<CallbackRegistration>() {
                @Override
                public CallbackRegistration createFromParcel(final Parcel source) {
                    return new CallbackRegistration(source);
                }

                @Override
                public CallbackRegistration[] newArray(int size) {
                    return new CallbackRegistration[size];
                }
            };

    /**
     * Type of the sensor.
     */
    private final SensorType mType;
    /**
     * Id of the callback generated using {@link System#identityHashCode(Object)}.
     */
    private final int mCallbackId;
    /**
     * Requested update frequency in milliseconds.
     */
    private final long mUpdateFrequency;

    /**
     * Construcor taking the {@link SensorType},  {@link ISensorServiceCallback} and update
     * frequency in milliseconds.
     *
     * @param type            {@link SensorType}
     * @param callback        {@link ISensorServiceCallback}
     * @param updateFrequency update frequency in milliseconds
     */
    public CallbackRegistration(final SensorType type,
                                final ISensorServiceCallback callback,
                                final long updateFrequency) {
        mType = type;
        mCallbackId = System.identityHashCode(callback);
        mUpdateFrequency = updateFrequency;
    }

    /**
     * Constructor taking a {@link Parcel} to reconstruct the {@link CallbackRegistration}.
     *
     * @param parcel the {@link Parcel} containing the data
     */
    private CallbackRegistration(final Parcel parcel) {
        mType = SensorType.values()[parcel.readInt()];
        mCallbackId = parcel.readInt();
        mUpdateFrequency = parcel.readLong();
    }

    /**
     * returns the Type of the sensor.
     *
     * @return type {@link SensorType}
     */
    public SensorType getType() {
        return mType;
    }

    /**
     * Returns the id of the {@link ISensorServiceCallback}.
     *
     * @return the id of the {@link ISensorServiceCallback}
     */
    public int getCallbackId() {
        return mCallbackId;
    }

    /**
     * Returns the requested update frequency.
     *
     * @return the requested update frequency
     */
    public long getUpdateFrequency() {
        return mUpdateFrequency;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, int flags) {
        dest.writeInt(mType.ordinal());
        dest.writeInt(mCallbackId);
        dest.writeLong(mUpdateFrequency);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("{type: ");
        builder.append(getType());
        builder.append(", callbackId: ");
        builder.append(getCallbackId());
        builder.append(", updateFrequency: ");
        builder.append(getUpdateFrequency());
        builder.append('}');
        return builder.toString();
    }
}
