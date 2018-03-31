package de.gotovoid.components.arcitecture;

import android.arch.lifecycle.MutableLiveData;
import android.support.annotation.NonNull;

/**
 * Created by DJ on 20/03/18.
 */

/**
 * Extension of the {@link MutableLiveData}.
 * Automatically registers and unregisters an {@link IObservable.Observer} depending on the
 * {@link MutableLiveData}'s active state.
 * Like {@link MutableLiveData} only delivers updates when there is at least one active observer
 * registered at the {@link ObserverLiveData}.
 * Furthermore, when no active observers are connected, the {@link ObserverLiveData} removes
 * itself from the {@link IObservable} it is observing.
 *
 * @param <T> type of the observed data
 */
public class ObserverLiveData<T> extends MutableLiveData<T> {
    private static final String TAG = ObserverLiveData.class.getSimpleName();
    private final RegistrationHandler mRegistrationHandler;

    /**
     * Creates a new {@link ObserverLiveData} taking the {@link IObservable} to be observed
     * and the {@link IObservable.Observer} to be used for observing.
     * This is tailored to implementations of the {@link IObservable} interface.
     *
     * @param observable {@link IObservable} to be observed
     * @param observer   {@link IObservable.Observer} to register for observing
     */
    public ObserverLiveData(final IObservable<T> observable,
                            final IObservable.Observer<T> observer) {
        mRegistrationHandler = new RegistrationHandlerImpl<>(observable, observer);
    }

    /**
     * Takes the {@link IObservable} to be observed.
     * This is tailored for implementations of the {@link IObservable} interface.
     *
     * @param observable the {@link IObservable}
     */
    public ObserverLiveData(final IObservable<T> observable) {
        mRegistrationHandler = new RegistrationHandlerImpl<>(observable, (data) -> postValue(data));
    }

    /**
     * Takes the {@link RegistrationHandler} which handles the registration on an
     * {@link IObservable}.
     * This is intended for usage with generic observer interfaces.
     *
     * @param registrationHandler the {@link RegistrationHandler}
     */
    public ObserverLiveData(final @NonNull RegistrationHandler registrationHandler) {
        mRegistrationHandler = registrationHandler;
    }

    @Override
    protected void onActive() {
        super.onActive();
        if (mRegistrationHandler != null) {
            mRegistrationHandler.onRegister();
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        if (mRegistrationHandler != null) {
            mRegistrationHandler.onUnregister();
        }
    }

    /**
     * Handler for registration and removing of the {@link IObservable.Observer}.
     * This can be extended and used for every observer implementation.
     */
    public interface RegistrationHandler {
        /**
         * Called when there are active observers registered at the {@link MutableLiveData},
         * so registration on the {@link java.util.Observable} is required.
         */
        void onRegister();

        /**
         * Called when there are no active observers registered anymore at the
         * {@link MutableLiveData}.
         * So the registration on the {@link java.util.Observable} needs to be cancelled.
         */
        void onUnregister();
    }

    /**
     * Implementation of the {@link RegistrationHandler}.
     * This is used to provide a generic implementation for the {@link IObservable}.
     *
     * @param <T>
     */
    private static class RegistrationHandlerImpl<T>
            implements RegistrationHandler {
        final IObservable<T> mObservable;
        final IObservable.Observer<T> mObserver;

        /**
         * Constructor taking an implementation of the {@link IObservable} and
         * {@link IObservable.Observer} interface.
         *
         * @param observable the {@link IObservable}
         * @param observer   the {@link IObservable.Observer}
         */
        public RegistrationHandlerImpl(final @NonNull IObservable<T> observable,
                                       final @NonNull IObservable.Observer<T> observer) {
            mObservable = observable;
            mObserver = observer;
        }

        @Override
        public void onRegister() {
            if (mObservable != null) {
                mObservable.addObserver(mObserver);
            }
        }

        @Override
        public void onUnregister() {
            if (mObservable != null) {
                mObservable.removeObserver(mObserver);
            }
        }
    }
}
