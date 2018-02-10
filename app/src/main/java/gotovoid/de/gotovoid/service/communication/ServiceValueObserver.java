package gotovoid.de.gotovoid.service.communication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by DJ on 19/01/18.
 */

public abstract class ServiceValueObserver<Value> {
    private static final String TAG = ServiceValueObserver.class.getSimpleName();
    private final Messenger mLocalMessenger;
    private final int mStartMessageWhat;
    private final int mStopMessageWhat;
    private final int mUpdateMessageWhat;
    private final Set<Observer> mObservers;

    private Messenger mRemoteMessenger;
    private boolean mIsConnected;

    public ServiceValueObserver(final int startMessageWhat,
                                final int stopMessageWhat,
                                final int updateMessageWhat) {
        mLocalMessenger = new Messenger(new IncomingHandler());
        mStartMessageWhat = startMessageWhat;
        mStopMessageWhat = stopMessageWhat;
        mUpdateMessageWhat = updateMessageWhat;
        mObservers = new HashSet<>();
        mIsConnected = false;
    }

    public boolean hasObservers() {
        return !mObservers.isEmpty();
    }

    public void add(@NonNull final Observer<Value> observer) {
        synchronized (mObservers) {
            if (mIsConnected && mObservers.isEmpty()) {
                startObserving();
            }
            mObservers.add(observer);
        }
    }

    public void remove(@NonNull final Observer<Value> observer) {
        synchronized (mObservers) {
            mObservers.remove(observer);
            if (mObservers.isEmpty()) {
                stopObserving();
            }
        }
    }

    public void notifyObservers(@Nullable final Value value) {
        synchronized (mObservers) {
            for (Observer<Value> observer : mObservers) {
                observer.onChange(value);
            }
        }
    }

    public void setRemoteMessenger(@Nullable final Messenger messenger) {
        Log.d(TAG, "setRemoteMessenger() called with: messenger = [" + messenger + "]");
        mRemoteMessenger = messenger;
        synchronized (mObservers) {
            mIsConnected = messenger != null;
            if (hasObservers()) {
                if (mIsConnected) {
                    startObserving();
                } else {
                    stopObserving();
                }
            }
        }
    }

    @Nullable
    protected abstract Value getValue(@NonNull final Bundle bundle);

    private boolean startObserving() {
        Log.d(TAG, "startObserving() called");
        return sendMessage(mStartMessageWhat);
    }

    protected boolean stopObserving() {
        Log.d(TAG, "stopObserving() called");
        return sendMessage(mStopMessageWhat);
    }

    private boolean sendMessage(final int messageWhat) {
        Log.d(TAG, "sendMessage() called with: messageWhat = [" + messageWhat + "]");
        if (!mIsConnected) {
            Log.e(TAG, "sendMessage: not connected");
            return false;
        }
        try {
            Message message = Message.obtain(null, messageWhat);
            message.replyTo = mLocalMessenger;
            mRemoteMessenger.send(message);
        } catch (final RemoteException exception) {
            Log.e(TAG, "sendMessage: ", exception);
            return false;
        }
        return true;
    }

    private class IncomingHandler extends Handler {
        private IncomingHandler() {
            super(Looper.getMainLooper());
        }

        @Override
        public void handleMessage(final Message msg) {
            if (msg != null && msg.what == mUpdateMessageWhat) {
                if (hasObservers()) {
                    if (msg.getData() != null) {
                        Value value = getValue(msg.getData());
                        notifyObservers(value);
                    }
                }
            }
        }
    }

    public interface Observer<Value> {
        void onChange(@NonNull final Value value);
    }
}
