package gotovoid.de.gotovoid.view;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.wear.widget.SwipeDismissFrameLayout;
import android.support.wear.widget.SwipeDismissFrameLayout.Callback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import gotovoid.de.gotovoid.R;
import gotovoid.de.gotovoid.service.repository.LocationRepository;
import gotovoid.de.gotovoid.view.model.CalibratorViewModel;

/**
 * This {@link Fragment} displays the current pressure and altitude and allow for calibration
 * of the current altitude.
 * <p>
 * Created by DJ on 22/12/17.
 */

public class CalibratorFragment extends Fragment {
    private static final String TAG = CalibratorFragment.class.getSimpleName();
    private CalibratorViewModel mViewModel;

    private CircularProgress mProgress;
    private TextView mAltitudeText;
    private TextView mPressureText;
    private SwipeDismissFrameLayout mDismissLayout;


    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(CalibratorViewModel.class);
        mViewModel.init(LocationRepository.getRepository(getActivity()));
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater,
                             @Nullable final ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called with: inflater = [" + inflater
                + "], container = [" + container
                + "], savedInstanceState = [" + savedInstanceState + "]");
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.calibrator_fragment, container, false);

        mAltitudeText = view.findViewById(R.id.altitude);
        showAltitude(null);
        mPressureText = view.findViewById(R.id.pressure);
        showPressure(null);


        // Add the back action.
        mDismissLayout = (SwipeDismissFrameLayout) view.findViewById(R.id.dismiss_layout);
        mDismissLayout.addCallback(new Callback() {
            @Override
            public void onDismissed(final SwipeDismissFrameLayout layout) {
                getFragmentManager().popBackStack();
                mDismissLayout.setVisibility(View.GONE);
            }
        });

        // Add listener for the rotary input to change the calibrated altitude value.
        mProgress = (CircularProgress) view.findViewById(R.id.progress);
        mProgress.setOnProgressChangedListener(new CircularProgress.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(final float progress) {
                Log.d(TAG, "onProgressChanged() called with: progress = [" + progress + "]");
                Integer altitude = mViewModel.getAltitude().getValue();
                if (altitude == null) {
                    altitude = 0;
                }
                altitude += (int) progress;
                mViewModel.setAltitude(altitude);
            }
        });

        // Observe the altitude changes and adapt the ui.
        mViewModel.getAltitude().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable final Integer altitude) {
                Log.d(TAG, "onChanged() called with: altitude = [" + altitude + "]");
                if (altitude == null) {
                    return;
                }
                showAltitude(altitude);
            }
        });

        // Observe the pressure changes and adapt the ui.
        mViewModel.getPressure().observe(this, new Observer<Float>() {
            @Override
            public void onChanged(@Nullable final Float pressure) {
                Log.d(TAG, "onChanged() called with: aFloat = [" + pressure + "]");
                if (pressure == null) {
                    return;
                }
                showPressure(pressure);
            }
        });

        return view;
    }

    /**
     * Show the new altitude value.
     *
     * @param altitude altitude value to be displayed
     */
    private void showAltitude(final Integer altitude) {
        Log.d(TAG, "showAltitude() called with: altitude = [" + altitude + "]");
        if (altitude == null) {
            // TODO: add string resource
            mAltitudeText.setText("---");
        } else {
            mAltitudeText.setText(altitude + " m");
        }
    }

    /**
     * Show the new pressure value.
     *
     * @param pressure pressure value to be displayed
     */
    private void showPressure(final Float pressure) {
        Log.d(TAG, "showPressure() called with: pressure = [" + pressure + "]");
        if (pressure == null) {
            // TODO: add string resource
            mPressureText.setText("---");
        } else {
            mPressureText.setText(pressure + " hPa");
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume() called", new NullPointerException());
        super.onResume();
        mProgress.requestFocus();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause() called");
        // Save the new calibrated altitude if it has been modified by the user.
        if (mViewModel.isAltitudeChanged()) {
            mViewModel.persistCalibratedAltitude();
        }
        super.onPause();
    }
}
