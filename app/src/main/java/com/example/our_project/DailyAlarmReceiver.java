package com.example.our_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DailyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            FileInputStream fis = context.openFileInput("location_log.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();

            String summary = sb.toString();

            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
            // Add custom message
            String finalMessage = "On " + currentDate + ", I visited these places with the time specified:\n" + summary;

            Log.d("DailyAlarmReceiver", "Daily Location Summary:\n" + finalMessage);

            // Log before starting the service
            Log.d("DailyAlarmReceiver", "Starting MessageSendService with location summary...");

            // Create intent to call the service
            Intent serviceIntent = new Intent(context, MessageSendService.class);
            serviceIntent.putExtra(MessageSendService.EXTRA_MESSAGE, finalMessage);
            context.startService(serviceIntent);  // Start the service with the location summary

            // After sending, clear the file
            context.deleteFile("location_log.txt");
            Log.d("DailyAlarmReceiver", "Location log cleared after sending summary.");
        } catch (Exception e) {
            Log.e("DailyAlarmReceiver", "Error reading location log", e);
        }
    }
}
