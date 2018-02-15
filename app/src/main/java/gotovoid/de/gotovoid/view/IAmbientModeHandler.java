package gotovoid.de.gotovoid.view;

/**
 * This interface defines methods to be implemented by {@link android.view.View}s that support
 * {@link android.support.wear.ambient.AmbientMode}.
 * The respective {@link android.view.View} must implement the method in order to change it's
 * visual representation to ambient mode.
 * <p>
 * The {@link android.view.View} should set it's background color to black and reduce the content
 * to be a minimal black and white design that uses less power on OLED screen.
 * Also disabling {@link android.graphics.Paint#setAntiAlias(boolean)} might save power by reducing
 * rendering complexity.
 * <p>
 * Created by DJ on 13/02/18.
 */

public interface IAmbientModeHandler {
    /**
     * Called to propagate the current {@link android.support.wear.ambient.AmbientMode}.
     * Set to true if {@link android.support.wear.ambient.AmbientMode} is active.
     * The {@link android.view.View} then changes it's visual representation fitting the
     * current {@link android.support.wear.ambient.AmbientMode}.
     *
     * @param isAmbient true if {@link android.support.wear.ambient.AmbientMode} is active
     */
    // To be implemented by the extending {@link android.view.View}.
    void setIsAmbient(final boolean isAmbient);
}
