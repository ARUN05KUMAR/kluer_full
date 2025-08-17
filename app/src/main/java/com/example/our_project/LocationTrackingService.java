package com.example.our_project;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LocationTrackingService extends Service {

    private static final String CHANNEL_ID = "location_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "LocationService";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result != null && result.getLastLocation() != null) {
                    saveLocationToFile(result.getLastLocation());
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Location Tracking Active")
                .setContentText("Tracking your location in background...")
                .setSmallIcon(R.drawable.ic_location) // Ensure this icon exists
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();

        startForeground(NOTIFICATION_ID, notification);
        startLocationUpdates();

        return START_STICKY;
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10 * 60 * 1000); // 10 minutes
        locationRequest.setFastestInterval(5 * 60 * 1000); // 5 minutes
        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission missing", e);
        }
    }

    private void saveLocationToFile(Location location) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            String logEntry;

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                if (addresses != null && !addresses.isEmpty()) {
                    String addressLine = addresses.get(0).getAddressLine(0);
                    logEntry = timestamp + " | " + addressLine;
                } else {
                    logEntry = timestamp + " | Unknown location";
                }
            } catch (IOException e) {
                logEntry = timestamp + " | Location fetch failed";
                Log.e(TAG, "Geocoder failed", e);
                Toast.makeText(this, "Failed to fetch location address", Toast.LENGTH_SHORT).show();
                return;  // exit here, since no valid address to save
            }

            File file = new File(getFilesDir(), "location_log.txt"); // Use internal storage here
            try (FileWriter writer = new FileWriter(file, true)) {
                writer.append(logEntry).append("\n");
                Log.d(TAG, "Saved location: " + logEntry);
                Toast.makeText(this, "Location saved: " + logEntry, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e(TAG, "Failed to write location", e);
                Toast.makeText(this, "Failed to save location", Toast.LENGTH_SHORT).show();
            }
        }


        private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking Service Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
