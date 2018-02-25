package de.gotovoid.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.TextView;

import de.gotovoid.R;

/**
 * Created by DJ on 23/01/18.
 */

public class FlightInfoView extends ConstraintLayout {
    private TextView mAscendingSpeed;
    private TextView mAltitude;
    private TextView mSpeed;

    public FlightInfoView(final Context context) {
        super(context);
        init();
    }

    public FlightInfoView(final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FlightInfoView(final Context context,
                          @Nullable final AttributeSet attrs,
                          final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.flight_info_view, this);
        mAscendingSpeed = findViewById(R.id.ascending_speed);
        mAltitude = findViewById(R.id.altitude);
        mSpeed = findViewById(R.id.speed);
    }

    public void setSpeed(final int speed) {
        mSpeed.setText(speed + "km/h");
    }

    public void setAltitude(final int altitude) {
        mAltitude.setText(altitude + "m");
    }

    public void setAscendingSpeed(final int ascendingSpeed) {
        mAscendingSpeed.setText(ascendingSpeed + "m/s");
    }
}
