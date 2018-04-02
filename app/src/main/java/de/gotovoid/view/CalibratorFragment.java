package de.gotovoid.view;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.wear.widget.SwipeDismissFrameLayout;
import android.support.wear.widget.SwipeDismissFrameLayout.Callback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.gotovoid.databinding.CalibratorFragmentBinding;
import de.gotovoid.service.repository.LocationRepository;
import de.gotovoid.view.model.CalibratorViewModel;
import de.gotovoid.R;
/**
 * Created by DJ on 22/12/17.
 */

/**
 * This {@link Fragment} displays the current pressure and altitude and allow for calibration
 * of the current altitude.
 * <p>
 */
public class CalibratorFragment extends Fragment implements IUpdateableAmbientModeHandler {
    private static final String TAG = CalibratorFragment.class.getSimpleName();
    /**
     * {@link android.arch.lifecycle.ViewModel} for the {@link CalibratorFragment}.
     */
    private CalibratorViewModel mViewModel;
    private CalibratorFragmentBinding mBinding;

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
        mBinding = DataBindingUtil.inflate(inflater,
                R.layout.calibrator_fragment,
                container,
                false);

        showAltitude(null);
        showPressure(null);


        // Add the back action.
        mBinding.dismissLayout.addCallback(new Callback() {
            @Override
            public void onDismissed(final SwipeDismissFrameLayout layout) {
                getFragmentManager().popBackStack();
                mBinding.dismissLayout.setVisibility(View.GONE);
            }
        });

        // Add listener for the rotary input to change the calibrated altitude value.
        mBinding.progress.setOnProgressChangedListener(
                (progress) -> {
                    // TODO: use float value!
                    Integer altitude = mViewModel.getAltitude().getValue().intValue();
                    if (altitude == null) {
                        altitude = 0;
                    }
                    altitude += (int) progress;
                    // TODO: use float value!
                    mViewModel.setAltitude(altitude.floatValue());
                });

        // Observe the altitude changes and adapt the ui.
        mViewModel.getAltitude().observe(this, (result) -> {
            if (result == null) {
                return;
            }
            showAltitude(result);
        });

        // Observe the pressure changes and adapt the ui.
        mViewModel.getPressure().observe(this, (result) -> {
            if (result == null) {
                return;
            }
            showPressure(result.getValue());
            mBinding.calibrating.setState(result.getSensorState());
        });
        return mBinding.getRoot();
    }

    /**
     * Show the new altitude value.
     *
     * @param altitude altitude value to be displayed
     */
    private void showAltitude(final Float altitude) {
        Log.d(TAG, "showAltitude() called with: altitude = [" + altitude + "]");
        if (altitude == null) {
            // TODO: add string resource
            mBinding.altitude.setText("---");
        } else {
            mBinding.altitude.setText(altitude + " m");
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
            mBinding.pressure.setText("---");
        } else {
            mBinding.pressure.setText(pressure + " hPa");
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume() called");
        super.onResume();
        mBinding.progress.requestFocus();
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

    @Override
    public void setIsAmbient(final boolean isAmbient) {
        // TODO: couldn't this be solved with style??
        mBinding.progress.setIsAmbient(isAmbient);
        if (isAmbient) {
            mBinding.dismissLayout.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.black));
            mBinding.altitude.getPaint().setAntiAlias(false);
            mBinding.pressure.getPaint().setAntiAlias(false);
        } else {
            mBinding.dismissLayout.setBackgroundColor(
                    ContextCompat.getColor(getContext(), R.color.background_default));
            mBinding.altitude.getPaint().setAntiAlias(true);
            mBinding.pressure.getPaint().setAntiAlias(true);
        }
    }

    @Override
    public void onUpdateAmbient() {
        Log.d(TAG, "onUpdateAmbient() called");
        showAltitude(mViewModel.getAltitude().getValue());
        showPressure(mViewModel.getPressure().getValue().getValue());
    }
}
