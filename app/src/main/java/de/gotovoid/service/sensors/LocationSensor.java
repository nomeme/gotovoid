package de.gotovoid.service.sensors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import de.gotovoid.domain.model.geodata.ExtendedGeoCoordinate;
import de.gotovoid.domain.model.geodata.GeoCoordinate;

/**
 * Created by DJ on 07/01/18.
 */

/**
 * Implementation of the {@link AbstractSensor} for location data.
 */
public class LocationSensor extends AbstractSensor<ExtendedGeoCoordinate> {
    private static final String TAG = LocationSensor.class.getSimpleName();
    /**
     * {@link FusedLocationProviderClient} providing access to the sensor data.
     */
    private final FusedLocationProviderClient mLocationProvider;
    /**
     * {@link LocationCallback} to be notified on sensor changes.
     */
    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationAvailability(final LocationAvailability locationAvailability) {
            Log.d(TAG, "onLocationAvailability() called with: locationAvailability = ["
                    + locationAvailability + "]");
        }

        @Override
        public void onLocationResult(final LocationResult locationResult) {
            Log.d(TAG, "onLocationResult() called with: locationResult = ["
                    + locationResult + "]");
            if (locationResult == null || locationResult.getLastLocation() == null) {
                return;
            }
            Location location = locationResult.getLastLocation();
            final ExtendedGeoCoordinate coordinate = new ExtendedGeoCoordinate(location.getLatitude(),
                    location.getLongitude(),
                    location.getAltitude(),
                    location.getAccuracy());
            notifyObserver(coordinate);
            Log.d(TAG, "onLocationResult: " + location);
            Log.d(TAG, "onLocationResult: " + location.getAccuracy());
        }
    };

    /**
     * Package private constructor taking the {@link Context}, so the location services can
     * be registered
     *
     * @param context the {@link Context} to request location data for
     */
    @Deprecated
    LocationSensor(final Context context) {
        this(LocationServices.getFusedLocationProviderClient(context));
    }

    /**
     * Package private constructor taking the {@link FusedLocationProviderClient} to addObserver
     * for updates.
     *
     * @param provider the {@link FusedLocationProviderClient} to addObserver at
     */
    LocationSensor(final FusedLocationProviderClient provider) {
        super(new StateEvaluator(5, 40));
        mLocationProvider = provider;
    }

    @Override
    protected void startSensor() {
        Log.d(TAG, "startSensor() called");
        if (mLocationProvider != null) {
            Log.d(TAG, "startSensor: " + getUpdateFrequency());
            LocationRequest request = new LocationRequest();
            request.setInterval(getUpdateFrequency());
            request.setFastestInterval(getUpdateFrequency());
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            final Context context = mLocationProvider.getApplicationContext();
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "startSensor: no permissions");
                return;
            }
            getLocationProvider().requestLocationUpdates(request,
                    mLocationCallback,
                    Looper.getMainLooper());
        }
    }

    @Override
    protected void stopSensor() {
        Log.d(TAG, "stopSensor() called");
        if (mLocationProvider != null) {
            getLocationProvider().removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void restartSensor() {
        stopSensor();
        startSensor();
    }

    protected FusedLocationProviderClient getLocationProvider() {
        return mLocationProvider;
    }

    protected LocationCallback getSensorCallback() {
        return mLocationCallback;
    }

    private static class StateEvaluator
            extends AbstractSensor.StateEvaluator<ExtendedGeoCoordinate> {

        public StateEvaluator(int bufferSize, double tolerance) {
            super(bufferSize, tolerance);
        }

        // TODO: use lambda
        @Override
        protected double computeDifference(final ExtendedGeoCoordinate first,
                                           final ExtendedGeoCoordinate second) {
            GeoCoordinate firstCoord = new GeoCoordinate(first.getLatitude(),
                    first.getLongitude());
            GeoCoordinate secondCoord = new GeoCoordinate(second.getLatitude(),
                    second.getLongitude());
            return Math.abs(firstCoord.getHaversineDistanceTo(secondCoord));
        }
    }

}
