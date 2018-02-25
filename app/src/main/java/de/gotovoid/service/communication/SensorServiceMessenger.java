package de.gotovoid.service.communication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import de.gotovoid.service.LocationService;
import de.gotovoid.service.communication.ISensorService;
import de.gotovoid.service.communication.ISensorServiceCallback;

/**
 * Created by DJ on 16/02/18.
 */

/**
 * Class for communication with the {@link LocationService}.
 */
public class SensorServiceMessenger {
    private static final String TAG = SensorServiceMessenger.class.getSimpleName();
    private final ServiceConnection mServiceConnection;
    private ISensorService mService;
    private boolean mIsBound;

    /**
     * Creates a new instance of the {@link SensorServiceMessenger}.
     */
    public SensorServiceMessenger() {
        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName name, final IBinder service) {
                Log.d(TAG, "onServiceConnected() called with: name = [" + name
                        + "], service = [" + service + "]");
                mService = ISensorService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(final ComponentName name) {
                mService = null;
            }
        };
    }

    /**
     * Returns true if the {@link SensorServiceMessenger} is currently bound to the
     * {@link LocationService}
     *
     * @return true if bound
     */
    public boolean isBound() {
        return mIsBound;
    }

    /**
     * Bind this {@link SensorServiceMessenger} to the {@link LocationService} using the given
     * {@link Context}.
     *
     * @param context the {@link Context} tu use to register
     */
    public void doBind(@NonNull final Context context) {
        if (context == null || isBound()) {
            return;
        }
        mIsBound = context.bindService(new Intent(context, LocationService.class),
                mServiceConnection,
                Context.BIND_AUTO_CREATE);
    }

    /**
     * Unbind the {@link SensorServiceMessenger} from the {@link LocationService} using the
     * given {@link Context}
     *
     * @param context the {@link Context} to use to unregister
     */
    public void doUnbind(@NonNull final Context context) {
        if (context == null || !isBound()) {
            return;
        }
        context.unbindService(mServiceConnection);
        mIsBound = false;
    }

    /**
     * Tell the {@link LocationService} to start the sensor updates using the provided
     * {@link CallbackRegistration} to register the callback with it's properties and the
     * {@link ISensorServiceCallback} to provide a callback for sensor updates.
     *
     * @param registration the {@link CallbackRegistration}
     * @param callback     the {@link ISensorServiceCallback}
     * @return true if successful
     */
    public boolean start(final CallbackRegistration registration,
                         final ISensorServiceCallback callback) {
        Log.d(TAG, "start() called with: registration = [" + registration
                + "], callback = [" + callback + "]");
        try {
            mService.startSensor(registration, callback);
            return true;
        } catch (final RemoteException exception) {
            Log.e(TAG, "start: ", exception);
            return false;
        }
    }

    /**
     * Tell the {@link LocationService} to stop the sensor updates using the provided
     * {@link CallbackRegistration}.
     *
     * @param registration the {@link CallbackRegistration}
     * @return true if successful
     */
    public boolean stop(final CallbackRegistration registration) {
        Log.d(TAG, "stop() called with: registration = [" + registration + "]");
        try {
            mService.stopSensor(registration);
            return true;
        } catch (final RemoteException exception) {
            Log.e(TAG, "stop: ", exception);
            return false;
        }
    }

    /**
     * Tell the {@link LocationService} to start recording.
     *
     * @param recordingId the id of the recording
     * @return true if successful
     */
    public boolean startRecording(final long recordingId) {
        try {
            mService.startRecording(recordingId);
            return true;
        } catch (final RemoteException exception) {
            Log.e(TAG, "startRecording: ", exception);
            return false;
        }
    }

    /**
     * Tell the {@link LocationService} to stop the recording.
     *
     * @return true if successful
     */
    public boolean stopRecording() {
        try {
            mService.stopRecording();
            return true;
        } catch (final RemoteException exception) {
            Log.e(TAG, "stopRecording: ", exception);
            return false;
        }
    }

    /**
     * Tell the {@link LocationService} to enable sending sensor updates.
     *
     * @param isEnabled true if updates are enabled
     * @return true if successful
     */
    public boolean setUpdatesEnabled(final boolean isEnabled) {
        if (mService == null) {
            return false;
        }
        try {
            mService.setUpdatePaused(!isEnabled);
            return true;
        } catch (final RemoteException exception) {
            Log.e(TAG, "setUpdatesEnabled: ", exception);
            return false;
        }
    }
}
