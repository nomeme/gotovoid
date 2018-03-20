package de.gotovoid.service.communication;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.SparseArray;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.SensorHandler;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.service.sensors.SensorType;

/**
 * Created by DJ on 16/02/18.
 */

/**
 * Implementation of the {@link ISensorService}.
 * This implements the service side AIDL interface and enables to addObserver and unregister
 * callbacks for the sensor service.
 */
public class SensorServiceBinder extends ISensorService.Stub {
    private static final String TAG = SensorServiceBinder.class.getSimpleName();

    /**
     * {@link Map} containing references to the {@link Callback} referenced by {@link SensorType}
     * and {@link CallbackRegistration#getCallbackId()}.
     */
    // TODO: use sparse array!
    private final Map<SensorType, SparseArray<Callback>> mCallbacks;
    /**
     * Synchronization object.
     */
    private final Object mLock = new Object();
    /**
     * Handler for the sensor instances.
     */
    private final SensorHandler mSensorHandler;
    /**
     * Set to true if the client requested the updates to be paused.
     * TODO: maybe consider moving this into the Callbacks
     */
    private boolean mIsUpdatePaused;

    /**
     * True if there was a request to start a new recording.
     */
    private boolean mIsRecordingStartRequested;

    /**
     * Create a new instance of the {@link SensorServiceBinder} using the given
     * {@link SensorHandler} instance.
     *
     * @param sensorHandler the {@link SensorHandler}
     */
    public SensorServiceBinder(final SensorHandler sensorHandler) {
        mCallbacks = new HashMap<>();
        for (SensorType type : SensorType.values()) {
            mCallbacks.put(type, new SparseArray<Callback>());
        }
        mSensorHandler = sensorHandler;
    }

    @Override
    public void setUpdatePaused(final boolean isUpdatePaused) throws RemoteException {
        synchronized (mLock) {
            mIsUpdatePaused = isUpdatePaused;
        }
    }

    /**
     * Returns true if the updates from the sensors are currently paused.
     *
     * @return true if paused
     */
    boolean isUpdatePaused() {
        synchronized (mLock) {
            return mIsUpdatePaused;
        }
    }

    @Override
    public void startSensor(final CallbackRegistration registration,
                            final ISensorServiceCallback callback)
            throws RemoteException {
        Log.d(TAG, "startSensor() called with: registration = [" + registration
                + "], callback = [" + callback + "]");
        addCallback(registration, callback);
    }

    @Override
    public void stopSensor(final CallbackRegistration registration)
            throws RemoteException {
        Log.d(TAG, "stopSensor() called with: registration = [" + registration + "]");
        removeCallback(registration);
    }

    @Override
    public void requestUpdate(final CallbackRegistration registration)
            throws RemoteException {
        // TODO:implement
    }

    @Override
    public void startRecording(long recordingId) throws RemoteException {
        synchronized (mLock) {
            if (!mIsRecordingStartRequested) {
                mIsRecordingStartRequested = true;
                mSensorHandler.startRecording(recordingId);
            }
        }
    }

    @Override
    public void stopRecording() throws RemoteException {
        synchronized (mLock) {
            mIsRecordingStartRequested = false;
            mSensorHandler.stopRecording();
        }
    }

    /**
     * Add a {@link Callback} to the managed {@link Callback}s and addObserver it at the {@link SensorHandler}.
     * Takes the {@link CallbackRegistration} to identify the callback and the
     * {@link ISensorServiceCallback} for the actual communication with the client.
     *
     * @param registration   the {@link CallbackRegistration}
     * @param sensorCallback the {@link ISensorServiceCallback}
     */
    private void addCallback(final CallbackRegistration registration,
                             final ISensorServiceCallback sensorCallback) {
        Log.d(TAG, "addCallback() called with: registration = [" + registration
                + "], callback = [" + sensorCallback + "]");
        // Fail fast
        if (registration == null || registration.getType() == null || sensorCallback == null) {
            return;
        }
        synchronized (mCallbacks) {
            // TODO: minimize synchronized block.
            // Get the map of callbacks for the appropriate SensorType
            SparseArray<Callback> callbacks = mCallbacks.get(registration.getType());
            // Check whether the callback is already registered
            if (callbacks.get(registration.getCallbackId()) == null) {
                Log.d(TAG, "addCallback: add new callback: " + registration.getType());
                // Create a local Observer instance to be stored in the map
                final Callback callback = createCallback(registration, sensorCallback);
                if (callback != null) {
                    // Store the Observer and addObserver it with the SensorHandler.
                    callbacks.put(registration.getCallbackId(), callback);
                    mSensorHandler.addObserver(callback.getObserver());
                }
            } else {
                Log.d(TAG, "addCallback: callback already registered");
            }
            Log.d(TAG, "addCallback: callbacks " + callbacks.size());
        }
    }

    /**
     * Removes the {@link Callback} from the managed {@link Callback}s and {@link SensorHandler}.
     * Uses the {@link CallbackRegistration} to identify the {@link Callback} to be removed
     *
     * @param registration the {@link CallbackRegistration}
     */
    private void removeCallback(final CallbackRegistration registration) {
        Log.d(TAG, "removeCallback() called with: registration = [" + registration + "]");
        if (registration == null || registration.getType() == null) {
            return;
        }
        synchronized (mCallbacks) {
            // TODO: minimize synchronized block.
            // Get the appropriate map for the SensorType
            SparseArray<Callback> callbacks = mCallbacks.get(registration.getType());
            // Check whether the Observer is actually managed.
            if (callbacks.get(registration.getCallbackId()) == null) {
                Log.d(TAG, "removeCallback: callback not registered");
            } else {
                // If the callback is managed, remove it from the managed Callbacks.
                Log.d(TAG, "removeCallback: remove callback");
                final Callback callback = callbacks.get(registration.getCallbackId());
                callbacks.remove(registration.getCallbackId());
                if (callbacks.size() == 0) {
                    // Then also remove it from the SensorHandler.
                    mSensorHandler.removeObserver(callback.getObserver());
                }
            }
            Log.d(TAG, "removeCallback: callbacks: " + callbacks.size());
        }
    }

    /**
     * Returns the {@link Callback} for the given {@link CallbackRegistration}.
     *
     * @param registration the {@link CallbackRegistration}
     * @return the {@link Callback}
     */
    Callback getCallback(final CallbackRegistration registration) {
        if (registration == null || registration.getType() == null) {
            return null;
        }
        synchronized (mCallbacks) {
            SparseArray<Callback> callbacks = mCallbacks.get(registration.getType());
            return callbacks.get(registration.getCallbackId());
        }
    }

    /**
     * Generates a {@link Callback} instance to be more easy to store and manage.
     * Also creates an {@link AbstractSensor.Observer}
     * instance to provide the {@link SensorHandler}.
     * Takes the {@link CallbackRegistration} to extract the {@link SensorType} and callback id
     * so the given {@link ISensorServiceCallback} can be stored and managed more easy.
     *
     * @param registration the {@link CallbackRegistration}
     * @param callback     the {@link ISensorServiceCallback}
     * @return a new {@link Callback} instance
     */
    private Callback createCallback(@NonNull final CallbackRegistration registration,
                                    @NonNull final ISensorServiceCallback callback) {
        Log.d(TAG, "getObserver() called with: type = [" + registration + "]");
        // Fail fast.
        if (registration == null || registration.getType() == null || callback == null) {
            return null;
        }
        final AbstractSensor.Observer observer;
        switch (registration.getType()) {
            case PRESSURE:
                observer = new SensorObserver<Float>(
                        callback,
                        registration.getUpdateFrequency(),
                        registration.getType());
                break;
            case LOCATION:
                observer = new SensorObserver<ExtendedGeoCoordinate>(
                        callback,
                        registration.getUpdateFrequency(),
                        registration.getType());
                break;
            case RECORDING:
                observer = new SensorObserver<Long>(
                        callback,
                        registration.getUpdateFrequency(),
                        registration.getType());
                break;
            default:
                observer = null;
                break;
        }

        // Create an Observable for a sensor to be stored and managed.
        if (observer == null) {
            return null;
        } else {
            // Create the Observer to be returned
            return new Callback(callback, observer);
        }
    }

    private class SensorObserver<T extends Serializable> extends AbstractSensor.Observer<T> {
        private ISensorServiceCallback mCallback;

        /**
         * Constructor taking the update frequency in milliseconds and {@link SensorType}.
         *
         * @param updateFrequency the update frequency requested by the {@link AbstractSensor.Observer}
         * @param type            the {@link SensorType}
         */
        public SensorObserver(final ISensorServiceCallback callback,
                              final long updateFrequency,
                              final SensorType type) {
            super(updateFrequency, type);
            mCallback = callback;
        }

        @Override
        public void onChange(@NonNull final AbstractSensor.Result<T> result) {
            if (isUpdatePaused()) {
                return;
            }
            try {
                mCallback.onSensorValueChanged(new Response(result));
            } catch (final RemoteException exception) {
                Log.e(TAG, "onChange: ", exception);
            }
        }
    }

    /**
     * Data holder for the {@link AbstractSensor.Observer} and the {@link ISensorServiceCallback}.
     */
    class Callback {
        /**
         * The {@link ISensorServiceCallback} to for the service listener.
         */
        final ISensorServiceCallback mCallback;
        /**
         * The {@link AbstractSensor.Observer} for the {@link SensorHandler}.
         */
        final AbstractSensor.Observer mObserver;

        /**
         * Constructor taking the {@link ISensorServiceCallback} and
         * {@link AbstractSensor.Observer}.
         *
         * @param callback the {@link ISensorServiceCallback}
         * @param observer the {@link AbstractSensor.Observer}
         */
        public Callback(final ISensorServiceCallback callback,
                        final AbstractSensor.Observer observer) {
            mCallback = callback;
            mObserver = observer;
        }

        /**
         * Returns the {@link ISensorServiceCallback}.
         *
         * @return the {@link ISensorServiceCallback}
         */
        public ISensorServiceCallback getCallback() {
            return mCallback;
        }

        /**
         * Returns the {@link AbstractSensor.Observer}.
         *
         * @return the {@link AbstractSensor.Observer}
         */
        public AbstractSensor.Observer getObserver() {
            return mObserver;
        }
    }
}
