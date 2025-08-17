package com.example.our_project;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Base64;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 101;

    boolean menuclicked = false;
    private FragmentManager manager;
    private View menuBackground;

    @Override
    protected void onResume() {
        super.onResume();
        loadProfileImage();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request runtime permissions
        requestNecessaryPermissions();

        FrameLayout container = findViewById(R.id.menu_container);
        menuBackground = findViewById(R.id.menu_background);
        manager = getSupportFragmentManager();

        ImageView menubutton = findViewById(R.id.menu);
        ImageView homebutton = findViewById(R.id.home);
        ImageView searchbutton = findViewById(R.id.search);
        ImageView insertbutton = findViewById(R.id.insert);
        ImageView profile = findViewById(R.id.profile);

        loadProfileImage();

        profile.setOnClickListener(v -> manager.beginTransaction()
                .replace(R.id.main_fragment_container, new ProfileFragment())
                .addToBackStack(null)
                .commit());

        menuBackground.setVisibility(View.GONE);
        menuBackground.setAlpha(0f);

        menubutton.setOnClickListener(view -> {
            FragmentTransaction transaction = manager.beginTransaction();
            if (!menuclicked) {
                transaction.setCustomAnimations(R.anim.slide_in_left, 0, 0, R.anim.slide_out_left);
                transaction.add(R.id.menu_container, new menu_fragment(), "menu_frag");
                menuclicked = true;
                menuBackground.setVisibility(View.VISIBLE);
                menuBackground.bringToFront();
                menuBackground.animate().alpha(1f).setDuration(300).start();
                menuBackground.setClickable(true);
            } else {
                remove_menu_fragment(transaction);
            }
            transaction.commit();
        });

        menuBackground.setOnClickListener(view -> {
            if (menuclicked) {
                FragmentTransaction transaction = manager.beginTransaction();
                remove_menu_fragment(transaction);
                transaction.commit();
            }
        });

        homebutton.setOnClickListener(view -> {
            FragmentTransaction transaction = manager.beginTransaction();
            if (menuclicked) remove_menu_fragment(transaction);
            remove_fragment(transaction);
            transaction.commit();
        });

        searchbutton.setOnClickListener(view -> {
            FragmentTransaction transaction = manager.beginTransaction();
            if (menuclicked) remove_menu_fragment(transaction);
            remove_fragment(transaction);
            add_fragment_in_main_container(transaction, new search_fragment());
            transaction.commit();
        });

        insertbutton.setOnClickListener(view -> {
            FragmentTransaction transaction = manager.beginTransaction();
            if (menuclicked) remove_menu_fragment(transaction);
            remove_fragment(transaction);
            add_fragment_in_main_container(transaction, new insert_fragment());
            transaction.commit();
        });
    }

    private void requestNecessaryPermissions() {
        if (!hasLocationPermissions()) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationServiceAndAlarm();
        }

        // For Android 13+ ask for POST_NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }

    private void startLocationServiceAndAlarm() {
        Intent serviceIntent = new Intent(this, LocationTrackingService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
            AlarmHelper.scheduleDailyAlarm(this);
        } else {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length >= 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                startLocationServiceAndAlarm();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadProfileImage() {
        ImageView profile = findViewById(R.id.profile);
        SharedPreferences sharedPreferences = getSharedPreferences("userDetails", MODE_PRIVATE);
        String encodedImage = sharedPreferences.getString("profileImage", null);
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            profile.setImageBitmap(bitmap);
        }
    }

    private void add_fragment_in_main_container(FragmentTransaction transaction, Fragment fragment) {
        transaction.setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.slide_out_right,
                R.anim.slide_in_left,
                R.anim.slide_out_left
        );
        transaction.replace(R.id.main_fragment_container, fragment, "main_frag");
    }

    private void remove_fragment(FragmentTransaction transaction) {
        Fragment fragment = manager.findFragmentById(R.id.main_fragment_container);
        if (fragment != null) {
            transaction.remove(fragment);
        }
    }

    private void remove_menu_fragment(FragmentTransaction transaction) {
        Fragment fragment = manager.findFragmentById(R.id.menu_container);
        if (fragment != null) {
            transaction.remove(fragment);
            menuclicked = false;
            menuBackground.animate().alpha(0f).setDuration(200)
                    .withEndAction(() -> menuBackground.setVisibility(View.GONE)).start();
        }
    }

    public void closeMenu() {
        FragmentTransaction transaction = manager.beginTransaction();
        Fragment menuFragment = manager.findFragmentById(R.id.menu_container);
        if (menuFragment != null) {
            transaction.remove(menuFragment);
            menuclicked = false;
            menuBackground.animate().alpha(0f).setDuration(300)
                    .withEndAction(() -> menuBackground.setVisibility(View.GONE)).start();
        }
        transaction.commit();
    }
}
