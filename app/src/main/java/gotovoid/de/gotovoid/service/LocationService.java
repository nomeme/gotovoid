package gotovoid.de.gotovoid.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import gotovoid.de.gotovoid.MainActivity;
import gotovoid.de.gotovoid.R;
import gotovoid.de.gotovoid.service.sensors.SensorHandler;
import gotovoid.de.gotovoid.service.communication.ServiceMessageHandler;

/**
 * Created by DJ on 22/12/17.
 */

public class LocationService extends Service {
    private static final String TAG = LocationService.class.getSimpleName();


    private NotificationManager mNotificationManager;
    private ServiceMessageHandler mServiceMessenger;
    /**
     * Instance taking care of collection of sensor data.
     */
    private SensorHandler mSensorHandler;

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        Log.d(TAG, "onStartCommand: " + Build.VERSION.SDK_INT);
        if (Build.VERSION.SDK_INT >= 26) {
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            showNotification();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mSensorHandler = new SensorHandler(getApplication());
        mServiceMessenger = new ServiceMessageHandler(mSensorHandler);
        // TODO: move this to a client binding command response
        // This is necessary for the location service to run as foreground service.

    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        mSensorHandler.stopSensors();
        if (mNotificationManager != null) {
            mNotificationManager.cancel(R.string.location_service_started);
        }
        // TODO: make string resource!
        Toast.makeText(this, "Location service stopped", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    @Override
    public boolean stopService(final Intent name) {
        Log.d(TAG, "stopService() called with: name = [" + name + "]");
        if(mSensorHandler.isRecording()) {
            return false;
        }
        return super.stopService(name);
    }

    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return mServiceMessenger.getBinder();
    }

    /**
     * Detect whether the system has built in GPS.
     *
     * @return true
     */
    private boolean hasGPS() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
    }

    private void showNotification() {
        Log.d(TAG, "showNotification: ");
        // This was created for api 26 so the service is not terminated
        // TODO: remove this if the other way works
        final Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "de.gotovoid.location";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            builder = new Notification.Builder(this, CHANNEL_ID);

            mNotificationManager.createNotificationChannel(channel);
        } else {
            builder = new Notification.Builder(this);
        }

        // Text for the notification.
        // TODO: make string resource!
        String text = "GotoVoid location service started.";
        // Intent to addObserver the MainActivity.
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Generate the notification.
        Notification notification = builder
                .setSmallIcon(R.drawable.icon)
                .setTicker(text) //status text
                // Time stamp when it happened
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Location Service started.")
                .setContentText(text)
                .setContentIntent(contentIntent)
                .build();


        if (Build.VERSION.SDK_INT >= 26) {
            Log.d(TAG, "showNotification: start Foreground");
            startForeground(1, notification);
        } else {
            Log.d(TAG, "showNotification: show notification");
            // Use a string resource because string resources are unique ids.
            mNotificationManager.notify(R.string.location_service_started, notification);
        }
    }
}
