package de.gotovoid.service.sensors;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by DJ on 07/01/18.
 */

/**
 * This ist the abstract implementation of a sensor.
 * It manages the {@link Observer}s registering for sensor updates.
 * It also takes care of starting and stopping the sensor.
 * Therefore concrete implementations need to override {@link #startSensor()}
 * and {@link #stopSensor()}. These methods will be called when an {@link Observer} is registered
 * or respectively the last {@link Observer} is unregistered.
 *
 * @param <Type> Type of the sensor data
 */
public abstract class AbstractSensor<Type extends Serializable> {
    private static final String TAG = AbstractSensor.class.getSimpleName();
    private final List<Observer<Type>> mObservers = new ArrayList<>();
    private final StateEvaluator<Type> mStateEvaluator;
    private long mUpdateFrequency = 1000;

    public AbstractSensor(@NonNull final StateEvaluator<Type> stateEvaluator) {
        mStateEvaluator = stateEvaluator;
    }

    /**
     * Add an {@link Observer} for the sensor data.
     * It automatically selects the shortest update frequency and notifies the
     * registered {@link Observer}s based on a multiple of the minimal update frequency.
     *
     * @param observer the {@link Observer} to be added
     */
    public void addObserver(@NonNull final Observer<Type> observer) {
        // TODO: use list that holds similar instances only once
        Log.d(TAG, "addObserver() called with: observer = [" + observer + "]");
        synchronized (mObservers) {
            if (!mObservers.contains(observer)) {
                if (mObservers.isEmpty()) {
                    Log.d(TAG, "addObserver: start sensor");
                    // TODO: move outside synchronized
                    mUpdateFrequency = observer.getUpdateFrequency();
                    startSensor();
                }
                mObservers.add(observer);
                // TODO: move outside synchronized
                if (mUpdateFrequency > observer.getUpdateFrequency()) {
                    mUpdateFrequency = observer.getUpdateFrequency();
                    restartSensor();
                }
            }
        }
    }

    /**
     * Returns the registered {@link Observer}s.
     *
     * @return the {@link Observer}s
     */
    protected List<Observer<Type>> getObservers() {
        return mObservers;
    }

    /**
     * Start the sensor.
     * The concrete implementation needs to override this in oder to start
     * the actual sensor to provide data.
     * This method will be called as soon as an {@link Observer} is registered.
     */
    protected abstract void startSensor();

    /**
     * Removes the given {@link Observer}.
     *
     * @param observer {@link Observer} to be removed
     */
    public void removeObserver(@NonNull final Observer<Type> observer) {
        Log.d(TAG, "removeObserver() called with: observer = [" + observer + "]");
        synchronized (mObservers) {
            // TODO: minimize synchronized scope.
            mObservers.remove(observer);
            if (mObservers.isEmpty()) {
                Log.d(TAG, "removeObserver: stopSensor");
                stopSensor();
            } else if (observer != null) {
                if (mUpdateFrequency >= observer.getUpdateFrequency()) {
                    mUpdateFrequency = Integer.MAX_VALUE;
                    // Find minimal update frequency.
                    for (Observer obs : mObservers) {
                        if (mUpdateFrequency > obs.getUpdateFrequency()) {
                            mUpdateFrequency = obs.getUpdateFrequency();
                            restartSensor();
                        }
                    }
                }
            }

        }
    }

    /**
     * Stops the sensor.
     * The concrete implementation needs to override this in order to stop the sensor when the
     * data is not needed anymore.
     * This method will be called when the last {@link Observer} has been removed.
     */
    protected abstract void stopSensor();

    /**
     * Returns true if the sensor is currently providing data.
     *
     * @return true if sensor is active
     */
    public boolean isStarted() {
        synchronized (mObservers) {
            return !mObservers.isEmpty();
        }
    }

    /**
     * Returns the current update frequency for the sensor in milliseconds.
     *
     * @return update frequency in milliseconds
     */
    protected long getUpdateFrequency() {
        return mUpdateFrequency;
    }

    /**
     * Restart the sensor.
     * TODO: check if this can be implemented generically
     */
    protected abstract void restartSensor();

    /**
     * Notify all registered {@link Observer}s with the new sensor data.
     * Only notifies the {@link Observer} if the timing fits the update frequency of the
     * {@link Observer}.
     *
     * @param type the sensor data to be sent.
     */
    protected void notifyObserver(@NonNull final Type type) {
        Log.d(TAG, "notifyObserver() called with: type = [" + type + "]");
        synchronized (mObservers) {
            for (Observer<Type> observer : mObservers) {
                if (observer.canUpdate()) {
                    observer.setLastUpdate(System.currentTimeMillis());
                    observer.onChange(new Result<Type>(
                            mStateEvaluator.evaluateState(type, getObservers()),
                            type));
                }
            }
        }
    }
/*
    protected SensorState updateState(final Type value) {
        return SensorState.STOPPED;
    }
*/
    /**
     * Observer for the {@link AbstractSensor}.
     * The {@link Observer} can be registered at a concrete implementation of the
     * {@link AbstractSensor} to receive updates of the sensor data.
     * The {@link Observer} therefore needs to provide the frequency with which the updates need to
     * be received. The {@link AbstractSensor} will then notify the {@link Observer} in a time
     * frame which is a multiple of the minimum update frequency requested by an {@link Observer}
     *
     * @param <Type> Type of the sensor data
     */
    public static abstract class Observer<Type extends Serializable> {
        private static final long UPDATE_FREQUENCY_TOLERANCE = 30;
        private final long mUpdateFrequency;
        // TODO: check if this is needed
        private final SensorType mType;
        private long mLastUpdate;

        /**
         * Constructor taking the update frequency in milliseconds and {@link SensorType}.
         *
         * @param updateFrequency the update frequency requested by the {@link Observer}
         * @param type            the {@link SensorType}
         */
        public Observer(final long updateFrequency, final SensorType type) {
            mUpdateFrequency = updateFrequency;
            mType = type;
        }

        /**
         * Returns the update frequency in milliseconds requested by this {@link Observer}.
         *
         * @return the update frequency in milliseconds
         */
        public long getUpdateFrequency() {
            return mUpdateFrequency;
        }

        /**
         * Returns the {@link SensorType}.
         *
         * @return the {@link SensorType}
         */
        public SensorType getType() {
            return mType;
        }

        /**
         * Called when the sensor value changed.
         * Needs to be overridden by the extending class.
         *
         * @param type the sensor value
         */
        public abstract void onChange(@NonNull final Result<Type> type);

        /**
         * Set the time when the last update was received.
         * This is a private method and should only be accessed directly by the
         * {@link AbstractSensor}.
         *
         * @param lastUpdate time of the last update
         */
        private void setLastUpdate(final long lastUpdate) {
            mLastUpdate = lastUpdate;
        }

        /**
         * Returns true if sufficient time has passed since the last update.
         *
         * @return true if update can be sent to the {@link Observer}
         */
        private boolean canUpdate() {
            final long timeDiff = System.currentTimeMillis() - mLastUpdate;
            return timeDiff - getUpdateFrequency() > -UPDATE_FREQUENCY_TOLERANCE;
        }
    }

    /**
     * This class contains the {@link Result} of a sensor reading.
     * This consists of the data provided by the {@link AbstractSensor} and the
     * {@link SensorState}.
     *
     * @param <T> type of the sensor data
     */
    public static class Result<T extends Serializable> {
        private final SensorState mSensorState;
        private final T mValue;

        /**
         * Constructor taking the {@link SensorState} and data.
         *
         * @param state the {@link SensorState}
         * @param value the data
         */
        public Result(final SensorState state, final T value) {
            mSensorState = state;
            mValue = value;
        }

        /**
         * Returns the {@link SensorState}.
         *
         * @return the {@link SensorState}
         */
        public SensorState getSensorState() {
            return mSensorState;
        }

        /**
         * Returns the value.
         *
         * @return the value
         */
        public T getValue() {
            return mValue;
        }
    }

    /**
     * Evaluator for the {@link SensorState}. Stores the recent sensor values in a buffer and
     * determines the state by the average of the readings stored in the buffer.
     *
     * @param <Type> type of the sensor value
     */
    protected static abstract class StateEvaluator<Type extends Serializable> {
        private final int mBufferSize;
        private final double mTolerance;
        private final List<Type> mBuffer;
        private SensorState mOldSensorState;

        /**
         * Constructor taking the size of the buffer and tolerance value.
         *
         * @param bufferSize size of the buffer
         * @param tolerance  tolerance value
         */
        public StateEvaluator(final int bufferSize, final double tolerance) {
            mBufferSize = bufferSize;
            mTolerance = tolerance;
            mBuffer = new ArrayList<>();
        }

        /**
         * Add a sensor reading value.
         *
         * @param value value to add
         */
        private void addValue(final Type value) {
            if (mBuffer.size() >= mBufferSize) {
                mBuffer.remove(0);
            }
            mBuffer.add(value);
        }

        /**
         * Compute the difference between two sensor readings
         *
         * @param first  first reading
         * @param second second reading
         * @return the sensor reading
         */
        protected abstract double computeDifference(final Type first, final Type second);

        /**
         * Evaluate the {@link SensorState} by the given value and {@link List} of registered
         * {@link Observer}s.
         *
         * @param value     the new sensor reading
         * @param observers the {@link List} of {@link Observer}s
         * @return the {@link SensorState}
         */
        protected SensorState evaluateState(final Type value,
                                            final List<Observer<Type>> observers) {
            if (SensorState.RUNNING.equals(mOldSensorState)) {
                return mOldSensorState;
            }
            final SensorState state;
            if (observers.isEmpty()) {
                state = SensorState.STOPPED;
            } else {
                addValue(value);
                if (mBuffer.size() == 1) {
                    state = SensorState.STARTED;
                } else {
                    double totalDiff = 0;
                    for (int i = 1; i < mBuffer.size(); i++) {
                        totalDiff += computeDifference(mBuffer.get(i - 1), mBuffer.get(i));
                    }
                    totalDiff = totalDiff / (mBuffer.size() - 1);
                    if (totalDiff >= mTolerance) {
                        state = SensorState.CALIBRATING;
                    } else {
                        state = SensorState.RUNNING;
                    }
                }
            }
            return state;
        }

        /**
         * Stop the sensor.
         */
        private void stop() {
            mOldSensorState = SensorState.STOPPED;
            mBuffer.clear();
        }
    }

}
