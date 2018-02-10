package gotovoid.de.gotovoid.service.communication;

import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gotovoid.de.gotovoid.service.LocationService;
import gotovoid.de.gotovoid.service.sensors.SensorHandler;

/**
 * Created by DJ on 02/01/18.
 */

public class ServiceMessageHandler {
    private static final String TAG = ServiceMessageHandler.class.getSimpleName();

    // TODO: maybe use enums
    static final int MSG_REGISTER_CLIENT = 1;
    static final int MSG_UNREGISTER_CLIENT = 2;
    static final int MSG_REGISTERED = 3;
    static final int MSG_REGISTER_OBSERVE_PRESSURE = 4;
    static final int MSG_UNREGISTER_OBSERVE_PRESSURE = 5;
    static final int MSG_REGISTER_OBSERVE_LOCATION = 6;
    static final int MSG_UNREGISTER_OBSERVE_LOCATION = 7;
    static final int MSG_START_RECORDING = 8;
    static final int MSG_STOP_RECORDING = 9;
    static final int MSG_START_OBSERVE_RECORDING = 10;
    static final int MSG_STOP_OBSERVE_RECORDING = 11;
    static final int MSG_PRESSURE = 12;
    static final int MSG_LOCATION = 13;
    static final int MSG_RECORD = 14;
    static final int MSG_ENTER_AMBIENT = 15;
    static final int MSG_EXIT_AMBIENT = 16;

    /**
     * List holding all the {@link Messenger} instances registered with this service.
     */
    private static final List<Messenger> mClients = new ArrayList<>();
    /**
     * Observers for the location data.
     */
    private final Map<Messenger, LocationObserver> mLocationObservers
            = new HashMap<>();
    /**
     * Observers for the pressure data.
     */
    private final Map<Messenger, PressureObserver> mPressureObservers
            = new HashMap<>();
    /**
     * Observers for the pressure data.
     */
    private final Map<Messenger, RecordingObserver> mRecordingObservers
            = new HashMap<>();
    /**
     * Instance communicating with service clients.
     */
    private final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Handler for the sensors
     */
    private final SensorHandler mSensorHandler;

    private boolean mIsAmbient = false;


    public ServiceMessageHandler(@NonNull final SensorHandler sensorManager) {
        mSensorHandler = sensorManager;
    }

    public IBinder getBinder() {
        return mMessenger.getBinder();
    }

    private void addPressureObserver(final Messenger messenger) {
        if (mPressureObservers.containsKey(messenger)) {
            return;
        }
        final PressureObserver observer = new PressureObserver(messenger);
        synchronized (mPressureObservers) {
            mPressureObservers.put(observer.getMessenger(), observer);
        }
        mSensorHandler.addObserver(observer);
    }

    private void removePressureObserver(final Messenger messenger) {
        if (mPressureObservers.containsKey(messenger)) {
            final PressureObserver observer;
            synchronized (mPressureObservers) {
                observer = mPressureObservers.remove(messenger);
            }
            mSensorHandler.removeObserver(observer);
        }
    }

    private void addLocationObserver(final Messenger messenger) {
        if (mLocationObservers.containsKey(messenger)) {
            return;
        }
        final LocationObserver observer = new LocationObserver(messenger);
        synchronized (mLocationObservers) {
            mLocationObservers.put(observer.getMessenger(), observer);
        }
        mSensorHandler.addObserver(observer);
    }

    private void removeLocationObserver(final Messenger messenger) {
        if (mLocationObservers.containsKey(messenger)) {
            final LocationObserver observer;
            synchronized (mLocationObservers) {
                observer = mLocationObservers.get(messenger);
            }
            mSensorHandler.removeObserver(observer);
        }
    }

    private void addRecordingObserver(final Messenger messenger) {
        if (mRecordingObservers.containsKey(messenger)) {
            return;
        }
        final RecordingObserver observer = new RecordingObserver(messenger);
        synchronized (mRecordingObservers) {
            mRecordingObservers.put(observer.getMessenger(), observer);
        }
        mSensorHandler.addObserver(observer);
    }

    private void removeRecordingObserver(final Messenger messenger) {
        if (mRecordingObservers.containsKey(messenger)) {
            final RecordingObserver observer;
            synchronized (mRecordingObservers) {
                observer = mRecordingObservers.remove(messenger);
            }
            mSensorHandler.removeObserver(observer);
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(@NonNull final Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    Log.d(TAG, "handleMessage: register client");
                    synchronized (mClients) {
                        mClients.add(msg.replyTo);
                    }
                    break;
                case MSG_UNREGISTER_CLIENT:
                    Log.d(TAG, "handleMessage: unregister client " + mClients.size());
                    synchronized (mClients) {
                        mClients.remove(msg.replyTo);
                        /* TODO: stop service in on stop if this is empty.
                        if (mClients.size() == 0) {
                            if (!mSensorHandler.isRecording()) {
                                // If there are no Clients bound, destroy service
                                Log.d(TAG, "handleMessage: Stopping service");
                                mService.stopForeground(true);
                                Log.d(TAG, "handleMessage: stop self");
                                mService.stopSelf();
                            } else {
                                Log.d(TAG, "handleMessage: Keep serivce alive, recording");
                            }
                        }*/
                    }
                    break;
                case MSG_REGISTER_OBSERVE_PRESSURE:
                    Log.d(TAG, "handleMessage: addObserver pressure");
                    addPressureObserver(msg.replyTo);
                    break;
                case MSG_UNREGISTER_OBSERVE_PRESSURE:
                    Log.d(TAG, "handleMessage: removeObserver pressure");
                    removePressureObserver(msg.replyTo);
                    break;
                case MSG_REGISTER_OBSERVE_LOCATION:
                    Log.d(TAG, "handleMessage: addObserver location");
                    addLocationObserver(msg.replyTo);
                    break;
                case MSG_UNREGISTER_OBSERVE_LOCATION:
                    Log.d(TAG, "handleMessage: removeObserver location");
                    removeLocationObserver(msg.replyTo);
                    break;
                case MSG_START_RECORDING: {
                    Log.d(TAG, "handleMessage: start recording");
                    final Bundle bundle = msg.getData();
                    if (bundle == null || bundle.isEmpty()) {
                        Log.e(TAG, "handleMessage: empty bundle");
                        break;
                    }
                    mSensorHandler.startRecording(bundle.getLong("recording_id"));
                }
                break;
                case MSG_STOP_RECORDING: {
                    Log.d(TAG, "handleMessage: stop recording");
                    mSensorHandler.stopRecording();
                }
                break;
                case MSG_START_OBSERVE_RECORDING:
                    Log.d(TAG, "handleMessage: addObserver recording");
                    addRecordingObserver(msg.replyTo);
                    break;
                case MSG_STOP_OBSERVE_RECORDING:
                    Log.d(TAG, "handleMessage: removeObserver recording");
                    removeRecordingObserver(msg.replyTo);
                    break;
                case MSG_ENTER_AMBIENT:
                    Log.d(TAG, "handleMessage: enter ambient mode");
                    mIsAmbient = true;
                    break;
                case MSG_EXIT_AMBIENT:
                    Log.d(TAG, "handleMessage: exit ambient mode");
                    mIsAmbient = false;
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }
        }
    }

    private abstract class AbstractObserver<Type> {
        private final Messenger mMessenger;
        private final int mMessageWhat;

        protected AbstractObserver(final Messenger messenger,
                                   final int messageWhat) {
            mMessageWhat = messageWhat;
            mMessenger = messenger;
        }

        protected void sendMessage(final Type type) {
            if (mIsAmbient) {
                return;
            }
            Message message = Message.obtain(null, mMessageWhat);
            Bundle bundle = new Bundle();
            putData(type, bundle);
            message.setData(bundle);
            try {
                mMessenger.send(message);
            } catch (final RemoteException exception) {
                Log.e(TAG, "onChange: ", exception);
                removeObserver();
            }
        }

        public Messenger getMessenger() {
            return mMessenger;
        }

        protected abstract void putData(final Type type, final Bundle bundle);

        protected abstract void removeObserver();
    }

    private class LocationObserver
            extends AbstractObserver<Location>
            implements SensorHandler.LocationObserver {

        protected LocationObserver(final Messenger messenger) {
            super(messenger, MSG_LOCATION);
        }

        @Override
        public void onChange(@NonNull final Location location) {
            sendMessage(location);
        }

        @Override
        protected void putData(final Location location, final Bundle bundle) {
            bundle.putParcelable("location", location);
        }

        @Override
        protected void removeObserver() {
            removeLocationObserver(getMessenger());
        }
    }

    private class PressureObserver
            extends AbstractObserver<Float>
            implements SensorHandler.PressureObserver {

        protected PressureObserver(final Messenger messenger) {
            super(messenger, MSG_PRESSURE);
        }

        @Override
        public void onChange(@NonNull final Float pressure) {
            sendMessage(pressure);
        }

        @Override
        protected void putData(final Float pressure, final Bundle bundle) {
            bundle.putFloat("pressure", pressure);
        }

        @Override
        protected void removeObserver() {
            removePressureObserver(getMessenger());
        }
    }

    private class RecordingObserver
            extends AbstractObserver<Long>
            implements SensorHandler.RecordingObserver {

        protected RecordingObserver(final Messenger messenger) {
            super(messenger, MSG_RECORD);
        }

        @Override
        public void onChange(@NonNull final Long record) {
            sendMessage(record);
        }

        @Override
        protected void putData(final Long record, final Bundle bundle) {
            bundle.putLong("recording_id", record);
        }

        @Override
        protected void removeObserver() {
            removeRecordingObserver(getMessenger());
        }
    }
}
