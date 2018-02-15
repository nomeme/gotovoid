package gotovoid.de.gotovoid.view;

/**
 * This interface extends the {@link IAmbientModeHandler} to support updates during
 * {@link android.support.wear.ambient.AmbientMode}.
 * <p>
 * This interface should be extended by a {@link android.app.Fragment} supporting
 * {@link android.support.wear.ambient.AmbientMode}. It then can propagate
 * {@link #setIsAmbient(boolean)} to it's {@link android.view.View}s and perform updates of the
 * displayed data during {@link android.support.wear.ambient.AmbientMode} via
 * {@link #onUpdateAmbient()}.
 * <p>
 * Created by DJ on 07/01/18.
 */

public interface IUpdateableAmbientModeHandler extends IAmbientModeHandler {
    /**
     * Called if the system requests updates during
     * {@link android.support.wear.ambient.AmbientMode}.
     * The extending class then takes care of refreshing the data displayed .
     */
    void onUpdateAmbient();
}
