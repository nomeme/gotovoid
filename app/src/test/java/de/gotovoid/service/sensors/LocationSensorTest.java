package de.gotovoid.service.sensors;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.support.v4.BuildConfig;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import java.util.ArrayList;
import java.util.List;

import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.service.LocationService;

/**
 * Created by DJ on 27/02/18.
 */

/**
 * Test for the {@link LocationSensor}.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class LocationSensorTest extends GenericSensorTest {
    private LocationSensor mLocationSensor;
    private FusedLocationProviderClient mLocationProvider;
    private LocationService mService;

    /**
     * Prepare the test run.
     */
    @Before
    public void before() {
        mService = Robolectric.buildService(LocationService.class).get();
        mLocationSensor = new LocationSensor(mService.getApplicationContext());
        mLocationProvider = Mockito.mock(FusedLocationProviderClient.class);
    }

    /**
     * Grants the necessary permissions.
     */
    private void grantPermissions() {
        ShadowApplication application = Shadows.shadowOf(mService.getApplication());
        application.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    @Override
    protected LocationSensor getSensor() {
        return mLocationSensor;
    }

    /**
     * Verifies that the {@link LocationSensor#startSensor()} method behaves correctly when the
     * permissions are not granted.
     */
    @Test
    public void testStartSensorPermissionNotGranted() {
        mLocationSensor = new TestLocationSensor(mService.getApplicationContext());
        getSensor().startSensor();
        Mockito.verify(mLocationProvider, Mockito.times(0))
                .requestLocationUpdates(Mockito.any(LocationRequest.class),
                        Mockito.any(LocationCallback.class),
                        Mockito.any(Looper.class));
    }

    /**
     * Verifies that the {@link LocationSensor#startSensor()} method behaves correctly when the
     * permissions are not granted.
     */
    @Test
    public void testStartSensorPermissionGranted() {
        grantPermissions();
        mLocationSensor = new TestLocationSensor(mService.getApplicationContext());
        getSensor().startSensor();
        Mockito.verify(mLocationProvider, Mockito.times(1))
                .requestLocationUpdates(Mockito.any(LocationRequest.class),
                        Mockito.any(LocationCallback.class),
                        Mockito.any(Looper.class));
    }

    /**
     * Verifies that the {@link LocationSensor#stopSensor()} method behaves correctly when the
     * permissions are not granted.
     */
    @Test
    public void testStopSensorPermissionNotGranted() {
        mLocationSensor = new TestLocationSensor(mService.getApplicationContext());
        getSensor().stopSensor();
        Mockito.verify(mLocationProvider, Mockito.times(1))
                .removeLocationUpdates(Mockito.any(LocationCallback.class));
    }

    /**
     * Verifies that the {@link LocationSensor#stopSensor()} method behaves correctly when the
     * permissions are granted.
     */
    @Test
    public void testStopSensorPermissionGranted() {
        grantPermissions();
        mLocationSensor = new TestLocationSensor(mService.getApplicationContext());
        getSensor().stopSensor();
        Mockito.verify(mLocationProvider, Mockito.times(1))
                .removeLocationUpdates(Mockito.any(LocationCallback.class));
    }

    /**
     * Verify that observers are correctly notified.
     */
    @Test
    public void testNotify() {
        final LocationSensor.Observer observer = Mockito.mock(LocationSensor.Observer.class);
        getSensor().addObserver(observer);
        Location location = Mockito.mock(Location.class);
        List<Location> list = new ArrayList<>();
        list.add(location);
        getSensor().getSensorCallback().onLocationResult(LocationResult.create(list));
        Mockito.verify(observer, Mockito.times(1))
                .onChange(Mockito.any(ExtendedGeoCoordinate.class));
    }

    /**
     * Verify that null values do not cause exceptions.
     */
    @Test
    public void testNotifyNull() {
        final LocationSensor.Observer observer = Mockito.mock(LocationSensor.Observer.class);
        getSensor().addObserver(observer);
        getSensor().getSensorCallback().onLocationResult(null);
        Mockito.verify(observer, Mockito.times(0))
                .onChange(Mockito.any(ExtendedGeoCoordinate.class));
    }

    /**
     * Verify that empty notifies do not cause exceptions.
     */
    @Test
    public void testNotifyEmpty() {
        final LocationSensor.Observer observer = Mockito.mock(LocationSensor.Observer.class);
        getSensor().addObserver(observer);
        getSensor().getSensorCallback().onLocationResult(LocationResult.create(null));
        Mockito.verify(observer, Mockito.times(0))
                .onChange(Mockito.any(ExtendedGeoCoordinate.class));
    }

    /**
     * Test class for the {@link LocationSensor}. This is used to verify the appropriate methods
     * on the {@link FusedLocationProviderClient} are called.
     */
    private class TestLocationSensor extends LocationSensor {

        /**
         * Constructor taking the {@link Context}.
         *
         * @param context the {@link Context}
         */
        TestLocationSensor(Context context) {
            super(context);
        }

        /**
         * Constructor taking the {@link FusedLocationProviderClient}.
         *
         * @param provider the {@link FusedLocationProviderClient}
         */
        TestLocationSensor(FusedLocationProviderClient provider) {
            super(provider);
        }

        @Override
        protected FusedLocationProviderClient getLocationProvider() {
            return mLocationProvider;
        }
    }
}
