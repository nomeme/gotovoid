package gotovoid.de.gotovoid.service.sensors;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DJ on 07/01/18.
 */

public abstract class AbstractSensor<Type> {
    private static final String TAG = AbstractSensor.class.getSimpleName();
    private final List<Observer<Type>> mObservers = new ArrayList<>();
    private long mUpdateFrequency = 1000;

    public void addObserver(@NonNull final Observer<Type> observer) {
        // TODO: use list that holds similar instances only once
        Log.d(TAG, "addObserver() called with: observer = [" + observer + "]");
        synchronized (mObservers) {
            if (!mObservers.contains(observer)) {
                if (mObservers.isEmpty()) {
                    Log.d(TAG, "addObserver: start sensor");
                    startSensor();
                }
                mObservers.add(observer);
            }
        }

    }

    protected abstract void startSensor();

    public void removeObserver(final Observer<Type> observer) {
        Log.d(TAG, "removeObserver() called with: observer = [" + observer + "]");
        synchronized (mObservers) {
            mObservers.remove(observer);
            if (mObservers.isEmpty()) {
                Log.d(TAG, "removeObserver: stopSensor");
                stopSensor();
            }
        }
    }

    protected abstract void stopSensor();

    public void setUpdateFrequency(final long updateFrequency) {
        mUpdateFrequency = updateFrequency;
        restartSensor();
    }

    public boolean isStarted() {
        return !mObservers.isEmpty();
    }

    protected long getUpdateFrequency() {
        return mUpdateFrequency;
    }

    protected abstract void restartSensor();

    protected void notifyObserver(@NonNull final Type type) {
        for (Observer<Type> observer : mObservers) {
            observer.onChange(type);
        }
    }


    public interface Observer<Type> {
        void onChange(@NonNull final Type type);
    }
}
