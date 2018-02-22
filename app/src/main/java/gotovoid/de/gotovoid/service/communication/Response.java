package gotovoid.de.gotovoid.service.communication;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by DJ on 17/02/18.
 */

/**
 * This class is a response for the service communication.
 * It is a {@link Parcelable} containing a {@link Serializable} object to be
 * transmitted to the application via an AIDL interface.
 *
 * @param <T> type of the data to be sent to the application
 */
public class Response<T extends Serializable> implements Parcelable {
    /**
     * The creator for the {@link Parcelable}.
     */
    public static final Parcelable.Creator<Response> CREATOR =
            new Parcelable.Creator<Response>() {
                @Override
                public Response createFromParcel(final Parcel source) {
                    return new Response(source);
                }

                @Override
                public Response[] newArray(int size) {
                    return new Response[size];
                }
            };
    /**
     * The value to be sent.
     */
    private final T mValue;

    /**
     * Creates a new {@link Response} to transmit the given value.
     *
     * @param value the value to be transmitted
     */
    public Response(final T value) {
        mValue = value;
    }

    /**
     * Create a {@link Response} from the given {@link Parcel}.
     *
     * @param parcel the {@link Parcel} containing the data
     */
    protected Response(final Parcel parcel) {
        mValue = (T) parcel.readSerializable();
    }

    /**
     * Returns the transmitted value.
     *
     * @return the value
     */
    public T getValue() {
        return mValue;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeSerializable(mValue);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getClass().getSimpleName());
        builder.append("{value: ");
        builder.append(getValue().toString());
        builder.append('}');
        return builder.toString();
    }
}
