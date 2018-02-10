package gotovoid.de.gotovoid.service.communication;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import gotovoid.de.gotovoid.database.model.Recording;
import gotovoid.de.gotovoid.service.LocationService;

/**
 * Created by DJ on 26/12/17.
 */

public class LocationServiceMessenger {
    private static final String TAG = LocationServiceMessenger.class.getSimpleName();
    /**
     * Messenger for the service.
     */
    private final ServiceConnection mConnection;
    /**
     * The {@link Messenger} receiving messages from the service.
     */
    private final Messenger mMessenger;

    private final ServiceValueObserver<Location> mLocationObservers =
            new ServiceValueObserver<Location>(ServiceMessageHandler.MSG_REGISTER_OBSERVE_LOCATION,
                    ServiceMessageHandler.MSG_UNREGISTER_OBSERVE_LOCATION,
                    ServiceMessageHandler.MSG_LOCATION) {

                @Nullable
                @Override
                protected Location getValue(@NonNull final Bundle bundle) {
                    if (bundle == null) {
                        return null;
                    }
                    return bundle.getParcelable("location");
                }
            };
    private final ServiceValueObserver<Float> mPressureObservers =
            new ServiceValueObserver<Float>(ServiceMessageHandler.MSG_REGISTER_OBSERVE_PRESSURE,
                    ServiceMessageHandler.MSG_UNREGISTER_OBSERVE_PRESSURE,
                    ServiceMessageHandler.MSG_PRESSURE) {
                @Nullable
                @Override
                protected Float getValue(@NonNull final Bundle bundle) {
                    if (bundle == null) {
                        return null;
                    }
                    return bundle.getFloat("pressure");
                }
            };

    private final ServiceValueObserver<Long> mRecordingObservers =
            new ServiceValueObserver<Long>(ServiceMessageHandler.MSG_START_OBSERVE_RECORDING,
                    ServiceMessageHandler.MSG_STOP_OBSERVE_RECORDING,
                    ServiceMessageHandler.MSG_RECORD) {
                @Nullable
                @Override
                protected Long getValue(@NonNull final Bundle bundle) {
                    Log.d(TAG, "getValue() called with: bundle = [" + bundle + "]");
                    if (bundle == null) {
                        return null;
                    }
                    return bundle.getLong("recording_id");
                }
            };

    private final List<Message> mMessageBuffer = new ArrayList<>();


    private Messenger mService;

    private enum State {
        UNBOUND, BINDING, UNBINDING, BOUND;
    }

    private State mState = State.UNBOUND;

    public LocationServiceMessenger() {
        mMessenger = new Messenger(new IncomingHandler());
        mConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(final ComponentName componentName,
                                           final IBinder iBinder) {
                Log.d(TAG, "onServiceConnected() called with: " +
                        "componentName = [" + componentName + "], " +
                        "iBinder = [" + iBinder + "]");
                /*
                 * This is called when the service connects. Do the initial registration here.
                 * Register the messenger as client so the service can keep track of the connected
                 * instances and decide when to stop.
                 */
                mService = new Messenger(iBinder);
                try {
                    Message message = Message.obtain(null,
                            ServiceMessageHandler.MSG_REGISTER_CLIENT);
                    message.replyTo = mMessenger;
                    mService.send(message);
                    setState(State.BOUND);
                } catch (final RemoteException exception) {
                    /*
                     * The service has crashed before we could register to it.
                     * Disconnect will be called soon.
                     */
                    Log.e(TAG, "onServiceConnected: ", exception);
                }
                Log.d(TAG, "onServiceConnected: state: " + getState());
                final List<Message> messages;
                synchronized (mMessageBuffer) {
                    messages = new ArrayList<>(mMessageBuffer);
                    Log.d(TAG, "onServiceConnected: messages: " + messages.size());
                    mMessageBuffer.clear();
                    Log.d(TAG, "onServiceConnected: messages: " + messages.size());
                }
                for (final Message message : messages) {
                    Log.d(TAG, "onServiceConnected: send queued message");
                    try {
                        mService.send(message);
                    } catch (final RemoteException exception) {
                        Log.e(TAG, "onServiceConnected: ", exception);
                    }
                }
                mPressureObservers.setRemoteMessenger(mService);
                mLocationObservers.setRemoteMessenger(mService);
                mRecordingObservers.setRemoteMessenger(mService);
            }

            @Override
            public void onServiceDisconnected(final ComponentName componentName) {
                Log.d(TAG, "onServiceDisconnected() called with: componentName = ["
                        + componentName + "]");
                mService = null;
                setState(State.UNBOUND);
                mPressureObservers.setRemoteMessenger(null);
                mLocationObservers.setRemoteMessenger(null);
                mRecordingObservers.setRemoteMessenger(null);
            }

            @Override
            public void onBindingDied(final ComponentName name) {
                Log.d(TAG, "onBindingDied() called with: name = [" + name + "]");
                setState(State.UNBOUND);
            }
        };
    }

    private void setState(final State state) {
        Log.d(TAG, "setState() called with: state = [" + state + "]");
        synchronized (mState) {
            mState = state;
        }
    }

    private State getState() {
        Log.d(TAG, "getState() called: " + mState);
        synchronized (mState) {
            return mState;
        }
    }

    public void addObserver(final ServiceValueObserver.Observer observer) {
        Log.d(TAG, "addObserver: state: " + getState());
        if (observer instanceof PressureObserver) {
            Log.d(TAG, "addObserver: pressure");
            mPressureObservers.add(observer);
        }
        if (observer instanceof LocationObserver) {
            Log.d(TAG, "addObserver: location");
            mLocationObservers.add(observer);
        }
        if (observer instanceof RecordingObserver) {
            Log.d(TAG, "addObserver: recording");
            mRecordingObservers.add(observer);
        }
    }

    public void removeObserver(final ServiceValueObserver.Observer observer) {
        if (observer instanceof PressureObserver) {
            Log.d(TAG, "removeObserver: pressure");
            mPressureObservers.remove(observer);
        }
        if (observer instanceof LocationObserver) {
            Log.d(TAG, "removeObserver: location");
            mLocationObservers.remove(observer);
        }
        if (observer instanceof RecordingObserver) {
            Log.d(TAG, "removeObserver: recording");
            mRecordingObservers.remove(observer);
        }
    }

    public void startRecording(final Recording recording) {
        Log.d(TAG, "startRecording() called with: recording = [" + recording + "]");
        final Message message = Message.obtain(null,
                ServiceMessageHandler.MSG_START_RECORDING);
        message.replyTo = mMessenger;
        final Bundle bundle = new Bundle();
        Log.d(TAG, "startRecording: recording_id: " + recording.getId());
        bundle.putLong("recording_id", recording.getId());
        message.setData(bundle);
        if (!isBound()) {
            Log.d(TAG, "startRecording: add to queue");
            mMessageBuffer.add(message);
        } else {
            Log.d(TAG, "startRecording: send directly");
            try {
                mService.send(message);
            } catch (final RemoteException exception) {
                Log.e(TAG, "startRecording: ", exception);
            }
        }
    }

    public void stopRecording() {
        Log.d(TAG, "stopRecording() called");
        final Message message = Message.obtain(null,
                ServiceMessageHandler.MSG_STOP_RECORDING);
        message.replyTo = mMessenger;
        if (!isBound()) {
            mMessageBuffer.add(message);
        } else {
            try {
                mService.send(message);
            } catch (final RemoteException exception) {
                Log.e(TAG, "startRecording: ", exception);
            }
        }
    }

    public void setAmbientMode(final boolean isAmbient) {
        if (!isBound()) {
            Log.e(TAG, "setAmbientMode: Service is null");
            return;
        }
        final int messageWhat;
        if (isAmbient) {
            messageWhat = ServiceMessageHandler.MSG_ENTER_AMBIENT;
        } else {
            messageWhat = ServiceMessageHandler.MSG_EXIT_AMBIENT;
        }
        try {
            final Message message = Message.obtain(null, messageWhat);
            message.replyTo = mMessenger;
            mService.send(message);
        } catch (final RemoteException exception) {
            Log.e(TAG, "setAmbientMode: ", exception);
        }
    }

    public void doBind(@NonNull final Context context) {
        Log.d(TAG, "doBind() called with: context = [" + context + "]", new NullPointerException());
        /*
         * Establish a connection to the service.
         */
        Log.d(TAG, "doBind: state: " + getState());
        if (context == null || isBound()) {
            return;
        }
        boolean bound = context.bindService(new Intent(context, LocationService.class),
                mConnection, Context.BIND_AUTO_CREATE);
        if (bound) {
            setState(State.BINDING);
        }
        Toast.makeText(context, "Binding service", Toast.LENGTH_SHORT).show();
    }

    public void doUnbind(@NonNull final Context context) {
        Log.d(TAG, "doUnbind() called with: context = [" + context + "]");
        Log.d(TAG, "doUnbind: state: " + getState());
        Log.d(TAG, "doUnbind: service: " + mService);
        Log.d(TAG, "doUnbind: connection: " + mConnection);
        /*
         * Close established connection to a service.
         */
        if (context == null) {
            return;
        }

        if (mService != null) {
            Log.d(TAG, "doUnbind: unregister");
            // Only do this if we already received the service instance.
            try {
                Message message = Message.obtain(null, ServiceMessageHandler.MSG_UNREGISTER_CLIENT);
                message.replyTo = mMessenger;
                mService.send(message);
                mService = null;
            } catch (final RemoteException exception) {
                Log.e(TAG, "doUnbind: ", exception);
            }
        }
        synchronized (mState) {
            if (State.BINDING.equals(mState) || State.BOUND.equals(mState)) {
                Toast.makeText(context, "Unbinding service", Toast.LENGTH_SHORT).show();
                context.unbindService(mConnection);
            }
            setState(State.UNBINDING);
        }
    }

    private class IncomingHandler extends Handler {

        private IncomingHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(final Message msg) {
            if (msg != null && msg.what == ServiceMessageHandler.MSG_REGISTERED) {
                Log.d(TAG, "handleMessage: bound");
            }
        }
    }

    public boolean isBound() {
        Log.d(TAG, "isBound: service: " + mService + ", state: " + getState());
        synchronized (mState) {
            return mService != null && State.BOUND.equals(getState());
        }
    }

    public interface PressureObserver extends ServiceValueObserver.Observer<Float> {
    }

    // TODO: remove android class from here!
    public interface LocationObserver extends ServiceValueObserver.Observer<Location> {
    }

    public interface RecordingObserver extends ServiceValueObserver.Observer<Long> {
    }

}
