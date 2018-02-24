package gotovoid.de.gotovoid;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.wear.ambient.AmbientMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

import gotovoid.de.gotovoid.repository.IRepositoryProvider;
import gotovoid.de.gotovoid.service.communication.SensorServiceMessenger;
import gotovoid.de.gotovoid.service.repository.LocationRepository;
import gotovoid.de.gotovoid.service.LocationService;
import gotovoid.de.gotovoid.view.CalibratorFragment;
import gotovoid.de.gotovoid.view.IUpdateableAmbientModeHandler;
import gotovoid.de.gotovoid.view.RecorderFragment;
import gotovoid.de.gotovoid.view.RecordingListFragment;

public class MainActivity extends FragmentActivity
        // We need this to support AmbientMode, so the app is always visible
        implements AmbientMode.AmbientCallbackProvider,
        IRepositoryProvider {
    private static final String TAG = MainActivity.class.getSimpleName();

    private LocationRepository mLocationRepository;
    private final SensorServiceMessenger mMessenger = new SensorServiceMessenger();

    /**
     * Controller for the ambient mode. Need this to check current ambient mode..
     */
    private AmbientMode.AmbientController mAmbientController;
    /**
     * Callback for ambient mode changes.
     */
    private ActivityAmbientCallback mAmbientCallback;

    private ViewGroup mContentView;

    private Button mCalibrateButton;
    private Button mListButton;
    private Button mStartButton;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
        mAmbientCallback = new ActivityAmbientCallback();
        mAmbientController = AmbientMode.attachAmbientSupport(this);

        setContentView(R.layout.activity_main);

        mContentView = findViewById(R.id.content_view);

        mCalibrateButton = (Button) findViewById(R.id.calibrate_button);
        mCalibrateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (view == mCalibrateButton) {
                    CalibratorFragment fragment = new CalibratorFragment();
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(R.id.content_container, fragment);
                    transaction.addToBackStack(fragment.getClass().getSimpleName());
                    transaction.commit();
                }
            }
        });

        mListButton = findViewById(R.id.recording_list_button);
        mListButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (view == mListButton) {
                    final RecordingListFragment fragment = new RecordingListFragment();
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(R.id.content_container,
                            fragment,
                            fragment.getClass().getSimpleName());
                    transaction.addToBackStack(fragment.getClass().getSimpleName());
                    transaction.commit();
                }
            }
        });

        mStartButton = findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (view == mStartButton) {
                    final RecorderFragment fragment = new RecorderFragment();
                    FragmentManager manager = getSupportFragmentManager();
                    FragmentTransaction transaction = manager.beginTransaction();
                    transaction.add(R.id.content_container,
                            fragment,
                            fragment.getClass().getSimpleName());
                    transaction.addToBackStack(fragment.getClass().getSimpleName());
                    transaction.commit();
                }
            }
        });

    }

    private void startService() {
        Intent startServiceIntent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            startForegroundService(startServiceIntent);
        } else {
            startService(startServiceIntent);
        }
    }

    private void stopService() {
        Intent stopServiceIntent = new Intent(this, LocationService.class);
        stopService(stopServiceIntent);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    R.string.request_location_permission);
            Log.e(TAG, "initSensors: no permissions");
        } else {
            startService();
        }
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, R.string.request_write_external_storage_permission);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        mMessenger.doBind(this);
        mLocationRepository = new LocationRepository(mMessenger);
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,
                                           @NonNull final String[] permissions,
                                           @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case R.string.request_location_permission:
                Log.d(TAG, "onRequestPermissionsResult: location permissions: " + grantResults);
                startService();
                // DO nothing
                break;
            case R.string.request_write_external_storage_permission:
                Log.d(TAG, "onRequestPermissionsResult: write external: " + grantResults);
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() called");
        mMessenger.doUnbind(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() called");
        stopService();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() called");
        super.onDestroy();
    }

    @Override
    public AmbientMode.AmbientCallback getAmbientCallback() {
        return mAmbientCallback;
    }

    @Override
    public LocationRepository getLocationRepository() {
        return mLocationRepository;
    }

    private class ActivityAmbientCallback extends AmbientMode.AmbientCallback {

        @Override
        public void onEnterAmbient(final Bundle ambientDetails) {
            super.onEnterAmbient(ambientDetails);

            Log.d(TAG, "onEnterAmbient() called with: ambientDetails = [" + ambientDetails + "]");
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            Log.d(TAG, "onEnterAmbient: fragments: " + fragments);
            if (fragments.size() > 0) {
                Fragment fragment = fragments.get(fragments.size() - 1);
                Log.d(TAG, "onEnterAmbient: fragment: " + fragment);
                if (fragment != null && fragment instanceof IUpdateableAmbientModeHandler) {
                    IUpdateableAmbientModeHandler activeView = (IUpdateableAmbientModeHandler) fragment;
                    activeView.setIsAmbient(true);
                } else {
                    // TODO: exit app
                }
            }
            mContentView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.background_default_ambient));
            mMessenger.setUpdatesEnabled(false);
            // Will be executed when ambient mode is entered
            // Reduce colors to black and white
            // disable anti aliasing getPaint().setAntiAlias(false)
        }

        @Override
        public void onExitAmbient() {

            Log.d(TAG, "onExitAmbient() called");
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.size() > 0) {
                Fragment fragment = fragments.get(fragments.size() - 1);
                if (fragment != null && fragment instanceof IUpdateableAmbientModeHandler) {
                    IUpdateableAmbientModeHandler activeView = (IUpdateableAmbientModeHandler) fragment;
                    activeView.setIsAmbient(false);
                }
            }
            mContentView.setBackgroundColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.background_default));
            mMessenger.setUpdatesEnabled(true);
            // Will be executed when ambient mode is exited
            // restore normal state
        }

        @Override
        public void onUpdateAmbient() {
            Log.d(TAG, "onUpdateAmbient() called");
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            if (fragments.size() > 0) {
                Fragment fragment = fragments.get(fragments.size() - 1);
                if (fragment != null && fragment instanceof IUpdateableAmbientModeHandler) {
                    IUpdateableAmbientModeHandler activeView = (IUpdateableAmbientModeHandler) fragment;
                    activeView.onUpdateAmbient();
                }
            }
            // Will be executed when the displayed data in ambient mode needs an update.
            // Will be called once a minute as the recommended interval to update ui
            // For more frequent updates use alarm manager!
        }
    }
}
