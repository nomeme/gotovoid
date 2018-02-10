package gotovoid.de.gotovoid.service.sensors;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Created by DJ on 07/01/18.
 */

public class LocationSensor extends AbstractSensor<Location> {
    private static final String TAG = LocationSensor.class.getSimpleName();
    private final FusedLocationProviderClient mLocationProvider;
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
            Location location = locationResult.getLastLocation();
            notifyObserver(location);
            Log.d(TAG, "onLocationResult: " + location);
            Log.d(TAG, "onLocationResult: " + location.getAccuracy());
        }
    };

    LocationSensor(final Context context) {
        mLocationProvider = LocationServices.getFusedLocationProviderClient(context);
    }

    @Override
    protected void startSensor() {
        if (mLocationProvider != null) {
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
            mLocationProvider.requestLocationUpdates(request,
                    mLocationCallback,
                    null);
        }
    }

    @Override
    protected void stopSensor() {
        if (mLocationProvider != null) {
            mLocationProvider.removeLocationUpdates(mLocationCallback);
        }
    }

    @Override
    protected void restartSensor() {
        mLocationProvider.removeLocationUpdates(mLocationCallback);
        startSensor();
    }
}
