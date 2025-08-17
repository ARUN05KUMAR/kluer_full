package com.example.our_project;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.d("BootCompletedReceiver", "Device rebooted, restarting LocationTrackingService and scheduling alarm.");

            // Restart the LocationTrackingService
            Intent serviceIntent = new Intent(context, LocationTrackingService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

            // Reschedule the exact alarm
            AlarmHelper.scheduleDailyAlarm(context);
        }
    }
}
