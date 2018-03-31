package de.gotovoid.service.repository;

import android.os.RemoteException;
import android.util.Log;

import java.io.Serializable;

import de.gotovoid.components.arcitecture.IObservable;
import de.gotovoid.service.communication.CallbackRegistration;
import de.gotovoid.service.communication.ISensorServiceCallback;
import de.gotovoid.service.communication.Response;
import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.SensorType;

/**
 * Created by DJ on 20/03/18.
 */

/**
 * {@link IObservable.Observer} implementation for the repository.
 * This class serves as {@link IObservable.Observer} for the {@link LocationRepository}.
 * It will be registered at the {@link RepositoryObservable} to receive sensor updates.
 * In order to receive these updates from the {@link RepositoryObservable},
 * the {@link ISensorServiceCallback} must be extended.
 *
 * @param <T> type of the observed data.
 */
public abstract class RepositoryObserver<T extends Serializable>
        extends ISensorServiceCallback.Stub
        implements IObservable.Observer<AbstractSensor.Result<T>> {
    private static final String TAG = RepositoryObserver.class.getSimpleName();
    private final long mUpdateFrequency;
    private final SensorType mSensorType;
    private CallbackRegistration mCallbackRegistration;

    /**
     * Constructor taking the update frequency and {@link SensorType} needed for the callback
     * registration.
     *
     * @param updateFrequency frequency in ms to receive updates
     * @param sensorType      {@link SensorType} to receive updates for
     */
    public RepositoryObserver(final long updateFrequency,
                              final SensorType sensorType ) {
        mUpdateFrequency = updateFrequency;
        mSensorType = sensorType;
    }

    /**
     * Returns the frequency in ms to receive updates in.
     *
     * @return the update frequency
     */
    public long getUpdateFrequency() {
        return mUpdateFrequency;
    }

    /**
     * Returns the {@link SensorType}
     *
     * @return the {@link SensorType}
     */
    public SensorType getSensorType() {
        return mSensorType;
    }

    /**
     * Returns the {@link CallbackRegistration} for this {@link RepositoryObserver}.
     *
     * @return the {@link CallbackRegistration}
     */
    public CallbackRegistration getCallbackRegistration() {
        if (mCallbackRegistration == null) {
            mCallbackRegistration = new CallbackRegistration(getSensorType(), this, getUpdateFrequency());
        }
        return mCallbackRegistration;
    }

    @Override
    public void onSensorValueChanged(final Response response) throws RemoteException {
        Log.d(TAG, "onSensorValueChanged() called with: response = [" + response + "]");
        onChange(response.getValue());
    }

}
