package de.gotovoid.components.arcitecture;

/**
 * Created by DJ on 20/03/18.
 */

/**
 * Generic interface for the observer pattern.
 *
 * @param <T> type of the observed data
 */
public interface IObservable<T> {
    /**
     * Add an observer to the {@link IObservable}.
     *
     * @param observer the {@link Observer} to be added
     */
    void addObserver(final Observer<T> observer);

    /**
     * Remove an observer from the {@link IObservable}.
     *
     * @param observer the {@link Observer} to be removed
     */
    void removeObserver(final Observer<T> observer);

    /**
     * Observer for the {@link IObservable} interface.
     *
     * @param <T> the type of the observed data
     */
    interface Observer<T> {
        /**
         * Called when the observed value changed
         *
         * @param data the new data
         */
        void onChange(final T data);
    }
}
