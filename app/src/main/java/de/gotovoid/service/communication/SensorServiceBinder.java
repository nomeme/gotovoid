package de.gotovoid.service.communication;

import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import de.gotovoid.service.sensors.AbstractSensor;
import de.gotovoid.service.sensors.PressureSensor;
import de.gotovoid.service.sensors.SensorHandler;
import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.service.communication.ISensorService;
import de.gotovoid.service.communication.ISensorServiceCallback;
import de.gotovoid.service.sensors.LocationSensor;
import de.gotovoid.service.sensors.RecordingSensor;
import de.gotovoid.service.sensors.SensorType;

/**
 * Created by DJ on 16/02/18.
 */

/**
 * Implementation of the {@link ISensorService}.
 * This implements the service side AIDL interface and enables to register and unregister
 * callbacks for the sensor service.
 */
public class SensorServiceBinder extends ISensorService.Stub {
    private static final String TAG = SensorServiceBinder.class.getSimpleName();

    /**
     * {@link Map} containing references to the {@link Callback} referenced by {@link SensorType}
     * and {@link CallbackRegistration#getCallbackId()}.
     */
    private final Map<SensorType, Map<Integer, Callback>> mCallbacks;
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
     * Create a new instance of the {@link SensorServiceBinder} using the given
     * {@link SensorHandler} instance.
     *
     * @param sensorHandler the {@link SensorHandler}
     */
    public SensorServiceBinder(final SensorHandler sensorHandler) {
        mCallbacks = new HashMap<>();
        for (SensorType type : SensorType.values()) {
            mCallbacks.put(type, new HashMap());
        }
        mSensorHandler = sensorHandler;
    }

    @Override
    public void setUpdatePaused(final boolean isUpdatePaused) throws RemoteException {
        synchronized (mLock) {
            mIsUpdatePaused = isUpdatePaused;
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

    }

    @Override
    public void startRecording(long recordingId) throws RemoteException {
        mSensorHandler.startRecording(recordingId);
    }

    @Override
    public void stopRecording() throws RemoteException {
        mSensorHandler.stopRecording();
    }

    /**
     * Add a {@link Callback} to the managed {@link Callback}s and register it at the {@link SensorHandler}.
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
            Map<Integer, Callback> callbacks = mCallbacks.get(registration.getType());
            // Check whether the callback is already registered
            if (callbacks.get(registration.getCallbackId()) == null) {
                Log.d(TAG, "addCallback: add new callback: " + registration.getType());
                // Create a local Callback instance to be stored in the map
                final Callback callback = getCallback(registration, sensorCallback);
                if (callback != null) {
                    // Store the Callback and register it with the SensorHandler.
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
            Map<Integer, Callback> callbacks = mCallbacks.get(registration.getType());
            // Check whether the Callback is actually managed.
            if (callbacks.get(registration.getCallbackId()) == null) {
                Log.d(TAG, "removeCallback: callback not registered");
            } else {
                // If the callback is managed, remove it from the managed Callbacks.
                Log.d(TAG, "removeCallback: remove callback");
                final Callback callback = callbacks.remove(registration.getCallbackId());
                if (callbacks.isEmpty()) {
                    // Then also remove it from the SensorHandler.
                    mSensorHandler.removeObserver(callback.getObserver());
                }
            }
            Log.d(TAG, "removeCallback: callbacks: " + callbacks.size());
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
    private Callback getCallback(@NonNull final CallbackRegistration registration,
                                 @NonNull final ISensorServiceCallback callback) {
        Log.d(TAG, "getObserver() called with: type = [" + registration + "]");
        // Fail fast.
        if (registration == null || registration.getType() == null || callback == null) {
            return null;
        }
        final AbstractSensor.Observer observer;
        // Create an Observer for a sensor to be stored and managed.
        switch (registration.getType()) {
            case PRESSURE:
                observer = new PressureSensor.Observer(registration.getUpdateFrequency()) {
                    @Override
                    public void onChange(@NonNull final Float aFloat) {
                        if (mIsUpdatePaused) {
                            return;
                        }
                        Log.d(TAG, "onChange() called with: aFloat = ["
                                + aFloat + "]");
                        // TODO: maybe we can generalize this.
                        try {
                            callback.onSensorValueChanged(new Response<>(aFloat));
                        } catch (final RemoteException exception) {
                            Log.e(TAG, "onChange: ", exception);
                        }
                    }
                };
                break;
            case LOCATION:
                observer = new LocationSensor.Observer(registration.getUpdateFrequency()) {
                    @Override
                    public void onChange(@NonNull final ExtendedGeoCoordinate location) {
                        if (mIsUpdatePaused) {
                            return;
                        }
                        Log.d(TAG, "onChange() called with: location = ["
                                + location + "]");
                        Response<ExtendedGeoCoordinate> coord = new Response<>(location);
                        // TODO: maybe we can generalize this.
                        Log.d(TAG, "onChange: ");
                        try {
                            callback.onSensorValueChanged(new Response(location));
                        } catch (final RemoteException exception) {
                            Log.e(TAG, "onChange: ", exception);
                        }
                    }
                };
                break;
            case RECORDING:
                observer = new RecordingSensor.Observer(registration.getUpdateFrequency()) {
                    @Override
                    public void onChange(@NonNull final Long recordingId) {
                        if (mIsUpdatePaused) {
                            return;
                        }
                        Log.d(TAG, "onChange() called with: recordingId = ["
                                + recordingId + "]");
                        // TODO: maybe we can generalize this.
                        try {
                            callback.onSensorValueChanged(new Response(recordingId));
                        } catch (final RemoteException exception) {
                            Log.e(TAG, "onChange: ", exception);
                        }
                    }
                };
                break;
            default:
                observer = null;
                break;
        }
        if (observer == null) {
            return null;
        } else {
            // Create the Callback to be returned
            return new Callback(callback, observer);
        }
    }

    /**
     * Data holder for the {@link AbstractSensor.Observer} and the {@link ISensorServiceCallback}.
     */
    private class Callback {
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
